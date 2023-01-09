package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class HLHHTrackingShell2023 extends Algo {
	private Serie atr;
	private Serie minPercent;
	private int period = 200;
	private double devBreath = 6;
	
	public HLHHTrackingShell2023() {
		addAlgoComponent(new TimedExit(10));
		
		// HLHHTrackingShell2023-HH/ADP2004.5-NVMD/SDP5-OBB/202.5false--TE/10--RS/204.0--RSIT/70true--SFM/706.01.0 854 -53,619 20,547,114 21.91%
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR 20
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ZigZag
		minPercent = Calc.atrPercent(hd, period);
		hd.syncReferenceIndex(minPercent);
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
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent.get(0) * devBreath);

		// Find last 2 high
		if (zz.getHighs().size() < 2)
			return;

		// Make sure we are after HH
		if (zz.getAll().get(0) <= zz.getAll().get(1))
			return;

		SerieItem h0 = zz.getHighs().getItem(0);
		SerieItem h1 = zz.getHighs().getItem(1);
		SerieItem l = zz.getLows().getItem(0);

		// Regress highs, offset to low
		double entry = Calc.twoPointsRegression(h0, h1, l, hd.getReferenceDate());
		
		if (hd.midClose.get(0) < entry-atr.get(0)) 
			return;

		// Place order
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(),
				entry + atr.get(0) * 0.1);
		order.setStop(entry - atr.get(0) * 2);
		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new HLHHTrackingShell2023();
	}
}
