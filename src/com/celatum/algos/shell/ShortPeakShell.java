package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.GreenDays;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.RSI;
import com.celatum.trading.ShortOrder;

public class ShortPeakShell extends Algo {
	private Serie atr;
	private Serie rsi;

	public ShortPeakShell() {
		addAlgoComponent(new GreenDays(5, true));
		// ShortPeakShell-GD/5true-!HH/ADP2005.0-NVMD/ADP10--EMAT/70--RL--DTS/SDP204.0 379 -4,011 58,343 0.94%
	}

	@Override
	public Algo getInstance() {
		return new ShortPeakShell();
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
		double entry = hd.midHigh.get(0);
		ShortOrder o = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		o.setStop(entry + 3 * atr.get(0));
		o.setLimit(entry - 3 * atr.get(0));
		bor.addOrder(o);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
