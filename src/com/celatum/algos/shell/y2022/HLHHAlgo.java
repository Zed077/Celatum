package com.celatum.algos.shell.y2022;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class HLHHAlgo extends Algo {
	private Serie atr;
	private double minPercent;
	private double devBreath = 3;

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR 20
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ZigZag
		int period = Math.min(1000, hd.fullSize());
//		double atrRange = Calc.atr(hd, period).get(0);
//		minPercent = devBreath * atrRange / (hd.midClose.get(0) + hd.midClose.get(period)) * 2;
		
		double adp = Calc.atrPercent(hd, period).get(0) * devBreath;
		
//		double sdp = Calc.standardDeviationPercent(hd, period).get(0) * devBreath;
		minPercent = adp;
		
		// HLHHShell-!HH/ADP2001.0-HH/SDP2002.5--DTS/ADP703.0--SFM/705.02.0 649 -18,930 33,003,704 37.81%
		addAlgoComponent(new ReverseCondition(new HigherHighs(HigherHighs.Method.ADP, 200, 1)));
		addAlgoComponent(new HigherHighs(HigherHighs.Method.SDP, 200, 2.5));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP, 70, 3));
		addAlgoComponent(new SignificantFavorableMove(70, 5, 2));
		
		// HLHHShell-!HH/ADP2001.0-HH/SDP2002.5--DTS/ADP703.0 649 -18,930 28,050,203 36.35%
//		addAlgoComponent(new ReverseCondition(new HigherHighs(HigherHighs.Method.ADP, 200, 1)));
//		addAlgoComponent(new HigherHighs(HigherHighs.Method.SDP, 200, 2.5));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP, 70, 3));
		
		// Manual
//		addAlgoComponent(new HigherHighs(HigherHighs.Method.SDP, 70, 2.5));
//		addAlgoComponent(new NoViolentMoveDown(3, NoViolentMoveDown.Method.SDP));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP, 70, 3));
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 2 high
		if (zz.getHighs().size() < 2)
			return;

		// Make sure we are after HH
		if (zz.getAll().get(0) <= zz.getAll().get(1))
			return;

		SerieItem h0 = zz.getHighs().getItem(0);
		SerieItem h1 = zz.getHighs().getItem(1);
		SerieItem l = zz.getLows().getItem(0);

		// Regress highs, offset to low
		double entry = Calc.twoPointsRegression(h0, h1, l, hd.getReferenceDate());
		
		if (hd.midClose.get(0) < entry-atr.get(0)) 
			return;

		// Place order
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				entry + atr.get(0) * 0.1);
		order.setStop(entry - atr.get(0) * 2);
		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new HLHHAlgo();
	}
}
