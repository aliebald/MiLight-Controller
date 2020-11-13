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
	private static final String webuiPath		= ".\\src\\main\\webui";  // use the following path for deployment: ".\\webui";
	private static final String settingsPath	= ".\\src\\main\\resources\\"; // use the following path for deployment: ".\\";

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
