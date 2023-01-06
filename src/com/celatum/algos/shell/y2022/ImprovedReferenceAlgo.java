package com.celatum.algos.shell.y2022;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMATippingStop;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;

/**
 * Tuned for big moves like Tesla. Not super efficient outside of that.
 * @author cedric.ladde
 *
 */
public class ImprovedReferenceAlgo extends Algo {
	private Serie atr;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie ema50;

	@Override
	public Algo getInstance() {
		return new ImprovedReferenceAlgo();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
//		bor.singlePositionRisk = 0.1;

		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// KC
		Serie[] kc = Calc.keltnerChannel(hd, 20, 1.4);
		lowerKC = kc[0];
		midKC = kc[1];
		upperKC = kc[2];

		hd.syncReferenceIndex(lowerKC);
		hd.syncReferenceIndex(midKC);
		hd.syncReferenceIndex(upperKC);
		
		// EMEA 50
		ema50 = Calc.ema(hd.midClose, 50);
		hd.syncReferenceIndex(ema50);
		
		// ImprovedReferenceShell-!HH/SDP204.5-HH/ADP203.0--DTS/ADP2003.5 336 -20,810 29,821,381 37.34%
//		addAlgoComponent(new ReverseCondition(new HigherHighs(Method.SDP, 20, 4.5)));
//		addAlgoComponent(new HigherHighs(Method.ADP, 20, 3.0));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP, 200, 3.5));
		
		// ImprovedReferenceShell-!HH/SDP204.5-HH/ADP203.0--DTS/SDP703.0--EMAT/70 373 -11,646 25,702,914 35.98%
		addAlgoComponent(new ReverseCondition(new HigherHighs(Method.SDP, 20, 4.5)));
		addAlgoComponent(new HigherHighs(Method.ADP, 20, 3.0));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.SDP, 70, 3));
		addAlgoComponent(new EMATippingStop(70));
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		// Regression over last 50 days
		LinearRegression lr = new LinearRegression(hd.midClose, 50);
		boolean trendUp = lr.getPercentYearlyRateOfChange() >= 0.1;

		// Identify falling knife
		boolean fallingKnife = false;
//		fallingKnife = (hd.midHigh.get(1) - hd.midLow.get(0)) > 2 * atr.get(0);
//		fallingKnife = hd.midClose.get(0) < hd.midOpen.get(0);
		
		/**
		 * TODO see if we can increase the limit placed on rate of change dynamically.
		 * e.g. if we have 0.2 then we should sell if we go below 0.2.
		 */

		// If uptrend then buy
		if (trendUp && bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0
//				&& hd.midClose.get(0) < midKC.get(0) && hd.midClose.get(0) > lowerKC.get(0)
//				&& hd.midHigh.get(0) < midKC.get(0) && hd.midHigh.get(0) > lowerKC.get(0)
//				&& hd.midLow.get(0) < midKC.get(0) && hd.midLow.get(0) > lowerKC.get(0)
				&& ema50.get(0) + 0.5 * atr.get(0) > midKC.get(0) && !fallingKnife) {
			LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), midKC.get(0));
			double stop1 = midKC.get(0) - 1 * atr.get(0);
			double stop2 = hd.midLow.get(0) - 1 * atr.get(0);
			o.setStop(stop1);
			bor.addOrder(o);
		}

		if (lr.getPercentYearlyRateOfChange() >= 2) {
			LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), upperKC.get(0));
			o.setStopCorrect(hd.midLow.get(0) - 1 * atr.get(0));
			bor.addOrder(o);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
