package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.SMADistance;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.RSI;
import com.celatum.trading.LongOrder;

public class LongMeanReversionShell extends Algo {
	private Serie atr;
	private Serie rsi;

	public LongMeanReversionShell() {
		addAlgoComponent(new SMADistance(200, -7, false));
		addAlgoComponent(new TimedExit(20));
		// LongMeanReversionShell-SMAD/200-7.0false-HH/SDP2004.5-OBB/201.0false--TE/20--RS/203.5--NGMW/50.1--SFM/2003.01.0 456 -72,633 4,480,909 14.25%

	}

	@Override
	public Algo getInstance() {
		return new LongMeanReversionShell();
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
