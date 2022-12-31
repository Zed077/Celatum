package com.celatum.maths;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

public class KeltnerChannel {
	private static final ConcurrentSkipListMap<String, Serie[]> cache = new ConcurrentSkipListMap<String, Serie[]>();

	public static Serie[] calc(HistoricalData hd, int nPeriods, double multiplier) {
		// Cache
		String key = hd.getCode() + nPeriods + multiplier;
		Serie[] channels = cache.get(key);
		if (channels != null)
			return clone(channels);

		synchronized (cache) {
			channels = cache.get(key);
			if (channels != null)
				return clone(channels);

			
			// Computation
			Serie mid = Calc.ema(hd.midClose, nPeriods);
			Serie atr = Calc.atr(hd, 10);

			Serie low = new Serie();
			Serie high = new Serie();
			for (int i = 0; i <= mid.size() - nPeriods; i++) {
				Date dd = mid.getDate(i);
				low.put(dd, mid.get(i) - multiplier * atr.get(i));
				high.put(dd, mid.get(i) + multiplier * atr.get(i));
			}

			channels = new Serie[3];
			channels[0] = low;
			channels[1] = mid;
			channels[2] = high;
			
			
			cache.put(key, channels);
		}
		return clone(channels);
	}
	
	private static Serie[] clone(Serie[] s) {
		Serie[] res = new Serie[s.length];
		for (int i=0; i<s.length; i++) {
			res[i] = s[i].clone();
		}
		return res;
	}
}
