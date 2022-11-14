package com.celatum.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.util.ShapeUtilities;

import com.celatum.data.Serie;

public class PlotSerie extends AbstractPlot {
	private Serie serie;
	private Color color;
	private String title;
	private boolean dashed;
	private Marker marker;
	final static float dash1[] = { 5.0f };
	public enum Marker {
		TriangleUp, TriangleDown
	}

	public PlotSerie(Serie s, String title, Color c, boolean dashed) {
		this.serie = s;
		this.color = c;
		this.title = title;
		this.dashed = dashed;
	}

	public PlotSerie(Serie s, String title, Color c, Marker marker) {
		this(s, title, c, false);
		this.marker = marker;
	}

	public PlotSerie(Serie s, String title, Color c) {
		this(s, title, c, false);
	}

	public PlotSerie(Serie s, String title) {
		this(s, title, null);
	}

	@Override
	public void addTo(XYPlot plot) {
		TimeSeries indSerie = toTimeSeries(serie, title);
		XYItemRenderer indRenderer = new StandardXYItemRenderer();
		indRenderer.setDefaultToolTipGenerator(
				new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));
		if (color != null)
			indRenderer.setSeriesPaint(0, color);

		if (dashed) {
			Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash1, 0.0f);
			indRenderer.setSeriesStroke(0, dashed);
		} else if (marker != null) {
			Stroke dashed = new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, dash1, 0.0f);
			indRenderer.setSeriesStroke(0, dashed);
		    indRenderer.setSeriesShape(0, ShapeUtilities.createUpTriangle(3.5f));
		}

		int index = plot.getDatasetCount();
		plot.setDataset(index, new TimeSeriesCollection(indSerie));
		plot.setRenderer(index, indRenderer);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
	}
}
