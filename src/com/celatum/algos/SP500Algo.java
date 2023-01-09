package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class SP500Algo extends Algo {
	private double factor = 0.42;

	public SP500Algo() {
		// SP500Algo 15 -237,647 358,387 5.89%
		
		// SP500Algo-RT/200-0.2-HH/SDP204.5-NVMD/SDP5-OBB/202.0false 10 -91,214 917,039 12.28%
	}

	@Override
	public Algo getInstance() {
		return new SP500Algo();
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		// Manage position
		double stop = hd.midHigh.get(0) * factor;
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			p.setStop(Math.max(stop, p.getStop()));
		}
		
		// Only invest on the SP500
		if (hd.getCode().equals("IX.D.SPTRD.IFM.IP")) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), hd.midHigh.get(0));
			order.setStop(stop);
			bor.addOrder(order);
		}
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		bor.singlePositionRisk = 1;
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
