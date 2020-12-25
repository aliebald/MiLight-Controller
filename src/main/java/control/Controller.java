package control;

import settings.Settings;
import bridge.*;
import server.HttpWebServer;
import java.io.IOException;

/*
 *  Copyright 2020 Alexander Liebald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * TODO Description
 **
 * @author Alexander Liebald
 */
public class Controller {
	// If def is true then the paths are set to the dev paths, if false they are set to release paths (required for builds)
	private static final boolean dev = true;

	// Develop: "./src/main/webui"
	// Release: "./webui"
	private static final String webuiPath		= dev ? "./src/main/webui" : "./webui";
	// Develop: "./src/main/resources/"
	// Release: "./settings/"
	private static final String settingsPath	= dev ? "./src/main/resources" : "./settings";

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
