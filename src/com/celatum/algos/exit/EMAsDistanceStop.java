package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class EMAsDistanceStop extends DailyTrailingStop {
	private Serie ema20;
	private Serie ema50;
	private Serie adp;
	private int period = 70;
	private double emaDistance = 0;
	private double limitDistance = 3;

	public EMAsDistanceStop() {
	}

	public EMAsDistanceStop(int period, double emaDistance, double limitDistance) {
		this.period = period;
		this.emaDistance = emaDistance;
		this.limitDistance = limitDistance;
	}

	@Override
	public void setUp(HistoricalData hd) {
		ema50 = Calc.ema(hd.midClose, 50);
		ema20 = Calc.ema(hd.midClose, 20);
		adp = Calc.atrPercent(hd, period);

		hd.syncReferenceIndex(ema20);
		hd.syncReferenceIndex(ema50);
		hd.syncReferenceIndex(adp);
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
		// check trend is going up
		boolean trendUp = ema20.get(0) - ema50.get(0) > adp.get(0) * emaDistance;

		if (!trendUp) {
			double currentStop = p.getStop();
			double proposedStop = hd.bidLow.get(0);
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
			p.setLimit(ema20.get(0) * (1 + adp.get(0) * limitDistance));
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, Position p) {
		// check trend is going down
		boolean trendDown = ema50.get(0) - ema20.get(0) > adp.get(0) * emaDistance;

		if (!trendDown) {
			double currentStop = p.getStop();
			double proposedStop = hd.askHigh.get(0);
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
			p.setLimit(ema20.get(0) * (1 - adp.get(0) * limitDistance));
		}
	}

	@Override
	public String toString() {
		return "EDS/" + period + emaDistance + limitDistance;
	}

	@Override
	public ExitStrategy clone() {
		EMAsDistanceStop clone = new EMAsDistanceStop(period, emaDistance, limitDistance);
		return clone;
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new EMAsDistanceStop(70, 0, 1));
		set.add(new EMAsDistanceStop(70, 0, 1.5));
		set.add(new EMAsDistanceStop(70, 0, 2));
		set.add(new EMAsDistanceStop(70, 0, 2.5));
		set.add(new EMAsDistanceStop(70, 0, 3));

		set.add(new EMAsDistanceStop(70, 1, 1));
		set.add(new EMAsDistanceStop(70, 1, 1.5));
		set.add(new EMAsDistanceStop(70, 1, 2));
		set.add(new EMAsDistanceStop(70, 1, 2.5));
		set.add(new EMAsDistanceStop(70, 1, 3));

		set.add(new EMAsDistanceStop(70, 2, 1));
		set.add(new EMAsDistanceStop(70, 2, 1.5));
		set.add(new EMAsDistanceStop(70, 2, 2));
		set.add(new EMAsDistanceStop(70, 2, 2.5));
		set.add(new EMAsDistanceStop(70, 2, 3));

		return set;
	}

}
