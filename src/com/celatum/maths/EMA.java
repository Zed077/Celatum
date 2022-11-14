package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

public class EMA {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(Serie source, int nPeriods) {
		// Validation
		if (nPeriods < 2)
			throw new RuntimeException("EMA days > 1 : " + nPeriods);

		// Cache
		String key = Integer.toString(source.hashCode()) + nPeriods;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			// Computation
			double multiplier = 2 / ((double) nPeriods + 1);
			Serie sma = Calc.sma(source, nPeriods);

			res = new Serie();

			// Initialisation
			int initPointer = source.size() - nPeriods;
			res.put(sma.getDate(initPointer), sma.get(initPointer));

			// Remainder of the values
			for (int j = source.size() - nPeriods - 1; j >= 0; j--) {
				double v = source.get(j) * multiplier + res.get(0) * (1 - multiplier);
				res.put(source.getDate(j), v);
			}
			cache.put(key, res);
		}
		return res.clone();
	}
}
