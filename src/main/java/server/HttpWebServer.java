package main.java.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.bridge.Bridge;
import main.java.bridge.Zone;
import main.java.control.MusicModeController;
import main.java.musicModes.*;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
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

	public HttpWebServer(int port, Bridge bridge, MusicModeController musicModeController) throws IOException {
		this.musicModeController = musicModeController;
		this.port 		= port;
		this.bridge 	= bridge;

		site 			= new String(Files.readAllBytes(Paths.get(path + "\\index.html")));
		customCSS 		= new String(Files.readAllBytes(Paths.get(path + "\\css\\custom.css")));
		customJs		= new String(Files.readAllBytes(Paths.get(path + "\\js\\custom.js")));
	}

	/**
	 * Start the website server
	 */
	public void start() throws IOException {
		InetAddress address = InetAddress.getLocalHost();

		HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 0);
		server.createContext("/", new HttpWebServer.Handler(site));
		server.setExecutor(null); // creates a default executor
		server.start();

		// Try opening a browser tab
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("http://" + address.getHostAddress() + ":" + port));
			} catch (URISyntaxException ignored) {}
		}

		System.out.println("IP Address of http server:- " + address.getHostAddress() + ":" + port);
		System.out.println("Host Name:- " + address.getHostName());
	}

	private class Handler implements HttpHandler {
		private String site;

		public Handler(String site) {
			this.site = site;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			URI request = t.getRequestURI();
			System.out.println("requested: " + request);
			String response = site;

			// select the correct response
			if (isCommand(request.getPath())){
				System.out.println("request recognised as command");
				response = handleCommand(request.getPath());
			} else {
				// Website content request
				switch (request.getPath()) {
					case ("/"): {
						response = site;
						System.out.println("	set the response to site");
						break;
					}
					case ("/css/custom.css"): {
						response = customCSS;
						System.out.println("	set the response to customCSS");
						break;
					}
					case ("/js/custom.js"): {
						response = customJs;
						System.out.println("	set the response to custom.js");
						break;
					}
					case ("/favicon.ico"): {
						response = "";
						System.out.println("	return empty String. No favicon.ico right now");
						break;
					}
					default: {
						System.out.println("	return empty String. (default case, request unknown)");
						response = "";
					}
				}
			}
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
			System.out.println("############################################");
		}

		private boolean isCommand(String request) {
			return request.startsWith("/command=");
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
			musicModeController.stop();

			// get command
			int end = request.indexOf('&');
			String command = request.substring(9, end);
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
					num = Integer.parseInt(command.substring(11, 13), 10);
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
				mmcThread = new Thread(musicModeController);
				mmcThread.start();
				return "Changed mode to: " + command.substring(8);
			}

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
