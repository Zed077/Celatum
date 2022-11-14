package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class SignificantFavorableMove extends ExitStrategy {
	private int period = 20;
	private double distanceToEntry = 3;
	private double tightenedDistance = 0.5;
	private Serie atr;

	// TODO move away from ATR to ADP
	public SignificantFavorableMove() {
	}

	public SignificantFavorableMove(int period, double distanceToEntry, double tightenedDistance) {
		this.period = period;
		this.distanceToEntry = distanceToEntry;
		this.tightenedDistance = tightenedDistance;
	}

	@Override
	public void setUp(HistoricalData hd) {
		atr = Calc.atr(hd, period);
		hd.syncReferenceIndex(atr);
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
		if (hd.midClose.get(0) > p.getEntryPrice() + distanceToEntry * atr.get(0)) {
			double currentStop = p.getStop();
			double proposedStop = hd.midClose.get(0) - atr.get(0) * tightenedDistance;
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, Position p) {
		if (hd.midClose.get(0) < p.getEntryPrice() - distanceToEntry * atr.get(0)) {
			double currentStop = p.getStop();
			double proposedStop = hd.midClose.get(0) + atr.get(0) * tightenedDistance;
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	public String toString() {
		return "SFM/" + period + distanceToEntry + tightenedDistance;
	}

	@Override
	public ExitStrategy clone() {
		SignificantFavorableMove clone = new SignificantFavorableMove(period, distanceToEntry, tightenedDistance);
		return clone;
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
		
		set.add(new SignificantFavorableMove(20, 3, 0.5));
		set.add(new SignificantFavorableMove(20, 4, 0.5));
		set.add(new SignificantFavorableMove(20, 5, 0.5));
		set.add(new SignificantFavorableMove(20, 6, 0.5));
		
		set.add(new SignificantFavorableMove(20, 3, 1));
		set.add(new SignificantFavorableMove(20, 4, 1));
		set.add(new SignificantFavorableMove(20, 5, 1));
		set.add(new SignificantFavorableMove(20, 6, 1));
		
		set.add(new SignificantFavorableMove(20, 3, 2));
		set.add(new SignificantFavorableMove(20, 4, 2));
		set.add(new SignificantFavorableMove(20, 5, 2));
		set.add(new SignificantFavorableMove(20, 6, 2));
		
		
		set.add(new SignificantFavorableMove(70, 3, 0.5));
		set.add(new SignificantFavorableMove(70, 4, 0.5));
		set.add(new SignificantFavorableMove(70, 5, 0.5));
		set.add(new SignificantFavorableMove(70, 6, 0.5));
		
		set.add(new SignificantFavorableMove(70, 3, 1));
		set.add(new SignificantFavorableMove(70, 4, 1));
		set.add(new SignificantFavorableMove(70, 5, 1));
		set.add(new SignificantFavorableMove(70, 6, 1));
		
		set.add(new SignificantFavorableMove(70, 3, 2));
		set.add(new SignificantFavorableMove(70, 4, 2));
		set.add(new SignificantFavorableMove(70, 5, 2));
		set.add(new SignificantFavorableMove(70, 6, 2));
		
		
		set.add(new SignificantFavorableMove(200, 3, 0.5));
		set.add(new SignificantFavorableMove(200, 4, 0.5));
		set.add(new SignificantFavorableMove(200, 5, 0.5));
		set.add(new SignificantFavorableMove(200, 6, 0.5));
		
		set.add(new SignificantFavorableMove(200, 3, 1));
		set.add(new SignificantFavorableMove(200, 4, 1));
		set.add(new SignificantFavorableMove(200, 5, 1));
		set.add(new SignificantFavorableMove(200, 6, 1));
		
		set.add(new SignificantFavorableMove(200, 3, 2));
		set.add(new SignificantFavorableMove(200, 4, 2));
		set.add(new SignificantFavorableMove(200, 5, 2));
		set.add(new SignificantFavorableMove(200, 6, 2));

		return set;
	}

}
