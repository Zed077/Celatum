package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;

public class Hammer extends EntryCondition {
	private Serie percent;
	private int period = 70;
	private boolean bullish = true;

	public Hammer() {
	}

	/**
	 * 
	 * @param period
	 * @param bullish false=bearish, true=bullish
	 */
	public Hammer(int period, boolean bullish) {
		this.period = period;
		this.bullish = bullish;
	}

	public void setUp(HistoricalData hd) {
		percent = Calc.atrPercent(hd, period);
		hd.syncReferenceIndex(percent);
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		// Significant breath
		boolean sb = hd.midHigh.get(0) - hd.midLow.get(0) >= hd.midClose.get(1) * percent.get(0) * 1;

		// Hammer shape
		boolean hs = hd.midHigh.get(0) - hd.midLow.get(0) >= Math.abs(hd.midOpen.get(0) - hd.midClose.get(0)) * 4;

		boolean bh;
		if (bullish) {
			// Bullish hammer
			bh = Math.max(hd.midOpen.get(0), hd.midClose.get(0))
					- hd.midLow.get(0) >= (hd.midHigh.get(0) - hd.midLow.get(0)) * 0.95;
		} else {
			// Bearish hammer
			bh = hd.midHigh.get(0)
					- Math.min(hd.midOpen.get(0), hd.midClose.get(0)) >= (hd.midHigh.get(0) - hd.midLow.get(0)) * 0.95;
		}

		boolean res = sb && hs && bh;
		if (res) {
//			algo.plot(hd.midLow.getItem(0));
		}
		return res;
	}

	@Override
	public String toString() {
		return "HAM/" + period + bullish;
	}

	public EntryCondition clone() {
		return new Hammer(period, bullish);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new Hammer(20, true));
		set.add(new Hammer(70, true));
		set.add(new Hammer(200, true));
		
		set.add(new Hammer(20, false));
		set.add(new Hammer(70, false));
		set.add(new Hammer(200, false));

		return set;
	}
}
