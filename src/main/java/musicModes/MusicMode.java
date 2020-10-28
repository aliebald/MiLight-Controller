package musicModes;

/**
 * @author Alexander Liebald
 */
public interface MusicMode {
	/**
	 * This method gets called when a beat is detected.
	 * Depending on the implementation of this method, a
	 * light effect will be executed
	 */
	void beat();

	/**
	 * This method gets called when no beat is detected.
	 * Depending on the implementation of this method, a
	 * light effect will be executed. Some implementations also
	 * do nothing when this method is called.
	 */
	void maintain();
}
