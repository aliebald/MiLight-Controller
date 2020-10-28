package bridge;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Bridge can be used as an API for MiLight WiFi Bridge version 6.
 * Please Note that only MiLight Bridge version 6 with MiLight RGB and MiLight RGBW controllers are supported.
 * If you want to use this with another version it will probably not work. Feel free to contact me (https://github.com/ALiebald), maybe we can add your bridge/controller to the supported bridge versions/controllers.
 *
 * @author Alexander Liebald
 */
@SuppressWarnings("unused")
public class Bridge {
	private DatagramSocket socket;
	private InetAddress bridgeIp;
	private Integer port;
	private int timeout;
	private byte sequentialByte;
	private byte wifiBridgeSessionID1;
	private byte wifiBridgeSessionID2;
	private final byte remoteStyle = 0x07;

	// Restart the session in case it dies.
	private boolean automaticallyRestartSession = true;

	/**
	 * @param ip Bridge ip
	 * @param port port of the Bridge
	 * @param keepAlive keep the session alive? default: false
	 *                  Starts a new Thread that keeps the session alive by sending a Message every 5 seconds. This message does not affect the lights.
	 *                  Note: The session will also automatically be restarted if it is lost, so keepAlive is not required.
	 * @param timeout timeout in milliseconds for receiving answers from the bridge
	 */
	public Bridge (String ip, Integer port, Boolean keepAlive, int timeout) throws UnknownHostException, BridgeException {
		this(InetAddress.getByName(ip), port, keepAlive, timeout);
	}

	/**
	 * @param ip Bridge ip
	 * @param port port of the Bridge
	 * @param keepAlive keep the session alive? default: false
	 *                  Starts a new Thread that keeps the session alive by sending a message every 5 seconds. This message does not affect the lights.
	 *                  Note: The session will also automatically be restarted if it is lost, so keepAlive is not required.
	 * @param timeout timeout in milliseconds for receiving answers from the bridge. A timeout of zero is interpreted as an infinite timeout.
	 */
	public Bridge (InetAddress ip, Integer port, Boolean keepAlive, int timeout) throws BridgeException {
		System.out.println("Initializing Bridge at " + ip + ":" + port);
		this.port = port;
		this.sequentialByte = 1;
		this.bridgeIp = ip;
		this.timeout = timeout;

		// start the session
		startNewSession(10);

		// sends a keep alive message every 5 seconds
		if (keepAlive) {
			startKeepAliveThread();
		}

		System.out.println("#################################\nBridge created" + this.toString() + "\n#################################");
	}

	/**
	 * Turns on lights in the selected zone
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void turnOn (Zone zone) {
		System.out.println("\nTurn " + zone + " zone/s on");
		sendData(createData(1,zone.gebByte()));
	}

	/**
	 * Turns off all Lights in the selected zone
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void turnOff (Zone zone) {
		System.out.println("\nTurn " + zone + " zone/s off");
		sendData(createData(2,zone.gebByte()));
	}

	//TODO nightLight does not yet work
	/**
	 * Turns on Night mode in the selected zone
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void nightLight (Zone zone) {
		System.out.println("\nTurn night light for " + zone + " zone/s on");
		sendData(createData(3,zone.gebByte()));
	}

	/**
	 * Turns on white light in the selected zone
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void turnOnWhiteLight (Zone zone) {
		System.out.println("\nTurning on white light in zone  " + zone);
		sendData(createData(4,zone.gebByte()));
	}

	/**
	 * Changes the color to the given value
	 * 0x1C = Red, D9 = Lavender, BA = Blue, 85 = Aqua, 7A = Green, 54 = Lime, 3B = Yellow, 0x25 = Orange
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColor (Zone zone, byte color) {
		System.out.println("\nSet color in " + zone + " to " + bytesToHexString(color));
		sendData(createData(5,zone.gebByte(), color));
	}

	/**
	 * Changes the color to Blue
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToBlue (Zone zone) {
		System.out.println("\nSet color in " + zone + " to blue");
		sendData(createData(5,zone.gebByte(),(byte) 0xBA));
	}

	/**
	 * Changes the color to Red
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToRed (Zone zone) {
		System.out.println("\nSet color in " + zone + " to red");
		sendData(createData(5,zone.gebByte(),(byte) 0x1C));
	}

	/**
	 * Changes the color to lavender
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToLavender (Zone zone) {
		System.out.println("\nSet color in " + zone + " to lavender");
		sendData(createData(5,zone.gebByte(),(byte) 0xD9));
	}

	/**
	 * Changes the color to aqua
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToAqua (Zone zone) {
		System.out.println("\nSet color in " + zone + " to aqua");
		sendData(createData(5,zone.gebByte(),(byte) 0x85));
	}

	/**
	 * Changes the color to green
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToGreen (Zone zone) {
		System.out.println("\nSet color in " + zone + " to green");
		sendData(createData(5,zone.gebByte(),(byte) 0x7A));
	}

	/**
	 * Changes the color to lime
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToLime (Zone zone) {
		System.out.println("\nSet color in " + zone + " to lime");
		sendData(createData(5,zone.gebByte(),(byte) 0x54));
	}

	/**
	 * Changes the color to yellow
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToYellow (Zone zone) {
		System.out.println("\nSet color in " + zone + " to yellow");
		sendData(createData(5,zone.gebByte(),(byte) 0x3B));
	}

	/**
	 * Changes the color to orange
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void setColorToOrange (Zone zone) {
		System.out.println("\nSet color in " + zone + " to orange");
		sendData(createData(5,zone.gebByte(),(byte) 0x25));
	}

	//TODO setSaturation does not yet work
	/**
	 * Set saturation to value
	 *
	 * @param zone zone in which the command should be applied
	 * @param saturation saturation between 0 and 100 in percent
	 */
	public void setSaturation (Zone zone, int saturation) {
		System.out.println("\nSet saturation in " + zone + " to " + (int) convertPercentage(saturation) + "%");
		sendData(createData(6, zone.gebByte(), convertPercentage(saturation)));
	}

	/**
	 * Set brightness to given percentage
	 *
	 * @param zone zone in which the command should be applied
	 * @param brightness brightness between 0 and 100 in percent
	 */
	public void setBrightness (Zone zone, int brightness) {
		System.out.println("\nSet brightness in " + zone + " to " + (int) convertPercentage(brightness) + "%");
		sendData(createData(7, zone.gebByte(), convertPercentage(brightness)));
	}

	/**
	 * Set Kelvin to given percentage
	 * Example:
	 * 0x00 (0 in decimal) = 2700K (Warm White), 0x19 = 3650K, 0x32 = 4600K, 0x4B, = 5550K, 0x64 = 6500K (100 in decimal) (Cool White)
	 *
	 * Note:
	 * Because of Hardware restrictions this function is not tested. Please contact me if your Lights support different kelvin levels (Warm White & Cool White)
	 *
	 * @param zone zone in which the command should be applied
	 * @param kelvin kelvin between 0 and 100 in percent
	 */
	public void setKelvin (Zone zone, int kelvin) {
		System.out.println("\nSet kelvin in " + zone + " to " + convertPercentage(kelvin) + "%");
		sendData(createData(8, zone.gebByte(), convertPercentage(kelvin)));
	}

	/**
	 * Starts the build in mode with the given number
	 * The speed can be set using the methods {@link Bridge#increaseSpeed} and {@link Bridge#decreaseSpeed)}
	 *
	 * @param zone zone in which the command should be applied
	 * @param mode Control.Mode
	 */
	public void setMode (Zone zone, Mode mode) {
		System.out.println("\nSet mode in " + zone + " to mode  " + mode);
		sendData(createData(9,zone.gebByte(), mode.gebByte()));
	}

	/**
	 * Starts the mode with the given number
	 * The speed can be set using the methods {@link Bridge#increaseSpeed} and {@link Bridge#decreaseSpeed)}
	 *
	 * @param zone zone in which the command should be applied
	 * @param modeNr Number of mode to start. In case there is no mode with the given number the bridge will probably ignore the command
	 */
	public void setMode (Zone zone, int modeNr) {
		System.out.println("\nSet mode in " + zone + " to mode nr " + modeNr);
		sendData(createData(9,zone.gebByte(),(byte) modeNr));
	}

	/**
	 * Increase speed of current mode
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void increaseSpeed (Zone zone) {
		System.out.println("\nIncrease speed in " + zone);
		sendData(createData(10,zone.gebByte()));
	}

	/**
	 * Decrease speed of current mode
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void decreaseSpeed (Zone zone) {
		System.out.println("\nDecrease speed in " + zone);
		sendData(createData(11,zone.gebByte()));
	}

	/**
	 * Links a light to the selected zone. 
	 * Please refer to your manual for this step, but in general it should work as follows:
	 * Plug your light in to a power source and within 3 seconds use this command to link the Light to the selected zone.
	 * 
	 * @param zone zone in which the command should be applied
	 */
	public void linkLightsToZone (Zone zone) {
		System.out.println("\nLinking Light to " + zone);
		sendData(createData(12, zone.gebByte()));
	}
	
	/**
	 * Unlink a light to the selected zone. 
	 * Please refer to your manual for this step, but in general it should work as follows:
	 * Plug your light in to a power source and within 3 seconds use this command to unlink the Light to the selected zone.
	 *
	 * @param zone zone in which the command should be applied
	 */
	public void unlinkLightsToZone (Zone zone) {
		System.out.println("\nUnlink Light from " + zone);
		sendData(createData(13, zone.gebByte()));
	}


	/**
	 * Sends a keep alive message
	 */
	private void keepAlive () {
		System.out.println("~~ sending keep alive message");
		sendData(new byte[]{(byte) 0xD0, 0x00, 0x00, 0x00, 0x02, wifiBridgeSessionID1, wifiBridgeSessionID2});
	}

	/**
	 * Converts an int value between 0 and 100 to its byte equivalent.
	 * If the Value is less than 0, it will return 0x00 (0).
	 * If the Value is greater than 100, it will return 0x64 (100).
	 *
	 * @param percentage integer percentage value between 0 and 100 (if above 100 or bellow 0 the value will be 100 or 0)
	 * @return the given value as byte between 0x00 and 0x64
	 */
	private byte convertPercentage (int percentage) {
		if (100 < percentage) {
			return 0x64;
		} else if (percentage < 0) {
			return 0x00;
		} else {
			return (byte) percentage;
		}
	}

	/**
	 * Discovers all v6 and v5 bridges in the network.
	 * Please note that no part of this software was tested with v5 bridges. See README.md.
	 *
	 * @return null if an error occurred, otherwise a set containing the ip addresses of all v6 and v5 bridges in the local network.
	 * @throws SocketException Throws an SocketException if unable to open new DatagramSocket
	 */
	public static HashSet<String> discoverBridges() throws SocketException {
		byte[] b1 = "HF-A11ASSISTHREAD".getBytes(StandardCharsets.UTF_8);	// Discover v6 Bridges.
		byte[] b2 = "Link_Wi-Fi".getBytes(StandardCharsets.UTF_8);			// Discover v5 Bridges.
		byte[] receivedData = new byte[32];

		HashSet<String> bridges = new HashSet<String>();
		DatagramSocket socket = new DatagramSocket();;
		InetAddress broadcast = null;
		String ip;

		socket.setSoTimeout(1000);

		try {
			broadcast = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}

		DatagramPacket sendPacketV6	 = new DatagramPacket(b1, b1.length, broadcast, 48899);
		DatagramPacket sendPacketV5	 = new DatagramPacket(b2, b2.length, broadcast, 48899);
		DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);

		// Send the discover packages
		for (int i = 0; i < 10; i++) {
			try {
				socket.send(sendPacketV6);
				socket.send(sendPacketV5);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ignored) {
			}
		}

		// receive bridges until the timout is over
		while(true) {
			try {
				socket.receive(receivePacket);
			} catch (IOException ignored) {
				break;
			}
			ip = new String(receivedData, StandardCharsets.UTF_8);
			ip = ip.substring(0, ip.indexOf(','));
			bridges.add(ip);
		}

		return bridges;
	}

	/**
	 * Sends data to the bridge
	 *
	 * @param data data that will be send to the bridge
	 * @return received answer from the bridge. Null if timeout is reached
	 */
	private byte[] sendData (byte[] data) {
		byte[] receiveData = new byte[32];

		DatagramPacket sendPacket = new DatagramPacket(data, data.length, bridgeIp, port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		System.out.println("\nattempting to send: " + bytesToHexString(sendPacket.getData()));

		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Exception handling
			e.printStackTrace();
		}

		try {
			socket.receive(receivePacket);
		} catch (IOException e) {
			// Sometimes the bridge does not answer.
			e.printStackTrace();
			return null;
		}

		String modifiedSentence = bytesToHexString(receivePacket.getData());
		System.out.println("received data: " + modifiedSentence + "\n");
		System.out.println("wifiBridgeSessionID1: " + bytesToHexString(wifiBridgeSessionID1) + ", wifiBridgeSessionID2: " + bytesToHexString(wifiBridgeSessionID2));

		// Check if the Session is still alive
		if(!sessionAlive(receiveData)) {
			System.out.println("###\n\tBRIDGE NOT CONNECTED\n###");

			// Restart the session if automaticallyRestartSession is enabled.
			if (automaticallyRestartSession) {
				try {
					startNewSession(2);
				} catch (BridgeException e) {
					// TODO: do not exit, better error handing required
					e.printStackTrace();
					System.exit(1); // only for testing!
				}
			}
		}

		return receiveData;
	}

	/**
	 * Sends data to the bridge and retries if no answer is received.
	 *
	 * @param data data that will be send to the bridge
	 * @param maxAttempts maximum number of attempts to send and receive data. If this value is lower than 2 it will be set to 2. In case only one attempt is wanted, use sendData().
	 * @param waitBeforeRetry Waits for waitBeforeRetry milliseconds before retry. must be greater than or equal to 0
	 * @return received answer from the bridge. Null if timeout is reached
	 */
	private byte[] sendDataRetry (byte[] data, int maxAttempts, int waitBeforeRetry) {
		if (waitBeforeRetry < 0) {
			waitBeforeRetry = 75;
		}

		// Set maxAttempts to two if maxAttempts is less than 2. Using this function with maxAttempts lower than 2 would not make sense, use sendData() instead.
		if (maxAttempts < 2) {
			maxAttempts = 2;
		}

		// Try sending the data until either a answer is received of we ran out of attempts.
		byte[] received = null;
		for (byte attempt = 0; received == null && attempt < maxAttempts; attempt++) {
			received = sendData(data);

			// If the timeout was reached wait a bit and try again
			if (received == null) {
				System.out.println("### Failed to receive answer from bridge. Attempt: " + (attempt + 1) + " of: " + maxAttempts + " ###");
				try {
					Thread.sleep(waitBeforeRetry);
				} catch (InterruptedException ignored) {}
			}
		}
		return received;
	}

	/**
	 * simplified version of createData for commands that only require the commandNr and Zone.
	 *
	 * Creates a byte array that can be send to the Bridge
	 *
	 * @param commandNr see createCommand
	 *                  1 = Light on
	 *                  2 = Light off
	 *                  3 = Night Light ON
	 *                  4 = White Light ON (Color RGB OFF)
	 *                  10 = increase mode speed
	 *               	11 = decrease mode speed
	 *             		12 = link light to bridge
	 *              	13 = unlink light from bridge
	 *
	 * @param zone zone for the command
	 *
	 * @return byte array that can be send to the Bridge
	 *
	 * Commands that require an additional value:
	 * 	- 5 : set color
	 * 	- 6 : set saturation
	 * 	- 7 : set brightens
	 * 	- 8 : set Kelvin
	 * 	- 9 : set Mode
	 */
	private byte[] createData(int commandNr, byte zone) {
		// if the commandNr requires an additional value TODO: Exception?
		if (commandNr == 5 || commandNr == 6 || commandNr == 7 || commandNr == 8 || commandNr == 9) {
			System.out.println("\n ### WARNING ###\ncommandNr: " + commandNr + " requires an additional parameter!\n ### END WARNING ###");
		}
		return createData(commandNr, zone, (byte) 0);
	}

	/**
	 *  Creates a byte array that can be send to the Bridge
	 *
	 * @param commandNr see createCommand
	 *					1 = Light on						(does not require value)
	 *                  2 = Light off						(does not require value)
	 *                  3 = Night Light ON					(does not require value)
	 *                  4 = White Light ON (Color RGB OFF)	(does not require value)
	 *                  5 = set color to value
	 *                  6 = set saturation to value (hex values 0x00 to 0x64 : examples: 0x00 = 0%, 0x19 = 25%, 0x32 = 50%, 0x4B, = 75%, 0x64 = 100%)
	 *                  	Note: 0x64 equals 100 in decimal
	 *                  7 = set brightens to value (hex values 0x00 to 0x64 : examples: 0x00 = 0%, 0x19 = 25%, 0x32 = 50%, 0x4B, = 75%, 0x64 = 100%)
	 * 	                   	Note: 0x64 equals 100 in decimal
	 *                  8 = set Kelvin to value (KV hex values 0x00 to 0x64 : examples: 0x00 = 2700K (Warm White), 0x19 = 3650K, 0x32 = 4600K, 0x4B, = 5550K, 0x64 = 6500K (Cool White)
	 *              		Note: 0x64 equals 100 in decimal
	 *              	9 = set Mode to mode nr given in value
	 *              	10 = increase mode speed			(does not require value)
	 *              	11 = decrease mode speed 			(does not require value)
	 *              	12 = link light to bridge			(does not require value)
	 *              	13 = unlink light from bridge 		(does not require value)
	 *
	 * @param zone zone for the command
	 * @param value value used for the following commands: set color (5),
	 * @return byte array that can be send to the Bridge
	 */
	private byte[] createData(int commandNr, byte zone, byte value) {
		// format of command:
		// 9 byte packet = 0x31 {PasswordByte1 default 00} {PasswordByte2 default 00} {remoteStyle 08 for RGBW/WW/CW or 00 for bridge lamp} {LightCommandByte1} {LightCommandByte2} 0x00 0x00 0x00 {Zone1-4 0=All} 0x00 {Checksum}
		byte[] command = createCommand(commandNr, value);

		byte[] data = new byte[]{
				(byte) 0x80, 0x00, 0x00, 0x00, 0x11, wifiBridgeSessionID1, wifiBridgeSessionID2, 0x00, sequentialByte, 0x00,
				command[0], command[1], command[2], command[3], command[4], command[5], command[6], command[7], command[8],
				zone, 0x00, 0x00 //Last byte is placeholder for checksum
		};

		sequentialByte++;

		// insert checksum
		// RGBW/WW/CW Checksum Byte Calculation is the sum of the 11 bytes before end of the UDP packet. The checksum is then added to the end of the UDP message.
		// take the 9 bytes of the command, and 1 byte of the zone, and add the 0 = the checksum
		byte checksum = calculateChecksum(Arrays.copyOfRange(data, 10,21));
		data [21] = checksum;

		return data;
	}

	/**
	 * Creates a 9 byte long command.
	 *
	 * @param commandNr 1 = Light on						(does not require value)
	 *                  2 = Light off						(does not require value)
	 *                  3 = Night Light ON					(does not require value)
	 *                  4 = White Light ON (Color RGB OFF)	(does not require value)
	 *                  5 = set color to value
	 *                  6 = set saturation to value (hex values 0x00 to 0x64 : examples: 0x00 = 0%, 0x19 = 25%, 0x32 = 50%, 0x4B, = 75%, 0x64 = 100%)
	 *                  	Note: 0x64 equals 100 in decimal
	 *                  7 = set brightens to value (hex values 0x00 to 0x64 : examples: 0x00 = 0%, 0x19 = 25%, 0x32 = 50%, 0x4B, = 75%, 0x64 = 100%)
	 * 	                  	 Note: 0x64 equals 100 in decimal
	 *                  8 = set Kelvin to value (KV hex values 0x00 to 0x64 : examples: 0x00 = 2700K (Warm White), 0x19 = 3650K, 0x32 = 4600K, 0x4B, = 5550K, 0x64 = 6500K (Cool White)
	 *              		Note: 0x64 equals 100 in decimal
	 *              	9 = set Mode to mode nr given in value
	 *              	10 = increase mode speed			(does not require value)
	 *              	11 = decrease mode speed 			(does not require value)
	 *              	12 = link light to bridge			(does not require value)
	 *              	13 = unlink light from bridge 		(does not require value)
	 *
	 * @return 9 byte long command
	 */
	private byte[] createCommand(int commandNr, byte value) {
		switch (commandNr) {
			case 1: {
				// Light on
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, 0x01, 0x00, 0x00, 0x00}; // return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x04, 0x01, 0x00, 0x00, 0x00};
			}
			case 2: {
				// Light off
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, 0x02, 0x00, 0x00, 0x00}; // return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x04, 0x02, 0x00, 0x00, 0x00};
			}
			case 3: {
				// Night light
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x04, 0x05, 0x00, 0x00, 0x00};
			}
			case 4: {
				// White Light ON (Color RGB OFF)
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, 0x05, 0x00, 0x00, 0x00};
			}
			case 5: {
				// set color
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x01, value, value, value, value};
			}
			case 6: {
				// set saturation to value
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, value, 0x00, 0x00, 0x00};
			}
			case 7: {
				// set brightness to value
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x02, value, 0x00, 0x00, 0x00};
			}
			case 8: {
				// set kelvin to value
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x05, value, 0x00, 0x00, 0x00};
			}
			case 9: {
				// set mode to mode nr value
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x04, value, 0x00, 0x00, 0x00};
			}
			case 10: {
				// increase mode speed
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, 0x03, 0x00, 0x00, 0x00};
			}
			case 11: {
				// decrease mode speed
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x03, 0x04, 0x00, 0x00, 0x00};
			}
			case 12: {
				// link
				return new byte[]{0x3d, 0x00, 0x00, remoteStyle, 0x00, 0x00, 0x00, 0x00, 0x00};
			}
			case 13: {
				// unlink
				return new byte[]{0x3e, 0x00, 0x00, remoteStyle, 0x00, 0x00, 0x00, 0x00, 0x00};
			}
			default:
				// default case: Light on
				return new byte[]{0x31, 0x00, 0x00, remoteStyle, 0x04, 0x01, 0x00, 0x00, 0x00};
		}

		/*
		 * The 5th byte is he command byte used for the identification of some commands. Summary of command bytes:
		 * 0 :
		 * 1 : set color
		 * 2 : set brightness
		 * 3 : Light on / off, in-/decrease mode speed
		 * 4 : set mode
		 * 5 :
		 */
	}

	/**
	 * Checks if the session is still alive by looking into a answer from the bridge
	 *
	 * @param data byte array containing the answer from the bridge
	 * @return true if the session is still alive, false otherwise.
	 */
	private boolean sessionAlive(byte[] data) {
		return !(data[7] == 1 && data[0] == (byte) 0x88)
				|| Arrays.equals(data, new byte[data.length]); // Response timeout, do nothing (return true). TODO inefficient
	}

	/**
	 * Calculates the checksum for {@code data}
	 *
	 * @param data byte array
	 * @return sum of 1's
	 */
	private static byte calculateChecksum(byte[] data) {
		byte checksum = 0;
		for (byte datum : data) {
			checksum += datum;
		}
		return checksum;
	}

	/**
	 * Starts a new Session and updates class variables wifiBridgeSessionID1 and wifiBridgeSessionID2.
	 * Increases the response timeout temporarily
	 *
	 * @param maxAttempts max attempts to restart the session.
	 * @throws BridgeException If {@code maxAttempts} is reached and the session is still not alive, a BridgeException is thrown.
	 */
	private void startNewSession(int maxAttempts) throws BridgeException {
		// Backup and disable automaticallyRestartSession to avoid a loop;
		boolean autoRestBackup = automaticallyRestartSession;
		automaticallyRestartSession = false;

		// Replace socket to discard possible old packages in receive buffer.
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// increase response timeout
		int oldTimeout = 0;
		try {
			oldTimeout = socket.getSoTimeout();
			socket.setSoTimeout(oldTimeout * 4);
		} catch (SocketException ignored) {
		}

		System.out.println("\n## Starting a new session ##");
		// Get wifiBridgeSessionID1 and wifiBridgeSessionID2
		byte[] received = null;

		// Attempt to get a new session. If the response begins with 0x28 the bridge accepted the request.
		for (int i = 0; i < maxAttempts && (received == null || received[0] != (byte) 0x28); i++) {
			received = sendDataRetry(new byte[]{
					(byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x02, (byte) 0x62, (byte) 0x3A, (byte) 0xD5, (byte) 0xED, (byte) 0xA3, (byte) 0x01, (byte) 0xAE,
					(byte) 0x08, (byte) 0x2D, (byte) 0x46, (byte) 0x61, (byte) 0x41, (byte) 0xA7, (byte) 0xF6, (byte) 0xDC, (byte) 0xAF, (byte) 0x0, (byte) 0x0, (byte) 0x00, (byte) 0x00, (byte) 0x1E
			}, 10, 250);

			// Wait before the next attempt, if another attempt is required
			if (received == null || received[0] != (byte) 0x28) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Error handling when failed to create a new session
					System.err.println("FAILED TO START A NEW SESSION");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

		// Failed to connect to the bridge
		if (received == null) {
			throw new BridgeException("Could not start a new Session");
		}

		// The wifiBridgeSessionID1 is the 20th byte of response above
		wifiBridgeSessionID1 = received[19];
		// The wifiBridgeSessionID2 is the 21th byte of response above
		wifiBridgeSessionID2 = received[20];

		// restore automaticallyRestartSession
		automaticallyRestartSession = autoRestBackup;

		// Debug output
		System.out.println("bridge connected");

		// reset the timeout
		if(oldTimeout > 0) {
			try {
				socket.setSoTimeout(oldTimeout);
			} catch (SocketException ignored) {
			}
		}
	}

	/**
	 * Starts a thread which periodically sends a message to the bridge to keep the session alive.
	 */
	private void startKeepAliveThread() {
		new Thread(() -> {
			while (true) {
				keepAlive();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Converts each byte in a byte array to its Hex value
	 *
	 * @param bytes byte array
	 * @return String of comma separated hex values
	 */
	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder("[");
		for (byte b : bytes) {
			sb.append(String.format("%02X", b));
			sb.append(", ");
		}
		sb.replace(sb.length()-2,sb.length(),"]");
		sb.append(" (length=").append(bytes.length).append(")");
		return sb.toString();
	}

	/**
	 * Converts a byte to its Hex value
	 *
	 * @param b byte to convert
	 * @return String with the hex value
	 */
	private static String bytesToHexString(byte b) {
		return "0x" + String.format("%02X", b);
	}

	@Override
	public String toString() {
		return  "\n-- Bridge Ip: " + this.bridgeIp +
				"\n-- Bridge Port: " + this.port +
				"\n-- sequentialByte: " + bytesToHexString(sequentialByte) +
				"\n-- wifiBridgeSessionID1: " + bytesToHexString(wifiBridgeSessionID1) +
				"\n-- wifiBridgeSessionID2: " + bytesToHexString(wifiBridgeSessionID2) +
				"\n-- remote style: " + bytesToHexString(remoteStyle);
	}
}
