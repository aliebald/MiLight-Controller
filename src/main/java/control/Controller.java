package main.java.control;

import main.java.Settings;
import main.java.audioProcessing.BeatDetector;
import main.java.bridge.*;
import main.java.musicModes.CyclicLights;
import main.java.server.HttpWebServer;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * TODO Description
 **
 * @author Alexander Liebald
 */
public class Controller {
	private HttpWebServer server;
	private Settings settings;

	public Controller(Settings settings) {
		this.settings = settings;
	}

	// TODO
	public void startServer(int port) throws IOException, BridgeException {
		server = new HttpWebServer(port, settings);
		server.start();
	}

	public static void main(String[] args) throws IOException, BridgeException {
		// Bridge bridge = new Bridge("192.168.0.52", 5987, false, 250);

		Controller controller = new Controller(new Settings());
		controller.startServer(8000);
	}
}
