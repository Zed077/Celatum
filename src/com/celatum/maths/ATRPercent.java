package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

public class ATRPercent {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(HistoricalData hd, int period, ATR.Method method) {		
		// Cache
		String key = hd.getCode() + period + method;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			switch (method) {
			case Smoothed:
				res = computeSmoothed(hd, period);
				break;
			case SMA:
				res = computeSME(hd, period);
				break;
			case EMA:
				res = computeEMA(hd, period);
				break;
			}
						
			cache.put(key, res);
		}
		return res.clone();
	}

	private static Serie computeSmoothed(HistoricalData hd, int period) {
		Serie res = new Serie();
		// Computation
		Serie trueRange = TrueRange.calc(hd, TrueRange.Method.Percent);

		// Init
		int initPointer = trueRange.size() - period;
		double sum = 0;
		for (int i = initPointer; i < trueRange.size(); i++) {
			sum += trueRange.get(i);
		}
		res.put(trueRange.getDate(initPointer), sum / (double) period);

		// Smoothing
		for (int i = trueRange.size() - period - 1; i >= 0; i--) {
			res.put(trueRange.getDate(i), (res.get(0) * (period - 1) + trueRange.get(i)) / (double) period);
		}
		return res;
	}
	
	private static Serie computeSME(HistoricalData hd, int period) {
		// True Range
		Serie trueRange = TrueRange.calc(hd, TrueRange.Method.Percent);
		return Calc.sma(trueRange, period);
	}
	
	private static Serie computeEMA(HistoricalData hd, int period) {
		// True Range
		Serie trueRange = TrueRange.calc(hd, TrueRange.Method.Percent);
		return Calc.ema(trueRange, period);
	}
}
