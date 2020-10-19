package main.java.musicModes;

import main.java.bridge.Bridge;
import main.java.bridge.Zone;

/**
 * PulseLights MusicMode
 *
 * @author Alexander Liebald
 */
public class PulseLights implements MusicMode {
	private byte brightness = 80;
	private Bridge bridge;

	public PulseLights(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		brightness = 110;
		bridge.setBrightness(Zone.ALL, brightness);
	}

	@Override
	public void maintain() {
		if (brightness > 50){
			brightness -= 2;
		} else if (brightness > 35){
			brightness -= 2;
		} else if (brightness > 10){
			brightness -= 1;
		} else {
			// return if brightness is unchanged
			return;
		}

		bridge.setBrightness(Zone.ALL, brightness);
	}
}
