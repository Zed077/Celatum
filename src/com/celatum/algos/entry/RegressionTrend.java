package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.maths.LinearRegression;

public class RegressionTrend extends EntryCondition {
	private int period;
	private double rate;

	public RegressionTrend() {
	}

	public RegressionTrend(int period, double rate) {
		this.period = period;
		this.rate = rate;
	}

	public void setUp(HistoricalData hd) {
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		LinearRegression lr = new LinearRegression(hd.midClose, period);
		if (rate >= 0) {
			return lr.getPercentYearlyRateOfChange() >= rate;
		} else {
			return lr.getPercentYearlyRateOfChange() <= rate;
		}
	}

	@Override
	public String toString() {
		return "RT/" + period + rate;
	}

	public EntryCondition clone() {
		return new RegressionTrend(period, rate);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new RegressionTrend(20, 0.1));
		set.add(new RegressionTrend(70, 0.1));
		set.add(new RegressionTrend(200, 0.1));
		
		set.add(new RegressionTrend(20, 0.2));
		set.add(new RegressionTrend(70, 0.2));
		set.add(new RegressionTrend(200, 0.2));
		
		set.add(new RegressionTrend(20, 0.3));
		set.add(new RegressionTrend(70, 0.3));
		set.add(new RegressionTrend(200, 0.3));

		set.add(new RegressionTrend(20, -0.1));
		set.add(new RegressionTrend(70, -0.1));
		set.add(new RegressionTrend(200, -0.1));
		
		set.add(new RegressionTrend(20, -0.2));
		set.add(new RegressionTrend(70, -0.2));
		set.add(new RegressionTrend(200, -0.2));
		
		set.add(new RegressionTrend(20, -0.3));
		set.add(new RegressionTrend(70, -0.3));
		set.add(new RegressionTrend(200, -0.3));

		return set;
	}
}
