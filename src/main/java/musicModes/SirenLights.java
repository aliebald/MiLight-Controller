package musicModes;

import bridge.Bridge;
import bridge.Zone;
import java.util.Random;

/**
 * SirenLights MusicMode
 *
 * @author Alexander Liebald
 */
public class SirenLights implements MusicMode {
	private Bridge bridge;
	private final Random random = new Random();
	private Zone lastZone = Zone.ALL;
	private byte run = 0;
	private boolean lastBlue = false;

	public SirenLights(Bridge bridge) {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() {
		Zone zone = Zone.getNextZone();
		System.out.println("SirenLights.beat run = " + run);
		switch (run) {
			case 0: {
				bridge.setColorToRed(Zone.FIRST);
				bridge.setColorToBlue(Zone.SECOND);
				run++;
				break;
			}
			case 1: {
				bridge.setColorToRed(Zone.THIRD);
				bridge.setColorToBlue(Zone.FOURTH);
				run++;
				break;
			}
			case 2: {
				bridge.setColorToBlue(Zone.FIRST);
				bridge.setColorToRed(Zone.SECOND);
				run++;
				break;
			}
			case 3: {
				bridge.setColorToBlue(Zone.THIRD);
				bridge.setColorToRed(Zone.FOURTH);
				run = 0;
			}
		}

		/*
		switch (run) {
			case 0: {
				bridge.setColorToRed(Zone.FIRST);
				run++;
				break;
			}
			case 1: {
				bridge.setColorToBlue(Zone.SECOND);
				run++;
				break;
			}
			case 2: {
				bridge.setColorToRed(Zone.THIRD);
				run++;
				break;
			}
			case 3: {
				bridge.setColorToBlue(Zone.FORTH);
				run++;
				break;
			}
			case 4: {
				bridge.setColorToBlue(Zone.FIRST);
				run++;
				break;
			}
			case 5: {
				bridge.setColorToRed(Zone.SECOND);
				run++;
				break;
			}
			case 6: {
				bridge.setColorToBlue(Zone.THIRD);
				run = 0;
			}
		}
		*/

		// bridge.turnOn(lastZone);
		// bridge.turnOff(zone);
		lastZone = zone;
	}

	@Override
	public void maintain() {
	}
}
