package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class RemoveLimit extends ExitStrategy {
	public RemoveLimit() {
	}

	@Override
	public void setUp(HistoricalData hd) {
		List<ExitStrategy> ess = algo.getExitStrategies();
		ess.remove(this);
		ess.add(ess.size(), this);
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup())) {
			if (p instanceof LongPosition) {
				p.setLimit(Double.MAX_VALUE);
			} else {
				p.setLimit(0);
			}
		}
	}

	@Override
	public String toString() {
		return "RL";
	}

	@Override
	public ExitStrategy clone() {
		return new RemoveLimit();
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
		set.add(new RemoveLimit());
		return set;
	}

}
