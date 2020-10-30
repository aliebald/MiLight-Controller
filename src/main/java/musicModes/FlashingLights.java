package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.io.IOException;

/**
 * FlashingLights MusicMode
 *
 * @author Alexander Liebald
 */
public class FlashingLights implements MusicMode {
	private Bridge bridge;
	private int turnedOn = 0; // Times maintain was called since the last beat


	public FlashingLights(Bridge bridge) throws IOException {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() throws IOException {
		turnedOn = 0;
		bridge.turnOn(Zone.ALL);
	}

	@Override
	public void maintain() throws IOException {
		if(turnedOn > 8) {
			bridge.turnOff(Zone.ALL);
		} else {
			turnedOn++;
		}
	}
}
