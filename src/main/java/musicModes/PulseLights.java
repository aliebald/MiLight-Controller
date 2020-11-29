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
