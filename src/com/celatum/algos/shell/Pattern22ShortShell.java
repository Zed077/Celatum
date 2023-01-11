package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.EMADistance;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.SMADistance;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.DailyTrailingStop.Method;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.ShortOrder;

public class Pattern22ShortShell extends Algo {
	private Serie atr;
	private double atrMultiple = 2;

	public Pattern22ShortShell() {
		// Pattern22ShortShell-EMAD/51.5-OBB/202.5true--TSEMA/SDP2002.0--RL 471 -3,739 143,861 2.06%
		// Pattern22ShortShell-EMAD/51.5-HH/ADP2005.5-NPO--DTS/ADP703.0--RL--SFM/2005.00.5 302 -6,344 9,674 0.17%


	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);
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
		// Place order
		double entry = hd.midLow.get(0) - atrMultiple * atr.get(0);
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry + atr.get(0) * atrMultiple);
		order.setLimit(entry - atr.get(0) * atrMultiple);
		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new Pattern22ShortShell();
	}
}
