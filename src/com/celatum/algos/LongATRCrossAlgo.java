package com.celatum.algos;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class LongATRCrossAlgo extends Algo {
	private Serie ema50;
	private Serie ema20;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie atr;
	private double keltnerMultiplier = 1.4;

	@Override
	public Algo getInstance() {
		return new LongATRCrossAlgo();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		ema50 = Calc.ema(hd.midClose, 50);
		ema20 = Calc.ema(hd.midClose, 20);
		atr = Calc.atr(hd, 20);

		Serie[] kc = Calc.keltnerChannel(hd, 20, keltnerMultiplier);
		lowerKC = kc[0];
		midKC = kc[1];
		upperKC = kc[2];

		hd.syncReferenceIndex(ema20);
		hd.syncReferenceIndex(ema50);
		hd.syncReferenceIndex(lowerKC);
		hd.syncReferenceIndex(midKC);
		hd.syncReferenceIndex(upperKC);
		hd.syncReferenceIndex(atr);

		this.plot(lowerKC, "Lower KC", Color.YELLOW);
		this.plot(upperKC, "Upper KC", Color.YELLOW);
		this.plot(midKC, "EMA20 KC", Color.YELLOW, true);
		this.plot(ema50, "EMA50 KC", Color.ORANGE);
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
		this.crossLong(hd, bor);
	}

	protected void crossLong(HistoricalData hd, BookOfRecord bor) {
		// check trend is going up
		boolean trendUp = ema20.get(0) - ema50.get(0) > atr.get(0);

		if (trendUp) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), upperKC.get(0));
			order.setStop(lowerKC.get(0));
			bor.addOrder(order);
		}

		if (trendUp) {
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidClose.get(0) - 3 * atr.get(0);
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
			}
		} else {
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidLow.get(0);
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
				p.setLimit(upperKC.get(0));
			}
		}
	}

	@Override
	protected int minPeriods() {
		return 70;
	}
}
