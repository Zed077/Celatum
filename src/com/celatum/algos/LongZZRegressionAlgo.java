package com.celatum.algos;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.maths.ZigZagFull;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class LongZZRegressionAlgo extends Algo {
	private Serie ema50;
	private Serie ema20;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie atr;
	private ZigZagFull zz;
	private double keltnerMultiplier = 1.4;
	private int period = 50;

	public LongZZRegressionAlgo() {
	}

	public LongZZRegressionAlgo(double keltnerMultiplier, int period) {
		this.keltnerMultiplier = keltnerMultiplier;
		this.period = period;
	}

	@Override
	public Algo getInstance() {
		return new LongZZRegressionAlgo(keltnerMultiplier, period);
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		ema50 = Calc.ema(hd.midClose, 50);
		ema20 = Calc.ema(hd.midClose, 20);
		atr = Calc.atr(hd, 20);
		zz = ZigZagFull.calc(hd, 1);

		Serie[] kc = Calc.keltnerChannel(hd, 20, keltnerMultiplier);
		lowerKC = kc[0];
		midKC = kc[1];
		upperKC = kc[2];

		// TODO autoSync on creation
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
//		this.plot(zz);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		this.zzLong22(hd, bor);
	}

	/**
	 * Find last 3 lows Check they are increasing Draw a regression over these Buy
	 * when price is lower than or equal to this line
	 * 
	 * @param hd
	 * @param bor
	 */
	protected void zzLong3(HistoricalData hd, BookOfRecord bor) {
		// Find last 3 lows
		double low1 = zz.getLows().get(0);
		double low2 = zz.getLows().get(1);
		double low3 = zz.getLows().get(2);

		// Check it is trending up
		if (low1 <= low2 || low2 <= low3)
			return;

		// Regress these 3 points
		Serie s = new Serie();
		for (int i = 0; i < 3; i++) {
			SerieItem si = zz.getLows().getItem(i);
			s.put(si);
		}
		LinearRegression lr = new LinearRegression(s, 3);

		// Buy when price is lower than or equal to this line
		double regressionValue = lr.predict(hd.getReferenceDate());
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), regressionValue);
		order.setStop(lowerKC.get(0) - 3 * atr.get(0));
		order.setLimit(midKC.get(0));
		bor.addOrder(order);

		// Positions
		boolean trendUp = ema20.get(0) > ema50.get(0);
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
	 * Find last 3 lows Check they are increasing Draw a regression over these Buy
	 * when price is lower than or equal to this line
	 * 
	 * @param hd
	 * @param bor
	 */
	protected void zzLong22(HistoricalData hd, BookOfRecord bor) {
		// Find last 2 lows and 2 highs
		double low1 = zz.getLows().get(0);
		double low2 = zz.getLows().get(1);
		double high1 = zz.getHighs().get(0);
		double high2 = zz.getHighs().get(1);

		// Check it is trending up
		if (low1 <= low2 || high1 <= high2 || hd.midClose.get(0) <= low1)
			return;

		// Buy when in lower KC
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), midKC.get(0));
		order.setStop(lowerKC.get(0) - 3 * atr.get(0));
		order.setLimit(midKC.get(0));
		bor.addOrder(order);

		// Positions
		boolean trendUp = ema20.get(0) > ema50.get(0);
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

	@Override
	protected int minPeriods() {
		return Math.max(period, 70);
	}
}
