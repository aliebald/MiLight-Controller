package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * CyclicLightsMultipleColors MusicMode
 *
 * @author Alexander Liebald
 */
public class CyclicLightsMultipleColors implements MusicMode{
	private final Bridge bridge;
	private static final byte[] lastColor = new byte[]{0,20,40,60};
	private static final Zone[] zones = new Zone[]{Zone.FIRST, Zone.SECOND, Zone.THIRD, Zone.FOURTH};

	public CyclicLightsMultipleColors(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		for (int i = 0; i < 4; i++) {
			bridge.setColor(zones[i], lastColor[i] += 22);
		}
	}

	@Override
	public void maintain() {
	}
}
