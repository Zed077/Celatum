package com.celatum.algos;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Order;
import com.celatum.trading.Position;
import com.celatum.trading.ShortOrder;
import com.celatum.trading.StopLongOrder;

public class LongRegressionAlgo extends Algo {
	private Serie ema50;
	private Serie ema20;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie atr;
	private double keltnerMultiplier = 1.4;
	private int period = 200;
	private LinearRegression lr;

	public LongRegressionAlgo() {
	}

	public LongRegressionAlgo(double keltnerMultiplier, int period) {
		this.keltnerMultiplier = keltnerMultiplier;
		this.period = period;
	}

	@Override
	public Algo getInstance() {
		return new LongRegressionAlgo(keltnerMultiplier, period);
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

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		lr = new LinearRegression(hd.midClose, minPeriods());
		this.regLongSafe(hd, bor);
//		this.regLongMax(hd, bor);
	}

	/**
	 * Safest
	 * 
	 * @param hd
	 * @param bor
	 */
	protected void regLongSafe(HistoricalData hd, BookOfRecord bor) {
		// Check trend is going up
		boolean trendUp = lr.getPercentYearlyRateOfChange() >= 0.1;

		// Orders
		if (trendUp 
				&& hd.midHigh.get(0) > lowerKC.get(0)
				&& hd.midHigh.get(0) < midKC.get(0)
				&& ema50.get(0) > ema20.get(0)
				&& hd.midClose.get(0) > hd.midOpen.get(0)) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), lowerKC.get(0));
			order.setStop(lowerKC.get(0) - 3 * atr.get(0));
			order.setLimit(midKC.get(0));
			bor.addOrder(order);
		}

		// Positions
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
				double proposedStop = hd.bidLow.get(0) - 0.5 * atr.get(0);
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
				p.setLimit(upperKC.get(0));
			}
		}
	}

	/**
	 * No good
	 * 
	 * @param hd
	 * @param bor
	 */
	protected void regShort(HistoricalData hd, BookOfRecord bor) {
		boolean trendDown = lr.getPercentYearlyRateOfChange() <= -0.1;

		// Orders
		if (trendDown && hd.midLow.get(0) < upperKC.get(0)) {
			Order order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), upperKC.get(0));
			order.setStop(upperKC.get(0) + 3 * atr.get(0));
			order.setLimit(lowerKC.get(0));
			bor.addOrder(order);
		}

		// Positions
		if (trendDown) {
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidClose.get(0) + 3 * atr.get(0);
				double newStop = Math.min(currentStop, proposedStop);
				p.setStop(newStop);
			}
		} else {
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidHigh.get(0) + 0.5 * atr.get(0);
				double newStop = Math.min(currentStop, proposedStop);
				p.setStop(newStop);
				p.setLimit(lowerKC.get(0));
			}
		}
	}

	/**
	 * @param hd
	 * @param bor
	 */
	protected void regLongMax(HistoricalData hd, BookOfRecord bor) {
		boolean trendDown = lr.getPercentYearlyRateOfChange() <= -0.1;

		// Sync limits with 1st position
		double syncLimit = upperKC.get(0) + 3 * atr.get(0);

		// Orders
		if (trendDown && hd.midLow.get(0) < upperKC.get(0)
				&& bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() < 5
				&& hd.midHigh.get(0) > upperKC.get(0) && hd.midOpen.get(0) < hd.midClose.get(0)) {
			Order order = new StopLongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), midKC.get(0));
			order.setLimit(syncLimit);
			order.setStop(lowerKC.get(0));
			bor.addOrder(order);
		}

		// Positions
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			double currentStop = p.getStop();
			double proposedStop = lowerKC.get(0);
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	protected int minPeriods() {
		return Math.max(period, 70);
	}
}
