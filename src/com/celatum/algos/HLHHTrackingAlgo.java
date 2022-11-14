package com.celatum.algos;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;
import com.celatum.trading.LongOrder;

public class HLHHTrackingAlgo extends Algo {
	private double deviationPercent;
	private double devBreath = 3;
	private Serie adp70;

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		// ADP 70
		adp70 = Calc.atrPercent(hd, minPeriods());
		hd.syncReferenceIndex(adp70);

		// HLHHTrackingShell-HH/ADP704.0-NPO--DTS/SDP702.5 715 -132,727 2,340,298 15.8%
		this.addAlgoComponent(new HigherHighs(HigherHighs.Method.ADP, 70, 4));
		this.addAlgoComponent(new NoPositionOpen());
		this.addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.SDP, 70, 2.5));
		
		//GenTestAlgo/HH/SDP2003.0/!HH/ADP2001.5/NPO 667 0.7998091818350355
//		this.addAlgoComponent(new HigherHighs(HigherHighs.Method.SDP, 200, 3));
//		this.addAlgoComponent(new ReverseCondition(new HigherHighs(HigherHighs.Method.SDP, 200, 1.5)));
//		this.addAlgoComponent(new NoPositionOpen());
//		
//		this.addAlgoComponent(new TightenStopWithAge());
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
		deviationPercent = adp70.get(0);

		ZigZagRelative zz = new ZigZagRelative(hd, deviationPercent*devBreath);
		
		if (zz.getHighs().size() < 2) {
			return;
		}

		// Make sure we are after a high (and not a low)
		if (zz.getAll().get(0) <= zz.getAll().get(1))
			return;

		SerieItem h0 = zz.getHighs().getItem(0);
		SerieItem h1 = zz.getHighs().getItem(1);
		SerieItem l = zz.getLows().getItem(0);

		// Compute entry point
		double entry = Calc.twoPointsRegression(h0, h1, l, hd.getReferenceDate());

		// TODO: need to introduce a variant
		
		// Too far past entry point, likely to be a down trend
//		if (hd.midClose.get(0) < entry*(1 - deviationPercent*0.1))
//			return;

		// Place order
		LongOrder order = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		order.setStopCorrect(entry * (1 - deviationPercent * 0.1));
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
