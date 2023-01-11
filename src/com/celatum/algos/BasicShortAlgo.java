package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.NotGoneMyWay;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.ShortOrder;

public class BasicShortAlgo extends Algo {
	private Serie atr;
	
	public BasicShortAlgo() {
		// BasicShortShell-!NVMD/ADP3-HH/SDP701.5--TE/10--NGMW/50.1 370 0 105,447 1.59%
		addAlgoComponent(new ReverseCondition(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 3)));
		addAlgoComponent(new HigherHighs(HigherHighs.Method.SDP, 70, 1.5));
		addAlgoComponent(new TimedExit(10));
		addAlgoComponent(new NotGoneMyWay(5, 0.1));
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = Calc.atr(hd, 20);
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
		// Place order
		double entry = hd.midClose.get(0);
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry + atr.get(0) * 3);
		order.setLimit(entry - atr.get(0) * 3);
		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new BasicShortAlgo();
	}
}
