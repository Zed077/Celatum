package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class HLHHShell2023 extends Algo {
	private Serie atr;
	private double minPercent;
	private double devBreath = 3;
	
	public HLHHShell2023() {
		// HLHHShell2023-HH/SDP2004.5-RT/20-0.3--TE/20--RS/703.5--DTS/ADP704.0--SFM/706.01.0 1047 -39,469 35,507,369 24.89%
		addAlgoComponent(new TimedExit(20));
		addAlgoComponent(new HigherHighs(Method.SDP, 200, 4.5));
		addAlgoComponent(new RegressionTrend(20, -0.3));
		addAlgoComponent(new RegressedStop(70, 3.5));
		addAlgoComponent(new DailyTrailingStop(com.celatum.algos.exit.DailyTrailingStop.Method.ADP, 70, 4.0));
		addAlgoComponent(new SignificantFavorableMove(70, 6, 1));
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR 20
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ZigZag
		int period = 1000;
//		double atrRange = Calc.atr(hd, period).get(0);
//		minPercent = devBreath * atrRange / (hd.midClose.get(0) + hd.midClose.get(period)) * 2;
		
		double adp = Calc.atrPercent(hd, period).get(0) * devBreath;
		
//		double sdp = Calc.standardDeviationPercent(hd, period).get(0) * devBreath;
		minPercent = adp;
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
		return new HLHHShell2023();
	}
}
