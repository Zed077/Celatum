package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMAsDistanceStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.Order;
import com.celatum.trading.Position;
import com.celatum.trading.StopLongOrder;
import com.celatum.trading.StopShortOrder;

public class BreakoutShortShell extends Algo {
	private Serie atr;
	private LinearRegression lrLong;
	private LinearRegression lrShort;
	private double minPercent;
	private double atrBreath = 3;
	private Serie previousH = null;
	private Serie previousL = null;
	private Serie ema50;

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ZigZag
		int period = 1000;
		double atrRange = Calc.atr(hd, period).get(0);
		minPercent = atrBreath * atrRange / (hd.midClose.get(0) + hd.midClose.get(period)) * 2;

		// EMEA 50
		ema50 = Calc.ema(hd.midClose, 50);
		hd.syncReferenceIndex(ema50);

//		addAlgoComponent(new NoPositionOpen());
//		addAlgoComponent(new DailyTrailingStop());
		
		// BreakoutShortShell-HH/ADP2004.0-NVMO/ADP3--EDS/702.01.5 144 -9,891 205,849 4.01%
		addAlgoComponent(new HigherHighs(HigherHighs.Method.ADP, 200, 4));
		addAlgoComponent(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 3));
		addAlgoComponent(new EMAsDistanceStop(70, 2, 1.5));
		
		// BreakoutShortShell-HH/ADP2001.5-NVMO/ATR5--DTS/ATR702.5--EMAT/70--SFM/2003.02.0 131 -3,166 152,330 3.17%
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		updateOrdersShort(hd, bor, getGroup());
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		shortBreakout(hd, bor);
	}

	private void shortBreakout(HistoricalData hd, BookOfRecord bor) {
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 3 lows
		Serie selectL = new Serie();
		if (zz.getLows().size() < 3)
			return;

		for (int i = 0; i < 3; i++) {
			selectL.put(zz.getLows().getItem(i));
		}

		// Exit if we have already identified this line
		if (selectL.deepEquals(previousL))
			return;

		// Ensure today is not new low
//		if (selectL.getDate(0).compareTo(hd.getReferenceDate()) >= 0)
//			return;

		// Ensure we are in an downtrend
		if (selectL.get(0) < selectL.get(1) || selectL.get(1) < selectL.get(2))
			return;
		
		// Distance between low and high
		double distance = zz.getHighs().get(0) - zz.getLows().get(0);

		// Regress lows
		lrShort = new LinearRegression(selectL, 3);

		// Compute breakout point
		double breakout = lrShort.predict(hd.getReferenceDate());

		// Place order
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		this.plot(selectL, getGroup());
		StopShortOrder order = new StopShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				breakout - atr.get(0) * 0.5);
		order.setStop(breakout + atr.get(0) * 3);
		order.setLimit(breakout - atr.get(0) * 3); 
		previousL = selectL;
		bor.addOrder(order);
	}

	private void updateOrdersShort(HistoricalData hd, BookOfRecord bor, String group) {
		if (lrShort == null)
			return;
		double breakout = lrShort.predict(hd.getReferenceDate());
		for (Order o : bor.getActiveOrders(hd.instrument, hd.getReferenceDate(), group)) {
			o.setPriceLevel(breakout - atr.get(0) * 0.1);
			o.setStop(breakout + atr.get(0) * 2);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new BreakoutShortShell();
	}
}
