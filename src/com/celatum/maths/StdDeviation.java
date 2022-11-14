package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

/**
 * StdDeviation of the serie
 * @author cedric.ladde
 *
 */
public class StdDeviation {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(Serie source, int nPeriods) {
		// Validation
		if (nPeriods < 1)
			throw new RuntimeException("StdDeviation days > 0 : " + nPeriods);

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

	// Results are incorrect for some reason
	/**
	import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
	private static Serie compute(Serie source, int nPeriods) {
		Serie res = new Serie();
		StandardDeviation sd = new StandardDeviation();
		for (int t = 0; t <= source.size() - nPeriods; t++) {
			double[] values = new double[nPeriods];
			for (int i = t; i < nPeriods + t; i++) {
				values[i - t] = source.get(i);
			}
			double dev = sd.evaluate(values);
			res.put(source.getDate(t), dev);
		}
		return res;
	}
	**/

	private static Serie compute(Serie source, int nPeriods) {
		Serie res = new Serie();
		for (int t = 0; t <= source.size() - nPeriods; t++) {
			// Mean
			double mean = 0;
			for (int i = t; i < nPeriods + t; i++) {
				mean += source.get(i);
			}
			mean = mean / nPeriods;

			// Std Dev
			double value = 0;
			for (int i = t; i < nPeriods + t; i++) {
				value += Math.pow(source.get(i) - mean, 2);
			}
			double dev = Math.sqrt(value / nPeriods);
			res.put(source.getDate(t), dev);
		}
		return res;
	}
}
