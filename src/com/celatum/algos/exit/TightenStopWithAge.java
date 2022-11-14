package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class TightenStopWithAge extends ExitStrategy {
	private int agePeriod = 10;

	public TightenStopWithAge() {
	}

	public TightenStopWithAge(int agePeriod) {
		this.agePeriod = agePeriod;
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
			int da = 0;
			for (; da < hd.size(); da++) {
				if (p.getEntryDate().compareTo(hd.midClose.getDate(da)) >= 0)
					break;
			}
			if (da < agePeriod)
				return;

			double newStop;
			if (p instanceof LongPosition) {
				newStop = Math.max(p.getStop(), hd.midLow.get(0));
			} else {
				newStop = Math.min(p.getStop(), hd.midHigh.get(0));
			}
			p.setStop(newStop);
		}
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new TightenStopWithAge(1));
		set.add(new TightenStopWithAge(2));
		set.add(new TightenStopWithAge(3));
		set.add(new TightenStopWithAge(5));
		set.add(new TightenStopWithAge(10));
		set.add(new TightenStopWithAge(20));
		set.add(new TightenStopWithAge(40));
		set.add(new TightenStopWithAge(80));

		return set;
	}

	@Override
	public String toString() {
		return "TSWA/" + agePeriod;
	}

	@Override
	public ExitStrategy clone() {
		return new TightenStopWithAge(agePeriod);
	}

}
