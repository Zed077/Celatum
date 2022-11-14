package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

/**
 * If a position has been constantly loosing money for positionAge bars then
 * exit.
 * 
 * @author cedric.ladde
 *
 */
public class NegativeForTooLong extends ExitStrategy {
	private int positionAge = 3;

	public NegativeForTooLong() {
	}

	public NegativeForTooLong(int positionAge) {
		this.positionAge = positionAge;
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
			// Compute age of the position as a number of bars
			int age = 0;
			int daysNegative = 0;
			for (; age < hd.size(); age++) {
				if (p.getDeltaPrice() < 0) {
					daysNegative++;
				}

				if (p.getEntryDate().compareTo(hd.midClose.getDate(age)) >= 0 || age == positionAge)
					break;
			}

			if (daysNegative == positionAge) {
				double newLimit;
				if (p instanceof LongPosition) {
					newLimit = 0;
				} else {
					newLimit = p.getEntryPrice() * 10;
				}
				p.setLimit(newLimit);
			}
		}
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new NegativeForTooLong(3));
		set.add(new NegativeForTooLong(5));
		set.add(new NegativeForTooLong(10));

		return set;
	}

	@Override
	public String toString() {
		return "NFTL/" + positionAge;
	}

	@Override
	public ExitStrategy clone() {
		return new NegativeForTooLong(positionAge);
	}

}
