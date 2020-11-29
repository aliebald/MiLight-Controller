package audioProcessing;

import java.util.Collections;
import java.util.LinkedList;

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
 * Lightweight normalizer for 16 bit audio
 *
 * @author Alexander Liebald
 */
public class Normalizer {
	@SuppressWarnings("FieldCanBeLocal")
	private final int historyBufferSize = 80; // One second are about 43 1024 byte samples (with 44100Hz, 8bit dual channel or 16 bin mono).
	private final LinkedList<Short> historyBuffer = new LinkedList<>();

	/**
	 * Initiates a new Normalizer
	 */
	public Normalizer() {
		// Add a few "silent" values to avoid index out of bounds in isSilent()
		this.historyBuffer.add((short) 5);
		this.historyBuffer.add((short) 5);
		this.historyBuffer.add((short) 5);
		this.historyBuffer.add((short) 5);
	}

	/**
	 * Normalizes a frame using the information from older frames
	 *
	 * @param frame Audio frame with one value per short. Use AudioProcessor to convert 16 bit streams (2 bytes per value) to a short array
	 * @see AudioProcessor
	 */
	public short[] normalize(short[] frame) {
		// Add the maximum of the current frame to the historyBuffer
		short max = findAbsMax(frame);
		historyBuffer.add(max);

		if (historyBuffer.size() > historyBufferSize) {
			historyBuffer.pop();
		}

		// If the current input is silent, so new music / voices etc., return a static signal
		if (isSilent()) {
			return new short[]{100};
		}

		// find the largest value from the last HISTORY_BUFFER_SIZE frames
		max = Collections.max(historyBuffer);
		double offset = (double) Short.MAX_VALUE / max; // Warning: changing ths computation leads to problems in BeatDetector!

		// Normalize each sample in-place
		for (int i = 0; i < frame.length; i++) {
			frame[i] = (short) ((double) frame[i] * offset);
		}

		return frame;
	}

	/**
	 * Checks the last 5 few frames to determine whether or not there is no sound output.
	 * This can be used to avoid upscaling noise between songs or when no music is playing.
	 *
	 * @return true if the current signal is silent and therefore should not be up-scaled.
	 */
	private boolean isSilent() {
		return historyBuffer.get(0) < 20 && historyBuffer.get(1) < 20 && historyBuffer.get(2) < 20 && historyBuffer.get(3) < 20 && historyBuffer.get(4) < 20;
	}

	/**
	 * Finds the largest absolute value in the array
	 *
	 * @param array array with at least one value
	 * @return largest absolute element of the Array
	 */
	private short findAbsMax(short[] array) {
		if (array.length == 0) {
			return 0;
		}

		short max = array[0];
		for (short s : array) {
			if (Math.abs(s) > max) {
				max = s;
			}
		}
		return max;
	}
}
