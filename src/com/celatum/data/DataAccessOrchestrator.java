package com.celatum.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;
import com.celatum.data.Instrument.Source;
import com.celatum.service.json.AlgoRun;
import com.celatum.service.json.AlgoRunPosition;

public class DataAccessOrchestrator {

	public static final String LIVE_WATCHLIST = "live";
	public static final String TEST_WATCHLIST = "test";
	public static final String STOCK_TRAIN_WATCHLIST = "stocksTrain";
	public static final String STOCK_LIVE_WATCHLIST = "stocksLive";
	private static final int INSTRUMENT_UPDATE_TIME = 14; // in days
	private static TreeMap<String, List<Instrument>> watchlists;
	private static int counter = 0;

	public static List<Instrument> getLiveWatchlist() {
		return getWatchlist(LIVE_WATCHLIST);
	}

	public static List<Instrument> getTestWatchlist() {
		return getWatchlist(TEST_WATCHLIST);
	}

	public static List<Instrument> getStockTrainWatchlist() {
		return getWatchlist(STOCK_TRAIN_WATCHLIST);
	}

	static {
		// Instruments
		try {
			DatabaseConnector.loadInstruments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Watchlists
		try {
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

			DatabaseConnector.saveInstruments(Instrument.getInstrumentCache().values());
			
			refreshInstrumentStatistics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void init() {}

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
	
	private static void refreshInstrumentStatistics() throws IOException, InterruptedException {
		// Create instrument statistics placeholders in the database
		Collection<Instrument> instruments = getInstruments().values();

		// Only refresh weekly
		Date today = new Date();
		GregorianCalendar gc = new GregorianCalendar();

		// Get account size from IG CH
		IGConnector.connect(IGCredentials.CH_Credentials);

		// Generate the sizing figures
		for (Instrument inst : instruments) {
			try {
				// Only update every week
				Date instDate = inst.getLastUpdated();
				if (instDate == null) {
					instDate = new Date(0);
				}
				gc.setTime(instDate);
				gc.add(GregorianCalendar.DAY_OF_MONTH, INSTRUMENT_UPDATE_TIME);
				Date updateDate = gc.getTime();

				if (today.after(updateDate)) {
					DatabaseConnector.createInstrumentStatisticsShell(inst);
					
					// History to be used to compute the stats
					HistoricalData hd = new HistoricalData(inst, Source.AV_CODE);

					// Get short term historical data from AV
					AlphaVantageConnector.loadHistoricalPrices(hd, false);
					hd.initialiseData();
					InstrumentStats is = new InstrumentStats(hd);
					DatabaseConnector.saveInstrumentStatistics(is);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stats from IG
	 * @param insts
	 * @param s
	 * @return
	 */
	public static Collection<InstrumentStats> getInstrumentStatistics(Collection<Instrument> insts) {
		ArrayList<InstrumentStats> res = new ArrayList<>();
		try {
			for (Instrument i : insts) {
				InstrumentStats is = DatabaseConnector.getInstrumentStatistics(i, Source.IG_EPIC);
				res.add(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	/**
	 * Stats from IG
	 * @param inst
	 * @return
	 */
	public static InstrumentStats getInstrumentStatistics(Instrument inst) {
		try {
			return DatabaseConnector.getInstrumentStatistics(inst, Source.IG_EPIC);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/**
	 * Get full historical data available from the database Refreshes the database
	 * if refresh is true
	 * 
	 * @param instrument
	 * @param refresh
	 * @return
	 */
	public static HistoricalData getHistoricalData(Instrument instrument, Source s) {
		HistoricalData hd = new HistoricalData(instrument, s);

		try {
			DatabaseConnector.getHistoricalData(hd);
			hd.initialiseData();
			return hd;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}

	public static List<HistoricalData> getHistoricalData(Collection<Instrument> instruments, Source s) {
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, s);
			histories.add(hd);
		}
		return histories;
	}

	public static void refreshSavedHistories() {
		// Get saved history codes
		Collection<HistoricalData> savedHistories = null;
		try {
			savedHistories = DatabaseConnector.getSavedHistories();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		for (HistoricalData hd : savedHistories) {
			Source s = hd.getSource();
			Instrument i = hd.instrument;
			try {
				// Get penultimate recorded date from DB.
				Date startDate = DatabaseConnector.getLastUpdatedDate(i, s);

				// Get data from relevant source
				switch (s) {
				case IG_EPIC:
					// Obtain new IG prices from that date onward
					IGConnector.connect(IGCredentials.UK_Credentials);
					IGConnector.getHistoricalPrices(hd, startDate);
					break;
				case AV_CODE:
					double days = TimeUnit.DAYS.convert(new Date().getTime() - startDate.getTime(), TimeUnit.MILLISECONDS);
					AlphaVantageConnector.loadHistoricalPrices(hd, days > 100);
					break;
				default:
					break;
				}

				// Store prices in DB
				DatabaseConnector.updateHistoricalData(hd);
			} catch (Exception e) {
				System.err
						.println("Could not refresh data for code:" + hd.getCode() + ", source:" + s + ", name:" + i.getName());
				e.printStackTrace();
			}
		}
	}

	public static boolean saveHistories(Collection<Instrument> instruments, Source s) {
		boolean allUpdated = true;
		for (Instrument inst : instruments) {
			HistoricalData hd = new HistoricalData(inst, s);
			try {
				// Get penultimate recorded date from DB. Can be null
				Date startDate = DatabaseConnector.getLastUpdatedDate(inst, s);

				// Get data from relevant source
				switch (s) {
				case IG_EPIC:
					// Obtain new IG prices from that date onward
					IGConnector.connect(IGCredentials.UK_Credentials);
					IGConnector.getHistoricalPrices(hd, startDate);
					break;
				case AV_CODE:
					double days = 5500;
					if (startDate != null) {
						days = TimeUnit.DAYS.convert(new Date().getTime() - startDate.getTime(), TimeUnit.MILLISECONDS);
					}
					AlphaVantageConnector.loadHistoricalPrices(hd, days > 100);
					break;
				default:
					break;
				}

				// Store prices in DB
				DatabaseConnector.updateHistoricalData(hd);
			} catch (Exception e) {
				System.err
						.println("Could not refresh data for code:" + hd.getCode() + ", source:" + s + ", name:" + inst.getName());
				e.printStackTrace();
				allUpdated = false;
			}
		}
		return allUpdated;
	}
	
	public static void saveHistories(Instrument instrument) {
		// IG
		try {
			if (DatabaseConnector.getLastUpdatedDate(instrument, Source.IG_EPIC) == null) {
				HistoricalData igHist = new HistoricalData(instrument, Source.IG_EPIC);
				IGConnector.connect(IGCredentials.UK_Credentials);
				IGConnector.getHistoricalPrices(igHist, null);
				DatabaseConnector.updateHistoricalData(igHist);
			}
		} catch (Exception e) {
			System.err.println("Unable to get historical data from IG for " + instrument.getName() + " " + instrument.getCode(Source.IG_EPIC));
//			e.printStackTrace();
		}

		// AV
		try {
			if (DatabaseConnector.getLastUpdatedDate(instrument, Source.AV_CODE) == null) {
				HistoricalData avHist = new HistoricalData(instrument, Source.AV_CODE);
				AlphaVantageConnector.loadHistoricalPrices(avHist, true);
				DatabaseConnector.updateHistoricalData(avHist);
			}
		} catch (Exception e) {
			System.err.println("Unable to get historical data from AV for " + instrument.getName() + " "
					+ instrument.getCode(Source.AV_CODE));
			e.printStackTrace();
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

	public synchronized static void saveAlgoRun(BookOfRecord bor, List<Algo> algos, String type, Source s) {
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
			DatabaseConnector.saveAlgoRun(algoRunRef, runDate, type, algoDescription, algoStatistics, s);
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
