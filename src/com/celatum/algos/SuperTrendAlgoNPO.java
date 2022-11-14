package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.entry.SuperTrendIndicator;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.Calc;
import com.celatum.maths.SuperTrend;
import com.celatum.trading.LongOrder;
import com.celatum.trading.Position;

public class SuperTrendAlgoNPO extends Algo {
	private Serie adp;
	private Serie supertrend;
	private Serie ema5;
	private Serie ema20;
//	private Serie hplot = new Serie();
//	private Serie lplot = new Serie();
//	private Serie oplot = new Serie();
	private int adpPeriod = 70;

	public SuperTrendAlgoNPO() {
		// SuperTrendShell-NPO-ST/10SMA-HH/ADP703.5-!HH/ADP702.0--TSWA/80--SFM/704.00.5--FFE/708.0 256 -7,426 1,296,228 12.7%
		
		addAlgoComponent(new TightenStopWithAge(80));
		addAlgoComponent(new NoPositionOpen());
		addAlgoComponent(new SuperTrendIndicator());
		
		addAlgoComponent(new HigherHighs(HigherHighs.Method.ADP, 70, 3.5));
		addAlgoComponent(new ReverseCondition(new HigherHighs(HigherHighs.Method.ADP, 70, 2)));
		addAlgoComponent(new SignificantFavorableMove(70, 4, 0.5));
		addAlgoComponent(new FarFromEMAStop(70, 8));
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
		
		// EMAs
		ema5 = Calc.ema(hd.midClose, 5);
		hd.syncReferenceIndex(ema5);
		ema20 = Calc.ema(hd.midClose, 20);
		hd.syncReferenceIndex(ema20);
		
		plot(supertrend, "ST");
		plot(ema5, "5");
		plot(ema20, "20");
	}

	/**
	 * TODO build test to ensure all series are always in sync from a date / index
	 * perspective
	 */
	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
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
		return new SuperTrendAlgoNPO();
	}
}
