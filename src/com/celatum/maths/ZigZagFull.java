package com.celatum.maths;

import java.util.Date;
import java.util.concurrent.ConcurrentSkipListMap;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

/**
 * The problem with computing the Zigzag ahead of time is that the time series
 * can say that today is a low when in fact we will only determine it is a low
 * in the future.
 * 
 * This class should not be used.
 * 
 * @author cedric.ladde
 *
 */
public class ZigZagFull {
	private static final ConcurrentSkipListMap<String, ZigZagFull> cache = new ConcurrentSkipListMap<String, ZigZagFull>();
	private double minPercent;
	private Serie highs = new Serie();
	private Serie lows = new Serie();
	private Serie all = new Serie();
	private Serie atr;
	private double atrBreath = 3;

	public static ZigZagFull calc(HistoricalData hd, int minPeriod) {
		// Cache
		String key = hd.getEpic() + minPeriod;
		ZigZagFull res = cache.get(key);
		if (res != null)
			return res.clone(hd);

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone(hd);

			// Computation
			res = new ZigZagFull(hd, minPeriod);
			cache.put(key, res);
		}
		return res.clone(hd);
	}

	public static ZigZagFull calc(HistoricalData hd, int minPeriod, double minPercent) {
		// Cache
		String key = hd.getEpic() + minPeriod + minPercent;
		ZigZagFull res = cache.get(key);
		if (res != null)
			return res.clone(hd);

		synchronized (cache) {
			res = cache.get(key);
			if (res != null)
				return res.clone(hd);

			// Computation
			res = new ZigZagFull(hd, minPeriod, minPercent);
			cache.put(key, res);
		}
		return res.clone(hd);
	}

	protected ZigZagFull clone(HistoricalData hd) {
		ZigZagFull clone = new ZigZagFull();
		clone.highs = this.highs.clone();
		clone.lows = this.lows.clone();
		clone.all = this.all.clone();

		hd.syncReferenceIndex(clone.highs);
		hd.syncReferenceIndex(clone.lows);
		hd.syncReferenceIndex(clone.all);

		return clone;
	}

	private ZigZagFull() {

	}

	private ZigZagFull(HistoricalData hd, int minPeriod) {
		atr = Calc.atr(hd, minPeriod);
		hd.syncReferenceIndex(atr);
		run(hd, minPeriod);
		hd.syncReferenceIndex(highs);
		hd.syncReferenceIndex(lows);
		hd.syncReferenceIndex(all);
	}

	private ZigZagFull(HistoricalData hd, int minPeriod, double minPercent) {
		this.minPercent = minPercent;
		run(hd, minPeriod);
		hd.syncReferenceIndex(highs);
		hd.syncReferenceIndex(lows);
		hd.syncReferenceIndex(all);
	}

	private void run(HistoricalData hd, int minPeriod) {
		// Initialisation
		hd.setReferenceIndex(0);
		double lowestLow = hd.midLow.get(0);
		Date lowestDate = hd.getReferenceDate();
		double highestHigh = hd.midHigh.get(0);
		Date highestDate = hd.getReferenceDate();
		boolean isLastHigh = false;
		boolean isLastLow = false;

		// Run
		for (int i = 1; i < hd.fullSize(); i++) {
			// Auto percent
			if (atr != null) {
				minPercent = atrBreath * atr.get(0) / hd.midClose.get(0);
			}

			hd.setReferenceIndex(i);
			double currentLow = hd.midLow.get(0);
			double currentHigh = hd.midHigh.get(0);

			if (currentLow < lowestLow) {
				lowestLow = currentLow;
				lowestDate = hd.getReferenceDate();
			}

			if (currentHigh > highestHigh) {
				highestHigh = currentHigh;
				highestDate = hd.getReferenceDate();
			}

			if (highestDate.compareTo(lowestDate) < 0) {
				double diffPercent = (highestHigh - lowestLow) / highestHigh;
				if (diffPercent > minPercent) {
					// Last high now confirmed as a high
					if (!isLastHigh) {
						SerieItem si = new SerieItem(highestDate, highestHigh);
						highs.put(si);
						all.put(si);
						isLastHigh = true;
						isLastLow = false;
					}

					// Resetting high
					highestHigh = currentHigh;
					highestDate = hd.getReferenceDate();
				}
			} else if (highestDate.compareTo(lowestDate) > 0) {
				double diffPercent = (highestHigh - lowestLow) / lowestLow;
				if (diffPercent > minPercent) {
					// Last low now confirmed as a low
					if (!isLastLow) {
						SerieItem si = new SerieItem(lowestDate, lowestLow);
						lows.put(si);
						all.put(si);
						isLastHigh = false;
						isLastLow = true;
					}

					// Resetting low
					lowestLow = currentLow;
					lowestDate = hd.getReferenceDate();
				}
			}
		}

		hd.resetReferenceIndex();
	}

	public Serie getHighs() {
		return highs;
	}

	public Serie getLows() {
		return lows;
	}

	public Serie getAll() {
		return all;
	}

}
