package settings;

import audioProcessing.BeatDetector;
import bridge.Bridge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

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
 * Handles application settings
 *
 * @author Alexander Liebald
 */
public class Settings {
	private JSONObject settings;
	private String path;

	/**
	 * Construct a new Settings object which handles server and client settings.
	 * Will try to load custom settings. If none are found it will load the default settings
	 *
	 * @throws IOException Throws an if an error occurs when reading the settings
	 * @throws SocketException Throws an SocketException if resetting possibleBridgeIpAddresses fails to open a new socket in {@link Bridge#discoverBridges()}.
	 */
	public Settings(String path) throws IOException {
		this.path = path;
		try {
			settings = new JSONObject(new String(Files.readAllBytes(Paths.get(this.path + "/settings.json"))));
			resetPossibleBridgeIpAddresses();
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
		if (targetDataLine.equals("none") || BeatDetector.isValidTargetDataLine(targetDataLine)) {
			settings.put("activeTargetDataLine", targetDataLine);
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
	 * @return Sensitivity for beat detection
	 */
	public double getSensitivity() {
		return (double) settings.getDouble("sensitivity");
	}

	/**
	 * @param sensitivity new sensitivity for beat detection
	 */
	public void setSensitivity(double sensitivity){
		settings.put("sensitivity", sensitivity);
	}

	/**
	 * possibleBridgeIpAddresses lists all discovered bridges
	 *
	 * @return all possible bridge ip addresses for bridges in the local network (if set)
	 */
	public String[] getPossibleBridgeIpAddresses() {
		JSONArray arr = settings.getJSONArray("possibleBridgeIpAddresses");
		String[] ret = new String[arr.length()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (String) arr.get(i);
		}
		return ret;
	}

	/**
	 * possibleBridgeIpAddresses lists all discovered bridges
	 *
	 * @param possibleBridgeIpAddresses  all possible bridge ip addresses for bridges in the local network
	 */
	private void setPossibleBridgeIpAddresses(String[] possibleBridgeIpAddresses) {
		settings.put("possibleBridgeIpAddresses", new JSONArray(possibleBridgeIpAddresses));
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
		setBeatCooldown(in.getInt("beatCooldown"));
		setSensitivity(in.getDouble("sensitivity"));
		if (setActiveTargetDataLine((String) in.get("activeTargetDataLine"))) {
			// If the TargetDataLine is invalid it will not be updated, but a warning will be printed
			System.out.println("WARNING: invalid TargetDataLine");
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
		FileWriter fw = new FileWriter(path + "/settings.json");
		fw.write(settings.toString(2));
		fw.close();
		System.out.println("saved settings.json");
	}

	/**
	 * Resets current setting to default settings
	 *
	 * @throws IOException throws an IOException if reading or writing the file fails
	 * @throws SocketException Throws an SocketException if resetting possibleBridgeIpAddresses fails to open a new socket in {@link Bridge#discoverBridges()}.
	 */
	public void setToDefaultSettings() throws IOException {
		settings = new JSONObject(new String(Files.readAllBytes(Paths.get(path + "/defaultSettings.json"))));
		updatePossibleTargetDataLines();
		resetPossibleBridgeIpAddresses();
		saveSettings();
	}

	/**
	 * Resets the activeTargetDataLine to none and saves the changes
	 */
	public void resetActiveTargetDataLine() {
		setActiveTargetDataLine("none");
		try {
			saveSettings();
		} catch (IOException e) {
			// TODO Handle IOException
			e.printStackTrace();
		}
	}

	/**
	 * Discovers bridges in the local network and replaces possibleBridgeIpAddresses.
	 *
	 * This should not be called regularly since discovering takes a bit of time, see {@link Bridge#discoverBridges()}
	 *
	 * @throws	SocketException
	 * 			Throws a SocketException if {@link Bridge#discoverBridges()} is unable to open up a new socket.
	 * @throws	IOException
	 * 			if an I/O error occurs.
	 * @throws PortUnreachableException
	 * 			may be thrown if bridge is currently unreachable. Note, there is no
	 * 			guarantee that the exception will be thrown.
	 * @throws	SecurityException
	 * 			if a security manager exists and its {@code checkMulticast}
	 * 			or {@code checkConnect} method doesn't allow the send.
	 */
	public void resetPossibleBridgeIpAddresses() throws IOException {
		HashSet<String> bridges = Bridge.discoverBridges();
		setPossibleBridgeIpAddresses(bridges.toArray(new String[bridges.size()]));
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

	/**
	 * @return returns beatCooldown
	 */
	public int getBeatCooldown() {
		return settings.getInt("beatCooldown");
	}

	/**
	 * @param beatCooldown beatCooldown intended for BeatDetector
	 */
	public void setBeatCooldown(int beatCooldown) {
		settings.put("beatCooldown", beatCooldown);
	}
}
