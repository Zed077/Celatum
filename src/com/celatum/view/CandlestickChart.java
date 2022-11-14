package com.celatum.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.trading.Position;


@SuppressWarnings("serial")
public class CandlestickChart extends JPanel {
	private OHLCSeries ohlcSeries;
	private TimeSeries volumeSeries;

	public CandlestickChart(String title, Algo algo, BookOfRecord bor) {
		// Create new chart
		final JFreeChart candlestickChart = createChart(title, algo.plots, bor);
		// Create new chart panel
		final ChartPanel chartPanel = new ChartPanel(candlestickChart);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension chartSize = new Dimension(screenSize.width-150, screenSize.height-50);
		chartPanel.setPreferredSize(chartSize);
		// Enable zooming
		chartPanel.setMouseZoomable(true);
		chartPanel.setMouseWheelEnabled(true);
		add(chartPanel, BorderLayout.CENTER);
	}

	private JFreeChart createChart(String chartTitle, List<AbstractPlot> indicators, BookOfRecord bor) {

		/**
		 * Creating candlestick subplot
		 */
		// Create OHLCSeriesCollection as a price dataset for candlestick chart
		OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
		ohlcSeries = new OHLCSeries("Price");
		candlestickDataset.addSeries(ohlcSeries);
		// Create candlestick chart priceAxis
		NumberAxis priceAxis = new NumberAxis("Price");
		priceAxis.setAutoRangeIncludesZero(false);
		
		// Create candlestick chart renderer
		CandlestickRenderer candlestickRenderer = new CandlestickRenderer(){
            public Paint getItemPaint(int row, int column) {
                OHLCDataset dataset = (OHLCDataset) getPlot().getDataset();

                double open  = dataset.getOpenValue(row, column);
                double close = dataset.getCloseValue(row, column);

                if(open > close){
                    return getDownPaint();
                } else {
                    return getUpPaint();
                }
            }
        };
        candlestickRenderer.setCandleWidth(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        candlestickRenderer.setDrawVolume(false);
		
		// Create candlestickSubplot
		XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
		candlestickSubplot.setBackgroundPaint(Color.BLACK);

		// Indicators
		for (AbstractPlot ps : indicators) {
			ps.addTo(candlestickSubplot);
		}
		
		// Positions
		for (Position p : bor.getClosedPositions(new java.util.Date())) {
			PlotPosition pp = new PlotPosition(p);
			pp.addTo(candlestickSubplot);
		}

		/**
		 * Creating volume subplot
		 */
		// creates TimeSeriesCollection as a volume dataset for volume chart
		TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
		volumeSeries = new TimeSeries("Volume");
		volumeDataset.addSeries(volumeSeries);
		// Create volume chart volumeAxis
		NumberAxis volumeAxis = new NumberAxis("Volume");
		volumeAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
		// Create volume chart renderer
		XYBarRenderer timeRenderer = new XYBarRenderer();
		timeRenderer.setShadowVisible(false);
		timeRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
				new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0")));
		timeRenderer.setSeriesPaint(0, Color.BLUE);
		// Create volumeSubplot
		XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
		volumeSubplot.setBackgroundPaint(Color.white);

		/**
		 * Creating PnL subplot
		 */
		TimeSeriesCollection pnlDataset = new TimeSeriesCollection();
		pnlDataset.addSeries(toTimeSeries(bor.profitAndLoss, "Profit & Loss"));
		// Create volume chart volumeAxis
		NumberAxis pnlAxis = new NumberAxis("PnL");
		pnlAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		pnlAxis.setNumberFormatOverride(nf);
		// Create volume chart renderer
		XYItemRenderer pnlRenderer = new StandardXYItemRenderer();
		pnlRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Time={1} PnL={2}",
				new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0")));
		pnlRenderer.setSeriesPaint(0, Color.BLACK);
		// Create volumeSubplot
		XYPlot pnlSubplot = new XYPlot(pnlDataset, null, pnlAxis, pnlRenderer);
		pnlSubplot.setBackgroundPaint(Color.white);

		/**
		 * Creating Capital Utilisation subplot
		 */
		TimeSeriesCollection capitalDataset = new TimeSeriesCollection();
		capitalDataset.addSeries(toTimeSeries(bor.capitalUtilisation, "Capital Utilisation"));
		// Create volume chart volumeAxis
		NumberAxis capitalAxis = new NumberAxis("Capital Utilisation");
		capitalAxis.setAutoRangeIncludesZero(false);
		// Set to no decimal
		nf.setMaximumFractionDigits(0);
		capitalAxis.setNumberFormatOverride(nf);
		// Create volume chart renderer
		XYItemRenderer capitalRenderer = new StandardXYItemRenderer();
		capitalRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Time={1} PnL={2}",
				new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0")));
		capitalRenderer.setSeriesPaint(0, Color.BLACK);
		// Create volumeSubplot
		XYPlot capitalSubplot = new XYPlot(capitalDataset, null, capitalAxis, capitalRenderer);
		capitalSubplot.setBackgroundPaint(Color.white);

		/**
		 * Create chart main plot with two subplots (candlestickSubplot, volumeSubplot)
		 * and one common dateAxis
		 */
		// Creating charts common dateAxis
		DateAxis dateAxis = new DateAxis("Time");
		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yy"));
		// reduce the default left/right margin from 0.05 to 0.02
		dateAxis.setLowerMargin(0.02);
		dateAxis.setUpperMargin(0.02);
		// Create mainPlot
		CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
		mainPlot.setGap(10.0);
		mainPlot.add(candlestickSubplot, 4);
//		mainPlot.add(volumeSubplot, 1);
		mainPlot.add(pnlSubplot, 1);
		mainPlot.add(capitalSubplot, 1);
		mainPlot.setOrientation(PlotOrientation.VERTICAL);

		JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
		chart.removeLegend();
		return chart;
	}

	/**
	 * Fill series with data.
	 *
	 * @param t the t
	 */
	public void addCandel(long time, double o, double h, double l, double c, long v) {
		// Add bar to the data. Let's repeat the same bar
		FixedMillisecond t = new FixedMillisecond(time);
		ohlcSeries.add(t, o, h, l, c);
		volumeSeries.add(t, v);
	}
	
	public TimeSeries toTimeSeries(Serie s, String title) {
		TimeSeries ts = new TimeSeries(title);
		
		for (int j = 0; j < s.size(); j++) {
			SerieItem si = s.getItem(j);
			Day d = new Day(si.getDate());
			ts.add(d, si.getValue());
		}
		
		return ts;
	}

	/**
	 * Aggregate the (open, high, low, close, volume) based on the predefined time
	 * interval (1 minute)
	 *
	 * @param t the t
	 */
	public void load(HistoricalData hd) {
		for (int i = 0; i < hd.size(); i++) {
			addCandel(hd.midClose.getDate(i).getTime(), hd.midOpen.get(i), hd.midHigh.get(i), hd.midLow.get(i),
					hd.midClose.get(i), (long) hd.volume.get(i));
		}
	}

	/**
	 * Aggregate the (open, high, low, close, volume) based on the predefined time
	 * interval (1 minute)
	 *
	 * @param t the t
	 */
	public void load(HistoricalData hd, long offset) {
		for (int i = 0; i < hd.size(); i++) {
			addCandel(hd.midClose.getDate(i).getTime() + offset, hd.midOpen.get(i), hd.midHigh.get(i), hd.midLow.get(i),
					hd.midClose.get(i), (long) hd.volume.get(i));
		}
	}

}