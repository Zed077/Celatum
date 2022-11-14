package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.EMA;
import com.celatum.maths.SuperTrend;

/**
 * Compares an EMA on a short time frame with one on a long time frame
 * @author cedric.ladde
 *
 */
public class EMACompare extends EntryCondition {
	private int periodShort = 5;
	private int periodLong = 20;
	private Serie emaShort;
	private Serie emaLong;

	public EMACompare() {
	}


	public EMACompare(int periodShort, int periodLong) {
		this.periodShort = periodShort;
		this.periodLong = periodLong;
	}


	public void setUp(HistoricalData hd) {
		emaShort = EMA.calc(hd.midClose, periodShort);
		hd.syncReferenceIndex(emaShort);
		
		emaLong = EMA.calc(hd.midClose, periodLong);
		hd.syncReferenceIndex(emaLong);
	}

	/**
	 * Close above buy, close below sell
	 */
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		return emaShort.get(0) > emaLong.get(0);
	}

	@Override
	public String toString() {
		return "EMAC/" + periodShort + periodLong;
	}

	public EntryCondition clone() {
		return new EMACompare(periodShort, periodLong);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();

		set.add(new EMACompare(5, 20));
		set.add(new EMACompare(20, 50));
		set.add(new EMACompare(50, 200));

		return set;
	}
}
