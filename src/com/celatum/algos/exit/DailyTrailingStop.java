package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class DailyTrailingStop extends ExitStrategy {
	private double breath = 3;
	private int period = 70;
	private Method method = Method.ADP;
	private Serie atr;
	private Serie adp;
	private Serie sdp;

	public enum Method {
		ATR, ADP, SDP
	}

	/**
	 * TODO create a DTS with low and close
	 */
	public DailyTrailingStop() {
	}

	public DailyTrailingStop(Method method, int period, double breath) {
		this.breath = breath;
		this.period = period;
		this.method = method;
	}

	@Override
	public void setUp(HistoricalData hd) {
		switch (method) {
		case ATR:
			atr = Calc.atr(hd, period);
			hd.syncReferenceIndex(atr);
			break;
		case ADP:
			adp = Calc.atrPercent(hd, period);
			hd.syncReferenceIndex(adp);
			break;
		case SDP:
			sdp = Calc.standardDeviationPercent(hd, period);
			hd.syncReferenceIndex(sdp);
			break;
		}
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup())) {
			double currentStop = p.getStop();

			double newStop;
			if (p instanceof LongPosition) {
				newStop = manageLongs(hd, bor, currentStop);
			} else {
				newStop = manageShorts(hd, bor, currentStop);
			}
			p.setStop(newStop);
		}
	}

	private double manageLongs(HistoricalData hd, BookOfRecord bor, double currentStop) {
		double proposedStop = 0;

		switch (method) {
		case ATR:
			proposedStop = hd.bidHigh.get(0) - atr.get(0) * breath;
			break;
		case ADP:
			proposedStop = hd.bidHigh.get(0) * (1 - adp.get(0) * breath);
			break;
		case SDP:
			proposedStop = hd.bidHigh.get(0) * (1 - sdp.get(0) * breath);
			break;
		}

		return Math.max(currentStop, proposedStop);
	}

	private double manageShorts(HistoricalData hd, BookOfRecord bor, double currentStop) {
		double proposedStop = 0;

		switch (method) {
		case ATR:
			proposedStop = hd.askLow.get(0) + atr.get(0) * breath;
			break;
		case ADP:
			proposedStop = hd.askLow.get(0) * (1 + adp.get(0) * breath);
			break;
		case SDP:
			proposedStop = hd.askLow.get(0) * (1 + sdp.get(0) * breath);
			break;
		}

		return Math.min(currentStop, proposedStop);
	}

	@Override
	public String toString() {
		return "DTS/" + method + period + breath;
	}

	@Override
	public ExitStrategy clone() {
		DailyTrailingStop clone = new DailyTrailingStop(method, period, breath);
		return clone;
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
		// ATR
//		set.add(new DailyTrailingStop(Method.ATR, 20, 0));
		set.add(new DailyTrailingStop(Method.ATR, 20, 0.5));
		set.add(new DailyTrailingStop(Method.ATR, 20, 1));
		set.add(new DailyTrailingStop(Method.ATR, 20, 1.5));
		set.add(new DailyTrailingStop(Method.ATR, 20, 2));
		set.add(new DailyTrailingStop(Method.ATR, 20, 2.5));
		set.add(new DailyTrailingStop(Method.ATR, 20, 3));
		set.add(new DailyTrailingStop(Method.ATR, 20, 3.5));
		set.add(new DailyTrailingStop(Method.ATR, 20, 4));

//		set.add(new DailyTrailingStop(Method.ATR, 70, 0));
		set.add(new DailyTrailingStop(Method.ATR, 70, 0.5));
		set.add(new DailyTrailingStop(Method.ATR, 70, 1));
		set.add(new DailyTrailingStop(Method.ATR, 70, 1.5));
		set.add(new DailyTrailingStop(Method.ATR, 70, 2));
		set.add(new DailyTrailingStop(Method.ATR, 70, 2.5));
		set.add(new DailyTrailingStop(Method.ATR, 70, 3));
		set.add(new DailyTrailingStop(Method.ATR, 70, 3.5));
		set.add(new DailyTrailingStop(Method.ATR, 70, 4));

//		set.add(new DailyTrailingStop(Method.ATR, 200, 0));
		set.add(new DailyTrailingStop(Method.ATR, 200, 0.5));
		set.add(new DailyTrailingStop(Method.ATR, 200, 1));
		set.add(new DailyTrailingStop(Method.ATR, 200, 1.5));
		set.add(new DailyTrailingStop(Method.ATR, 200, 2));
		set.add(new DailyTrailingStop(Method.ATR, 200, 2.5));
		set.add(new DailyTrailingStop(Method.ATR, 200, 3));
		set.add(new DailyTrailingStop(Method.ATR, 200, 3.5));
		set.add(new DailyTrailingStop(Method.ATR, 200, 4));
		
		// ADP
//		set.add(new DailyTrailingStop(Method.ADP, 20, 0));
		set.add(new DailyTrailingStop(Method.ADP, 20, 0.5));
		set.add(new DailyTrailingStop(Method.ADP, 20, 1));
		set.add(new DailyTrailingStop(Method.ADP, 20, 1.5));
		set.add(new DailyTrailingStop(Method.ADP, 20, 2));
		set.add(new DailyTrailingStop(Method.ADP, 20, 2.5));
		set.add(new DailyTrailingStop(Method.ADP, 20, 3));
		set.add(new DailyTrailingStop(Method.ADP, 20, 3.5));
		set.add(new DailyTrailingStop(Method.ADP, 20, 4));

//		set.add(new DailyTrailingStop(Method.ADP, 70, 0));
		set.add(new DailyTrailingStop(Method.ADP, 70, 0.5));
		set.add(new DailyTrailingStop(Method.ADP, 70, 1));
		set.add(new DailyTrailingStop(Method.ADP, 70, 1.5));
		set.add(new DailyTrailingStop(Method.ADP, 70, 2));
		set.add(new DailyTrailingStop(Method.ADP, 70, 2.5));
		set.add(new DailyTrailingStop(Method.ADP, 70, 3));
		set.add(new DailyTrailingStop(Method.ADP, 70, 3.5));
		set.add(new DailyTrailingStop(Method.ADP, 70, 4));

//		set.add(new DailyTrailingStop(Method.ADP, 200, 0));
		set.add(new DailyTrailingStop(Method.ADP, 200, 0.5));
		set.add(new DailyTrailingStop(Method.ADP, 200, 1));
		set.add(new DailyTrailingStop(Method.ADP, 200, 1.5));
		set.add(new DailyTrailingStop(Method.ADP, 200, 2));
		set.add(new DailyTrailingStop(Method.ADP, 200, 2.5));
		set.add(new DailyTrailingStop(Method.ADP, 200, 3));
		set.add(new DailyTrailingStop(Method.ADP, 200, 3.5));
		set.add(new DailyTrailingStop(Method.ADP, 200, 4));
		
		// SDP
//		set.add(new DailyTrailingStop(Method.SDP, 20, 0));
		set.add(new DailyTrailingStop(Method.SDP, 20, 0.5));
		set.add(new DailyTrailingStop(Method.SDP, 20, 1));
		set.add(new DailyTrailingStop(Method.SDP, 20, 1.5));
		set.add(new DailyTrailingStop(Method.SDP, 20, 2));
		set.add(new DailyTrailingStop(Method.SDP, 20, 2.5));
		set.add(new DailyTrailingStop(Method.SDP, 20, 3));
		set.add(new DailyTrailingStop(Method.SDP, 20, 3.5));
		set.add(new DailyTrailingStop(Method.SDP, 20, 4));

//		set.add(new DailyTrailingStop(Method.SDP, 70, 0));
		set.add(new DailyTrailingStop(Method.SDP, 70, 0.5));
		set.add(new DailyTrailingStop(Method.SDP, 70, 1));
		set.add(new DailyTrailingStop(Method.SDP, 70, 1.5));
		set.add(new DailyTrailingStop(Method.SDP, 70, 2));
		set.add(new DailyTrailingStop(Method.SDP, 70, 2.5));
		set.add(new DailyTrailingStop(Method.SDP, 70, 3));
		set.add(new DailyTrailingStop(Method.SDP, 70, 3.5));
		set.add(new DailyTrailingStop(Method.SDP, 70, 4));

//		set.add(new DailyTrailingStop(Method.SDP, 200, 0));
		set.add(new DailyTrailingStop(Method.SDP, 200, 0.5));
		set.add(new DailyTrailingStop(Method.SDP, 200, 1));
		set.add(new DailyTrailingStop(Method.SDP, 200, 1.5));
		set.add(new DailyTrailingStop(Method.SDP, 200, 2));
		set.add(new DailyTrailingStop(Method.SDP, 200, 2.5));
		set.add(new DailyTrailingStop(Method.SDP, 200, 3));
		set.add(new DailyTrailingStop(Method.SDP, 200, 3.5));
		set.add(new DailyTrailingStop(Method.SDP, 200, 4));
		
		return set;
	}

}
