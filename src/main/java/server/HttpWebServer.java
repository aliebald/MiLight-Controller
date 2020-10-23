package main.java.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.Settings;
import main.java.audioProcessing.BeatDetector;
import main.java.bridge.Bridge;
import main.java.bridge.BridgeException;
import main.java.bridge.Mode;
import main.java.bridge.Zone;
import main.java.control.MusicModeController;
import main.java.musicModes.*;

import javax.sound.sampled.LineUnavailableException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HttpWebServer to handle the web ui including its communication with the Bridge.
 *
 * @author Alexander Liebald
 */
public class HttpWebServer {
	private int port;
	private final String path = "D:\\Intellij Workspace\\MiLight Controller\\src\\main\\webui";
	private String site, customCSS, customJs;
	private Bridge bridge;
	private MusicModeController musicModeController;
	private Thread mmcThread;
	private Settings settings;

	public HttpWebServer(int port, Settings settings) throws IOException {
		this.port 		= port;
		this.settings	= settings;

		site 			= new String(Files.readAllBytes(Paths.get(path + "\\index.html")));
		customCSS 		= new String(Files.readAllBytes(Paths.get(path + "\\css\\custom.css")));
		customJs		= new String(Files.readAllBytes(Paths.get(path + "\\js\\custom.js")));
	}

	/**
	 * Start the website server
	 */
	public void start() throws IOException, BridgeException {
		setupBridgeAndMusicModeController();
		InetAddress address = InetAddress.getLocalHost();

		HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 0);
		server.createContext("/", new HttpWebServer.Handler(site));
		server.setExecutor(null); // creates a default executor
		server.start();

