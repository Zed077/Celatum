package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class EMATippingStop extends DailyTrailingStop {
	private Serie ema;
	private int period = 50;

	public EMATippingStop() {
	}

	/** 
	 * TODO could improve with a distance from close
	 * @param period
	 */
	public EMATippingStop(int period) {
		this.period = period;
	}

	@Override
	public void setUp(HistoricalData hd) {
		ema = Calc.ema(hd.midClose, period);
		hd.syncReferenceIndex(ema);
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup())) {
			if (p instanceof LongPosition) {
				manageLongs(hd, bor, p);
			} else {
				manageShorts(hd, bor, p);
			}
		}
	}

	private void manageLongs(HistoricalData hd, BookOfRecord bor, Position p) {
		if (ema.get(0) <= ema.get(1)) {
			double currentStop = p.getStop();
			double proposedStop = hd.bidClose.get(0);
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, Position p) {
		if (ema.get(0) >= ema.get(1)) {
			double currentStop = p.getStop();
			double proposedStop = hd.askClose.get(0);
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	public String toString() {
		return "EMAT/" + period;
	}

	@Override
	public ExitStrategy clone() {
		EMATippingStop clone = new EMATippingStop(period);
		return clone;
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
		
		set.add(new EMATippingStop(20));
		set.add(new EMATippingStop(50));
		set.add(new EMATippingStop(70));
		set.add(new EMATippingStop(200));

		return set;
	}

}
