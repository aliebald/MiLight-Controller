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

