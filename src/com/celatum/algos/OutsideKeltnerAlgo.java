package com.celatum.algos;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class OutsideKeltnerAlgo extends Algo {
	Serie ema50;
	Serie ema20;
	Serie lowerKC;
	Serie midKC;
	Serie upperKC;
	Serie atr;
	double keltnerMultiplier = 1.4;

	public OutsideKeltnerAlgo() {
		super();
	}

	public OutsideKeltnerAlgo(double keltnerMultiplier) {
		super();
		this.keltnerMultiplier = keltnerMultiplier;
	}

	@Override
	public Algo getInstance() {
		return new OutsideKeltnerAlgo(keltnerMultiplier);
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
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		this.keltnerLong(hd, bor);
	}

	protected void keltnerLong(HistoricalData hd, BookOfRecord bor) {
		LinearRegression lr = new LinearRegression(hd.midClose, 50);
		boolean trendUp = lr.getPercentYearlyRateOfChange() >= 0.1;
		
		/**
		 * Go above upper channel
		 * Triggers an order entry at ema20
		 * Trail exit at 2 ATRs
		 */
		if (hd.midLow.get(0) > upperKC.get(0)
				&& bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0
				&& hd.midClose.get(0) > hd.midOpen.get(0)
				&& trendUp) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), upperKC.get(0));
			order.setTrailingStop(lowerKC.get(0), atr.get(0)*2);
			bor.addOrder(order);
		}
		
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			// If EMA50 is tipping over then exit
			if (hd.midLow.get(0) > upperKC.get(0)) {
				double currentStop = p.getStop();
				double proposedStop = upperKC.get(0);
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
			}
		}
	}

	@Override
	protected int minPeriods() {
		return 70;
	}

	@Override
	public String getName() {
		return "Outside Keltner " + keltnerMultiplier;
	}
}
