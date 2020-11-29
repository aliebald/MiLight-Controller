package bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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
 * Zones for {@see Bridge}
 * Can be ALL, FIRST, SECOND, THIRD or FOURTH
 *
 * @author Alexander Liebald
 */
public enum Zone {
	ALL(0),
	FIRST(1),
	SECOND(2),
	THIRD(3),
	FOURTH(4);

	private final byte byteNr;
	private static final Queue<Zone> zones = new LinkedList<>(Arrays.asList(Zone.FIRST, Zone.SECOND, Zone.THIRD, Zone.FOURTH));

	public byte gebByte (){
		return byteNr;
	}

	/**
	 * @param nr 0 = All Zones
	 *           1 = Control.Zone 1
	 *           2 = Control.Zone 2
	 *           3 = Control.Zone 3
	 *           4 = Control.Zone 4
	 */
	Zone(int nr) {
		switch (nr){
			case 1:
				this.byteNr = 0x01;
				return;
			case 2:
				this.byteNr = 0x02;
				return;
			case 3:
				this.byteNr = 0x03;
				return;
			case 4:
				this.byteNr = 0x04;
				return;
			default:
				this.byteNr = 0x00;
		}
	}

	/**
	 * Loops through all 4 zones
	 *
	 * @return the next zone in the queue
	 */
	public static Zone getNextZone() {
		Zone z = zones.poll();
		zones.add(z);
		return z;
	}

	@Override
	public String toString() {
		return "Zone{" +
				"byteNr=" + byteNr +
				'}';
	}
}
