package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMAsDistanceStop;
import com.celatum.algos.exit.RegressedTrendStop;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;

/**
 * Tune without Tesla. Not very good. Does not work well with HLHH
 * @author cedric.ladde
 *
 */
public class ImprovedReferenceAlgo2 extends Algo {
	private Serie atr;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie ema50;

	@Override
	public Algo getInstance() {
		return new ImprovedReferenceAlgo2();
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

		// ImprovedReferenceShell-!HH/ADP205.0-NVMO/ATR10703.0--DTS/SDP703.0--EDS/700.02.0--RTS/700.05 327 -6,253 3,002,656 17.78%
		addAlgoComponent(new ReverseCondition(new HigherHighs(Method.SDP, 20, 5.0)));
		addAlgoComponent(new NoViolentMoveDown(NoViolentMoveDown.Method.ATR, 10));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.SDP, 70, 3));
		addAlgoComponent(new EMAsDistanceStop(70, 0, 2));
		addAlgoComponent(new RegressedTrendStop(70, 0.05));
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
