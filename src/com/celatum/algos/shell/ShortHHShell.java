package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.ShortOrder;

public class ShortHHShell extends Algo {
	private Serie atr;
	private Serie adp;
	private double deviationBreath = 2;
	private int adpPeriod = 70;
	
	public ShortHHShell() {
		// ShortHHShell-HH/SDP204.0-!HH/SDP2005.5--EDS/701.03.0--DTS/ADP204.0--SFM/205.02.0 359 0 539,075 5.26%
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR 20
		atr = Calc.atr(hd, 20);
		hd.syncReferenceIndex(atr);

		// ADP
		adp = Calc.atrPercent(hd, adpPeriod);
		hd.syncReferenceIndex(adp);
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
		double minPercent = adp.get(0) * deviationBreath;
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);

		// Find last 2 high
		if (zz.getHighs().size() < 2)
			return;

		// Make sure we are after a low
//		if (zz.getAll().get(0) >= zz.getAll().get(1))
//			return;

		// Regress highs
		SerieItem h0 = zz.getHighs().getItem(0);
		SerieItem h1 = zz.getHighs().getItem(1);
		double entry = Calc.twoPointsRegression(h0, h1, hd.getReferenceDate());
		if (entry < 0)
			return;
		
		// Make sure we are not above the high line
		if (hd.midClose.get(0) > entry) {
			return;
		}

		// Last high too far
		if (Calc.daysDifference(h0.getDate(), hd.getReferenceDate()) > 90)
			return;

		// Place order
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry * (1 + adp.get(0) * 1));

		// Regress lows
		SerieItem l0 = zz.getLows().getItem(0);
		SerieItem l1 = zz.getLows().getItem(1);
		double limit = Calc.twoPointsRegression(l0, l1, hd.getReferenceDate());
		order.setLimit(limit * (1 + adp.get(0) * 1));

		// Plot
//		hplot.put(h0);
//		hplot.put(h1);
//		lplot.put(l0);
//		lplot.put(l1);
//		hplot.put(new SerieItem(hd.getReferenceDate(), entry * (1 + adp.get(0) * 1)));
//		lplot.put(new SerieItem(hd.getReferenceDate(), limit * (1 + adp.get(0) * 1)));
//		oplot.put(new SerieItem(hd.getReferenceDate(), entry));

		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new ShortHHShell();
	}
}
