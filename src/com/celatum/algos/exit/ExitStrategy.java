package com.celatum.algos.exit;

import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.algos.AlgoComponent;
import com.celatum.data.HistoricalData;

public abstract class ExitStrategy extends AlgoComponent {
	
	public abstract void managePositions(HistoricalData hd, BookOfRecord bor);
	
	public abstract Set<ExitStrategy> generateVariants();
	
	@Override
	public abstract ExitStrategy clone();
}
