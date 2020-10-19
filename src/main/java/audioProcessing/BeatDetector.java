package main.java.audioProcessing;

import javax.sound.sampled.*;
import java.util.LinkedList;

/**
 * BeatDetector can be used to detect beats in the chosen audio output/input.
 *
 * @author Alexander Liebald
 */
// @SuppressWarnings({"FieldCanBeLocal"}) // TODO remove this and look over the variables when finished
public class BeatDetector {
	private final AudioFormat audioFormat;
	private final float sampleRate = 44100;
	private final int sampleSizeInBits = 16;
	private final int channels = 1;

	private final LinkedList <Long> historyBuffer = new LinkedList<>();
	private final Normalizer normalizer = new Normalizer();;
	private TargetDataLine line;

	// The cooldown regulates how often a beat can be detected. A beat can occur at most every cooldown milliseconds
	private long cooldown;
	private long lastBeat;

	// minBeatLength defines how many samples need to be above the threshold before a beat is detected. Do not set lower than 1.
	private final int minBeatLength = 1;
	private int beatsBefore = 0;

	// variables that change the bpm calculation
	private final long historyBufferSize = 43 * 2;
	private final int bufferSize = 1024;
	private byte[] buffer = new byte[bufferSize];

	/**
	 * @param cooldown minimum time in milliseconds between beats.
	 *                 A high value can lead to skipped beats while a low value may lead to the same beat being detected twice.
	 *                 Setting this to a higher value reduces load on the network, Bridge and controllers. A good starting point
	 *                 could be between 80-200, but values outside of this range can make sense too.
	 */
	public BeatDetector(int cooldown) {
		lastBeat = System.currentTimeMillis();
		this.cooldown = cooldown;
		// Create audio format
		audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, true);
		line = getStdTargetDataLine(audioFormat);

		// Open and start the TargetDataLine
		try {
			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			// TODO Handle Exception
			e.printStackTrace();
			return;
		}

