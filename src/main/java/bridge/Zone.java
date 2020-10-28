package bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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
	private static final Queue<Zone> zones = new LinkedList<Zone>(new ArrayList<Zone>(Arrays.asList(Zone.FIRST, Zone.SECOND, Zone.THIRD, Zone.FOURTH)));

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
