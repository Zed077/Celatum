package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.maths.BollingerBands;
import com.celatum.maths.SMA;

public class OutsideBollingerBands extends EntryCondition {
	private int period = 20;
	private double multiplier = 2;
	private boolean above = true;
	private BollingerBands bb;

	public OutsideBollingerBands() {
	}

	public OutsideBollingerBands(int period, double multiplier, boolean above) {
		this.period = period;
		this.multiplier = multiplier;
		this.above = above;
	}

	public void setUp(HistoricalData hd) {
		// BollingerBands
		bb = BollingerBands.calc(hd.midClose, period, multiplier);
		bb.syncReferenceIndex(hd);
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		boolean canEnter;
		if (above) {
			canEnter = hd.midClose.get(0) > bb.upper.get(0);
		} else {
			canEnter = hd.midClose.get(0) < bb.upper.get(0);
		}
		return canEnter;
	}

	@Override
	public String toString() {
		return "OBB/" + period + multiplier + above;
	}

	public EntryCondition clone() {
		return new OutsideBollingerBands(period, multiplier, above);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new OutsideBollingerBands(20, 1, true));
		set.add(new OutsideBollingerBands(20, 2, true));
		set.add(new OutsideBollingerBands(20, 2.5, true));
		set.add(new OutsideBollingerBands(20, 3, true));

		set.add(new OutsideBollingerBands(20, 1, false));
		set.add(new OutsideBollingerBands(20, 2, false));
		set.add(new OutsideBollingerBands(20, 2.5, false));
		set.add(new OutsideBollingerBands(20, 3, false));

		return set;
	}
}
