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
import com.celatum.algos.BasicShortAlgo;
import com.celatum.algos.BouncePeriodLowAlgo;
import com.celatum.algos.BreakoutLongAlgo;
import com.celatum.algos.HLHHAlgo;
import com.celatum.algos.HLHHAlgo200;
import com.celatum.algos.HLHHTrackingAlgo;
import com.celatum.algos.ImprovedReferenceAlgo3;
import com.celatum.algos.LongMeanBounceAlgo;
import com.celatum.algos.LongPeakAlgo;
import com.celatum.algos.LongRegressionAlgo2023;
import com.celatum.algos.SuperTrendLongAlgo;
import com.celatum.algos.shell.*;
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
		DataAccessOrchestrator.saveHistories(Instrument.getInstrumentByCode("UNH"));
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
		List<Instrument> instruments = DataAccessOrchestrator.getWatchlist(DataAccessOrchestrator.STOCK_TRAIN_WATCHLIST);
		Source s = Source.AV_CODE;
		
		// BESTs
		evolveAlgo(new LongMeanReversionShell(), instruments, s);

//		runAlgoAggregate(new BasicShortAlgo(), instruments, s);
//		runAlgoAggregate(new BasicShortShell(), instruments, s);
//		runAlgoAggregate(new ShortHighShell(), instruments, s);
//		runAlgoAggregate(new BreakoutShortShell(), instruments, s);
//		runAlgoAggregate(new LowerLowShortShell(), instruments, s);
//		runAlgoAggregate(new LowerLowShortShell2(), instruments, s);
//		runAlgoAggregate(new LowerLowShortShell3(), instruments, s);
//		runAlgoAggregate(new Pattern17ShortShell(), instruments, s);
//		runAlgoAggregate(new Pattern22ShortShell(), instruments, s);
//		runAlgoAggregate(new ShortATHShell(), instruments, s);
//		runAlgoAggregate(new ShortHHShell(), instruments, s);
//		runAlgoAggregate(new SuperTrendShortShell(), instruments, s);

//		runAlgoAggregate(new LongMeanBounceAlgo(), instruments, s);

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

	/**
	 * 2 Active Orders, 19566 Closed Orders, 6 Active Positions, 7828 Closed Positions
	 * PnL 339,947,349 inc. Costs 36,081,675 Yearly Return 38.0%
	 * Lowest PnL -151,294 Max Drawdown -3,195,244 Avg Trade Perf 2.5%
	 * @return
	 */
	public static List<Algo> getBestAlgos() {
		// Algos
		List<Algo> algos = new ArrayList<Algo>();
		algos.add(new LongMeanBounceAlgo()); // LongMeanBounceAlgo-SMAD/2001.0-EMAD/70-4.0--TE/20--RS/702.0--NGMW/30.5--SFM/2003.00.5 976 -45,864 10,458,037 18.31%
		algos.add(new BasicShortAlgo()); // BasicShortAlgo-!NVMD/ADP3-HH/SDP701.5--TE/10--NGMW/50.1 370.0 0 825,405 6.75%
		algos.add(new LongPeakAlgo()); //LongPeakAlgo-GD/5false-!HH/ADP202.0--TE/20--RS/204.0 328.0 -4,106 1,873,265 10.05%
		algos.add(new BreakoutLongAlgo()); // BreakoutLongAlgo-NPO-!HH/SDP2002.5-HH/SDP2004.0--SFM/705.02.0 305.0 -5,230 2,071,482 10.61%
		algos.add(new BouncePeriodLowAlgo()); // BouncePeriodLowAlgo-NPO-NPL/70-!HH/SDP2002.5--DTS/ATR201.5--SFM/2003.01.0 357.0 -16,952 1,195,698 8.14%
		algos.add(new HLHHTrackingAlgo()); // HLHHTrackingAlgo-HH/ADP2004.5-NVMD/SDP5-OBB/202.5false--TE/10--RS/204.0--RSIT/70true--SFM/706.01.0 872.0 -41,699 19,727,866 21.68%
		algos.add(new LongRegressionAlgo2023()); // LongRegressionAlgo2023-HH/ADP204.0-OBB/201.0false--RS/203.5--NGMW/50.5--RL--RTS/2000.05 381.0 -136,038 2,026,435 10.38%
		algos.add(new SuperTrendLongAlgo()); // SuperTrendLongAlgo-ST/10SMA-EMAD/51.0-HH/ADP704.0-EMAC/50200--TE/20--RS/703.5--RTS/700.05--TSWA/20--RL 1037.0 -155,023 8,623,337 17.27%
		algos.add(new ImprovedReferenceAlgo3()); // ImprovedReferenceAlgo3-HH/ADP203.0-RT/700.2--DTS/ADP2004.0--EMAT/50 1537.0 -187,709 115,397,476 31.64%
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
		Print.printShortAlgo(algo, bor);
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