		line.start();
	}

	/**
	 * Get and examine a audio sample.
	 * Should be called in a loop, otherwise the detection will not work correctly.
	 *
	 * @return true if the current sample is a beat, false if not.
	 */
	public boolean detect() {
		if (line.read(buffer, 0, bufferSize) > 0){
			return process(normalizer.normalize(AudioProcessor.convertByteToShortArray(buffer)));
		}
		return false;
	}

	/**
	 * The cooldown regulates how often a beat can be detected. A beat can occur at most every <code>cooldown</code> milliseconds.
	 *
	 * @return current cooldown in milliseconds
	 */
	public long getCooldown() {
		return cooldown;
	}

	/**
	 * The cooldown regulates how often a beat can be detected. A beat can occur at most every <code>cooldown</code> milliseconds.
	 *
	 * @param cooldown new cooldown in milliseconds
	 */
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	/**
	 * processes a normalized audio sample.
	 *
	 * @see Normalizer
	 * @param normalized normalized audio sample
	 * @return true if a beat was detected, false otherwise
	 */
	private boolean process(short[] normalized) {
		long energy;
		long threshold;
		long localAverageEnergy;
		boolean beat = false;

		// compute energy and localAverageEnergy
		energy = absSum(normalized);
		if (historyBuffer.size() > 0) {
			localAverageEnergy = absSum(historyBuffer) / historyBuffer.size();
		} else {
			localAverageEnergy = energy;
		}

		// TODO adjust the values for the variance
		// compute the variance. NOTE: if the normalizer is changed the values for c may need to change.
		int trueVariance = calculateVariance(localAverageEnergy);
		double factor = ((1.0 / 1800000.0) * trueVariance) + 1.1;

		// insert the energy into the historyBuffer
		historyBuffer.add(energy);
		if (historyBuffer.size() > historyBufferSize) {
			historyBuffer.pop();
		}

		threshold = (long) (factor * localAverageEnergy);

		// if energy is higher than the threshold and the last beat was at least <code>cooldown</code> milliseconds ago, we have a beat
		if (energy > threshold && lastBeat + cooldown < System.currentTimeMillis()) {
			if (beatsBefore >= minBeatLength) {
				beat = true;
				beatsBefore = 0;
				lastBeat = System.currentTimeMillis();
			} else {
				beatsBefore++;
			}
		}

		// Debug output. Use this in combination with the Display class
		/*
		int beatForDisplay;
		if (beat) {
			beatForDisplay = (int) threshold / 4;
		} else {
			beatForDisplay = 0;
		}
		Platform.runLater(() -> main.java.control.Display.update(energy, (int) (1.45 * localAverageEnergy), beatForDisplay, (int) threshold));
		*/
		return beat;
	}

	// for debugging
	private int findMin(LinkedList<Integer> list) {
		int min = Integer.MAX_VALUE;
		for (int elem : list) {
			if (elem < min && elem > 0) {
				min = elem;
			}
		}
		return min;
	}

	// for debugging
	private int findMax(LinkedList<Integer> list) {
		int min = list.get(0);
		for (int elem : list) {
			if (elem > min) {
				min = elem;
			}
		}
		return min;
	}

	/**
	 * Computes the variance of the historyBuffer using the localAverageEnergy.
	 *
	 * @param localAverageEnergy current localAverageEnergy.
	 * @return variance of historyBuffer.
	 */
	private int calculateVariance(long localAverageEnergy) {
		int variance = 0;
		if (historyBuffer.size() > 1) {
			for (long e : historyBuffer) {
				variance += Math.abs((e - localAverageEnergy));
			}
			variance /= historyBuffer.size();
		}
		return variance;
	}

	/**
	 * Computes the sum of the absolute values in a LinkedList.
	 *
	 * @param list list with values.
	 * @return sum of the absolute values.
	 */
	private long absSum(LinkedList <Long> list){
		long sum = 0;
		for (Long i : list){
			sum += Math.abs(i);
		}
		return sum;
	}

	/**
	 * Computes the sum of the absolute values in a byte array.
	 *
	 * @param array list with values.
	 * @return sum of the absolute values.
	 */
	private long absSum(byte[] array) {
		long sum = 0;
		for (byte b : array){
			sum += Math.abs(b);
		}
		return sum;
	}

	/**
	 * Computes the sum of the absolute values in a short array.
	 *
	 * @param array array with values.
	 * @return sum of the absolute values.
	 */
	private long absSum(short[] array) {
		long sum = 0;
		for (short s : array){
			sum += Math.abs(s);
		}
		return sum;
	}

	/**
	 * Work in progress!
	 * Returns the standard TargetDataLine.
	 * TODO: automatically find the true standard TargetDataLine and make it possible to choose TargetDataLine in web interface (setup and settings wip).
	 *
	 * @param audioFormat AudioFormat used for the TargetDataLine
	 * @return standard TargetDataLine to listen to.
	 */
	private static TargetDataLine getStdTargetDataLine (AudioFormat audioFormat) {
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

		// Print available mixers
		System.out.println("Mixers available:");
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixers.length; i++) {
			System.out.println(i + ": " + mixers[i]);
			Mixer mixer = AudioSystem.getMixer(mixers[i]);
			Line.Info[] sourceInfos = mixer.getSourceLineInfo();
			System.out.println("  Source Lines:");
			for (int j = 0; j < sourceInfos.length; j++) {
				System.out.println("    " + j + ": " + sourceInfos[j]);
			}
			Line.Info[] targetLines = mixer.getTargetLineInfo();
			System.out.println("  Target Lines:");
			for (int j = 0; j < targetLines.length; j++) {
				System.out.println("    " + j + ": " + targetLines[j]);
			}
			System.out.println("  canCreateTargetDataLine: " + canCreateTargetDataLine(mixer, info));
			System.out.println();
		}

		// TODO Select correct input
		Mixer mixer = AudioSystem.getMixer(mixers[5]);

		System.out.println("\n#######\nTry to get the Line");
		Line.Info targetLineInfo = mixer.getTargetLineInfo()[0];
		System.out.println("info: " + info.toString());
		TargetDataLine targetDataLine = null;
		try {
			targetDataLine = (TargetDataLine) mixer.getLine(targetLineInfo);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		System.out.println("TargetDataLine: " + targetDataLine.getLineInfo() + "\n#######\n");
		return targetDataLine;

	}

	private static boolean canCreateTargetDataLine(Mixer mixer, DataLine.Info info) {
		try {
			TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(info);
		} catch (Exception ignored) {
			return false;
		}
		return true;
	}
}
