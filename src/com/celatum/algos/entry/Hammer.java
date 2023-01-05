package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;

public class Hammer extends EntryCondition {
	private Serie atrPercent;
	private int atrPeriod;
	private boolean bullish = true;

	public Hammer() {
	}

	/**
	 * 
	 * @param atrPeriod
	 * @param bullish false=bearish, true=bullish
	 */
	public Hammer(int atrPeriod, boolean bullish) {
		this.atrPeriod = atrPeriod;
		this.bullish = bullish;
	}

	public void setUp(HistoricalData hd) {
		atrPercent = Calc.atrPercent(hd, atrPeriod);
		hd.syncReferenceIndex(atrPercent);
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		// Significant breath
		boolean breath = hd.midHigh.get(0) - hd.midLow.get(0) >= hd.midClose.get(1) * atrPercent.get(0) * 1;

		// Hammer shape: small body
		boolean body = hd.midHigh.get(0) - hd.midLow.get(0) >= Math.abs(hd.midOpen.get(0) - hd.midClose.get(0)) * 3;

		// Hammer shape: small wick
		boolean wick;
		if (bullish) {
			// Bullish hammer
			wick = hd.midHigh.get(0) - hd.midClose.get(0) <= (hd.midHigh.get(0) - hd.midLow.get(0)) * 0.1;
		} else {
			// Bearish hammer
			wick = hd.midClose.get(0) - hd.midLow.get(0) <= (hd.midHigh.get(0) - hd.midLow.get(0)) * 0.1;
		}
		
		// Hammer shape: ends better than it started
		boolean dir;
		if (bullish) {
			dir = hd.midOpen.get(0) < hd.midClose.get(0);
		} else {
			// Bearish hammer
			dir = hd.midOpen.get(0) > hd.midClose.get(0);
		}

//		boolean res = breath && body && wick && dir;
		boolean res = wick && body && dir;
		if (res) {
//			algo.plot(hd.midLow.getItem(0));
		}
		return res;
	}

	@Override
	public String toString() {
		return "HAM/" + atrPeriod + bullish;
	}

	public EntryCondition clone() {
		return new Hammer(atrPeriod, bullish);
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
