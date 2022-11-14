package com.celatum.maths;

import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

/** 
 * Standard deviation percent of the True Range (not the time series)
 * @author cedric.ladde
 *
 */
public class SDP {
	private static final ConcurrentSkipListMap<String, Serie> cache = new ConcurrentSkipListMap<String, Serie>();

	public static Serie calc(HistoricalData hd, int nPeriods) {
		// Cache
		String key = hd.getEpic() + nPeriods;
		Serie res = cache.get(key);
		if (res != null)
			return res.clone();

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone();

			
			// Computation
			Serie trueRange = Calc.trueRange(hd);
			res = new Serie();

			// No smoothing
			for (int pointer = trueRange.size() - nPeriods; pointer >= 0; pointer--) {
				double sum = 0;
				for (int i = pointer; i < pointer + nPeriods; i++) {
					sum += Math.pow(trueRange.get(i) / hd.midClose.get(i), 2);
				}
				res.put(trueRange.getDate(pointer), Math.sqrt(sum / (double) nPeriods));
			}
			
			
			cache.put(key, res);
		}
		return res.clone();
	}
}
