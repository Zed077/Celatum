package com.celatum.data;

import java.util.ArrayList;
import java.util.Date;

import com.celatum.maths.Calc;

public class HistoricalData implements Cloneable {
	public Instrument instrument;

	public Serie askHigh = new Serie();
	public Serie askLow = new Serie();
	public Serie askOpen = new Serie();
	public Serie askClose = new Serie();
	public Serie bidHigh = new Serie();
	public Serie bidLow = new Serie();
	public Serie bidOpen = new Serie();
	public Serie bidClose = new Serie();
	public Serie volume = new Serie();
	public Serie midHigh;
	public Serie midLow;
	public Serie midOpen;
	public Serie midClose;
	/**
	 * In ascending order
	 */
	private Date[] dates;

	private ArrayList<Serie> registeredSeries = new ArrayList<Serie>();

	private HistoricalData() {
	}

	public HistoricalData(Instrument instrument) {
		this(instrument, true);
	}

	public HistoricalData(Instrument instrument, boolean IGLoad) {
		try {
			this.instrument = instrument;

			if (IGLoad) {
				// Get penultimate recorded date from DB
				Date startDate = DatabaseConnector.getLastUpdatedDate(instrument);

				// Obtain new IG prices from that date onward
				IGConnector.getHistoricalPrices(this, startDate);

				// Store prices in DB
				DatabaseConnector.updateHistoricalData(this);
			}

			// Get full list of prices from DB
			this.empty();
			DatabaseConnector.getHistoricalData(this);

			// Compute mid
			midHigh = Calc.mid(askHigh, bidHigh);
			midLow = Calc.mid(askLow, bidLow);
			midOpen = Calc.mid(askOpen, bidOpen);
			midClose = Calc.mid(askClose, bidClose);

			// Dates
			this.dates = askHigh.getAllDates();

			// Register series
			syncReferenceIndex(askHigh);
			syncReferenceIndex(askLow);
			syncReferenceIndex(askOpen);
			syncReferenceIndex(askClose);
			syncReferenceIndex(bidHigh);
			syncReferenceIndex(bidLow);
			syncReferenceIndex(bidOpen);
			syncReferenceIndex(bidClose);
			syncReferenceIndex(volume);
			syncReferenceIndex(midHigh);
			syncReferenceIndex(midLow);
			syncReferenceIndex(midOpen);
			syncReferenceIndex(midClose);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @return size pertaining to the current reference date
	 */
	public int size() {
		return askHigh.size();
	}

	/**
	 * @return size of all records
	 */
	public int fullSize() {
		return dates.length;
	}

	public Date getReferenceDate() {
		return askHigh.getDate(0);
	}

	private void empty() {
		askHigh = new Serie();
		askLow = new Serie();
		askOpen = new Serie();
		askClose = new Serie();
		bidHigh = new Serie();
		bidLow = new Serie();
		bidOpen = new Serie();
		bidClose = new Serie();
		volume = new Serie();
	}

	public void println() {
		System.out.println(
				instrument.getName() + " " + askHigh.size() + " " + askHigh.oldestDate() + " " + askHigh.newestDate());
	}

	public void setReferenceIndex(int referenceIndex) {
		Date referenceDate = dates[referenceIndex];
		setReferenceIndex(referenceDate);
	}
	
	public int setReferenceIndex(Date referenceDate) {
		for (Serie s : registeredSeries) {
			s.setReferenceDate(referenceDate);
		}
		return size();
	}

	public void resetReferenceIndex() {
		for (Serie s : registeredSeries) {
			s.removeReferenceIndex();
		}
	}

	public void syncReferenceIndex(Serie s) {
		registeredSeries.add(s);
	}

	@Override
	public HistoricalData clone() {
		HistoricalData clone = new HistoricalData();

		clone.instrument = this.instrument;

		clone.askHigh = (Serie) this.askHigh.clone();
		clone.askLow = (Serie) this.askLow.clone();
		clone.askOpen = (Serie) this.askOpen.clone();
		clone.askClose = (Serie) this.askClose.clone();
		clone.bidHigh = (Serie) this.bidHigh.clone();
		clone.bidLow = (Serie) this.bidLow.clone();
		clone.bidOpen = (Serie) this.bidOpen.clone();
		clone.bidClose = (Serie) this.bidClose.clone();
		clone.volume = (Serie) this.volume.clone();
		clone.midHigh = (Serie) this.midHigh.clone();
		clone.midLow = (Serie) this.midLow.clone();
		clone.midOpen = (Serie) this.midOpen.clone();
		clone.midClose = (Serie) this.midClose.clone();

		clone.dates = this.dates;

		// Register series
		clone.syncReferenceIndex(clone.askHigh);
		clone.syncReferenceIndex(clone.askLow);
		clone.syncReferenceIndex(clone.askOpen);
		clone.syncReferenceIndex(clone.askClose);
		clone.syncReferenceIndex(clone.bidHigh);
		clone.syncReferenceIndex(clone.bidLow);
		clone.syncReferenceIndex(clone.bidOpen);
		clone.syncReferenceIndex(clone.bidClose);
		clone.syncReferenceIndex(clone.volume);
		clone.syncReferenceIndex(clone.midHigh);
		clone.syncReferenceIndex(clone.midLow);
		clone.syncReferenceIndex(clone.midOpen);
		clone.syncReferenceIndex(clone.midClose);

		return clone;
	}
	
	public String getEpic() {
		return this.instrument.getEpic();
	}
}
