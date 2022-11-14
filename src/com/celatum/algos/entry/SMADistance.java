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
	private double triggerDistance = 0.00001;
	private Serie sma;
	private Serie adp;

	public SMADistance() {
	}

	public SMADistance(int period, double triggerDistance) {
		this.period = period;
		this.triggerDistance = triggerDistance;
	}

	public void setUp(HistoricalData hd) {
		sma = SMA.calc(hd.midClose, period);
		hd.syncReferenceIndex(sma);

		adp = ATRPercent.calc(hd, period, ATR.Method.SMA);
		hd.syncReferenceIndex(adp);
	}

	/**
	 * enter if distance to ema is greater than distance
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		double currentDistance = hd.midClose.get(0) - sma.get(0);
		double limit = sma.get(0) * adp.get(0) * triggerDistance;
		return (triggerDistance > 0 && currentDistance > limit) || (triggerDistance < 0 && currentDistance < limit);
	}

	@Override
	public String toString() {
		return "SMAD/" + period + triggerDistance;
	}

	public EntryCondition clone() {
		return new SMADistance(period, triggerDistance);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new SMADistance(200, 0.0001));
		set.add(new SMADistance(200, 1));
		set.add(new SMADistance(200, 2));

		set.add(new SMADistance(200, -0.0001));
		set.add(new SMADistance(200, -1));
		set.add(new SMADistance(200, -2));

		return set;
	}
}
