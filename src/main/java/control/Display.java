package main.java.control;

import main.java.audioProcessing.BeatDetector;
import main.java.bridge.Bridge;
import javafx.application.Application;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.scene.*;
import main.java.musicModes.CyclicLights;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Live Graph used for debugging and improving {@see BeatDetector}.
 *
 * @author Alexander Liebald
 */
public class Display extends Application {
	private static Thread bpmThread;
	private static int WINDOW_SIZE = 630; // etwa 15 sec
	private static CategoryAxis xAxis;
	private static NumberAxis yAxis;
	private static LineChart<String, Number> lineChart;
	private static Scene scene;
	private static XYChart.Series<String, Number> series1, series2, series3, series4; // 1: data, 2: threshold, 3: Beat (yes/no), 4: variance/c value
	private static SimpleDateFormat simpleDateFormat;
	private Thread mmcThread;
	private MusicModeController mmc;

	/**
	 * For Testing
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Add a new value to the graph
	 * 1: data, 2: threshold, 3: Beat (yes/no)
	 */
	public static void update(long value1, long value2, int value3, int value4){
		// System.out.println("update called with: " + value1);

		Date now = new Date();

		series1.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), value1));
		series2.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), value2));
		series3.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), value3));
		series4.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), value4));


		if (series1.getData().size() > WINDOW_SIZE) {
			series1.getData().remove(0);
			series2.getData().remove(0);
			series3.getData().remove(0);
			series4.getData().remove(0);
		}
	}

	@Override
	public void start(Stage primaryStage) throws InterruptedException {
		primaryStage.setTitle("Graph");

		xAxis = new CategoryAxis();
		yAxis = new NumberAxis();
		lineChart = new LineChart<>(xAxis, yAxis);
		scene = new Scene(lineChart, 2560, 1000);
		series1 = new XYChart.Series<>();
		series2 = new XYChart.Series<>();
		series3 = new XYChart.Series<>();
		series4 = new XYChart.Series<>();
		simpleDateFormat = new SimpleDateFormat("HH:mm:ss:SS");

		// Defining the axes
		xAxis.setLabel("Time/s");
		xAxis.setAnimated(false); // axis animations are removed
		yAxis.setLabel("Value");
		yAxis.setAnimated(false); // axis animations are removed

		//creating the line chart with two axis created above
		lineChart.setTitle("WIP");
		lineChart.setAnimated(false); // disable animations

		//defining a series to display data
		series1.setName("Normalized Data");
		series2.setName("Threshold");
		series3.setName("Beat");
		series4.setName("Threshold with c factor");

		// add series to chart
		lineChart.getData().add(series1);
		lineChart.getData().add(series2);
		lineChart.getData().add(series3);
		lineChart.getData().add(series4);

		// setup scene
		primaryStage.setScene(scene);

		// run BeatDetector

		System.out.println("Thread Running");
		Bridge bridge = null;
		try {
			bridge = new Bridge("192.168.0.52", 5987, false, 100);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		mmc = new MusicModeController(new CyclicLights(bridge), new BeatDetector(120));
		mmcThread = new Thread(mmc);
		mmcThread.start();

		// show the stage
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		mmc.stop();
	}
}