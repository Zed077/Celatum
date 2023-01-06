package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.*;
import com.celatum.algos.entry.HigherHighs.Method;
import com.celatum.algos.exit.*;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.Calc;
import com.celatum.maths.SuperTrend;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class SuperTrendLongAlgo extends Algo {
	private Serie adp;
	private Serie supertrend;
	private int adpPeriod = 70;

	public SuperTrendLongAlgo() {
		// Base
		addAlgoComponent(new TimedExit(20));
		addAlgoComponent(new SuperTrendIndicator());
		
		// Algo on previous dataset
		// SuperTrendShell-ST/10SMA-HH/SDP703.0-!HH/ADP702.0-NVMD/ADP4--TSWA/80--DTS/ATR2003.5--FFE/707.0--SFM/2006.00.5 3190 -31,957 31,608,975 37.41%
		
		// SuperTrendLongShell-NPO-ST/10SMA-EMAD/704.0-OBB/201.0true-RT/200.2-NVMD/ADP5--TSWA/80--RS/204.0--FFE/708.0--RL 342 -97,502 7,195,594 16.39%
		// SuperTrendLongShell-ST/10SMA-NPO-EMAD/704.0-NVMD/ADP10--RS/2004.0--NGMW/50.5--FFE/708.0 954 -79,111 3,896,194 13.38%
		// SuperTrendLongShell-NPO-ST/10SMA-EMAD/704.0-OBB/201.0true-RT/200.2-NVMD/ADP5--DTS/SDP704.0--EDS/700.02.0--RS/204.0 628 -122,080 5,544,577 15.09%
		
		// NPO not great
		// SuperTrendLongShell-NPO-ST/10SMA-RT/200.2-NVMD/ADP5-OBB/201.0true-SMAD/2002.0--TE/20--SFM/704.01.0--DTS/SDP2004.0 1107 -93,558 3,192,158 12.42%
		// TightenStopWithAge not as good as TimedExit
		// SuperTrendLongShell-ST/10SMA-NPO-EMAD/704.0-NVMD/SDP10--TSWA/80--RS/204.0--FFE/708.0--RL 349 -93,445 6,804,937 16.11%
		
		// SuperTrendLongShell-ST/10SMA-EMAD/51.0-HH/ADP704.0-EMAC/50200--TE/20--RS/703.5--RTS/700.05--TSWA/20--RL 1012 -119,589 9,803,487 18.01%
		addAlgoComponent(new EMADistance(5, 1.0));
		addAlgoComponent(new HigherHighs(Method.ADP, 70, 4.0));
		addAlgoComponent(new EMACompare(50, 200));
		addAlgoComponent(new RegressedStop(70, 3.5));
		addAlgoComponent(new RegressedTrendStop(70, 0.05));
		addAlgoComponent(new TightenStopWithAge(20));
		addAlgoComponent(new RemoveLimit());
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
		double todaysClose = hd.midClose.get(0);
		double stv = supertrend.get(0);

		double entry = hd.midClose.get(0);
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStop(entry * (1 - adp.get(0) * 3));
		order.setLimit(entry * (1 + adp.get(0) * 3));
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
		return new SuperTrendLongAlgo();
	}
}
