package com.celatum.algos.shell;

import java.awt.Color;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.EMACompare;
import com.celatum.algos.entry.Hammer;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.entry.SuperTrendIndicator;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.RemoveLimit;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.algos.exit.TightenStopWithEMA;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.ATR;
import com.celatum.maths.Calc;
import com.celatum.maths.SuperTrend;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;
import com.celatum.trading.ShortOrder;

public class SuperTrendShell extends Algo {
	private Serie adp;
	private Serie supertrend;
	private int adpPeriod = 70;

	public SuperTrendShell() {
		addAlgoComponent(new TightenStopWithAge(80));
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new ReverseCondition(new SuperTrendIndicator()));
		
		// SuperTrendShell-ST/10SMA-HH/SDP703.0-!HH/ADP702.0-NVMD/ADP4--TSWA/80--DTS/ATR2003.5--FFE/707.0--SFM/2006.00.5 3190 -31,957 31,608,975 37.41%
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// SuperTrend
		supertrend = SuperTrend.calc(hd, 10, 3, ATR.Method.EMA);
		hd.syncReferenceIndex(supertrend);

		// ADP
		adp = Calc.atrPercent(hd, adpPeriod);
		hd.syncReferenceIndex(adp);
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double entry = hd.midClose.get(0);
		ShortOrder order = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry * (1 + adp.get(0) * 3));
		order.setLimit(entry * (1 - adp.get(0) * 3));
		bor.addOrder(order);

		// TODO make that an exit strategy
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), getGroup())) {
			p.setStop(supertrend.get(0));
		}
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new SuperTrendShell();
	}
}
