package com.celatum.algos.shell;

import java.util.Date;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.RSI;
import com.celatum.trading.ShortOrder;

public class ShortGreenDay extends Algo {
	private Serie atr;
	private Serie rsi;

	public ShortGreenDay() {
		addAlgoComponent(new RegressionTrend(7, 0.5));
		addAlgoComponent(new TimedExit(5));
	}

	@Override
	public Algo getInstance() {
		return new ShortGreenDay();
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
		if (rsi.get(0) < 60) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		}
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		// RSI is or has been above 70 then has declined for 2 candles
		boolean above70 = rsi.get(2) > 70;
		boolean declined = rsi.get(2) > rsi.get(1) && rsi.get(1) > rsi.get(0);

		Date day3 = rsi.getDate(2);
		double highest = hd.midHigh.getMaxSince(day3, 0).getValue();
		double lowest = hd.midLow.getMinSince(day3, 0).getValue();

		if (above70 && declined) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());

			ShortOrder o = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), (highest + lowest) / 2.0);
			double stop = highest + 1 * atr.get(0);
			o.setStop(stop);
			bor.addOrder(o);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
