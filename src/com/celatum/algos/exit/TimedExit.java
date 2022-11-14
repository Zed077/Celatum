package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

/**
 * Exit a position after X bars
 * 
 * @author cedric.ladde
 *
 */
public class TimedExit extends ExitStrategy {
	private int positionAge;

	public TimedExit() {
		
	}
			
	public TimedExit(int positionAge) {
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
			for (; age < hd.size(); age++) {
				if (p.getEntryDate().compareTo(hd.midClose.getDate(age)) >= 0 || age == positionAge)
					break;
			}

			if (age == positionAge) {
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

		set.add(new TimedExit(2));
		set.add(new TimedExit(3));
		set.add(new TimedExit(5));
		set.add(new TimedExit(10));
		set.add(new TimedExit(15));
		set.add(new TimedExit(20));
		set.add(new TimedExit(30));

		return set;
	}

	@Override
	public String toString() {
		return "TE/" + positionAge;
	}

	@Override
	public ExitStrategy clone() {
		return new TimedExit(positionAge);
	}

}