		// open a new browser tab
		if (settings.getOpenBrowserOnStart() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("http://" + address.getHostAddress() + ":" + port));
			} catch (URISyntaxException ignored) {}
		}

		System.out.println("IP Address of http server:- " + address.getHostAddress() + ":" + port);
		System.out.println("Host Name:- " + address.getHostName());
	}

	private void setupBridgeAndMusicModeController() {
		if (!settings.getBridgeIpAddress().equals("")) {
			try {
				bridge = new Bridge(settings.getBridgeIpAddress(), settings.getBridgePort(), false, 250);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				// TODO Exception Handling
			} catch (BridgeException e) {
				e.printStackTrace();
				// TODO Exception Handling
			}

			if (!settings.getActiveTargetDataLine().equals("none")) {
				try {
					musicModeController = new MusicModeController(null, new BeatDetector(100, settings.getActiveTargetDataLine()));
				} catch (LineUnavailableException e) {
					settings.resetActiveTargetDataLine();
				}
			}
		}
	}

	private class Handler implements HttpHandler {
		private String site;

		public Handler(String site) {
			this.site = site;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			URI request = t.getRequestURI();
			String response, requestBody = getRequestBody(t);
			System.out.println("requested: " + request + ", RequestMethod: " + t.getRequestMethod() + ", RequestBody: " + requestBody);

			switch (request.getPath()) {
				case "/command": {
					// select the correct response
					System.out.println("request recognised as command");
					if (!settings.getBridgeIpAddress().equals("")) {
						response = handleCommand(requestBody);
					} else {
						response = "ERROR: Please create a bridge";
					}
					break;
				}
				case "/": {
					response = site;
					System.out.println("	set the response to site");
					break;
				} case "/css/custom.css": {
					response = customCSS;
					System.out.println("	set the response to customCSS");
					break;
				} case "/js/custom.js": {
					response = customJs;
					System.out.println("	set the response to custom.js");
					break;
				} case "/favicon.ico": {
					response = "";
					System.out.println("	return empty String. No favicon.ico right now");
					break;
				} case "/settings.json": {
					response = settings.getSettings();
					System.out.println("	return set to settings.json");
					break;
				} case "/applySettings": {
					response = applySettings(requestBody);
					break;
				} case "/resetSettings": {
					settings.setToDefaultSettings();
					// Reset Bridge & MusicModeController
					bridge = null;
					musicModeController = null;
					setupBridgeAndMusicModeController();

					response = settings.getSettings();
					System.out.println("	return set to defaultSettings.json");
					break;
				}
				default: {
					System.out.println("	return empty String. (default case, request unknown)");
					response = "";
				}
			}

			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
			System.out.println("############################################");
		}

		/**
		 * a request needs to have the following form:
		 * "command=<command>&zone=<zoneNr>"
		 *
		 * @param request
		 * @return
		 */
		private String handleCommand(String request) {
			// Stop musicModeController
			if (musicModeController != null) {
				musicModeController.stop();
			}

			// get command
			int end = request.indexOf('&');
			String command = request.substring(8, end);
			System.out.print("## Command: " + command + ", ");

			// get zone
			char nr = request.substring(end + 6).charAt(0);
			Zone zone = getZone(nr);
			System.out.println("zone: " + zone + " ##");

			// set custom color
			if(command.startsWith("setColorTo:")) {
				System.out.println("got: \n" + command);
				int num;
				try {
					num = Integer.parseInt(command.substring(11), 10);
				} catch (NumberFormatException e){
					return "invalid color";
				}
				System.out.println(num + " =?= " +  (byte) num);
				bridge.setColor(zone, (byte) num);
				return "Set color to " + num;
			}

			// change brightness
			if(command.startsWith("setBrightness:")) {
				int brightness;
				try {
					brightness = Integer.parseInt(command.substring(14), 10);
				} catch (NumberFormatException e){
					return "ERROR: Invalid brightness";
				}
				System.out.println("Setting brightness to: " + brightness);
				bridge.setBrightness(zone, brightness);
			}

			// Change Mode
			if(command.startsWith("setMode:")) {
				System.out.println("looking for: " + command.substring(8));
				if (command.substring(8).charAt(0) == 'M') {
					// Check if musicModeController exists
					if (musicModeController == null) {
						return "ERROR: Select audio input to use music modes";
					}

					// Music Modes
					switch (command.substring(8)) {
						case "MCyclic": {
							musicModeController.setMusicMode(new CyclicLights(bridge));
							break;
						}
						case "MFlashing": {
							musicModeController.setMusicMode(new FlashingLights(bridge));
							break;
						}
						case "MPulse": {
							musicModeController.setMusicMode(new PulseLights(bridge));
							break;
						}
						case "MSequential": {
							musicModeController.setMusicMode(new SequentialLights(bridge));
							break;
						}
						case "MSiren": {
							musicModeController.setMusicMode(new SirenLights(bridge));
							break;
						}
						default: {
							return "ERROR: Unknown Mode";
						}
					}
					// Start Thread for MusicModeController
					mmcThread = new Thread(musicModeController);
					mmcThread.start();
				} else {
					// Build in modes
					switch (command.substring(8)) {
						case "ColorWheel": {
							bridge.setMode(zone, Mode.COLOR_WHEEL);
							break;
						}
						case "BreathingColorWheel": {
							bridge.setMode(zone, Mode.BREATHING_COLOR_WHEEL);
							break;
						}
						case "Party": {
							bridge.setMode(zone, Mode.PARTY);
							break;
						}
						case "PartyMultipleColors": {
							bridge.setMode(zone, Mode.PARTY_MULTIPLE_COLORS);
							break;
						}
						case "FlashRed": {
							bridge.setMode(zone, Mode.FLASH_RED);
							break;
						}
						case "FlashGreen": {
							bridge.setMode(zone, Mode.FLASH_GREEN);
							break;
						}
						case "FlashBlue": {
							bridge.setMode(zone, Mode.FLASH_BLUE);
							break;
						}
						default: {
							return "ERROR: Unknown Mode";
						}
					}
				}
				return "Changed mode to: " + command.substring(8);
			}

			// Other basic commands
			switch (command) {
				case "turnOn": {
					bridge.turnOn(zone);
					return "Turned on";
				}
				case "turnOff": {
					bridge.turnOff(zone);
					return "turned off";
				}
				case "setColorRed": {
					bridge.setColorToRed(zone);
					return "Changed color to red";
				}
				case "setColorGreen": {
					bridge.setColorToGreen(zone);
					return "Changed color to green";
				}
				case "setColorBlue": {
					bridge.setColorToBlue(zone);
					return "Changed color to blue";
				}
				case "setColorOrange": {
					bridge.setColorToOrange(zone);
					return "Changed color to orange";
				}
				case "setColorYellow": {
					bridge.setColorToYellow(zone);
					return "Changed color to yellow";
				}
				case "setColorLavender": {
					bridge.setColorToLavender(zone);
					return "Changed color to lavender";
				}
				case "setColorAqua": {
					bridge.setColorToAqua(zone);
					return "Changed color to aqua";
				}
				case "setColorLime": {
					bridge.setColorToLime(zone);
					return "Changed color to lime";
				}
			}

			return "ERROR: Command not found!";
		}

		private String getRequestBody(HttpExchange t) throws IOException {
			return new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		}

		/**
		 * Updates settings
		 *
		 * @param requestBody requestBody from http request
		 * @return response String
		 */
		private String applySettings(String requestBody) {
			String errorLog = "";
			String oldActiveTargetDataLine = settings.getActiveTargetDataLine();
			String oldIp = settings.getBridgeIpAddress();
			int oldPort = settings.getBridgePort();

			try {
				settings.updateSettings(requestBody);
			} catch (Exception ignored) {
				return "ERROR: Failed to update settings";
			}

			// setup bridge / replace bridge if ip or port changed
			if (settings.getHasBridge() && (bridge == null || !oldIp.equals(settings.getBridgeIpAddress()) || oldPort != settings.getBridgePort())) {
				// stop musicModeController, since it might use the old bridge
				if (musicModeController != null) {
					musicModeController.stop();
					musicModeController.setMusicMode(null);
				}

				try {
					bridge = new Bridge(settings.getBridgeIpAddress(), settings.getBridgePort(), false, 200); // TODO add timeout to settings
					System.out.println("created new Bridge");
				} catch (Exception ignored) {
					settings.setHasBridge(false);
					errorLog = "ERROR: Failed to created new Bridge.";
				}
			}

			if (musicModeController == null && !settings.getActiveTargetDataLine().equals("none")) {
				// Setup new MusicModeController if necessary
				try {
					musicModeController = new MusicModeController(null, new BeatDetector(120, settings.getActiveTargetDataLine()));
				} catch (LineUnavailableException e) {
					settings.resetActiveTargetDataLine();
					errorLog += "ERROR: Failed to get TargetDataLine. Make sure this line is set to 44100Hz.";
				}
				settings.setHasMusicModeController(true);

			} else if (musicModeController != null && !oldActiveTargetDataLine.equals(settings.getActiveTargetDataLine())){
				// Replace BeatDetector if activeTargetDataLine changed
				musicModeController.stop();
				try {
					musicModeController.setBeatDetector(new BeatDetector(120, settings.getActiveTargetDataLine()));
				} catch (LineUnavailableException e) {
					settings.resetActiveTargetDataLine();
					errorLog += "ERROR: Failed to get TargetDataLine. Make sure this line is set to 44100Hz.";
				}
			}

			if (errorLog.equals("")) {
				return settings.getSettings();
			} else {
				return errorLog + "ERROR-END" + settings.getSettings();
			}
		}

		private Zone getZone (char nr){
			switch (nr) {
				case '0':
					return Zone.ALL;
				case '1':
					return Zone.FIRST;
				case '2':
					return Zone.SECOND;
				case '3':
					return Zone.THIRD;
				case '4':
					return Zone.FOURTH;
				default: {
					System.err.println("ZONE " + nr + " NOT RECOGNISED (returned ALL)");
					return Zone.ALL;
				}
			}
		}
	}
}
