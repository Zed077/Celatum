package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

public class TrueRange {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static enum Method {
		Absolute, Percent
	}

	public static Serie calc(HistoricalData hd, Method method) {
		// Cache
		String key = hd.getCode() + method;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			switch (method) {
			case Absolute:
				res = computeAbsolute(hd);
				break;
			case Percent:
				res = computePercent(hd);
				break;
			}

			cache.put(key, res);
		}
		return res.clone();
	}

	private static Serie computeAbsolute(HistoricalData hd) {
		Serie res;
		// Computation
		res = new Serie();
		for (int i = 0; i < hd.midHigh.size() - 1; i++) {
			double trv = Math.max(hd.midHigh.get(i) - hd.midLow.get(i),
					Math.max(Math.abs(hd.midHigh.get(i) - hd.midClose.get(i + 1)),
							Math.abs(hd.midLow.get(i) - hd.midClose.get(i + 1))));
			res.put(hd.midHigh.getDate(i), trv);
		}

		int i = hd.midHigh.size() - 1;
		double trv = hd.midHigh.get(i) - hd.midLow.get(i);
		res.put(hd.midHigh.getDate(i), trv);
		return res;
	}

	private static Serie computePercent(HistoricalData hd) {
		Serie res;
		// Computation
		res = new Serie();
		for (int i = 0; i < hd.midHigh.size() - 1; i++) {
			double trv = Math.max(hd.midHigh.get(i) - hd.midLow.get(i),
					Math.max(Math.abs(hd.midHigh.get(i) - hd.midClose.get(i + 1)),
							Math.abs(hd.midLow.get(i) - hd.midClose.get(i + 1))));
			res.put(hd.midHigh.getDate(i), trv / hd.midClose.get(i));
		}

		int i = hd.midHigh.size() - 1;
		double trvp = (hd.midHigh.get(i) - hd.midLow.get(i)) / hd.midClose.get(i);
		res.put(hd.midHigh.getDate(i), trvp);
		return res;
	}
}
