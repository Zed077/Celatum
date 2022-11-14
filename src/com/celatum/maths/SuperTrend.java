package com.celatum.maths;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

public class SuperTrend {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(HistoricalData hd, int period, double multiplier, ATR.Method method) {
		// Cache
		String key = hd.getEpic() + period + multiplier + method;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = compute(hd, period, multiplier, method);
			cache.put(key, res);
		}
		return res.clone();
	}

	// Round values? Otherwise decimal computations may wreak havoc
	private static Serie compute(HistoricalData hd, int period, double multiplier, ATR.Method method) {
		// ATR
		Serie atrSerie = ATR.calc(hd, period, method);

		// High Low average and basic upper and lower band.
		Serie basicUpperBand = new Serie();
		Serie basicLowerBand = new Serie();
		Serie finalUpperBand = new Serie();
		Serie finalLowerBand = new Serie();
		Serie supertrend = new Serie();
		for (int i = hd.size() - period; i >= 0; i--) {
			Date d = hd.midClose.getDate(i);
			double atr = atrSerie.get(d);
			
			double hla = (hd.midHigh.get(i) + hd.midLow.get(i)) / 2;
			double currentBasicUpperBand = hla + atr * multiplier;
			basicUpperBand.put(d, currentBasicUpperBand);
			double currentBasicLowerBand = hla - atr * multiplier; 
			basicLowerBand.put(d, currentBasicLowerBand);
			
			if (finalUpperBand.size() == 0) {
				finalUpperBand.put(d, 0);
				finalLowerBand.put(d, 0);
				supertrend.put(d, 0);
				continue;
			}
			
			// Final upper band
			if (currentBasicUpperBand < finalUpperBand.get(0) || hd.midClose.get(i+1) > finalUpperBand.get(0)) {
				finalUpperBand.put(d, currentBasicUpperBand);
			} else {
				finalUpperBand.put(d, finalUpperBand.get(0));
			}
			
			// Final lower band
			if (currentBasicLowerBand > finalLowerBand.get(0) || hd.midClose.get(i+1) < finalLowerBand.get(0)) {
				finalLowerBand.put(d, currentBasicLowerBand);
			} else {
				finalLowerBand.put(d, finalLowerBand.get(0));
			}
			
			// Supertrend indicator
			if (supertrend.size() == 0) {
				supertrend.put(d, 0);
				continue;
			}
			if (supertrend.get(0) == finalUpperBand.get(1) && hd.midClose.get(i) < finalUpperBand.get(0)) {
				supertrend.put(d, finalUpperBand.get(0));
			}
			else if (supertrend.get(0) == finalUpperBand.get(1) && hd.midClose.get(i) > finalUpperBand.get(0)) {
				supertrend.put(d, finalLowerBand.get(0));
			}
			else if (supertrend.get(0) == finalLowerBand.get(1) && hd.midClose.get(i) > finalLowerBand.get(0)) {
				supertrend.put(d, finalLowerBand.get(0));
			}
			else if (supertrend.get(0) == finalLowerBand.get(1) && hd.midClose.get(i) < finalLowerBand.get(0)) {
				supertrend.put(d, finalUpperBand.get(0));
			}
		}
		
		supertrend.removeOldest();

		return supertrend;
	}
}
