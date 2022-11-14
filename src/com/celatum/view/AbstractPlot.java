package com.celatum.view;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

public abstract class AbstractPlot {
	public abstract void addTo(XYPlot plot);

	public static TimeSeries toTimeSeries(Serie s, String title) {
		TimeSeries ts = new TimeSeries(title);

		for (int j = 0; j < s.size(); j++) {
			SerieItem si = s.getItem(j);
			Day d = new Day(si.getDate());
			ts.add(d, si.getValue());
		}

		return ts;
	}
}
