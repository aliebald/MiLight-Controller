package control;

import audioProcessing.BeatDetector;
import musicModes.MusicMode;

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
