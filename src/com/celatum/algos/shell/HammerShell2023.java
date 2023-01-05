package com.celatum.algos.shell;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.Hammer;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMAsDistanceStop;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.RegressedTrendStop;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.StopLongOrder;

/**
 * Fundamentally not very good. Few signals and spent a lot of time losing money with a few large wins. 
 * @author cedric
 *
 */
public class HammerShell2023 extends Algo {
	private Serie atrPercent;
	private int lookbackPeriod = 20;

	public HammerShell2023() {
		addAlgoComponent(new Hammer(lookbackPeriod, true));
		addAlgoComponent(new TimedExit(10));
		
		// With a stop loss at 2 ATRs
		// HammerShell2023-HAM/20true-!EMAD/201.5-NVMD/ADP4--TE/10--RTS/700.2--EDS/701.02.5--DTS/SDP2002.5 422 -68,770 396,513 4.37%
		// HammerShell2023-HAM/20true-!EMAD/201.5-NVMD/ADP4-NPO--TE/10--RTS/700.2--EDS/701.02.5--DTS/SDP2002.5--DTS/ATR703.5--RL--NGMW/50.5 390 -147,109 510,982 5.14%
//		addAlgoComponent(new ReverseCondition(new EMADistance(20, 1.5)));
//		addAlgoComponent(new NoViolentMoveDown(Method.ADP, 4));
//		addAlgoComponent(new NoPositionOpen());
//		addAlgoComponent(new RegressedTrendStop(70, 0.2));
//		addAlgoComponent(new EMAsDistanceStop(70, 1, 2.5));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.SDP, 200, 2.5));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ATR, 70, 3.5));
//		addAlgoComponent(new RemoveLimit());
//		addAlgoComponent(new NotGoneMyWay(5, 0.5));
		
		// With a stop loss at the bottom of the candle
		// HammerShell2023-HAM/20true-!RT/2000.2--TE/10--DTS/ATR2001.5--RTS/500.2--FFE/705.0--EDS/701.03.0 305 -147,266 373,923 4.2%
		addAlgoComponent(new ReverseCondition(new RegressionTrend(200, 0.2)));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ATR, 200, 1.5));
		addAlgoComponent(new RegressedTrendStop(50, 0.2));
		addAlgoComponent(new FarFromEMAStop(70, 5));
		addAlgoComponent(new EMAsDistanceStop(70, 1.0, 3.0));
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ATR
		atrPercent = Calc.atrPercent(hd, lookbackPeriod);
		hd.syncReferenceIndex(atrPercent);

//		addAlgoComponent(new NoPositionOpen());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		double entry = hd.midHigh.get(0);

		// Place order
		StopLongOrder order = new StopLongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
//		order.setStop(entry * (1 - atrPercent.get(0) * 2));
		order.setStop(hd.midLow.get(0));

		// Regress lows
		order.setLimit(entry * (1 + atrPercent.get(0) * 2));

		bor.addOrder(order);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}

	@Override
	public Algo getInstance() {
		return new HammerShell2023();
	}
}
