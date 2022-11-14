package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;

/**
 * Ensure there is no position already open
 * @author cedric.ladde
 *
 */
public class NoPositionOpen extends EntryCondition {
	public void setUp(HistoricalData hd) {
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		return bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup()).size() == 0;
	}

	@Override
	public String toString() {
		return "NPO";
	}

	public EntryCondition clone() {
		return new NoPositionOpen();
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();
		
		set.add(new NoPositionOpen());
		
		return set;
	}
}
