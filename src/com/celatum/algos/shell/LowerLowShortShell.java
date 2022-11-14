package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.EMADistance;
import com.celatum.algos.entry.NearPeriodLow;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.RegressedTrendStop;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.StopShortOrder;

public class LowerLowShortShell extends Algo {
	private Serie atr;
	private int period = 70;
	
	public LowerLowShortShell() {
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new NearPeriodLow(period));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP,70, 2.5));
		
		// LowerLowShortShell-NPO-NPL/70-!HH/SDP2005.5--RTS/200.05--SFM/203.00.5--DTS/ADP702.5--NFTL/10 129 -31,651 499,415 7.33%
		// LowerLowShortShell-NPO-NPL/70-EMAD/20-1.5--DTS/ADP702.5--RTS/200.2 123 -20,295 684,839 8.89%
		addAlgoComponent(new EMADistance(20, -1.5));
		addAlgoComponent(new RegressedTrendStop(20, 0.2));

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
		double entry = lowest.getValue() - 0.2 * atr.get(0);
		StopShortOrder order = new StopShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry + atr.get(0) * 1);
		order.setLimit(entry - atr.get(0) * 2.5);
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
		return new LowerLowShortShell();
	}
}
