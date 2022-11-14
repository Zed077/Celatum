package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

public class SMA {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(Serie source, int nPeriods) {
		// Validation
		if (nPeriods < 1)
			throw new RuntimeException("SMA days > 0 : " + nPeriods);

		// Cache
		String key = Integer.toString(source.hashCode()) + nPeriods;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = compute(source, nPeriods);
			cache.put(key, res);
		}
		return res.clone();
	}

	private static Serie compute(Serie source, int nPeriods) {
		Serie res = new Serie();
		for (int j = 0; j <= source.size() - nPeriods; j++) {
			double values = 0;
			for (int i = j; i < nPeriods + j; i++) {
				values += source.get(i);
			}
			res.put(source.getDate(j), values / (double) nPeriods);
		}
		return res;
	}
}
