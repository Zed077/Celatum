package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.OutsideBollingerBands;
import com.celatum.algos.exit.RSIThreshold;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class HLHHTrackingAlgo extends Algo {
	private Serie atr;
	private Serie minPercent;
	private int period = 200;
	private double devBreath = 6;
	
	public HLHHTrackingAlgo() {
		// PnL 2,893,487 inc. Costs 2,218,826 Yearly Return 12.0%
		// Lowest PnL -164,448 Max Drawdown -238,021 Avg Trade Perf 1.3%
		addAlgoComponent(new TimedExit(10));
		
		// HLHHTrackingAlgo-NVMD/SDP3-HH/SDP704.5-OBB/202.5false-RSIF/70.0false--TE/10--RS/204.0--RSIT/70true--SFM/2006.00.5 985 -60,013 2,612,295 11.54%
		// PnL 19,693,477 inc. Costs 3,032,531 Yearly Return 21.7%
		// Lowest PnL -103,968 Max Drawdown -681,245 Avg Trade Perf 2.3%

		
		// HLHHTrackingShell2023-HH/ADP2004.5-NVMD/SDP5-OBB/202.5false--TE/10--RS/204.0--RSIT/70true--SFM/706.01.0 854 -53,619 20,547,114 21.91%
		// 872 Closed Positions
		// PnL 19,727,866 inc. Costs 3,036,575 Yearly Return 21.7%
		// Lowest PnL -34,638 Max Drawdown -511,382 Avg Trade Perf 2.2%
		addAlgoComponent(new HigherHighs(Method.ADP, 200, 4.5));
		addAlgoComponent(new NoViolentMoveDown(NoViolentMoveDown.Method.SDP, 5));
		addAlgoComponent(new OutsideBollingerBands(20, 2.5, false));
		addAlgoComponent(new RegressedStop(20, 4));
		addAlgoComponent(new RSIThreshold(70, true));
		addAlgoComponent(new SignificantFavorableMove(70, 6, 1));
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
		return new HLHHTrackingAlgo();
	}
}
