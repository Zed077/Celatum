package com.celatum.algos.shell.y2022;

import java.awt.Color;
import java.util.Date;
import java.util.List;

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

public class BreakoutAlgoExit extends Algo {
	private Serie atr;
	private LinearRegression lr;
	private double minPercent;
	private double atrBreath = 3;
	private Serie ema50;

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ZigZag
		double atrRange = Calc.atr(hd, 100).get(0);
		minPercent = atrBreath * atrRange / hd.midClose.get(0);

		// EMEA 50
		ema50 = Calc.ema(hd.midClose, 50);
		hd.syncReferenceIndex(ema50);
		this.plot(ema50, "EMA50", Color.ORANGE);

//		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);
//		this.plot(zz);
		
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
		updateOrders(hd, bor);

		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 3 high
		Serie selectH = new Serie();
		if (zz.getHighs().size() < 3)
			return;

		for (int i = 0; i < 3; i++) {
			selectH.put(zz.getHighs().getItem(i));
		}

		// Ensure today is not new high
		if (selectH.getDate(0).compareTo(hd.getReferenceDate()) >= 0)
			return;

		// Ensure we are in a downtrend
		if (selectH.get(0) > selectH.get(1) || selectH.get(1) > selectH.get(2))
			return;

		// Regress highs
		lr = new LinearRegression(selectH, 3);

		// Compute breakout point
		double breakout = lr.predict(hd.getReferenceDate());

		// Ensure we come from below the breakout point
		if (hd.midClose.get(0) >= breakout)
			return;

		// Place order
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		this.plot(selectH, getGroup());
		StopLongOrder order = new StopLongOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				breakout + atr.get(0) * 0.1);
//		order.setTrailingStop(breakout, atr.get(0) * 2);
		order.setStop(breakout - atr.get(0) * 2);
		bor.addOrder(order);
	}

	private void updateOrders(HistoricalData hd, BookOfRecord bor) {
		if (lr == null)
			return;
		double breakout = lr.predict(hd.getReferenceDate());
		for (Order o : bor.getActiveOrders(hd.instrument, hd.getReferenceDate(), getGroup())) {
			o.setPriceLevel(breakout);
//			o.setTrailingStop(breakout, atr.get(0) * 2);
		}
	}

	@Override
	protected int minPeriods() {
		return 70;
	}

	@Override
	public Algo getInstance() {
		return new BreakoutAlgoExit();
	}
}
