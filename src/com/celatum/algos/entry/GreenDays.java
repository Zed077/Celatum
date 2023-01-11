package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;

public class GreenDays extends EntryCondition {
	private int nDays = 5;
	private boolean green = true;

	public GreenDays() {

	}

	public GreenDays(int nDays, boolean green) {
		this.green = green;
		this.nDays = nDays;
	}

	public void setUp(HistoricalData hd) {
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		int greenCandles = 0;
		for (int i = 0; i<nDays; i++) {
			greenCandles += isGreen(hd, i) ? 1 : 0;
		}
		
		return (green && greenCandles == nDays) || (!green && greenCandles == 0);
	}
	
	public static boolean isGreen(HistoricalData hd, int index) {
		return hd.midClose.get(index) > hd.midClose.get(index+1);
	}

	@Override
	public String toString() {
		return "GD/" + nDays + green;
	}

	public EntryCondition clone() {
		return new GreenDays(nDays, green);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new GreenDays(3, true));
		set.add(new GreenDays(5, true));
		set.add(new GreenDays(7, true));

		set.add(new GreenDays(3, false));
		set.add(new GreenDays(5, false));
		set.add(new GreenDays(7, false));

		return set;
	}
}
