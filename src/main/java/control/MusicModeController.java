package control;

import audioProcessing.BeatDetector;
import musicModes.MusicMode;

import java.io.IOException;

/**
 * This class is responsible for controlling music effects.
 * It is recommended to start this in a separate Thread, can be stopped by calling {@code stop()} on {@code MusicModeController} (Not on the actual Thread!).
 *
 * @author Alexander Liebald
 */
public class MusicModeController implements Runnable {
	private volatile MusicMode musicMode;
	private volatile BeatDetector beatDetector;
	private volatile boolean running;

	public MusicModeController(MusicMode musicMode, BeatDetector beatDetector) {
		this.musicMode = musicMode;
		this.beatDetector = beatDetector;
		running = false;
	}

	public void run() {
		running = true;
		while (keepRunning()) {
			try {
				if (beatDetector.detect()) {
					musicMode.beat();
				} else {
					musicMode.maintain();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("MusicModeController Thread is donne");
	}

	private synchronized boolean keepRunning(){
		return running;
	}

	/**
	 * Stops the MusicModeController, if it is running.
	 */
	public synchronized void stop(){
		running = false;
	}

	/**
	 * @return Returns the current MusicMode
	 */
	public synchronized MusicMode getMusicMode() {
		return musicMode;
	}

	/**
	 * @param musicMode update MusicMode
	 */
	public synchronized void setMusicMode(MusicMode musicMode) {
		this.musicMode = musicMode;
	}

	/**
	 * Replaces the BeatDetector used.
	 *
	 * @param beatDetector new BeatDetector
	 */
	public synchronized void setBeatDetector(BeatDetector beatDetector) {
		this.beatDetector = beatDetector;
	}

	/**
	 * Returns the BeatDetector used.
	 */
	public synchronized BeatDetector getBeatDetector() {
		return beatDetector;
	}
}
