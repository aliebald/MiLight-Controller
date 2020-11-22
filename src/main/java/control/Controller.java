package control;

import settings.Settings;
import bridge.*;
import server.HttpWebServer;
import java.io.IOException;

/**
 * TODO Description
 **
 * @author Alexander Liebald
 */
public class Controller {
	// If def is true then the paths are set to the dev paths, if false they are set to release paths (required for builds)
	private static final boolean dev = true;

	// Develop: ".\\src\\main\\webui"
	// Release: ".\\webui"
	private static final String webuiPath		= dev ? ".\\webui" : ".\\src\\main\\webui";
	// Develop: ".\\src\\main\\resources\\"
	// Release: ".\\settings\\"
	private static final String settingsPath	= dev ? ".\\src\\main\\resources\\" : ".\\settings\\";

	private HttpWebServer server;
	private Settings settings;

	public Controller(Settings settings) {
		this.settings = settings;
	}

	// TODO
	public void startServer(int port) throws IOException, BridgeException {
		server = new HttpWebServer(port, settings, webuiPath);
		server.start();
	}

	public static void main(String[] args) throws IOException, BridgeException {
		// Bridge bridge = new Bridge("192.168.0.52", 5987, false, 250);

		Controller controller = new Controller(new Settings(settingsPath));
		controller.startServer(8000);
	}
}
