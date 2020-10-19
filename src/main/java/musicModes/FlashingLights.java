package main.java.musicModes;

import main.java.bridge.Bridge;
import main.java.bridge.Zone;

/**
 * FlashingLights MusicMode
 *
 * @author Alexander Liebald
 */
public class FlashingLights implements MusicMode {
	private Bridge bridge;
	private int turnedOn = 0; // Times maintain was called since the last beat


	public FlashingLights(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		turnedOn = 0;
		bridge.turnOn(Zone.ALL);
	}

	@Override
	public void maintain() {
		if(turnedOn > 8) {
			bridge.turnOff(Zone.ALL);
		} else {
			turnedOn++;
		}
	}
}
