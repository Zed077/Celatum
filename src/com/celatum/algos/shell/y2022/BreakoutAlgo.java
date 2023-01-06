package com.celatum.algos.shell.y2022;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.Order;
import com.celatum.trading.Position;
import com.celatum.trading.StopLongOrder;

public class BreakoutAlgo extends Algo {
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
		minPercent = atrBreath * atrRange / (hd.midClose.get(0)+hd.midClose.get(period)) * 2;
//		System.out.println("minPercent " + minPercent);

		// EMEA 50
		ema50 = Calc.ema(hd.midClose, 50);
		hd.syncReferenceIndex(ema50);
		this.plot(ema50, "EMA50", Color.ORANGE);

		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);
		this.plot(zz);
		
		addAlgoComponent(new DailyTrailingStop());
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
		longBreakout(hd, bor);
//		shortBreakout(hd, bor);
	}

	private void longBreakout(HistoricalData hd, BookOfRecord bor) {
		updateOrdersLong(hd, bor, getGroup());

		// Ensure there is no position already open
		if (bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() > 0)
			return;

		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 3 high
		Serie selectH = new Serie();
		if (zz.getHighs().size() < 3)
			return;

		for (int i = 0; i < 3; i++) {
			selectH.put(zz.getHighs().getItem(i));
		}

		// Exit if we have already identified this line
		if (selectH.deepEquals(previousH))
			return;

		// Ensure today is not new high
		if (selectH.getDate(0).compareTo(hd.getReferenceDate()) >= 0)
			return;

		// Ensure we are in a downtrend
		if (selectH.get(0) > selectH.get(1) || selectH.get(1) > selectH.get(2))
			return;

		// Regress highs
		lrLong = new LinearRegression(selectH, 3);

		// Compute breakout point
		double breakout = lrLong.predict(hd.getReferenceDate());

		// Place order
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		this.plot(selectH, getGroup());
		StopLongOrder order = new StopLongOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				breakout + atr.get(0) * 0.1);
		order.setStop(breakout - atr.get(0) * 2);
		previousH = selectH;
		bor.addOrder(order);
	}

	private void updateOrdersLong(HistoricalData hd, BookOfRecord bor, String group) {
		if (lrLong == null)
			return;
		double breakout = lrLong.predict(hd.getReferenceDate());
		for (Order o : bor.getActiveOrders(hd.instrument, hd.getReferenceDate(), group)) {
			o.setPriceLevel(breakout+ atr.get(0) * 0.1);
//			o.setTrailingStop(breakout, atr.get(0) * 2);
		}
	}
	
	private void shortBreakout(HistoricalData hd, BookOfRecord bor) {
		updateOrdersShort(hd, bor, getGroup());
		managePositionsShort(hd, bor, getGroup());

		// Ensure there is no position already open
		if (bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() > 0)
			return;

		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 3 high
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
		if (selectL.getDate(0).compareTo(hd.getReferenceDate()) >= 0)
			return;

		// Ensure we are in an uptrend
//		if (selectL.get(0) < selectL.get(1) || selectL.get(1) < selectL.get(2))
//			return;
		

		// Regress lows
		lrShort = new LinearRegression(selectL, 3);

		// Compute breakout point
		double breakout = lrShort.predict(hd.getReferenceDate());

		// Place order
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		this.plot(selectL, getGroup());
		StopLongOrder order = new StopLongOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				breakout - atr.get(0) * 0.1);
		order.setStop(breakout - atr.get(0) * 3);
		previousL = selectL;
		bor.addOrder(order);
	}

	private void updateOrdersShort(HistoricalData hd, BookOfRecord bor, String group) {
		if (lrShort == null)
			return;
		double breakout = lrShort.predict(hd.getReferenceDate());
		for (Order o : bor.getActiveOrders(hd.instrument, hd.getReferenceDate(), group)) {
			o.setPriceLevel(breakout);
		}
	}

	private void managePositionsShort(HistoricalData hd, BookOfRecord bor, String group) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), group)) {
			double currentStop = p.getStop();
			double proposedStop = hd.bidHigh.get(0) - atr.get(0) * 3;
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	protected int minPeriods() {
		return 70;
	}

	@Override
	public Algo getInstance() {
		return new BreakoutAlgo();
	}
}
