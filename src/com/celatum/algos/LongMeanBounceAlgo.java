package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.EMADistance;
import com.celatum.algos.entry.SMADistance;
import com.celatum.algos.exit.NotGoneMyWay;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.RSI;
import com.celatum.trading.LongOrder;

public class LongMeanBounceAlgo extends Algo {
	private Serie atr;
	private Serie rsi;

	public LongMeanBounceAlgo() {
		addAlgoComponent(new SMADistance(200, 1, false));
		addAlgoComponent(new TimedExit(20));
		// LongMeanBounceAlgo-SMAD/2001.0-EMAD/70-4.0--TE/20--RS/702.0--NGMW/30.5--SFM/2003.00.5 976 -45,864 10,458,037 18.31%
		addAlgoComponent(new EMADistance(70, -4));
		addAlgoComponent(new RegressedStop(70, 2));
		addAlgoComponent(new NotGoneMyWay(3, 0.5));
		addAlgoComponent(new SignificantFavorableMove(200, 3, 0.5));
	}

	@Override
	public Algo getInstance() {
		return new LongMeanBounceAlgo();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		rsi = RSI.calc(hd.midClose);
		hd.syncReferenceIndex(rsi);

		// ATR
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double entry = hd.midLow.get(0);
		LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		o.setStop(entry - 3 * atr.get(0));
		o.setLimit(entry + 3 * atr.get(0));
		bor.addOrder(o);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
