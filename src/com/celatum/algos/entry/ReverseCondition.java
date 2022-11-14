package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;

public class ReverseCondition extends EntryCondition {
	private EntryCondition ec;

	public ReverseCondition(EntryCondition ec) {
		this.ec = ec;
	}
	
	public void setUp(HistoricalData hd) {
		ec.setUp(hd);
	}
	
	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		return !ec.canEnter(hd, bor);
	}

	@Override
	public String toString() {
		return "!"+ec.toString();
	}

	@Override
	public EntryCondition clone() {
		EntryCondition clone = ec.clone();
		clone.linkAlgo(this.algo);
		return new ReverseCondition(clone);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();
		set.add(new ReverseCondition(ec.clone()));
		return set;
	}
}
