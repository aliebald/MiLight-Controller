package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.io.IOException;

/**
 * PulseLights MusicMode
 *
 * @author Alexander Liebald
 */
public class PulseLights implements MusicMode {
	private byte brightness = 80;
	private Bridge bridge;

	public PulseLights(Bridge bridge) throws IOException {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() throws IOException {
		brightness = 110;
		bridge.setBrightness(Zone.ALL, brightness);
	}

	@Override
	public void maintain() throws IOException {
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
