package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.SuperTrend;

public class SuperTrendIndicator extends EntryCondition {
	private Serie st;
	private int period = 10;
	private ATR.Method method = ATR.Method.SMA;

	public SuperTrendIndicator() {
	}

	/**
	 * 
	 * @param period
	 * @param bullish if false then bearish
	 */
	public SuperTrendIndicator(int period, ATR.Method method) {
		this.period = period;
		this.method = method;
	}

	public void setUp(HistoricalData hd) {
		st = SuperTrend.calc(hd, period, 3, method);
		hd.syncReferenceIndex(st);
	}

	/**
	 * Close above buy, close below sell
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		return hd.midClose.get(0) > st.get(0);
	}

	@Override
	public String toString() {
		return "ST/" + period + method;
	}

	public EntryCondition clone() {
		return new SuperTrendIndicator(period, method);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new SuperTrendIndicator(7, ATR.Method.SMA));
		set.add(new SuperTrendIndicator(10, ATR.Method.SMA));
		
		set.add(new SuperTrendIndicator(7, ATR.Method.EMA));
		set.add(new SuperTrendIndicator(10, ATR.Method.EMA));

		return set;
	}
}
