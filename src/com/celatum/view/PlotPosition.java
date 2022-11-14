package com.celatum.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.celatum.trading.Position;

public class PlotPosition extends AbstractPlot {
	private Position p;

	public PlotPosition(Position p) {
		this.p = p;
	}
	
	@Override
	public void addTo(XYPlot plot) {
		// Title
		String title = p.toString();
		
		TimeSeries ts = new TimeSeries(title);
		ts.add(new Day(p.getEntryDate()), p.getEntryPrice());
		ts.add(new Day(p.getCloseDate()), p.getClosePrice());

		XYItemRenderer indRenderer = new StandardXYItemRenderer();
		indRenderer.setDefaultToolTipGenerator(
				new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));
		indRenderer.setSeriesPaint(0, Color.WHITE);
		Stroke stroke = new BasicStroke(2.0f);
		indRenderer.setSeriesStroke(0, stroke);
		int index = plot.getDatasetCount();
		plot.setDataset(index, new TimeSeriesCollection(ts));
		plot.setRenderer(index, indRenderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}
}
