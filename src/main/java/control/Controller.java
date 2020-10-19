package main.java.control;

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
	Bridge bridge;
	HttpWebServer server;

	public Controller(Bridge bridge) {
		this.bridge = bridge;
	}

	public Controller(String ip, int port, int timeout) throws UnknownHostException, BridgeException {
		this(new Bridge(ip, port, false, timeout));
	}

	// TODO
	public void startServer(int port) throws IOException {
		Bridge bridge = null;
		try {
			bridge = new Bridge("192.168.0.52", 5987, false, 100);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		server = new HttpWebServer(port, bridge, new MusicModeController(new CyclicLights(bridge), new BeatDetector(100)));
		server.start();
	}

	public static void main(String[] args) throws IOException, BridgeException {
		Bridge bridge = new Bridge("192.168.0.52", 5987, false, 250);
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
		Controller controller = new Controller(bridge);
		controller.startServer(8000);
	}
}
