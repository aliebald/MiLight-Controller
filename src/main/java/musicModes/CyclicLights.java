package main.java.musicModes;

import main.java.bridge.Bridge;
import main.java.bridge.Zone;

/**
 * CyclicLights MusicMode
 *
 * @author Alexander Liebald
 */
public class CyclicLights implements MusicMode {
	private Bridge bridge;
	private byte lastColor = 0;


	public CyclicLights(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		lastColor = (byte) (lastColor + 22);
		bridge.setColor(Zone.ALL, lastColor);
	}

	@Override
	public void maintain() {
	}
}

