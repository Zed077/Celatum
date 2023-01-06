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
import com.celatum.algos.ImprovedReferenceAlgo3;
import com.celatum.algos.LongRegressionAlgo2023;
import com.celatum.algos.SuperTrendLongAlgo;
import com.celatum.algos.shell.LowerLowShortShell3;
import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.trading.Order;
import com.celatum.trading.Position;
import com.celatum.view.Chart;

public class Main {
	public static final int NTHREAD = 24; // 20 maxes out CPU at 100%
	

	private static void saveNewInstruments() {
		// Need to be refreshed again since bid / spread info is incorrect
//		DataAccessOrchestrator.refreshInstrumentStatistics();
		
		// Need to be deleted and loaded again to reflect bid / spread
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("RYAAY"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("CA.PAR"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("TAP"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("AAPL"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("AMZN"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("INTC"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("MSFT"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("NFLX"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("NVDA"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("SBUX"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("TLRY"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("TSLA"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("JPM"));
		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("DAX"));
		
		// Need instrument stats to be loaded for these instruments
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("UBER"));
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("DKS"));
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("SPY"));
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("RYRRX"));
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("VCNIX"));
//		DataAccessOrchestrator.saveHistory(Instrument.getInstrumentByCode("RYDAX"));
	}

	public static void main(String args[]) throws Exception {
		DataAccessOrchestrator.init();
		
		// Refresh data
		// TODO: re-run on Monday 09/01/23
//		saveNewInstruments();
		
		// Run
		try {
//			backTest();
			liveRun();
//			runBestAlgos(DataAccessOrchestrator.getStockWatchlist(), Source.AV_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provides all signals without money management concerns
	 * @throws Exception
	 */
	private static void liveRun() throws Exception {
		List<Instrument> instruments = DataAccessOrchestrator.getStockWatchlist();
		Source s = Source.AV_CODE;
		
		DataAccessOrchestrator.refreshHistories(instruments, s);
		
//		runAlgoAggregate(new BullishHammerAlgo(), true, false); // 8.2% but with high trade perf
//		runAlgoAggregate(new HLHHAlgo(), false, false); // 37.7%
//		runAlgoAggregate(new ImprovedReferenceAlgo(), false, false); // 31.7%
//		runAlgoAggregate(new BreakoutAlgo(), false, false); // 6.2% should not be on this list, needs tuning
//		runAlgoAggregate(new SuperTrendAlgoNPO(), false, false);
//		runAlgoAggregate(new LongRegressionAlgo(), false, false); // 10.7% should not be on this list, needs tuning
		
		// Algos
		List<Algo> algos = getBestAlgos();
		
		// Histories
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC);
			histories.add(hd);
		}
		
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

	private static void backTest() throws Exception {
		List<Instrument> instruments = DataAccessOrchestrator.getStockWatchlist();
		Source s = Source.AV_CODE;
		
		// BESTs
		evolveAlgo(new LowerLowShortShell3(), instruments, s);
		
//		runAlgoAggregate(new LowerLowShortShell3(), instruments, s);
	}

	/**
	 * Provides signals given capital constraints. TODO: start one year before
	 * current date
	 * 
	 * @param IGLoad
	 * @param view
	 * @throws Exception
	 */
	private static void runBestAlgos(List<Instrument> instruments, Source s) throws Exception {
		List<Algo> algos = getBestAlgos();

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

	private static List<Algo> getBestAlgos() {
		// Algos
		List<Algo> algos = new ArrayList<Algo>();
		algos.add(new LongRegressionAlgo2023());
		algos.add(new BreakoutLongAlgo());
		algos.add(new HLHHAlgo());
		algos.add(new ImprovedReferenceAlgo3());
		algos.add(new SuperTrendLongAlgo());
		algos.add(new BouncePeriodLowAlgo());
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

class AlgoWrapper implements Runnable {
	private HistoricalData hd;
	private BookOfRecord bor;
	private Algo a;
	private boolean view;

	public AlgoWrapper(Algo a, HistoricalData hd, BookOfRecord bor, boolean view) {
		this.hd = hd;
		this.bor = bor;
		this.a = a;
		this.view = view;
	}

	@Override
	public void run() {
		a.run(hd, bor);
		if (view) {
			Chart.createAndShowGUI(hd, a, bor);
		}
	}

}
