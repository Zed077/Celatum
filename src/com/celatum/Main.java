package com.celatum;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.celatum.algos.Algo;
import com.celatum.algos.BouncePeriodLowAlgo;
import com.celatum.algos.BreakoutLongAlgo;
import com.celatum.algos.HLHHAlgo;
import com.celatum.algos.HLHHAlgo200;
import com.celatum.algos.HLHHTrackingAlgo;
import com.celatum.algos.ImprovedReferenceAlgo3;
import com.celatum.algos.LongRegressionAlgo2023;
import com.celatum.algos.SuperTrendLongAlgo;
import com.celatum.algos.shell.HLHHShell2023;
import com.celatum.algos.shell.HLHHTrackingShell;
import com.celatum.algos.shell.HLHHTrackingShell2023;
import com.celatum.algos.shell.ImprovedReferenceShell;
import com.celatum.algos.shell.LowerLowShortShell3;
import com.celatum.algos.shell.ShortATHShell;
import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.trading.Order;
import com.celatum.trading.Position;

public class Main {
	public static final int NTHREAD = 24; // 20 maxes out CPU at 100%
	

	private static void saveNewInstruments() {
		// Need to be deleted and loaded again to reflect bid / spread
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("RYAAY"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("CA.PAR"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("TAP"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("AAPL"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("AMZN"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("INTC"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("MSFT"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("NFLX"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("NVDA"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("SBUX"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("TLRY"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("TSLA"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("JPM"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("DAX"));
		
		// Need instrument stats to be loaded for these instruments
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("UBER"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("DKS"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("SPY"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("RYRRX"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("VCNIX"));
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("RYDAX"));
	}

	public static void main(String args[]) throws Exception {
		DataAccessOrchestrator.init();
		
		// Refresh data
//		saveNewInstruments();
		
		// Run
		try {
			backTest();
//			liveRun(DataAccessOrchestrator.getWatchlist(DataAccessOrchestrator.STOCK_LIVE_WATCHLIST), Source.AV_CODE, true);
//			runBestAlgos(DataAccessOrchestrator.getWatchlist(DataAccessOrchestrator.STOCK_LIVE_WATCHLIST), Source.AV_CODE, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void backTest() throws Exception {
		List<Instrument> instruments = DataAccessOrchestrator.getStockTrainWatchlist();
		Source s = Source.AV_CODE;
		
		// BESTs
		evolveAlgo(new ShortATHShell(), instruments, s);

//		runAlgoAggregate(new ShortATHShell(), instruments, s);
		
//		runAlgoAggregate(new LongRegressionAlgo2023(), instruments, s);
//		runAlgoAggregate(new BreakoutLongAlgo(), instruments, s);
//		runAlgoAggregate(new HLHHTrackingAlgo(), instruments, s);
//		runAlgoAggregate(new ImprovedReferenceAlgo3(), instruments, s);
//		runAlgoAggregate(new SuperTrendLongAlgo(), instruments, s);
//		runAlgoAggregate(new BouncePeriodLowAlgo(), instruments, s);
	}

	/**
	 * Provides all signals without money management concerns
	 * @throws Exception
	 */
	private static void liveRun(List<Instrument> instruments, Source s, boolean refresh) throws Exception {
		// Lookback period
		int lookbackPeriod = 200+200;
		
		// Load the latest prices into the database
		if (refresh && !DataAccessOrchestrator.saveHistories(instruments, s)) {
			System.exit(-1);
		}
		
		// Get histories and truncate them to 240 candles
		List<HistoricalData> histories = DataAccessOrchestrator.getHistoricalData(instruments, s);
		for (HistoricalData hd : histories) {
			hd.head(lookbackPeriod);
		}
		
		// Algos
		List<Algo> algos = getBestAlgos();
		
		TreeMap<String, BookOfRecord> results = new TreeMap<String, BookOfRecord>();

		ExecutorService eservice = Executors.newFixedThreadPool(Main.NTHREAD);
		for (HistoricalData hd : histories) {
			for (Algo a : algos) {
				Algo aclone = a.clone();
				HistoricalData hdclone = hd.clone();
				BookOfRecord bor = new BookOfRecord();

				AlgoWrapper aw = new AlgoWrapper(aclone, hdclone, bor, false);
				eservice.execute(aw);
				String key = hdclone.instrument.getName() + " - " + aclone.getName();
				results.put(key, bor);
			}
		}

		eservice.shutdown();
		eservice.awaitTermination(10, TimeUnit.MINUTES);
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(1);
		
		// Orders
		System.out.println("\n\n------------------  ORDERS  ------------------");
		Date today = new Date();
		for(BookOfRecord bor : results.values()) {
			for (Order o : bor.getActiveOrders(today)) {
				String output = o + " [Yield " + percentFormat.format(bor.returnPercent(today)) + ", Perf " + percentFormat.format(bor.averagePerformance()) + "]";
				System.out.println(output);
			}
		}
		
		// Positions
		System.out.println("\n------------------  POSITIONS  ------------------");
		for(BookOfRecord bor : results.values()) {
			for (Position p : bor.getActivePositions(today)) {
				String output = p + " [Yield " + percentFormat.format(bor.returnPercent(today)) + ", Perf " + percentFormat.format(bor.averagePerformance()) + "]";
				System.out.println(output);
			}
		}

		// Positions
		System.out.println("\n------------------  CLOSURES  ------------------");
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DATE, -3);
		Date afterDay = cal.getTime();
		for (BookOfRecord bor : results.values()) {
			for (Position p : bor.getAllPositions(today)) {
				if (p.isClosed() && p.getCloseDate().after(afterDay)) {
					String output = p + " [Closed on " + p.getCloseDate() + "]";
					System.out.println(output);
				}
			}
		}
	}

	/**
	 * Provides signals given capital constraints. TODO: start one year before
	 * current date
	 * 
	 * @param IGLoad
	 * @param view
	 * @throws Exception
	 */
	private static void runBestAlgos(List<Instrument> instruments, Source s, boolean refresh) throws Exception {
		List<Algo> algos = getBestAlgos();
		if (refresh && !DataAccessOrchestrator.saveHistories(instruments, s)) {
			System.exit(-1);
		}

		// Histories
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, s);
			histories.add(hd);
		}

		// Run algo
		System.out.println("\n-----------------------------------------------  Best Algos   -----------------------------------------------");
		BookOfRecord bor = new BookOfRecord();
		Algo.run(algos, histories, bor);

		// Get stats
		bor.cleanStats();
		bor.printStats(new Date());
		
		// Save run in DB
		DataAccessOrchestrator.saveAlgoRun(bor, algos, "runBestAlgos", s);

		// View
//		if (view)
//			Chart.createAndShowGUI(histories, algo, bor, true);
	}

	public static List<Algo> getBestAlgos() {
		// Algos
		List<Algo> algos = new ArrayList<Algo>();
		algos.add(new BreakoutLongAlgo());
		algos.add(new LongRegressionAlgo2023());
		algos.add(new BouncePeriodLowAlgo());
		algos.add(new HLHHTrackingAlgo());
		algos.add(new ImprovedReferenceAlgo3());
		algos.add(new SuperTrendLongAlgo());
		return algos;
	}

	/**
	 * Runs one algo over a number of instrument using a single book of record
	 * @param algo
	 * @param instruments
	 * @param view
	 * @throws Exception
	 */
	private static void runAlgoAggregate(Algo algo, List<Instrument> instruments, Source s) throws Exception {
		// Make sure we have all the history we need
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, s);
			histories.add(hd);
		}

		// Run algo
		System.out.println("\n-----------------------------------------------  " + algo.getName()
				+ "  -----------------------------------------------");
		BookOfRecord bor = new BookOfRecord();
		Algo.run(algo, histories, bor);

		// Get stats
		bor.cleanStats();
		bor.printStats(new Date());

		// View
		List<Algo> algos = new ArrayList<>();
		algos.add(algo);
		DataAccessOrchestrator.saveAlgoRun(bor, algos, algo.getName(), s);
//			Chart.createAndShowGUI(histories, algo, bor, true);
	}

	/**
	 * Runs one algo on each instrument independently
	 * @param algo
	 * @param instruments
	 * @param view
	 * @throws Exception
	 */
	private static void runAlgoSeparate(Algo algo, List<Instrument> instruments, boolean view) throws Exception {
		ArrayList<BookOfRecord> bors = new ArrayList<BookOfRecord>();
		ArrayList<Algo> as = new ArrayList<Algo>();
		ArrayList<HistoricalData> hds = new ArrayList<HistoricalData>();

		ExecutorService eservice = Executors.newFixedThreadPool(Main.NTHREAD);
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC);
			BookOfRecord bor = new BookOfRecord();
			Algo a = algo.getInstance();

			AlgoWrapper aw = new AlgoWrapper(a, hd, bor, view);
			eservice.execute(aw);
			bors.add(bor);
			as.add(a);
			hds.add(hd);
		}

		eservice.shutdown();
		eservice.awaitTermination(10, TimeUnit.MINUTES);

		for (int i = 0; i < bors.size(); i++) {
			System.out.println("\n-----------------------------------------------------------------  "
					+ as.get(i).getName() + " - " + hds.get(i).instrument.getName()
					+ "  -----------------------------------------------------------------");

			bors.get(i).printStats(new Date());
		}
	}

	private static void evolveAlgo(Algo algo, List<Instrument> instruments, Source s) throws Exception {
		GeneticEvolution g = new GeneticEvolution(instruments, s);
		Algo bestAlgo = g.run(algo);
		runAlgoAggregate(bestAlgo, instruments, s);
	}
}