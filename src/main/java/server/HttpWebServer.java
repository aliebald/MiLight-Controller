package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import settings.Settings;
import audioProcessing.BeatDetector;
import bridge.Bridge;
import bridge.BridgeException;
import bridge.Mode;
import bridge.Zone;
import control.MusicModeController;
import musicModes.*;

import javax.sound.sampled.LineUnavailableException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;

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
	public void start() throws IOException {
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
			} catch (IOException e) {
				// TODO Exception Handling
				e.printStackTrace();
			}

			if (!settings.getActiveTargetDataLine().equals("none")) {
				try {
					musicModeController = new MusicModeController(null, new BeatDetector(settings.getBeatCooldown(), settings.getActiveTargetDataLine()));
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
						// check if a music mode is selected (music modes do not support zone selection)
						if(requestBody.startsWith("setMode:M", 8)) {
							response = handleCommand(requestBody, Zone.ALL);
						} else {
							StringBuilder sb = new StringBuilder();
							// Apply command to all requested zones
							for (Zone zone : getZones(requestBody)) {
								sb.append(handleCommand(requestBody, zone));
							}
							response = sb.toString();
						}
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
		 * Handles a command for a specific zone
		 *
		 * @param request request body received from the web ui
		 * @return returns a error or success message which can be returned to the web ui
		 */
		private String handleCommand(String request, Zone zone) throws IOException {
			// Stop musicModeController
			if (musicModeController != null) {
				musicModeController.stop();
			}

			// get command
			int end = request.indexOf('&');
			String command = request.substring(8, end);
			System.out.print("## Command: " + command + ", ");

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
				return "Set brightness to " + brightness;
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
						case "MCyclicMultipleColors": {
							musicModeController.setMusicMode(new CyclicLightsMultipleColors(bridge));
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
				case "decreaseSpeed": {
					bridge.decreaseSpeed(zone);
					break;
				}
				case "increaseSpeed": {
					bridge.increaseSpeed(zone);
					break;
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
			int oldBeatCooldown = settings.getBeatCooldown();

			try {
				settings.updateSettings(requestBody);
			} catch (Exception ignored) {
				return "ERROR: Failed to update settings";
			}

			// setup bridge / replace bridge if ip or port changed
			if (!settings.getBridgeIpAddress().equals("") && (bridge == null || !oldIp.equals(settings.getBridgeIpAddress()) || oldPort != settings.getBridgePort())) {
				// stop musicModeController, since it might use the old bridge
				if (musicModeController != null) {
					musicModeController.stop();
					musicModeController.setMusicMode(null);
				}

				try {
					bridge = new Bridge(settings.getBridgeIpAddress(), settings.getBridgePort(), false, 200); // TODO add timeout to settings
					System.out.println("created new Bridge");
				} catch (Exception ignored) {
					errorLog = "ERROR: Failed to created new Bridge.";
				}
			}

			if (musicModeController == null && !settings.getActiveTargetDataLine().equals("none")) {
				// Setup new MusicModeController if necessary
				try {
					musicModeController = new MusicModeController(null, new BeatDetector(settings.getBeatCooldown(), settings.getActiveTargetDataLine()));
				} catch (LineUnavailableException e) {
					settings.resetActiveTargetDataLine();
					errorLog += "ERROR: Failed to get TargetDataLine. Make sure this line is set to 44100Hz.";
				}

			} else if (musicModeController != null && !oldActiveTargetDataLine.equals(settings.getActiveTargetDataLine())){
				// Replace BeatDetector if activeTargetDataLine changed
				musicModeController.stop();
				try {
					musicModeController.setBeatDetector(new BeatDetector(settings.getBeatCooldown(), settings.getActiveTargetDataLine()));
				} catch (LineUnavailableException e) {
					settings.resetActiveTargetDataLine();
					errorLog += "ERROR: Failed to get TargetDataLine. Make sure this line is set to 44100Hz.";
				}
			}

			// check if BeatCooldown changed
			if (settings.getBeatCooldown() != oldBeatCooldown) {
				musicModeController.getBeatDetector().setCooldown(settings.getBeatCooldown());
			}

			if (errorLog.equals("")) {
				return settings.getSettings();
			} else {
				return errorLog + "ERROR-END" + settings.getSettings();
			}
		}

		/**
		 * Parses the zones from a request.
		 * The request body needs to be formatted as follows:
		 * command=<message>&zones=<z1>,<z2>,<z3>,<z4>
		 * where z1 ... z4 are either 0 or 1, 1 meaning the zone is requested.
		 *
		 * @param requestBody request body received from the web ui
		 * @return All requested zones or {@code Zone.All} in case all zones where requested.
		 */
		private LinkedList<Zone> getZones(String requestBody) {
			int index = requestBody.indexOf("&zones=") + 7;
			LinkedList<Zone> zones = new LinkedList<>();
			for (int i = 0; i < 4; i++) {
				if (requestBody.charAt(index + (i * 2)) == '1') {
					zones.add(getZone(i + 1));
				}
			}

			if (zones.size() >= 4) {
				return new LinkedList<>(Collections.singletonList(Zone.ALL));
			}
			return zones;
		}

		private Zone getZone (int nr){
			switch (nr) {
				case 0:
					return Zone.ALL;
				case 1:
					return Zone.FIRST;
				case 2:
					return Zone.SECOND;
				case 3:
					return Zone.THIRD;
				case 4:
					return Zone.FOURTH;
				default: {
					System.err.println("ZONE " + nr + " NOT RECOGNISED (returned ALL)");
					return Zone.ALL;
				}
			}
		}
	}
}
