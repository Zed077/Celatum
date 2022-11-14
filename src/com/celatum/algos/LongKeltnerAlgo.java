package com.celatum.algos;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.HighLow;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;
import com.celatum.trading.ShortOrder;
import com.celatum.trading.StopShortOrder;

public class LongKeltnerAlgo extends Algo {
	Serie ema50;
	Serie ema20;
	Serie lowerKC;
	Serie midKC;
	Serie upperKC;
	Serie atr;
	double keltnerMultiplier = 1.4;

	public LongKeltnerAlgo(double keltnerMultiplier) {
		super();
		this.keltnerMultiplier = keltnerMultiplier;
	}

	@Override
	public Algo getInstance() {
		return new LongKeltnerAlgo(keltnerMultiplier);
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
//		this.keltnerLong(hd, bor);
		this.periodLowShort(hd, bor);
	}

	protected void keltnerLong(HistoricalData hd, BookOfRecord bor) {
		boolean trendUp = ema20.get(0) > ema50.get(0);
		boolean trendDown = ema20.get(0) < ema50.get(0);
		double distance = atr.get(0);

		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());

		if (trendUp && hd.midHigh.get(0) > lowerKC.get(0)) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), lowerKC.get(0));
			order.setStop(lowerKC.get(0) - distance);
			bor.addOrder(order);
		}

		if (trendDown) {
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				p.setStop(hd.bidClose.get(0));
			}
		} else {
			// Manage stops
			for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidClose.get(0) - distance;
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
			}
		}
	}

	/**
	 * Not good
	 *  
	 * @param hd
	 * @param bor
	 */
	protected void bounceLong(HistoricalData hd, BookOfRecord bor) {
		boolean trendUp = ema20.get(0) > ema50.get(0);
		boolean trendDown = ema20.get(0) < ema50.get(0);
		double distance = atr.get(0);

		if (trendDown && hd.midClose.get(0) > midKC.get(0)) {
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), upperKC.get(0));
			order.setStop(midKC.get(0));
			bor.addOrder(order);
		}

		if (trendUp) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		}
		// Manage stops
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			double currentStop = p.getStop();
			double proposedStop = hd.bidLow.get(0)-distance;
			double newStop = Math.max(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}
	/**
	 * Not good
	 * 
	 * @param hd
	 * @param bor
	 */
	protected void followShort(HistoricalData hd, BookOfRecord bor) {
		boolean trendUp = ema20.get(0) > ema50.get(0);
		boolean trendDown = ema20.get(0) < ema50.get(0);
		double distance = atr.get(0);

		if (trendDown && hd.midClose.get(0) < lowerKC.get(0) && bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0) {
			StopShortOrder order = new StopShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), lowerKC.get(0));
			order.setStop(midKC.get(0));
			bor.addOrder(order);
		}

		if (trendUp) {
			bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
		}
		// Manage stops
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			double currentStop = p.getStop();
//			double proposedStop = Math.min(hd.askHigh.get(0), lowerKC.get(0));
			double proposedStop = hd.askHigh.get(0);
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}
	
	protected void periodLowShort(HistoricalData hd, BookOfRecord bor) {
		int period = 5;
		
		// period low
		double periodLow = HighLow.periodLow(hd.midLow, 1, period);
		
//		bor.removeAllOrders(hd.instrument, groupName);
		if (hd.midClose.get(0) < periodLow) {
			ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), lowerKC.get(0));
			order.setStop(periodLow + atr.get(0));
			bor.addOrder(order);
		}

		// Manage stops
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			double currentStop = p.getStop();
//			double proposedStop = Math.min(hd.askHigh.get(0), lowerKC.get(0));
//			double proposedStop = Math.min(hd.askHigh.get(0), Calc.periodLow(hd.midLow, p.order.getDateCreated(), period));
			double proposedStop = hd.askHigh.get(0);
			double newStop = Math.min(currentStop, proposedStop);
			p.setStop(newStop);
		}
	}

	@Override
	protected int minPeriods() {
		return 70;
	}

	@Override
	public String getName() {
		return "Long Keltner " + keltnerMultiplier;
	}
}
