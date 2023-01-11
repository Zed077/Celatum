package com.celatum.algos.shell;

import java.util.GregorianCalendar;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.LinearRegression;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.ShortOrder;

public class ShortHighShell extends Algo {
	// Needs to be a significant high
	private double deviationBreath = 4;
	private Serie atrPercent;
	private int atrPeriod = 70;
	private int regressionPeriod = 5;
	
	public ShortHighShell() {
		// ShortHighShell-!HH/ADP2001.0--DTS/ATR702.0--RL--SFM/2003.00.5 307 -10,660 177,719 2.42%
		// ShortHighShell-!EMAD/51.0-EMAC/50200-SMAD/2001.0--EDS/700.02.5--FFE/705.0--RS/200.0--NFTL/3 301 -68,873 157,567 2.2%
		// ShortHighShell-HH/SDP203.5-ST/7EMA--DTS/SDP201.5--NFTL/5 308 -44,770 85,750 1.33%
		// ShortHighShell-HH/SDP704.0--DTS/SDP201.5--NFTL/10 314 -45,040 242,846 3.07%


	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR Percent
		atrPercent = Calc.atrPercent(hd, atrPeriod);
		hd.syncReferenceIndex(atrPercent);
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
//		GregorianCalendar gc = new GregorianCalendar();
//		gc.set(2021, 3, 9);
//		if (hd.getReferenceDate().after(gc.getTime()) && hd.instrument.getName().equals("Amazon.com Inc (All Sessions)")) {
//			System.out.println();
//		}
		
		double minPercent = atrPercent.get(0) * deviationBreath;
		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);
		
		if (zz.getHighs().size() <= 0) {
			return;
		}
		
		double high = zz.getHighs().get(0);
		
		// Regress last x bars and ensure we have a nice move up
		LinearRegression lr = new LinearRegression(hd.midClose, regressionPeriod);
		boolean moveUp = lr.getAbsoluteDailyRateOfChange() > atrPercent.get(0) * hd.midClose.get(0)/16;
		
		// Make sure it is the first time we touch the high
		boolean firstTime = hd.askClose.getMaxSince(zz.getHighs().getDate(0), 0).getValue() < high;

		// Place order
		if (firstTime && moveUp && hd.midHigh.get(0) > high * (1 - 3 * atrPercent.get(0))) {
			ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), high);
			order.setStop(high * (1 + atrPercent.get(0) * 1));
			order.setLimit(high * (1 - atrPercent.get(0) * 2));
			bor.addOrder(order);
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new ShortHighShell();
	}
}
