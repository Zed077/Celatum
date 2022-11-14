package com.celatum.maths;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

/**
 * All time high
 * 
 * @author cedric.ladde
 *
 */
public class HighLow {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie allTimeHigh(Serie source) {
		// Cache
		String key = getKey(source, 0);
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = new Serie();

			// Compute
			res.put(source.getItem(source.size() - 1));
			double lastHigh = res.get(0);

			for (int i = source.size() - 2; i >= 0; i--) {
				lastHigh = Math.max(lastHigh, source.get(i));
				res.put(source.getDate(i), lastHigh);
			}
			cache.put(key, res);
		}
		return res.clone();
	}

	public static Serie Highest(Serie source, int period) {
		// Cache
		String key = getKey(source, period);
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = new Serie();

			// Compute
			for (int t = source.size() - 1; t >= 0; t--) {
				double lastHigh = 0;
				for (int d = t; d >= 0 && t - d < period; d--) {
					lastHigh = Math.max(lastHigh, source.get(d));
				}
				res.put(source.getDate(t), lastHigh);
			}
			cache.put(key, res);
		}
		return res.clone();
	}

	public static Serie Lowest(Serie source, int period) {
		// Cache
		String key = getKey(source, -period);
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			res = new Serie();

			// Compute
			for (int t = source.size() - 1; t >= 0; t--) {
				double lastLow = Double.MAX_VALUE;
				for (int d = t; d >= 0 && t - d < period; d--) {
					lastLow = Math.min(lastLow, source.get(d));
				}
				res.put(source.getDate(t), lastLow);
			}
			cache.put(key, res);
		}
		return res.clone();
	}

	private static String getKey(Serie source, int period) {
		return String.valueOf(source.hashCode()) + period;
	}

	public static double periodLow(Serie s, int start, int nPeriods) {
		double low = Double.MAX_VALUE;
		for (int i = start; i < nPeriods + start; i++) {
			low = Math.min(low, s.get(i));
		}
		return low;
	}

	public static double periodLow(Serie s, Date start, int nPeriods) {
		for (int i = 0; i < s.size(); i++) {
			if (s.getDate(i).compareTo(start) <= 0)
				return periodLow(s, i, nPeriods);
		}
		return periodLow(s, 0, nPeriods);
	}
}
