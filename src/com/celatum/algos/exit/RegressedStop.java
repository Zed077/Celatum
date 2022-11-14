package com.celatum.algos.exit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class RegressedStop extends ExitStrategy {
	private double breath = 2.5;
	private int period = 20;
	private Serie atr;

	public RegressedStop() {
	}

	public RegressedStop(int period, double breath) {
		this.breath = breath;
		this.period = period;
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		List<Position> positions = bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup());
		ArrayList<Position> longs = new ArrayList<Position>();
		ArrayList<Position> shorts = new ArrayList<Position>();
		
		for (Position p : positions) {
			if (p instanceof LongPosition) {
				longs.add(p);
			} else {
				shorts.add(p);
			}
		}
		
		manageLongs(hd, bor, longs);
		manageShorts(hd, bor, shorts);
	}

	private void manageLongs(HistoricalData hd, BookOfRecord bor, List<Position> positions) {
		if (positions.size() == 0)
			return;

		// Find oldest date
		Date dd = new Date();
		for (Position p : positions) {
			if (p.getEntryDate().before(dd)) {
				dd = p.getEntryDate();
			}
		}

		// Regress lows from day after entry
		Serie lows = new Serie();
		for (int i = 0; i < hd.midLow.size(); i++) {
			if (hd.midLow.getDate(i).compareTo(dd) <= 0)
				break;
			lows.put(hd.midLow.getItem(i));
		}

		double proposedStop;
//				if (lows.size() > 3 && lr.getAbsoluteYearlyRateOfChange() > 0) {
//					LinearRegression lr = new LinearRegression(lows, lows.size());
//					// Compute new stop
//					if (lr.getAbsoluteYearlyRateOfChange() > 0) {
//						proposedStop = lr.predict(hd.getReferenceDate());
//					} else {
//						proposedStop = hd.bidHigh.get(0) - atr.get(0) * 3;
//					}
//				} else {
		proposedStop = hd.bidHigh.get(0) - atr.get(0) * breath;
//				}

		for (Position p : positions) {
			p.setStop(proposedStop);
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, List<Position> positions) {
		if (positions.size() == 0)
			return;

		// Find oldest date
		Date dd = new Date();
		for (Position p : positions) {
			if (p.getEntryDate().before(dd)) {
				dd = p.getEntryDate();
			}
		}

		// Regress highs from day after entry
		Serie highs = new Serie();
		for (int i = 0; i < hd.midHigh.size(); i++) {
			if (hd.midHigh.getDate(i).compareTo(dd) <= 0)
				break;
			highs.put(hd.midHigh.getItem(i));
		}

		double proposedStop;
//				if (lows.size() > 3 && lr.getAbsoluteYearlyRateOfChange() > 0) {
//					LinearRegression lr = new LinearRegression(lows, lows.size());
//					// Compute new stop
//					if (lr.getAbsoluteYearlyRateOfChange() > 0) {
//						proposedStop = lr.predict(hd.getReferenceDate());
//					} else {
//						proposedStop = hd.bidHigh.get(0) - atr.get(0) * 3;
//					}
//				} else {
		proposedStop = hd.bidHigh.get(0) - atr.get(0) * breath;
//				}

		for (Position p : positions) {
			p.setStop(proposedStop);
		}
	}

	@Override
	public String toString() {
		return "RS/" + period + breath;
	}

	@Override
	public ExitStrategy clone() {
		RegressedStop clone = new RegressedStop(period, breath);
		return clone;
	}

	@Override
	public void setUp(HistoricalData hd) {
		atr = Calc.atr(hd, period);
		hd.syncReferenceIndex(atr);
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
//		// ATR
//		set.add(new RegressedStop(Method.ATR, 20, 0));
//		set.add(new RegressedStop(Method.ATR, 20, 0.5));
//		set.add(new RegressedStop(Method.ATR, 20, 1));
//		set.add(new RegressedStop(Method.ATR, 20, 1.5));
//		set.add(new RegressedStop(Method.ATR, 20, 2));
//		set.add(new RegressedStop(Method.ATR, 20, 2.5));
//		set.add(new RegressedStop(Method.ATR, 20, 3));
//		set.add(new RegressedStop(Method.ATR, 20, 3.5));
//		set.add(new RegressedStop(Method.ATR, 20, 4));
//
//		set.add(new RegressedStop(Method.ATR, 70, 0));
//		set.add(new RegressedStop(Method.ATR, 70, 0.5));
//		set.add(new RegressedStop(Method.ATR, 70, 1));
//		set.add(new RegressedStop(Method.ATR, 70, 1.5));
//		set.add(new RegressedStop(Method.ATR, 70, 2));
//		set.add(new RegressedStop(Method.ATR, 70, 2.5));
//		set.add(new RegressedStop(Method.ATR, 70, 3));
//		set.add(new RegressedStop(Method.ATR, 70, 3.5));
//		set.add(new RegressedStop(Method.ATR, 70, 4));
//
//		set.add(new RegressedStop(Method.ATR, 200, 0));
//		set.add(new RegressedStop(Method.ATR, 200, 0.5));
//		set.add(new RegressedStop(Method.ATR, 200, 1));
//		set.add(new RegressedStop(Method.ATR, 200, 1.5));
//		set.add(new RegressedStop(Method.ATR, 200, 2));
//		set.add(new RegressedStop(Method.ATR, 200, 2.5));
//		set.add(new RegressedStop(Method.ATR, 200, 3));
//		set.add(new RegressedStop(Method.ATR, 200, 3.5));
//		set.add(new RegressedStop(Method.ATR, 200, 4));
//
//		// ADP
//		set.add(new RegressedStop(Method.ADP, 20, 0));
//		set.add(new RegressedStop(Method.ADP, 20, 0.5));
//		set.add(new RegressedStop(Method.ADP, 20, 1));
//		set.add(new RegressedStop(Method.ADP, 20, 1.5));
//		set.add(new RegressedStop(Method.ADP, 20, 2));
//		set.add(new RegressedStop(Method.ADP, 20, 2.5));
//		set.add(new RegressedStop(Method.ADP, 20, 3));
//		set.add(new RegressedStop(Method.ADP, 20, 3.5));
//		set.add(new RegressedStop(Method.ADP, 20, 4));
//
//		set.add(new RegressedStop(Method.ADP, 70, 0));
//		set.add(new RegressedStop(Method.ADP, 70, 0.5));
//		set.add(new RegressedStop(Method.ADP, 70, 1));
//		set.add(new RegressedStop(Method.ADP, 70, 1.5));
//		set.add(new RegressedStop(Method.ADP, 70, 2));
//		set.add(new RegressedStop(Method.ADP, 70, 2.5));
//		set.add(new RegressedStop(Method.ADP, 70, 3));
//		set.add(new RegressedStop(Method.ADP, 70, 3.5));
//		set.add(new RegressedStop(Method.ADP, 70, 4));
//
//		set.add(new RegressedStop(Method.ADP, 200, 0));
//		set.add(new RegressedStop(Method.ADP, 200, 0.5));
//		set.add(new RegressedStop(Method.ADP, 200, 1));
//		set.add(new RegressedStop(Method.ADP, 200, 1.5));
//		set.add(new RegressedStop(Method.ADP, 200, 2));
//		set.add(new RegressedStop(Method.ADP, 200, 2.5));
//		set.add(new RegressedStop(Method.ADP, 200, 3));
//		set.add(new RegressedStop(Method.ADP, 200, 3.5));
//		set.add(new RegressedStop(Method.ADP, 200, 4));
//
//		// SDP
//		set.add(new RegressedStop(Method.SDP, 20, 0));
//		set.add(new RegressedStop(Method.SDP, 20, 0.5));
//		set.add(new RegressedStop(Method.SDP, 20, 1));
//		set.add(new RegressedStop(Method.SDP, 20, 1.5));
//		set.add(new RegressedStop(Method.SDP, 20, 2));
//		set.add(new RegressedStop(Method.SDP, 20, 2.5));
//		set.add(new RegressedStop(Method.SDP, 20, 3));
//		set.add(new RegressedStop(Method.SDP, 20, 3.5));
//		set.add(new RegressedStop(Method.SDP, 20, 4));
//
//		set.add(new RegressedStop(Method.SDP, 70, 0));
//		set.add(new RegressedStop(Method.SDP, 70, 0.5));
//		set.add(new RegressedStop(Method.SDP, 70, 1));
//		set.add(new RegressedStop(Method.SDP, 70, 1.5));
//		set.add(new RegressedStop(Method.SDP, 70, 2));
//		set.add(new RegressedStop(Method.SDP, 70, 2.5));
//		set.add(new RegressedStop(Method.SDP, 70, 3));
//		set.add(new RegressedStop(Method.SDP, 70, 3.5));
//		set.add(new RegressedStop(Method.SDP, 70, 4));
//
//		set.add(new RegressedStop(Method.SDP, 200, 0));
//		set.add(new RegressedStop(Method.SDP, 200, 0.5));
//		set.add(new RegressedStop(Method.SDP, 200, 1));
//		set.add(new RegressedStop(Method.SDP, 200, 1.5));
//		set.add(new RegressedStop(Method.SDP, 200, 2));
//		set.add(new RegressedStop(Method.SDP, 200, 2.5));
//		set.add(new RegressedStop(Method.SDP, 200, 3));
//		set.add(new RegressedStop(Method.SDP, 200, 3.5));
//		set.add(new RegressedStop(Method.SDP, 200, 4));
//
		return set;
	}

}
