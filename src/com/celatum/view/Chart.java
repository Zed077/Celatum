package com.celatum.view;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;

/**
 * The Class JfreeCandlestickChartDemo.
 * 
 * @author ashraf
 */
@SuppressWarnings("serial")
public class Chart extends JPanel {

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	public static void createAndShowGUI(HistoricalData hd, Algo algo, BookOfRecord bor) {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the chart.
		CandlestickChart jfreeCandlestickChart = new CandlestickChart(algo.getName() + " - " + hd.instrument.getName(),
				algo, bor);
		jfreeCandlestickChart.load(hd);
		frame.setContentPane(jfreeCandlestickChart);

		// Disable the resizing feature
		frame.setResizable(false);
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void createAndShowGUI(List<HistoricalData> histories, Algo algo, BookOfRecord bor) {
		createAndShowGUI(histories, algo, bor, false);
	}

	public static void createAndShowGUI(List<HistoricalData> histories, Algo algo, BookOfRecord bor,
			boolean statsOnly) {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the chart.
		CandlestickChart jfreeCandlestickChart = new CandlestickChart(algo.getName() + " - All", algo, bor);

		if (!statsOnly) {
			long offset = 0;
			for (HistoricalData hd : histories) {
				jfreeCandlestickChart.load(hd, offset);
				offset += 3600;
			}
		}

		frame.setContentPane(jfreeCandlestickChart);

		// Disable the resizing feature
		frame.setResizable(false);
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}