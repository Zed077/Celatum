package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.SMADistance;
import com.celatum.algos.exit.RSIThreshold;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.BollingerBands;
import com.celatum.trading.LongOrder;

public class RaynerTeosAlgo extends Algo {
	private BollingerBands bb;
	private Serie atr;
	
	public RaynerTeosAlgo() {
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new SMADistance(200, 0.00001));
//		addAlgoComponent(new TimedExit(10));
		addAlgoComponent(new RSIThreshold(50, true));
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// BollingerBands
		bb = BollingerBands.calc(hd.midClose, 20, 2.5);
		bb.syncReferenceIndex(hd);
		
		// ATR
		atr = ATR.calc(hd, 20, ATR.Method.SMA);
		hd.syncReferenceIndex(atr);
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
		boolean pullback = hd.midClose.get(0) < bb.lower.get(0);
		
		if (pullback) {
			double entry = hd.midClose.get(0) * 0.97;
			LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
			order.setStopCorrect(entry - 3 * atr.get(0));
			bor.addOrder(order);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new RaynerTeosAlgo();
	}
}
