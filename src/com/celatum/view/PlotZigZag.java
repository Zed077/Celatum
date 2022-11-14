package com.celatum.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.celatum.data.Serie;
import com.celatum.maths.ZigZagRelative;

public class PlotZigZag extends AbstractPlot {
	private Serie z;

	public PlotZigZag(ZigZagRelative z) {
		this.z = z.getAll();
	}

	@Override
	public void addTo(XYPlot plot) {
		// Title
		String title = z.toString();

		TimeSeries indSerie = toTimeSeries(z, title);

		XYItemRenderer indRenderer = new StandardXYItemRenderer();
		// Labels
		indRenderer.setDefaultItemLabelGenerator(new LabelGenerator());
		indRenderer.setDefaultItemLabelsVisible(true);
		indRenderer.setDefaultItemLabelPaint(Color.WHITE);

		indRenderer.setDefaultToolTipGenerator(
				new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));
		indRenderer.setSeriesPaint(0, Color.RED);
		Stroke stroke = new BasicStroke(2.0f);
		indRenderer.setSeriesStroke(0, stroke);

		int index = plot.getDatasetCount();
		plot.setDataset(index, new TimeSeriesCollection(indSerie));
		plot.setRenderer(index, indRenderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}
}

class LabelGenerator extends StandardXYItemLabelGenerator {
	private static final long serialVersionUID = -8213688643314965958L;
	private NumberFormat percentFormat = NumberFormat.getPercentInstance();
	NumberFormat fractionFormat = NumberFormat.getInstance();

	public LabelGenerator() {
		super();
		percentFormat.setMinimumFractionDigits(1);
		fractionFormat.setMaximumFractionDigits(0);
	}

	@Override
	public String generateLabelString(XYDataset dataset, int series, int item) {
		double currentPrice = dataset.getYValue(series, item);
		String label = fractionFormat.format(currentPrice);
		if (item > 1) {
			label += " (";
			double previousPrice = dataset.getYValue(series, item - 1);
			double percent = (currentPrice - previousPrice) / previousPrice;
			label += percentFormat.format(percent);
			label += ")";
		}
		return label;
	}
}
