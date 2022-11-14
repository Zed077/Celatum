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

public class BasicShortShell extends Algo {
	private Serie atr;
	
	public BasicShortShell() {
		addAlgoComponent(new NoPositionOpen());
		
		// BasicShortShell-NPO-EMAD/5-1.5-NVMD/SDP3-SMAD/200-2.0--NGMW/200.1--SFM/2004.00.5 80 -13,483 379,332 6.22%
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
		double entry = hd.midClose.get(0);
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry + atr.get(0) * 3);
		order.setLimit(entry - atr.get(0) * 3);
		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new BasicShortShell();
	}
}
