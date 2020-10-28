package bridge;

/**
 * Build in modes, tested for RGB and RGBW Controllers.
 *
 * @author Alexander Liebald
 */
public enum Mode {
	BREATHING_COLOR_WHEEL(2),
	PARTY(3),
	PARTY_MULTIPLE_COLORS(4),
	FLASH_RED(5),
	FLASH_GREEN(6),
	FLASH_BLUE(7),
	COLOR_WHEEL(9);

	private final byte byteNr;

	public byte gebByte (){
		return byteNr;
	}

	/**
	 * Supported Modes:
	 * 2: BreathingColorWheel,
	 * 3: Party,
	 * 4: PartyMultipleColors,
	 * 5: FlashRed,
	 * 6: FlashGreen,
	 * 7: FlashBlue,
	 * 9: ColorWheel
	 *
	 * @param nr mode number
	 */
	Mode(int nr) {
		switch (nr){
			case 2:
				this.byteNr = 0x02;
				return;
			case 3:
				this.byteNr = 0x03;
				return;
			case 4:
				this.byteNr = 0x04;
				return;
			case 5:
				this.byteNr = 0x05;
				return;
			case 6:
				this.byteNr = 0x06;
				return;
			case 7:
				this.byteNr = 0x07;
				return;
			case 9:
				this.byteNr = 0x09;
				return;
			default:
				this.byteNr = 0x00;

		}
	}
}
