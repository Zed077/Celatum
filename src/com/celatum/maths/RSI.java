package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

/**
 * Relative Strength Index
 * 
 * @author cedric.ladde
 *
 */
public class RSI {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(Serie close) {
		return calc(close, 14);
	}

	public static Serie calc(Serie close, int nPeriods) {
		// Validation
		if (nPeriods < 1)
			throw new RuntimeException("SMA days > 0 : " + nPeriods);

		// Cache
		String key = String.valueOf(close.hashCode()) + nPeriods;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = compute(close, nPeriods);
			cache.put(key, res);
		}
		return res.clone();
	}

	private static Serie compute(Serie close, int nPeriods) {
		Serie rsi = new Serie();

		// First RSI
		int firstIndex = close.size() - nPeriods - 1;
		double avgGain = 0;
		double avgLoss = 0;

		for (int t = close.size() - 2; t >= firstIndex; t--) {
			if (close.get(t + 1) < close.get(t)) {
				avgGain += close.get(t) - close.get(t + 1);
			} else if (close.get(t + 1) > close.get(t)) {
				avgLoss += close.get(t + 1) - close.get(t);
			}
		}

		avgGain = avgGain / nPeriods;
		avgLoss = avgLoss / nPeriods;

		double rs = avgGain / avgLoss;
		double rsiValue = 100.0 - 100.0 / (1.0 + rs);

		rsi.put(close.getDate(firstIndex), rsiValue);

		// Subsequent RSI
		for (int t = firstIndex - 1; t >= 0; t--) {
			double currentGain = 0;
			double currentLoss = 0;

			if (close.get(t + 1) < close.get(t)) {
				currentGain = close.get(t) - close.get(t + 1);
			} else if (close.get(t + 1) > close.get(t)) {
				currentLoss = close.get(t + 1) - close.get(t);
			}

			avgGain = (avgGain * (nPeriods - 1) + currentGain) / nPeriods;
			avgLoss = (avgLoss * (nPeriods - 1) + currentLoss) / nPeriods;

			rs = avgGain / avgLoss;
			rsiValue = 100.0 - 100.0 / (1.0 + rs);
			rsi.put(close.getDate(t), rsiValue);
		}

		return rsi;
	}

}
