package com.celatum.maths;

import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

/**
 * This calculation is not cached and should only be used in the body of the
 * algo i.e. the processToday method
 * 
 * @author cedric.ladde
 *
 */
public class ZigZagRelative {
	private Serie highs = new Serie();
	private Serie lows = new Serie();
	private Serie all = new Serie();
	private double atrBreath = 3;
	private double minPercent = 0.03;

	public ZigZagRelative(HistoricalData hd, Serie atr) {
		minPercent = atrBreath * atr.get(0) / hd.midClose.get(0);
		run(hd);
	}

	public ZigZagRelative(HistoricalData hd, double minPercent) {
		this.minPercent = minPercent;
		run(hd);
	}

	private void run(HistoricalData hd) {
		// Initialisation
		double lowestLow = hd.midLow.get(hd.size() - 1);
		Date lowestDate = hd.midLow.getDate(hd.size() - 1);
		double highestHigh = hd.midHigh.get(hd.size() - 1);
		Date highestDate = hd.midHigh.getDate(hd.size() - 1);
		boolean isLastHigh = false;
		boolean isLastLow = false;

		// Run
		for (int i = hd.size() - 2; i >= 0; i--) {
			// Auto percent
			double currentLow = hd.midLow.get(i);
			double currentHigh = hd.midHigh.get(i);

			if (currentLow < lowestLow) {
				lowestLow = currentLow;
				lowestDate = hd.midLow.getDate(i);
			}

			if (currentHigh > highestHigh) {
				highestHigh = currentHigh;
				highestDate = hd.midHigh.getDate(i);
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
					highestDate = hd.midHigh.getDate(i);
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
					lowestDate = hd.midLow.getDate(i);
				}
			}
		}
	}
	
	/**
	 * Will return the closet lowest if no low can be found in the period
	 * Returns null if no low can be found
	 * @param periodStart
	 * @return
	 */
	public SerieItem lowestSince(Date periodStart) {
		if (this.getLows().size() == 0) {
			return null;
		}
		
		// Period low
		SerieItem periodLow = this.getLows().getItem(0);
		for (int i = 1; i < this.getLows().size() && this.getLows().getItem(i).getDate().after(periodStart); i++) {
			SerieItem nlow = this.getLows().getItem(i);
			if (nlow.getValue() < periodLow.getValue()) {
				periodLow = nlow;
			}
		}
		return periodLow;
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
