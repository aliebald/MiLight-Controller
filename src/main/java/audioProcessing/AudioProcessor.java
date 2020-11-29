package audioProcessing;

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
 * AudioProcessor provides some misc static methods for working with Audio.
 *
 * @author Alexander Liebald
 */
public class AudioProcessor {
	/**
	 * Converts a 16 bit big endian frame, with 2 byte per sample, from a byte array to short with one short per sample.
	 *
	 * @param array byte array with 16 bit values / 2 byte per value.
	 * @return a short array with the combined values. The length of the resulting array is half the length of the original array. If the Array has the length 0 this method will return a short array with the length 0.
	 */
	public static short[] convertByteToShortArray(byte[] array) {
		if (array.length == 0) {
			return new short[0];
		}

		short[] res = new short[array.length / 2];
		for (int i = 0; i < array.length; i+=2) {
			res[i/2] = (short)((array[i] << 8) | (array[i + 1] & 0xFF));
		}

		return res;
	}

	/**
	 * Pretty prints a byte buffer. Used for debug output.
	 */
	public static String printBuffer(byte[] buffer){
		StringBuilder sb = new StringBuilder();
		for (byte b : buffer) {
			sb.append(b);
			sb.append("	");
		}
		return sb.toString();
	}

	/**
	 * Pretty prints a short buffer. Used for debug output.
	 */
	public static String printBuffer(short[] buffer){
		StringBuilder sb = new StringBuilder();
		for (short value : buffer) {
			sb.append(value);
			sb.append("	");
		}
		return sb.toString();
	}

	// for debugging only
	public static byte findMin(byte[] array) {
		byte min = array[0];
		for (byte elem : array) {
			if (elem < min) {
				min = elem;
			}
		}
		return min;
	}

	// for debugging only
	public static short findMin(short[] array) {
		short min = array[0];
		for (short elem : array) {
			if (elem < min) {
				min = elem;
			}
		}
		return min;
	}

	// for debugging only
	public static long findAverage(byte[] array) {
		long avg = array[0];
		for (byte elem : array) {
			avg += elem;
		}
		return avg / array.length;
	}

	// for debugging only
	public static long findAverage(short[] array) {
		long avg = array[0];
		for (short elem : array) {
			avg += elem;
		}
		return avg / array.length;
	}
}
