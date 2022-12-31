package com.celatum.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.Instrument.Source;
import com.celatum.service.json.AlgoRun;
import com.celatum.service.json.AlgoRunPosition;

public class DataAccessOrchestrator {

	private static final String LIVEWATCHLIST = "live";
	private static final String TESTWATCHLIST = "test";
	private static final int INSTRUMENT_UPDATE_TIME = 7; // in days
	private static TreeMap<String, List<Instrument>> watchlists;
	private static int counter = 0;

	public static List<Instrument> getLiveWatchlist() {
		return getWatchlist(LIVEWATCHLIST);
	}

	public static List<Instrument> getTestWatchlist() {
		return getWatchlist(TESTWATCHLIST);
	}

	static {
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

				DatabaseConnector.saveInstruments(Instrument.getInstrumentCache().values());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public static List<Instrument> getWatchlist(String watchlistName) {
		return watchlists.get(watchlistName);
	}

	/**
	 * 
	 * @return name of the instrument / Instrument object
	 */
	public static TreeMap<String, Instrument> getInstruments() {
		return Instrument.getInstrumentCache();
	}
	
	public static void refreshInstrumentStatistics() {
		// Only refresh weekly
		try {
			Collection<Instrument> instruments = DataAccessOrchestrator.getInstruments().values();
			Date today = new Date();
			GregorianCalendar gc = new GregorianCalendar();

			// Generate the sizing figures
			for (Instrument inst : instruments) {
				Date instDate = inst.getLastUpdated();
				if (instDate == null) {
					instDate = new Date();
				}
				gc.setTime(instDate);
				gc.add(GregorianCalendar.DAY_OF_MONTH, INSTRUMENT_UPDATE_TIME);
				Date updateDate = gc.getTime();
				
				if (today.after(updateDate)) {
					HistoricalData hd = DataAccessOrchestrator.getHistoricalData(inst, Source.IG_EPIC, 3);
					if (hd == null)
						continue;
					InstrumentStats is = new InstrumentStats(hd);
					DatabaseConnector.saveInstrumentStatistics(is);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static Collection<InstrumentStats> getInstrumentStatistics(Collection<Instrument> insts, Source s) {
		ArrayList<InstrumentStats> res = new ArrayList<>();
		try {
			for (Instrument i : insts) {
				InstrumentStats is = DatabaseConnector.getInstrumentStatistics(i, s);
				res.add(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	/**
	 * Get full historical data available from the database Refreshes the database
	 * if refresh is true
	 * 
	 * @param instrument
	 * @param refresh
	 * @return
	 */
	public static HistoricalData getHistoricalData(Instrument instrument, Source s, boolean refresh) {
		HistoricalData hd = new HistoricalData(instrument, s);

		try {
			if (refresh) {
				if (instrument.isIGDataAvailable()) {
					// Get penultimate recorded date from DB
					Date startDate = DatabaseConnector.getLastUpdatedDate(instrument, s);

					// Obtain new IG prices from that date onward
					IGConnector.connect(IGCredentials.UK_Credentials);
					IGConnector.getHistoricalPrices(hd, startDate);

					// Store prices in DB
					DatabaseConnector.updateHistoricalData(hd);
				} else if (!instrument.getCode(Source.AV_CODE).equals("null")) {
					System.out.println("Fetch data from AV!! for " + instrument.getName());
				}
			}

			// Get full list of prices from DB
			hd.empty();
			DatabaseConnector.getHistoricalData(hd);
			hd.initialiseData();
			return hd;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}
	
	public static void refreshSavedHistories() {
		// Get saved history codes
		
		
		if (instrument.isIGDataAvailable()) {
			// Get penultimate recorded date from DB
			Date startDate = DatabaseConnector.getLastUpdatedDate(instrument, s);

			// Obtain new IG prices from that date onward
			IGConnector.connect(IGCredentials.UK_Credentials);
			IGConnector.getHistoricalPrices(hd, startDate);

			// Store prices in DB
			DatabaseConnector.updateHistoricalData(hd);
		} else if (!instrument.getCode(Source.AV_CODE).equals("null")) {
			System.out.println("Fetch data from AV!! for " + instrument.getName());
		}
	}

	public static HistoricalData getHistoricalData(String algoRunRef, Instrument inst) {
		try {
			Source s = DatabaseConnector.getAlgoRunSource(algoRunRef);
			HistoricalData hd = new HistoricalData(inst, s);
			DatabaseConnector.getHistoricalData(hd);
			hd.initialiseData();
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
	public static HistoricalData getHistoricalData(Instrument instrument, Source s, int nMonths) {
		HistoricalData hd = new HistoricalData(instrument, s);

		// 3 months ago
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -nMonths);
		Date startDate = calendar.getTime();

		try {
			IGConnector.connect(IGCredentials.UK_Credentials);
			Date lastDate = DatabaseConnector.getLastUpdatedDate(instrument, s);
			if (lastDate != null) {
				return getHistoricalData(instrument, s, true);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (instrument.isIGDataAvailable()) {
			try {
				// Obtain new IG prices from that date onward
				IGConnector.getHistoricalPrices(hd, startDate);
				hd.initialiseData();
				return hd;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else if (!instrument.getCode(Source.AV_CODE).equals("null")) {
			System.out.println("LOADING AV DATA for " + instrument.getName());
		}

		return null;
	}

	public synchronized static void saveAlgoRun(BookOfRecord bor, List<Algo> algos, String type) {
		// Create parent entry
		Date runDate = new Date();
		String algoRunRef = type + "_" + runDate.getTime() + "_" + counter++;

		String algoDescription = "";
		for (Algo a : algos) {
			algoDescription += a + "<br>";
		}

		String algoStatistics = bor.getStats(runDate);
		algoStatistics = algoStatistics.replaceAll("[\r\n]+", "<br>");
		
		try {
			DatabaseConnector.saveAlgoRun(algoRunRef, runDate, type, algoDescription, algoStatistics);
			DatabaseConnector.saveAlgoRunPositions(algoRunRef, bor.getAllPositions(runDate));
			DatabaseConnector.saveAlgoRunPnL(algoRunRef, bor.profitAndLoss);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static Collection<AlgoRun> getRuns() {
		try {
			return DatabaseConnector.getAlgoRuns();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	public static Collection<Instrument> getAlgoRunInstruments(String algoRunRef) {
		try {
			return DatabaseConnector.getAlgoRunInstruments(algoRunRef);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public static Collection<AlgoRunPosition> getAlgoRunPositions(String algoRunRef, String instrumentName) {
		try {
			return DatabaseConnector.getAlgoRunPositions(algoRunRef, instrumentName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public static Serie getAlgoRunPnL(String algoRunRef) {
		try {
			return DatabaseConnector.getAlgoRunPnL(algoRunRef);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
}
