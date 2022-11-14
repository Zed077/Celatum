package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongOrder;

public class ReferenceAlgo extends Algo {

	@Override
	public Algo getInstance() {
		return new ReferenceAlgo();
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		if (bor.getActiveOrders(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0
				&& bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0) {
			for (int i = 0; i < 10; i++) {
				LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), 6000);
				o.setStop(1);
				bor.addOrder(o);
			}
		}
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
//		bor.singlePositionRisk = 1;
	}

	@Override
	protected int minPeriods() {
		return 0;
	}
}
