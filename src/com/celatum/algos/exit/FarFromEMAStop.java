package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class FarFromEMAStop extends DailyTrailingStop {
	private Serie ema;
	private Serie adp;
	private int adpPeriod = 70;
	private int emaPeriod = 50;
	private double emaDistance = 3;

	public FarFromEMAStop() {
	}

	public FarFromEMAStop(int adpPeriod, double emaDistance) {
		this.adpPeriod = adpPeriod;
		this.emaDistance = emaDistance;
	}

	@Override
	public void setUp(HistoricalData hd) {
		ema = Calc.ema(hd.midClose, emaPeriod);
		adp = Calc.atrPercent(hd, adpPeriod);

		hd.syncReferenceIndex(ema);
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
		// If we are far from upper KC then tighten stop
		if (hd.midClose.get(0) > ema.get(0) * (1 + adp.get(0) * emaDistance)) {
			double currentStop = p.getStop();
			double proposedStop = hd.midLow.get(0);
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, Position p) {
		// If we are far from upper KC then tighten stop
		if (hd.midClose.get(0) > ema.get(0) * (1 - adp.get(0) * emaDistance)) {
			double currentStop = p.getStop();
			double proposedStop = hd.midHigh.get(0);
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	public String toString() {
		return "FFE/" + adpPeriod + emaDistance;
	}

	@Override
	public ExitStrategy clone() {
		FarFromEMAStop clone = new FarFromEMAStop(adpPeriod, emaDistance);
		return clone;
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new FarFromEMAStop(70, 3));
		set.add(new FarFromEMAStop(70, 4));
		set.add(new FarFromEMAStop(70, 5));
		set.add(new FarFromEMAStop(70, 6));
		set.add(new FarFromEMAStop(70, 7));
		set.add(new FarFromEMAStop(70, 8));

		return set;
	}

}
