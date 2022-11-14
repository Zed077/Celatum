package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.ATR;
import com.celatum.maths.ZigZagRelative;

/**
 * Triggers if we are approaching the period low from above
 * 
 * @author cedric.ladde
 *
 */
public class NearPeriodLow extends EntryCondition {
	private int period = 70;
	private double distance = 2;
	private Serie atr;

	public NearPeriodLow() {
	}

	public NearPeriodLow(int period) {
		this();
		this.period = period;
	}

	public NearPeriodLow(int period, double distance) {
		this(period);
		this.distance = distance;
	}

	public void setUp(HistoricalData hd) {
		atr = ATR.calc(hd, period, ATR.Method.SMA);
		hd.syncReferenceIndex(atr);
	}

	/**
	 * Close above buy, close below sell
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		ZigZagRelative zz = new ZigZagRelative(hd, atr);

		// Validation
		if (zz.getLows().size() == 0) {
			return false;
		}

		// Period low
		SerieItem periodLow = zz.lowestSince(hd.midLow.getDate(period));

		// Coming from above
		SerieItem min = hd.midLow.getMinSince(periodLow.getDate(), 0);
		boolean above = min.getValue() >= periodLow.getValue();

		// Near low
		boolean near = hd.midLow.get(0) < periodLow.getValue() + distance * atr.get(0);

		return above && near;
	}

	@Override
	public String toString() {
		return "NPL/" + period;
	}

	public EntryCondition clone() {
		return new NearPeriodLow(period);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new NearPeriodLow(70));
		set.add(new NearPeriodLow(200));

		return set;
	}
}
