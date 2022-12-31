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
import com.celatum.algos.BreakoutAlgoG;
import com.celatum.algos.BullishHammerAlgo;
import com.celatum.algos.HLHHAlgo;
import com.celatum.algos.ImprovedReferenceAlgo;
import com.celatum.algos.LongRegressionAlgo;
import com.celatum.algos.SuperTrendAlgoNPO;
import com.celatum.algos.shell.BouncePeriodLowShell;
import com.celatum.algos.shell.LowerLowShortShell3;
import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.trading.Order;
import com.celatum.trading.Position;
import com.celatum.view.Chart;

public class Main {
	public static final int NTHREAD = 32;

	public static List<Instrument> instruments;

	public static void main(String args[]) throws Exception {
		// Get list of instruments in scope
		instruments = DataAccessOrchestrator.getTestWatchlist();

		// Run
		try {
			backTest();
//			liveRun();
//			runBestAlgos(false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Provides all signals without money management concerns
	 * @throws Exception
	 */
	private static void liveRun() throws Exception {
		instruments = DataAccessOrchestrator.getLiveWatchlist();
		
//		runAlgoAggregate(new BullishHammerAlgo(), true, false); // 8.2% but with high trade perf
//		runAlgoAggregate(new HLHHAlgo(), false, false); // 37.7%
//		runAlgoAggregate(new ImprovedReferenceAlgo(), false, false); // 31.7%
//		runAlgoAggregate(new BreakoutAlgo(), false, false); // 6.2% should not be on this list, needs tuning
//		runAlgoAggregate(new SuperTrendAlgoNPO(), false, false);
//		runAlgoAggregate(new LongRegressionAlgo(), false, false); // 10.7% should not be on this list, needs tuning
		
		// Algos
		List<Algo> algos = new ArrayList<Algo>();
		algos.add(new BullishHammerAlgo());
		algos.add(new HLHHAlgo());
		algos.add(new ImprovedReferenceAlgo());
		algos.add(new SuperTrendAlgoNPO());
		algos.add(new LongRegressionAlgo());
		algos.add(new BreakoutAlgoG());
		algos.add(new BouncePeriodLowShell());
		algos.add(new LowerLowShortShell3()); // Not improving main algo enough but only short that works
		
		// Histories
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC, true);
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
		// BESTs
//		runAlgoAggregate(new ImprovedReferenceAlgo(), false, false);
//		runAlgoAggregate(new HLHHAlgo(), false, false);
		
		
//		runAlgoSeparate(new LongKeltnerAlgo(1.4), false, false);
//		runAlgoSeparate(new LongATRCrossAlgo(), false, false);

//		runAlgoAggregate(new ReferenceAlgo(), false, false);
//		runAlgoAggregate(new LongRegressionAlgo(), false, false);
//		runAlgoAggregate(new ImprovedReferenceAlgo(), true, false);

//		runAlgoAggregate(new BreakoutAlgoExit(), false, false);
		

//		runAlgoAggregate(new BreakoutAlgo(), false, false);
//		runAlgoAggregate(new SP500Algo(), false, true);
		instruments = DataAccessOrchestrator.getLiveWatchlist();
		runAlgoAggregate(new LowerLowShortShell3(), false, true);
//		runAlgoSeparate(new LowerLowShortShell3(), false, true);
//		runAlgoSeparate(new BreakoutShortShell(), false, false);
//
//		runAlgoAggregate(new HammerShell(), false, true);
//		runAlgoAggregate(new BasicShortShell(), false, true);
//		runAlgoSeparate(new BreakoutAlgoG(), false, false);
//		runAlgoSeparate(new BreakoutLongShell(), false, false);
		
//		runAlgoSeparate(new RaynerTeosAlgo(), false, true);

//		evolveAlgo(new LowerLowShortShell3());
	}

	/**
	 * Provides signals given capital constraints.
	 * TODO: start one year before current date
	 * 
	 * @param IGLoad
	 * @param view
	 * @throws Exception
	 */
	private static void runBestAlgos(boolean IGLoad, boolean view) throws Exception {
		instruments = DataAccessOrchestrator.getLiveWatchlist();
		
		// Algos
		List<Algo> algos = new ArrayList<Algo>();
		algos.add(new BullishHammerAlgo());
		algos.add(new HLHHAlgo());
		algos.add(new ImprovedReferenceAlgo());
		algos.add(new SuperTrendAlgoNPO());
		algos.add(new BreakoutAlgoG());
		algos.add(new BouncePeriodLowShell());
		algos.add(new LowerLowShortShell3());

		// Histories
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC, IGLoad);
			histories.add(hd);
		}

		// Run algo
		System.out.println("\n-----------------------------------------------  Best Algos   -----------------------------------------------");
		BookOfRecord bor = new BookOfRecord();
		Algo.run(algos, histories, bor);

		// Get stats
		bor.cleanStats();
		bor.printStats(new Date());

		// View
//		if (view)
//			Chart.createAndShowGUI(histories, algo, bor, true);
	}

	private static void runAlgoAggregate(Algo algo, boolean IGLoad, boolean view) throws Exception {
		// Make sure we have all the history we need
		List<HistoricalData> histories = new ArrayList<HistoricalData>();
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC, IGLoad);
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
		if (view) {
			List<Algo> algos = new ArrayList<>();
			algos.add(algo);
			DataAccessOrchestrator.saveAlgoRun(bor, algos, "runAlgoAggregate");
//			Chart.createAndShowGUI(histories, algo, bor, true);
		}
	}

	private static void runAlgoSeparate(Algo algo, boolean IGLoad, boolean view) throws Exception {
		ArrayList<BookOfRecord> bors = new ArrayList<BookOfRecord>();
		ArrayList<Algo> as = new ArrayList<Algo>();
		ArrayList<HistoricalData> hds = new ArrayList<HistoricalData>();

		ExecutorService eservice = Executors.newFixedThreadPool(Main.NTHREAD);
		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, Source.IG_EPIC, IGLoad);
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

	private static void evolveAlgo(Algo algo) throws InterruptedException {
		GeneticEvolution g = new GeneticEvolution(instruments, Source.IG_EPIC);
		g.run(algo);
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
