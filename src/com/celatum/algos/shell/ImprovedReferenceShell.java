package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMATippingStop;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;

public class ImprovedReferenceShell extends Algo {
	private Serie atr;
	private Serie lowerKC;
	private Serie midKC;
	private Serie upperKC;
	private Serie ema50;
	

	public ImprovedReferenceShell() {
		// ImprovedReferenceShell-HH/ADP203.0-RT/700.2--DTS/ADP2004.0--EMAT/50 1537 -187,709 115,338,955 31.64%
		addAlgoComponent(new HigherHighs(Method.ADP, 20, 3));
		addAlgoComponent(new RegressionTrend(70, 0.2));
		addAlgoComponent(new DailyTrailingStop(com.celatum.algos.exit.DailyTrailingStop.Method.ADP, 200, 4));
		addAlgoComponent(new EMATippingStop(50));
		
		// ImprovedReferenceShell-!HH/SDP703.0-EMAC/50200-ST/10EMA-SMAD/2002.0--TE/20--RS/202.5--EDS/702.01.0--TSEMA/SDP203.5 345 -160,045 9,329,169 17.76%
		// ImprovedReferenceShell-!HH/ADP204.5-NVMD/SDP5--DTS/ADP2004.0--TSEMA/SDP704.0--RTS/500.05--EDS/700.01.0 1326 -236,092 46,760,223 26.37%
		// ImprovedReferenceShell-HH/ADP203.0-RT/700.2-HH/ADP701.5-!EMAD/5-1.0-ST/7EMA-OBB/202.0false--DTS/ADP2004.0--EMAT/50--RTS/500.1 523 -130,869 167,383,824 33.89%

		
		// ImprovedReferenceShell 376 -242,499 139,689 2%
		
//		addAlgoComponent(new TimedExit(20));
	}

	@Override
	public Algo getInstance() {
		return new ImprovedReferenceShell();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
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
		
//		addAlgoComponent(new EMATippingStop());
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
		
		/**
		 * TODO see if we can increase the limit placed on rate of change dynamically.
		 * e.g. if we have 0.2 then we should sell if we go below 0.2.
		 */

		// If uptrend then buy
		if (trendUp && bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup()).size() == 0
//				&& hd.midClose.get(0) < midKC.get(0) && hd.midClose.get(0) > lowerKC.get(0)
//				&& hd.midHigh.get(0) < midKC.get(0) && hd.midHigh.get(0) > lowerKC.get(0)
//				&& hd.midLow.get(0) < midKC.get(0) && hd.midLow.get(0) > lowerKC.get(0)
				&& ema50.get(0) + 0.5 * atr.get(0) > midKC.get(0)) {
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
