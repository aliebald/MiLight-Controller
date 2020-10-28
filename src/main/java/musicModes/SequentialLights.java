package musicModes;

import bridge.Bridge;
import bridge.Zone;
import java.util.Random;

/**
 * SequentialLights MusicMode
 *
 * @author Alexander Liebald
 */
public class SequentialLights implements MusicMode {
	private Bridge bridge;
	private final Random random = new Random();


	public SequentialLights(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		bridge.setColor(Zone.getNextZone(), (byte) random.nextInt(127));
	}

	@Override
	public void maintain() {
	}
}
