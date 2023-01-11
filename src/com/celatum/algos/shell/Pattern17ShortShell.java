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

public class Pattern17ShortShell extends Algo {
	private Serie atr;

	public Pattern17ShortShell() {
		// Pattern17ShortShell-!EMAD/20-2.0-OBB/201.0false-RSIF/60.0false--RTS/500.2--NFTL/3--EDS/702.02.5--NGMW/50.5 304 -431 59,189 0.96%
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
		if (hd.midLow.get(3) > hd.midHigh.get(0) && hd.midHigh.get(0) > hd.midLow.get(1)
				&& hd.midLow.get(0) > hd.midLow.get(2) && hd.midLow.get(1) > hd.midLow.get(2)) {
			// Place order
			double entry = hd.midLow.get(0);
			ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
			order.setStop(entry + atr.get(0) * 3);
			order.setLimit(entry - atr.get(0) * 3);
			bor.addOrder(order);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new Pattern17ShortShell();
	}
}
