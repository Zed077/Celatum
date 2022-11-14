package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.Serie;

public class Mid {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(Serie a, Serie b) {
		// Validation
		if (a.size() != b.size())
			throw new RuntimeException(
					"Mid can only be computed with series of equal length " + a.size() + " != " + b.size());

		// Cache
		String key = Integer.toString(a.hashCode()) + b.hashCode();
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			
			// Computation
			res = new Serie();
			for (int i = 0; i < a.size(); i++) {
				if (a.getDate(i).compareTo(b.getDate(i)) != 0) {
					throw new RuntimeException("Mid dates mismatch " + a.getDate(i) + " != " + b.getDate(i));
				}
				res.put(a.getDate(i), (a.get(i) + b.get(i)) / 2.0);
			}
			
			
			cache.put(key, res);
		}
		return res.clone();
	}
}
