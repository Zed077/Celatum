package com.celatum.algos.shell;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.Hammer;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.RemoveLimit;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.algos.exit.TightenStopWithEMA;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;
import com.celatum.trading.ShortOrder;

public class HammerShell extends Algo {
	private Serie adp;
//	private Serie hplot = new Serie();
//	private Serie lplot = new Serie();
//	private Serie oplot = new Serie();
	private int adpPeriod = 70;

	public HammerShell() {
		addAlgoComponent(new Hammer(70, false));
		addAlgoComponent(new TightenStopWithAge(80));
		
		// HammerShell-HAM/70false-!HH/SDP701.0-NVMD/SDP3--TSWA/80 21 -4,380 144,184 3.27%
		
		// BullishHammerShell-HAM/70true-RT/200-0.1-!HH/ADP2001.0--TSWA/80--RL--SFM/204.00.5 24 -21,537 567,741 8.2%
//		addAlgoComponent(new RegressionTrend(200, -0.1));
//		addAlgoComponent(new ReverseCondition(new HigherHighs(HigherHighs.Method.ADP, 200, 1)));
//		addAlgoComponent(new RemoveLimit());
//		addAlgoComponent(new SignificantFavorableMove(20, 4, 0.5));
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ADP
		adp = Calc.atrPercent(hd, adpPeriod);
		hd.syncReferenceIndex(adp);

//		addAlgoComponent(new NoPositionOpen());

		// Plot
//		double minPercent = adp.get(0) * deviationBreath;
//		ZigZagRelative zz = new ZigZagRelative(hd, minPercent);
//		this.plot(zz.getHighs(), "Highs");
//		this.plot(zz.getLows(), "Lows");

//		this.plot(hplot, "HH", Color.RED);
//		this.plot(lplot, "LL", Color.GREEN);
//		this.plot(oplot, "OO", Color.BLUE);
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double entry = hd.midLow.get(0);

		// Place order
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry * (1 + adp.get(0) * 1));

		// Regress lows
		order.setLimit(entry * (1 - adp.get(0) * 3));

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
		return new HammerShell();
	}
}
