package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.maths.EMA;

public class EMADistance extends EntryCondition {
	private int periodEMA = 5;
	private double triggerDistance = 1;
	private int periodATR = 20;
	private Serie ema;
	private Serie adp;

	public EMADistance() {
	}

	public EMADistance(int periodEMA, double triggerDistance) {
		this.periodEMA = periodEMA;
		this.triggerDistance = triggerDistance;
	}

	public void setUp(HistoricalData hd) {
		ema = EMA.calc(hd.midClose, periodEMA);
		hd.syncReferenceIndex(ema);

		adp = ATRPercent.calc(hd, periodATR, ATR.Method.SMA);
		hd.syncReferenceIndex(adp);
	}

	/**
	 * enter if distance to ema is greater than distance
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		double currentDistance = hd.midClose.get(0) - ema.get(0);
		double limit = ema.get(0) * adp.get(0) * triggerDistance;
		return (triggerDistance > 0 && currentDistance > limit) || (triggerDistance < 0 && currentDistance < limit);
	}

	@Override
	public String toString() {
		return "EMAD/" + periodEMA + triggerDistance;
	}

	public EntryCondition clone() {
		return new EMADistance(periodEMA, triggerDistance);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new EMADistance(5, 0.5));
		set.add(new EMADistance(5, 1));
		set.add(new EMADistance(5, 1.5));
		set.add(new EMADistance(20, 1.5));
		set.add(new EMADistance(20, 2));
		set.add(new EMADistance(50, 3));
		set.add(new EMADistance(70, 4));

		set.add(new EMADistance(5, -0.5));
		set.add(new EMADistance(5, -1));
		set.add(new EMADistance(5, -1.5));
		set.add(new EMADistance(20, -1.5));
		set.add(new EMADistance(20, -2));
		set.add(new EMADistance(50, -3));
		set.add(new EMADistance(70, -4));

		return set;
	}
}
