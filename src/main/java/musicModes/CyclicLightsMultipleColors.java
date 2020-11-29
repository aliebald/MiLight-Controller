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
