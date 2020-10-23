package main.java;

import main.java.audioProcessing.BeatDetector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles application settings
 *
 * @author Alexander Liebald
 */
public class Settings {
	private JSONObject settings;
	private String path = "D:\\Intellij Workspace\\MiLight Controller\\src\\main\\resources\\";

	/**
	 * Construct a new Settings object which handles server and client settings.
	 * Will try to load custom settings. If none are found it will load the default settings
	 *
	 * @throws IOException Throws an if an error occurs when reading the settings
	 */
	public Settings() throws IOException {
		try {
			settings = new JSONObject(new String(Files.readAllBytes(Paths.get(path + "settings.json"))));
			updatePossibleTargetDataLines();
		} catch (IOException e) {
			// Load default settings
			setToDefaultSettings();
		}
	}

	/**
	 * @return JSON formatted settings in a String
	 */
	public String getSettings() {
		return settings.toString(2);
	}

	/**
	 * openBrowserOnStart defines whether or not the website should be opened when the server starts
	 *
	 * @return returns the value of the openBrowserOnStart setting
	 */
	public boolean getOpenBrowserOnStart() {
		return settings.getBoolean("openBrowserOnStart");
	}

	/**
	 * openBrowserOnStart defines whether or not the website should be opened when the server starts
	 *
	 * @param openBrowserOnStart the value of the openBrowserOnStart setting
	 */
	public void setOpenBrowserOnStart(boolean openBrowserOnStart) {
		settings.put("openBrowserOnStart", openBrowserOnStart);
	}

	/**
	 * ActiveTargetDataLine represents the name of the selected TargetDataLine.
	 *
	 * @return returns the value of the activeTargetDataLine setting
	 */
	public String getActiveTargetDataLine() {
		try {
			return settings.get("activeTargetDataLine").toString();
		} catch (JSONException ignored) {
			return null;
		}
	}

	/**
	 * ActiveTargetDataLine represents the name of the selected TargetDataLine.
	 * Tests if {@code targetDataLine} is either a valid TargetDataLine name or "none"
	 *
	 * @param targetDataLine Name for the new TargetDataLine
	 * @return true if setting was successfully updated. Even if it was updated to "none"
	 */
	public boolean setActiveTargetDataLine(String targetDataLine) {
		if (BeatDetector.isValidTargetDataLine(targetDataLine)) {
			settings.put("activeTargetDataLine", targetDataLine);
		} else if(targetDataLine.equals("none")) {
			settings.put("activeTargetDataLine", targetDataLine);
			setHasMusicModeController(false);
		} else {
			return false;
		}

		return true;
	}

	/**
	 * @return Bridge ip address in string format
	 */
	public String getBridgeIpAddress() {
		return settings.get("bridgeIpAddress").toString();
	}

	/**
	 * @param ip new Bridge ip address in string format
	 */
	public void setBridgeIpAddress(String ip){
		settings.put("bridgeIpAddress", ip);
	}

	/**
	 * @return Port for the bridge
	 */
	public int getBridgePort() {
		return (int) settings.getNumber("bridgePort");
	}

	/**
	 * @param port new port for the bridge
	 */
	public void setBridgePort(int port){
		settings.put("bridgePort", port);
	}

	/**
	 * HasBridge is true if a valid & working bridge is connected
	 *
	 * @return returns the value of the hasBridge setting
	 */
	public boolean getHasBridge() {
		return settings.getBoolean("hasBridge");
	}

	/**
	 * HasBridge should only be set to true if a valid & working bridge is connected!
	 *
	 * @param hasBridge the value of the hasBridge setting
	 */
	public void setHasBridge(boolean hasBridge) {
		settings.put("hasBridge", hasBridge);
	}

	/**
	 * hasMusicModeController is true if a MusicModeController is available
	 *
	 * @return returns the value of the hasMusicModeController setting
	 */
	public boolean getHasMusicModeController() {
		return settings.getBoolean("hasMusicModeController");
	}

	/**
	 * hasMusicModeController should only be set to true if a MusicModeController is available
	 *
	 * @param hasMusicModeController value of the hasMusicModeController setting
	 */
	public void setHasMusicModeController(boolean hasMusicModeController) {
		settings.put("hasMusicModeController", hasMusicModeController);
	}

	/**
	 * Tests and Updates settings.
	 *
	 * @param settingsJSON whole JSON settings file as a String.
	 *                     Read only settings will be ignored.
	 */
	public void updateSettings(String settingsJSON) throws IOException {
		JSONObject in = new JSONObject(settingsJSON);

		setBridgeIpAddress((String) in.get("bridgeIpAddress"));
		setOpenBrowserOnStart(in.getBoolean("openBrowserOnStart"));
		setBridgePort((Integer) in.getNumber("bridgePort"));

		// insert TargetDataLine name and update hasMusicModeController if possible
		if (setActiveTargetDataLine((String) in.get("activeTargetDataLine")) && !in.get("activeTargetDataLine").equals("none")) {
			setHasMusicModeController(true);
		}

		// Check if hasBridge can be set
		if (!getBridgeIpAddress().equals("") && InetAddress.getByName(getBridgeIpAddress()).isReachable(750)) {
			setHasBridge(true);
			// Checks if there is actually a bridge need to happen on initiation (in HttpServer)
		}

		// Update client settings
		settings.put("clientSettings", in.get("clientSettings"));

		saveSettings();
	}

	/**
	 * Writes setting in resources/settings.json
	 *
	 * @throws IOException throws an IOException if writing the file fails
	 */
	private void saveSettings() throws IOException {
		FileWriter fw = new FileWriter(path + "settings.json");
		fw.write(settings.toString(2));
		fw.close();
		System.out.println("saved settings.json");
	}

	/**
	 * Resets current setting to default settings
	 *
	 * @throws IOException throws an IOException if reading the file fails
	 */
	public void setToDefaultSettings() throws IOException {
		settings = new JSONObject(new String(Files.readAllBytes(Paths.get(path + "defaultSettings.json"))));
		updatePossibleTargetDataLines();
	}

	/**
	 * Resets settings.possibleTargetDataLines
	 */
	private void updatePossibleTargetDataLines() {
		JSONArray possibleTargetDataLines = new JSONArray();
		for(String line : BeatDetector.getPossibleTargetDataLines()) {
			possibleTargetDataLines.put(line);
		}
		settings.put("possibleTargetDataLines", possibleTargetDataLines);
	}
}
