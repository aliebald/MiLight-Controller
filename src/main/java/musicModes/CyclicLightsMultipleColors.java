package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.io.IOException;

/**
 * CyclicLightsMultipleColors MusicMode
 *
 * @author Alexander Liebald
 */
public class CyclicLightsMultipleColors implements MusicMode{
	private final Bridge bridge;
	private static final byte[] lastColor = new byte[]{0,20,40,60};
	private static final Zone[] zones = new Zone[]{Zone.FIRST, Zone.SECOND, Zone.THIRD, Zone.FOURTH};

	public CyclicLightsMultipleColors(Bridge bridge) throws IOException {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() throws IOException {
		for (int i = 0; i < 4; i++) {
			bridge.setColor(zones[i], lastColor[i] += 22);
		}
	}

	@Override
	public void maintain() {
	}
}
