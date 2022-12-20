package com.celatum.data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class DataAccessOrchestrator {

	private static final String LIVEWATCHLIST = "live";
	private static final String TESTWATCHLIST = "test";
	private static TreeMap<String, List<Instrument>> watchlists;

	public static List<Instrument> getLiveWatchlist() {
		return getWatchlist(LIVEWATCHLIST);
	}

	public static List<Instrument> getTestWatchlist() {
		return getWatchlist(TESTWATCHLIST);
	}

	private static void init() {
		// Instruments
		try {
			DatabaseConnector.loadInstruments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Watchlists
		if (watchlists == null) {
			try {
				IGConnector.disconnect();
				IGConnector.connect(IGCredentials.CH_Credentials);

				// Get all watchlists
				List<WatchlistData> ws = IGConnector.getWatchlists();
				watchlists = new TreeMap<>();
				for (WatchlistData w : ws) {
					// Get all instruments
					if (w.isEditable()) {
						List<Instrument> is = IGConnector.getWatchlistById(w.getId());
						watchlists.put(w.getName(), is);
					}
				}

				// Augment the instruments
				for (Instrument i : Instrument.getInstrumentCache().values()) {
					if (i.marginFactor == null) {
						IGConnector.augmentInstrument(i);
					}
				}
				IGConnector.disconnect();

				DatabaseConnector.updateInstruments(Instrument.getInstrumentCache().values());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public static List<Instrument> getWatchlist(String watchlistName) {
		init();
		return watchlists.get(watchlistName);
	}

	public static TreeMap<String, Instrument> getInstruments() {
		init();
		return Instrument.getInstrumentCache();
	}

	/**
	 * Get full historical data available from the database Refreshes the database
	 * if refresh is true
	 * 
	 * @param instrument
	 * @param refresh
	 * @return
	 */
	public static HistoricalData getHistoricalData(Instrument instrument, boolean refresh) {
		HistoricalData hd = new HistoricalData(instrument);
		
		try {
			if (refresh) {
				if (instrument.isIGDataAvailable()) {
				// Get penultimate recorded date from DB
				Date startDate = DatabaseConnector.getLastUpdatedDate(instrument);

				// Obtain new IG prices from that date onward
				IGConnector.connect(IGCredentials.UK_Credentials);
				IGConnector.getHistoricalPrices(hd, startDate);

				// Store prices in DB
				DatabaseConnector.updateHistoricalData(hd);
				} else if (!instrument.getAVCode().equals("null")) {
					System.out.println("Fetch data from AV!! for " + instrument.getName());
				}
			}

			// Get full list of prices from DB
			hd.empty();
			DatabaseConnector.getHistoricalData(hd);

			hd.processData();
			return hd;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return null;
	}

	/**
	 * Loads last x months of data from IG but does not store it in DB.
	 * 
	 * @param instrument
	 */
	public static HistoricalData getHistoricalData(Instrument instrument, int nMonths) {
		HistoricalData hd = new HistoricalData(instrument);

		// 3 months ago
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -nMonths);
		Date startDate = calendar.getTime();
		
		try {
			IGConnector.connect(IGCredentials.UK_Credentials);
			Date lastDate = DatabaseConnector.getLastUpdatedDate(instrument);
			if (lastDate != null) {
				return getHistoricalData(instrument, true);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (instrument.isIGDataAvailable()) {
			try {
				// Obtain new IG prices from that date onward
				IGConnector.getHistoricalPrices(hd, startDate);
				hd.processData();
				return hd;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else if (!instrument.getAVCode().equals("null")) {
			System.out.println("LOADING AV DATA for " + instrument.getName());
		}
		
		return null;
	}
}
