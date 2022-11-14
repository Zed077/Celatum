package com.celatum.maths;

import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;

/**
 * Should really be driven by the adjusted close price but we do not have it
 * 
 * @author cedric.ladde
 *
 */
public class BollingerBands {
	private static final ConcurrentSkipListMap<String, BollingerBands> cache = new ConcurrentSkipListMap<String, BollingerBands>();

	public Serie lower;
	public Serie upper;
	public Serie mid;

	private BollingerBands() {
	}

	public static BollingerBands calc(Serie close) {
		return calc(close, 20, 2.5);
	}

	public static BollingerBands calc(Serie close, int nPeriods, double multiplier) {
		// Cache
		String key = String.valueOf(close.hashCode()) + nPeriods + multiplier;
		BollingerBands bb = cache.get(key);
		if (bb != null)
			return clone(bb);

		synchronized (cache) {
			bb = cache.get(key);
			if (bb != null)
				return clone(bb);

			// Computation
			bb = compute(close, nPeriods, multiplier);
			cache.put(key, bb);
		}
		return clone(bb);
	}

	private static BollingerBands compute(Serie close, int nPeriods, double multiplier) {
		BollingerBands bb = new BollingerBands();

		// Simple Moving Average
		bb.mid = SMA.calc(close, nPeriods);

		// Standard Deviation
		Serie stddev = StdDeviation.calc(close, nPeriods);

		bb.lower = new Serie();
		bb.upper = new Serie();
		for (int i = 0; i < bb.mid.size(); i++) {
			Date dd = bb.mid.getDate(i);
			bb.lower.put(dd, bb.mid.get(i) - multiplier * stddev.get(i));
			bb.upper.put(dd, bb.mid.get(i) + multiplier * stddev.get(i));
		}
		return bb;
	}

	private static BollingerBands clone(BollingerBands bb) {
		BollingerBands res = new BollingerBands();
		res.lower = bb.lower.clone();
		res.upper = bb.upper.clone();
		res.mid = bb.mid.clone();
		return res;
	}

	public void println() {
		NumberFormat nform = NumberFormat.getInstance();
		nform.setMaximumFractionDigits(2);
		for (int i = mid.size() - 1; i >= 0; i--) {
			System.out.println(
					nform.format(mid.get(i)) + "\t" + nform.format(upper.get(i)) + "\t" + nform.format(lower.get(i)));
		}
	}
	
	public void syncReferenceIndex(HistoricalData hd) {
		hd.syncReferenceIndex(lower);
		hd.syncReferenceIndex(upper);
		hd.syncReferenceIndex(mid);
	}
}
