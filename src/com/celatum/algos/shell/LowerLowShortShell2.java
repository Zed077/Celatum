package com.celatum.algos.shell;

import java.util.Date;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.NearPeriodLow;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.StopShortOrder;

public class LowerLowShortShell2 extends Algo {
	private Serie atr;
	private int period = 70;
	
	public LowerLowShortShell2() {
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new NearPeriodLow(period));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP,70, 2.5));

		// LowerLowShortShell2-NPO-NPL/70-EMAD/20-2.0-SMAD/200-1.0--TSEMA/SDP2004.0--DTS/SDP704.0--EMAT/200 94 -1,864 372,724 6.14%
		
		// LowerLowShortShell2-NPO-NPL/70-EMAD/20-2.0-NVMD/SDP5-SMAD/200-1.0E-4--DTS/ADP702.5--DTS/ADP702.0--RL--RTS/500.1--EMAT/20 81 -6,674 641,929 8.66%
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
		
		// Find entry
		Date lowestDate = lowest.getDate();
		double entry = Math.min(hd.midClose.get(lowestDate), hd.midOpen.get(lowestDate));
		
		// Place order
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
		return new LowerLowShortShell2();
	}
}
