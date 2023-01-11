package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.RSI;

public class RSIFilter extends EntryCondition {
	private double level = 70;
	private boolean above = false;
	private Serie rsi;

	public RSIFilter() {

	}

	public RSIFilter(double level, boolean above) {
		this.above = above;
		this.level = level;
	}

	public void setUp(HistoricalData hd) {
		// RSI
		rsi = RSI.calc(hd.midClose);
		hd.syncReferenceIndex(rsi);
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		double currentRSILevel = rsi.get(0);

		boolean canEnter;
		if (above) {
			canEnter = currentRSILevel > level;
		} else {
			canEnter = currentRSILevel < level;
		}
		return canEnter;
	}

	@Override
	public String toString() {
		return "RSIF/" + level + above;
	}

	public EntryCondition clone() {
		return new RSIFilter(level, above);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new RSIFilter(70, true));
		set.add(new RSIFilter(60, true));
		set.add(new RSIFilter(50, true));
		set.add(new RSIFilter(40, true));
		set.add(new RSIFilter(30, true));

		set.add(new RSIFilter(70, false));
		set.add(new RSIFilter(60, false));
		set.add(new RSIFilter(50, false));
		set.add(new RSIFilter(40, false));
		set.add(new RSIFilter(30, false));

		return set;
	}
}
