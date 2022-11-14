package com.celatum.algos.shell.failed;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.algos.entry.EMADistance;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.RegressedTrendStop;
import com.celatum.algos.exit.RemoveLimit;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.maths.EMA;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongOrder;
import com.celatum.trading.ShortOrder;

/**
 * Works on the basis that price is constantly oscillating. Regress to find
 * trend On trend up, if we move away significantly from ema on the downside
 * then buy and capture short term rebound On trend down, short
 * 
 * or go back to ema??
 * 
 * @author cedric.ladde
 *
 */
public class OscillatorShell extends Algo {
	private int periodEMA = 20;
	private double triggerDistance = -1.5;
	private int period = 20;
	private Serie ema;
	private Serie adp;

	public OscillatorShell() {
//		addAlgoComponent(new RegressionTrend(period, 0.1));
//		addAlgoComponent(new TightenStopWithAge(1));

//		addAlgoComponent(new EMADistance(periodEMA, triggerDistance));
		addAlgoComponent(new NoPositionOpen());
		
		
		// OscillatorShell-NPO-EMAD/5-1.0--DTS/ATR2001.5--RL--SFM/2003.00.5 39 -15,411 290,362 5.16%
//		addAlgoComponent(new EMADistance(5, -1));
//		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ATR, 200, 1.5));
//		addAlgoComponent(new RemoveLimit());
//		addAlgoComponent(new SignificantFavorableMove(200, 3, 0.5));
		
		// OscillatorShell-NPO-EMAD/70-4.0-NVMD/SDP3--FFE/703.0--RTS/700.2--DTS/ADP202.0--SFM/2003.00.5 228 -43,136 305,114 5.39%
		addAlgoComponent(new EMADistance(70, -4));
		addAlgoComponent(new NoViolentMoveDown(NoViolentMoveDown.Method.SDP, 3));
		addAlgoComponent(new FarFromEMAStop(70, 3));
		addAlgoComponent(new RegressedTrendStop(70, 0.2));
		addAlgoComponent(new DailyTrailingStop(DailyTrailingStop.Method.ADP, 20, 2));
		addAlgoComponent(new SignificantFavorableMove(200, 3, 0.5));
	}

	@Override
	public Algo getInstance() {
		return new OscillatorShell();
	}

	@Override
	protected void setUp(HistoricalData hd, BookOfRecord bor) {
		adp = ATRPercent.calc(hd, period, ATR.Method.SMA);
		hd.syncReferenceIndex(adp);
		ema = EMA.calc(hd.midClose, periodEMA);
		hd.syncReferenceIndex(ema);
	}

	@Override
	protected void manageOrders(HistoricalData hd, BookOfRecord bor) {
		bor.cancelAllOrders(hd.instrument, hd.getReferenceDate(), getGroup());
	}

	@Override
	protected void processToday(HistoricalData hd, BookOfRecord bor) {
		LinearRegression lr = new LinearRegression(hd.midClose, 20);
		double dev = Math.sqrt(lr.getMeanSquareError());
		
		double entry = lr.predict(hd.getReferenceDate()) + dev;
//		LongOrder o = new LongOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
//		o.setStop(entry * (1 - adp.get(0)));
//		o.setLimit(entry + 2 * dev);
//		bor.addOrder(o);
		
		ShortOrder o = new ShortOrder(hd.instrument, getGroup(), hd.getReferenceDate(), entry);
		o.setLimit(entry * (1 - adp.get(0)));
		o.setStop(entry + 2 * dev);
		bor.addOrder(o);
	}

	@Override
	protected int minPeriods() {
		return 200;
	}
}
