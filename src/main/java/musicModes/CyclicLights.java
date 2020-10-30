package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.io.IOException;

/**
 * CyclicLights MusicMode
 *
 * @author Alexander Liebald
 */
public class CyclicLights implements MusicMode {
	private Bridge bridge;
	private byte lastColor = 0;


	public CyclicLights(Bridge bridge) throws IOException {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() throws IOException {
		lastColor = (byte) (lastColor + 22);
		bridge.setColor(Zone.ALL, lastColor);
	}

	@Override
	public void maintain() {
	}
}

