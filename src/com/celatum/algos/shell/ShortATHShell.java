package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.HighLow;
import com.celatum.maths.Calc;
import com.celatum.trading.LongOrder;
import com.celatum.trading.ShortOrder;

/**
 * Does not work
 * @author cedric.ladde
 *
 */
public class ShortATHShell extends Algo {
	private Serie atrPercent;
	private Serie ath;
	private double distanceToATH = 3;
	private int period = 6*20;

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		atrPercent = Calc.atrPercent(hd, 20);
		hd.syncReferenceIndex(atrPercent);

		ath = HighLow.allTimeHigh(hd.midHigh);
		hd.syncReferenceIndex(ath);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	/**
	 * Short on ATH
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		sellOnATH(hd, bor);
	}

	private void buyOnATH(HistoricalData hd, BookOfRecord bor) {
		if (hd.midHigh.get(0) > ath.get(0) * (1 - distanceToATH * atrPercent.get(0))) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), ath.get(0));
			order.setStop(ath.get(0) * (1 - atrPercent.get(0) * 3));
			order.setLimit(ath.get(0) * (1 + atrPercent.get(0) * 3));
			bor.addOrder(order);
		}
	}

	private void sellOnATH(HistoricalData hd, BookOfRecord bor) {
		// Current ATH as of period ago
		double allTimeHigh = ath.get(period - 1);

		// Come from below
//		for (int i = 0; i < period; i++) {
//			if (hd.midHigh.get(0) > allTimeHigh)
//				return;
//		}

		// Sell for potential mini pull back
		if (hd.midHigh.get(0) > ath.get(0) * (1 - distanceToATH * atrPercent.get(0))) {
			ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), ath.get(0));
			order.setStop(ath.get(0) * (1 + atrPercent.get(0) * 1));
			order.setLimit(ath.get(0) * (1 - atrPercent.get(0) * 3));
			bor.addOrder(order);
		}
	}

	private void sellOnATHNotGood(HistoricalData hd, BookOfRecord bor) {
		// Sell for potential mini pull back
		if (hd.midHigh.get(0) > ath.get(0) * (1 - distanceToATH * atrPercent.get(0))) {
			ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), ath.get(0));
			order.setStop(ath.get(0) * (1 + atrPercent.get(0) * 1));
			order.setLimit(ath.get(0) * (1 - atrPercent.get(0) * 3));
			bor.addOrder(order);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new ShortATHShell();
	}
}
