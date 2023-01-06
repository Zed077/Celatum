package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.Hammer;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.NearPeriodLow;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.NegativeForTooLong;
import com.celatum.algos.exit.NotGoneMyWay;
import com.celatum.algos.exit.RemoveLimit;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class BouncePeriodLowShell extends Algo {
	private Serie atr;
	private int period = 70;
	
	public BouncePeriodLowShell() {
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new NearPeriodLow(period));
		
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP,70, 2.5));
		
		// BouncePeriodLowShell-NPO-NPL/70-!HH/SDP2002.5--DTS/ATR201.5--SFM/2003.01.0 349 -2,713 1,338,293 8.6%
//		addAlgoComponent(new ReverseCondition(new HigherHighs(Method.SDP, 200, 2.5)));
//		addAlgoComponent(new DailyTrailingStop(com.celatum.algos.exit.DailyTrailingStop.Method.ATR, 20, 1.5));
//		addAlgoComponent(new SignificantFavorableMove(200, 3, 1));
		
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atr = Calc.atr(hd, period);
		hd.syncReferenceIndex(atr);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		ZigZagRelative zz = new ZigZagRelative(hd, atr);
		SerieItem lowest = zz.lowestSince(hd.midLow.getDate(period));
		
		// Place order
		if (lowest == null) {
			System.out.println();
		}
		double entry = lowest.getValue() + 0.2 * atr.get(0);
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(lowest.getValue() - 0.2 * atr.get(0));
		order.setLimit(entry + atr.get(0) * 2);
		bor.addOrder(order);
		
		// Plot
//		Serie plot = new Serie();
//		plot.put(zz.getAll().getItem(0));
//		plot.put(zz.getAll().getItem(1));
//		this.plot(plot, "ZZ");
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new BouncePeriodLowShell();
	}
}
