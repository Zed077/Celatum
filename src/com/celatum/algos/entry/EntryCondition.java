package com.celatum.algos.entry;

import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.algos.AlgoComponent;
import com.celatum.algos.exit.ExitStrategy;
import com.celatum.data.HistoricalData;

public abstract class EntryCondition extends AlgoComponent {
	
	public abstract boolean canEnter(HistoricalData hd, BookOfRecord bor);

	@Override
	public abstract EntryCondition clone();
	
	public abstract Set<EntryCondition> generateVariants();
	
}
