package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.GreenDays;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.RSI;
import com.celatum.trading.LongOrder;
import com.celatum.trading.ShortOrder;

public class LongPeakShell extends Algo {
	private Serie atr;
	private Serie rsi;

	public LongPeakShell() {
		addAlgoComponent(new GreenDays(5, false));
		addAlgoComponent(new TimedExit(20));
		// LongPeakShell-GD/5false-!HH/ADP202.0--TE/20--RS/204.0 328 -827 191,398 2.58%
	}

	@Override
	public Algo getInstance() {
		return new LongPeakShell();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		rsi = RSI.calc(hd.midClose);
		hd.syncReferenceIndex(rsi);

		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double entry = hd.midLow.get(0);
		LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		o.setStop(entry - 3 * atr.get(0));
		o.setLimit(entry + 3 * atr.get(0));
		bor.addOrder(o);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
