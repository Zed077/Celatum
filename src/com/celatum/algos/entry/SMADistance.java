package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.maths.SMA;

public class SMADistance extends EntryCondition {
	private int period = 200;
	private double triggerDistance = 10;
	private boolean above = true;
	private Serie sma;
	private Serie atrPercent;

	public SMADistance() {
	}

	/**
	 * 
	 * @param period
	 * @param distance in numbers of ATRs
	 */
	public SMADistance(int period, double distance, boolean above) {
		this.period = period;
		this.triggerDistance = distance;
		this.above = above;
	}

	public void setUp(HistoricalData hd) {
		sma = SMA.calc(hd.midClose, period);
		hd.syncReferenceIndex(sma);

		atrPercent = ATRPercent.calc(hd, period, ATR.Method.SMA);
		hd.syncReferenceIndex(atrPercent);
	}

	/**
	 * enter if distance to ema is greater than distance
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		double atr = hd.midClose.get(0) * atrPercent.get(0);
		double currentDistance = (hd.midClose.get(0) - sma.get(0)) / atr;
		return (above && currentDistance > triggerDistance) || (!above && currentDistance < triggerDistance);
	}

	@Override
	public String toString() {
		return "SMAD/" + period + triggerDistance + above;
	}

	public EntryCondition clone() {
		return new SMADistance(period, triggerDistance, above);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new SMADistance(200, 12, true));
		set.add(new SMADistance(200, 6, true));
		set.add(new SMADistance(200, 3, true));
		set.add(new SMADistance(200, 1, true));
		set.add(new SMADistance(200, 0, true));
		set.add(new SMADistance(200, -1, true));
		set.add(new SMADistance(200, -3, true));
		set.add(new SMADistance(200, -6, true));
		set.add(new SMADistance(200, -12, true));
		
		set.add(new SMADistance(200, 12, false));
		set.add(new SMADistance(200, 6, false));
		set.add(new SMADistance(200, 3, false));
		set.add(new SMADistance(200, 1, false));
		set.add(new SMADistance(200, 0, false));
		set.add(new SMADistance(200, -1, false));
		set.add(new SMADistance(200, -3, false));
		set.add(new SMADistance(200, -6, false));
		set.add(new SMADistance(200, -12, false));
		
		
		set.add(new SMADistance(50, 5, true));
		set.add(new SMADistance(50, 3, true));
		set.add(new SMADistance(50, 2, true));
		set.add(new SMADistance(50, 1, true));
		set.add(new SMADistance(50, 0, true));
		set.add(new SMADistance(50, -1, true));
		set.add(new SMADistance(50, -2, true));
		set.add(new SMADistance(50, -3, true));
		set.add(new SMADistance(50, -5, true));
		
		set.add(new SMADistance(50, 5, false));
		set.add(new SMADistance(50, 3, false));
		set.add(new SMADistance(50, 2, false));
		set.add(new SMADistance(50, 1, false));
		set.add(new SMADistance(50, 0, false));
		set.add(new SMADistance(50, -1, false));
		set.add(new SMADistance(50, -2, false));
		set.add(new SMADistance(50, -3, false));
		set.add(new SMADistance(50, -5, false));

		return set;
	}
}
