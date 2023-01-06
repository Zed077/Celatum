package com.celatum.algos;

import java.util.Date;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.NotGoneMyWay;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class BreakoutLongAlgo extends Algo {
	private Serie atr;
	private double atrBreath = 2;
	
	public BreakoutLongAlgo() {
		addAlgoComponent(new NoPositionOpen());
		
		// BreakoutLongShell-NPO-!HH/SDP2002.5-HH/SDP2004.0--SFM/705.02.0 312 -17,632 2,653,155 11.56%
		addAlgoComponent(new ReverseCondition(new HigherHighs(Method.SDP, 200, 2.5)));
		addAlgoComponent(new HigherHighs(Method.SDP, 200, 4));
		addAlgoComponent(new SignificantFavorableMove(70, 5, 2));
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = ATRPercent.calc(hd, 20, ATR.Method.SMA);
		hd.syncReferenceIndex(atr);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double minPercent = atr.get(0) * atrBreath;
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 2 highs
		SerieItem h1;
		SerieItem h2;
		if (zz.getHighs().size() < 2)
			return;
		h1 = zz.getHighs().getItem(1);
		h2 = zz.getHighs().getItem(0);
		
		// Ensure we are in a uptrend
//		if (h2.getValue() < h1.getValue())
//			return;
		
		// Confirm breakout
		Date lastHigh = h2.getDate();
		boolean hasBrokenOut = false;
		double breakout = 0;
		for (int i = 0; hd.midClose.getDate(i).after(lastHigh); i++) {
			breakout = Calc.twoPointsRegression(h1, h2, hd.midClose.getDate(i));
			if (hd.midClose.get(i) > breakout) {
				hasBrokenOut = true;
			}
		}
		
		if (hasBrokenOut) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
			double distance = h2.getValue() - zz.getLows().get(0);
			double entry = breakout - distance / 2.0;
			
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
			order.setLimit(breakout + distance *0.9);
			order.setStopCorrect(zz.getLows().get(0));
			bor.addOrder(order);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new BreakoutLongAlgo();
	}
}
