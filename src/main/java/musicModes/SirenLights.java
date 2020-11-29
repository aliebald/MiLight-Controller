package musicModes;

import bridge.Bridge;
import bridge.Zone;

import java.io.IOException;

/*
 *  Copyright 2020 Alexander Liebald
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * SirenLights MusicMode
 *
 * @author Alexander Liebald
 */
public class SirenLights implements MusicMode {
	private Bridge bridge;
	private byte run = 0;

	public SirenLights(Bridge bridge) throws IOException {
		this.bridge = bridge;
		bridge.turnOn(Zone.ALL);
		bridge.setBrightness(Zone.ALL,100);
	}

	@Override
	public void beat() throws IOException {
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
	}

	@Override
	public void maintain() {
	}
}
