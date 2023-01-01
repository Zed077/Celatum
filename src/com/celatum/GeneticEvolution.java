package com.celatum;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.celatum.algos.Algo;
import com.celatum.algos.entry.EMACompare;
import com.celatum.algos.entry.EMADistance;
import com.celatum.algos.entry.EntryCondition;
import com.celatum.algos.entry.HigherHighs;
import com.celatum.algos.entry.NoPositionOpen;
import com.celatum.algos.entry.NoViolentMoveDown;
import com.celatum.algos.entry.OutsideBollingerBands;
import com.celatum.algos.entry.RegressionTrend;
import com.celatum.algos.entry.ReverseCondition;
import com.celatum.algos.entry.SMADistance;
import com.celatum.algos.entry.SuperTrendIndicator;
import com.celatum.algos.exit.DailyTrailingStop;
import com.celatum.algos.exit.EMATippingStop;
import com.celatum.algos.exit.EMAsDistanceStop;
import com.celatum.algos.exit.ExitStrategy;
import com.celatum.algos.exit.FarFromEMAStop;
import com.celatum.algos.exit.NegativeForTooLong;
import com.celatum.algos.exit.NotGoneMyWay;
import com.celatum.algos.exit.RSIThreshold;
import com.celatum.algos.exit.RegressedStop;
import com.celatum.algos.exit.RegressedTrendStop;
import com.celatum.algos.exit.RemoveLimit;
import com.celatum.algos.exit.SignificantFavorableMove;
import com.celatum.algos.exit.TightenStopWithAge;
import com.celatum.algos.exit.TightenStopWithEMA;
import com.celatum.algos.exit.TimedExit;
import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;

public class GeneticEvolution {
	private Vector<EntryCondition> entryConditions = new Vector<EntryCondition>();
	private Vector<ExitStrategy> exitStrategies = new Vector<ExitStrategy>();
	private Vector<AlgoRunner> population = new Vector<AlgoRunner>();
	private static final int maxGenerations = 3;
	private static final int maxPopulationSize = 20;
	private static final double minPositions = 3.7; //per stock for the entire period
	private static final int nSurvivors = 5;
	private NumberFormat percentFormat = NumberFormat.getPercentInstance();
	private NumberFormat numberFormat = NumberFormat.getNumberInstance();
	private Vector<HistoricalData> histories = new Vector<HistoricalData>();
	private AlgoRunner noGeneAlgo;

	public GeneticEvolution(List<Instrument> instruments, Source s) {
		percentFormat.setMaximumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(0);

		for (Instrument id : instruments) {
			HistoricalData hd = DataAccessOrchestrator.getHistoricalData(id, s);
			histories.add(hd);
		}
	}

	public void run(Algo shell) throws InterruptedException {
		/**
		 * All shells must have basic entry and exit mgt
		 */
		evolveEntryConditions(shell);

		// Evolution of exit strategies
		
		// Generation 0
		System.out.println("--------------- EXIT STRATEGIES");
		createExitStrategies();
		createExitStrategiesGen0();

		for (int i = 0; i < maxGenerations; i++) {
			System.out.println("--------------- EVALUATE EXIT GEN " + i);
			evaluatePopulation();
			orderPopulation(false);
			if (i < maxGenerations - 1) {
				geneticSelection();
				// Combine
				System.out.println("--------------- COMBINE");
				combine(false);
//				printPopulation();
			}
		}

		geneticSelection();
	}

	// TODO use an enum instead of a toggle
	private void combine(boolean entryExitToggle) {
		TreeMap<String, AlgoRunner> newPopulation = new TreeMap<String, AlgoRunner>();

		// Current population stays
		for (AlgoRunner ar : population) {
			newPopulation.put(ar.toString(), ar);
		}

		// Make babies
		for (int i = 0; i < population.size(); i++) {
			AlgoRunner father = population.get(i);
			for (int j = i + 1; j < population.size(); j++) {
				AlgoRunner mother = population.get(j);
				AlgoRunner baby = null;
				if (entryExitToggle) {
					baby = combineEC(father, mother);
				} else {
					baby = combineES(father, mother);
				}
				if (baby != null) {
					newPopulation.putIfAbsent(baby.toString(), baby);
				}
			}
		}

		population.clear();
		population.addAll(newPopulation.values());
	}

	private AlgoRunner combineES(AlgoRunner father, AlgoRunner mother) {
		Vector<String> ecrefs = new Vector<String>();

		AlgoRunner baby = father.clone();
		for (ExitStrategy es : father.algo.getExitStrategies()) {
			ecrefs.add(es.getName());
		}

		int count = 0;
		for (ExitStrategy es : mother.algo.getExitStrategies()) {
			if (!ecrefs.contains(es.getName())) {
				baby.algo.addAlgoComponent(es.clone());
				count++;
			}
		}

		if (count > 0) {
			return baby;
		} else
			return null;
	}

	private AlgoRunner combineEC(AlgoRunner father, AlgoRunner mother) {
		Vector<String> ecrefs = new Vector<String>();

		AlgoRunner baby = father.clone();
		for (EntryCondition ec : father.algo.getEntryConditions()) {
			ecrefs.add(ec.getName());
		}

		int count = 0;
		for (EntryCondition ec : mother.algo.getEntryConditions()) {
			if (!ecrefs.contains(ec.getName())) {
				baby.algo.addAlgoComponent(ec.clone());
				count++;
			}
		}

		if (count > 0) {
			return baby;
		} else
			return null;
	}

	private void evolveEntryConditions(Algo shell) throws InterruptedException {
		// Create genes
		createEntryConditions();

		// Generation 0
		createEntryConditionsGen0(shell);

		// Genetic evolution of entry conditions
		for (int i = 0; i < maxGenerations; i++) {
			System.out.println("\n--------------- EVALUATE GEN " + i);
			evaluatePopulation();

			// Order population
			orderPopulation(false);
			
			// Trim
			geneticSelection();

			if (i < maxGenerations - 1) {
				// Combine
				System.out.println("--------------- COMBINE");
				combine(true);
//				printPopulation();
			}
		}
	}

	private void orderPopulation(boolean print) {
		TreeSet<AlgoRunner> sars = new TreeSet<AlgoRunner>(population);
		population.clear();
		population.addAll(sars.descendingSet());

		if (print) {
			System.out.println("--------------- ORDER");
			printPopulation();
		}
	}

	private void createExitStrategiesGen0() {
		// Top 5
		List<AlgoRunner> cpop = new Vector<GeneticEvolution.AlgoRunner>();
		cpop.addAll(population.subList(0, 1));
		population.clear();
		population.addAll(cpop);
		
		// Clear exit strategies
//		for (AlgoRunner ar : population) {
//			ar.algo.clearExitStrategies();
//		}
		printPopulation();

		// Mutate
		Vector<AlgoRunner> mutants = new Vector<GeneticEvolution.AlgoRunner>();
		for (AlgoRunner ar : population) {
			for (ExitStrategy s : exitStrategies) {
				AlgoRunner mutant = ar.clone();
//				mutant.algo.clearExitStrategies();
				mutant.algo.addAlgoComponent(s.clone());
				mutants.add(mutant);
			}
		}

		population.addAll(mutants);
	}

	private void geneticSelection() {
		System.out.println("--------------- GENETIC SELECTION");

		// Remove broken algos
		Vector<AlgoRunner> remove = new Vector<GeneticEvolution.AlgoRunner>();
		for (AlgoRunner ar : population) {
			if (ar.getnPositions() <= histories.size() * minPositions) {
				remove.add(ar);
			}
		}
		population.removeAll(remove);

		// Keep the fittest
//		double baseScore = noGeneAlgo.score;
//		Vector<AlgoRunner> survivors = new Vector<GeneticEvolution.AlgoRunner>();
//		for (AlgoRunner ar : population) {
//			if (ar.score >= baseScore) {
//				survivors.add(ar);
//			}
//		}

		// Top 5
		Vector<AlgoRunner> survivors = new Vector<GeneticEvolution.AlgoRunner>();
		for (int i = 0; i < Math.min(nSurvivors, population.size()); i++) {
			survivors.add(population.get(i));
		}

		// Keep the best gene variants
		HashMap<String, AlgoRunner> bestGenes = new HashMap<String, GeneticEvolution.AlgoRunner>();
		for (AlgoRunner ar : population) {
			bestGenes.putIfAbsent(ar.getGenoType(), ar);
		}

		population.clear();
		population.addAll(survivors);
		population.addAll(bestGenes.values());

		// Deduplicate
		TreeMap<String, AlgoRunner> dedupe = new TreeMap<String, GeneticEvolution.AlgoRunner>();
		for (AlgoRunner ar : population) {
			dedupe.put(ar.toString(), ar);
		}
		
		// Order
		TreeSet<AlgoRunner> sars = new TreeSet<AlgoRunner>(dedupe.values());
		population.clear();
		population.addAll(sars.descendingSet());
		
		// Control population size
		survivors.clear();
		for (int i = 0; i < Math.min(maxPopulationSize, population.size()); i++) {
			survivors.add(population.get(i));
		}
		population.clear();
		population.addAll(survivors);

		printPopulation();
	}

	private void createEntryConditionsGen0(Algo shell) {
		// No gene
		noGeneAlgo = new AlgoRunner(shell.getInstance());
		population.add(noGeneAlgo);

		// One gene
		for (EntryCondition ec : entryConditions) {
			Algo copy = shell.getInstance();
			copy.addAlgoComponent(ec);
			AlgoRunner ar1 = new AlgoRunner(copy);
			population.add(ar1);
		}
	}

	private void evaluatePopulation() throws InterruptedException {
		ExecutorService eservice = Executors.newFixedThreadPool(Main.NTHREAD);

		for (AlgoRunner ar : population) {
			if (!ar.isEvaluated()) {
				eservice.execute(ar);
			}
		}

		eservice.shutdown();
		eservice.awaitTermination(6, TimeUnit.HOURS);
	}

	/**
	 * The entry condition class should provide the gene variations, not this class
	 */
	private void createEntryConditions() {
		entryConditions.addAll(new EMACompare().generateVariants());
		entryConditions.addAll(new EMADistance().generateVariants());
//		entryConditions.addAll(new Hammer().generateVariants());
		entryConditions.addAll(new HigherHighs().generateVariants());
		entryConditions.addAll(new NoPositionOpen().generateVariants());
		entryConditions.addAll(new NoViolentMoveDown().generateVariants());
		entryConditions.addAll(new OutsideBollingerBands().generateVariants());
		entryConditions.addAll(new RegressionTrend().generateVariants());
		entryConditions.addAll(new SMADistance().generateVariants());
		entryConditions.addAll(new SuperTrendIndicator().generateVariants());
		
		createOppositeEntryConditions();
	}

	private void createExitStrategies() {
		exitStrategies.addAll(new DailyTrailingStop().generateVariants());
		exitStrategies.addAll(new EMAsDistanceStop().generateVariants());
		exitStrategies.addAll(new EMATippingStop().generateVariants());
		exitStrategies.addAll(new FarFromEMAStop().generateVariants());
		exitStrategies.addAll(new NegativeForTooLong().generateVariants());
		exitStrategies.addAll(new NotGoneMyWay().generateVariants());
		exitStrategies.addAll(new RegressedStop().generateVariants());
		exitStrategies.addAll(new RegressedTrendStop().generateVariants());
		exitStrategies.addAll(new RemoveLimit().generateVariants());
		exitStrategies.addAll(new RSIThreshold().generateVariants());
		exitStrategies.addAll(new SignificantFavorableMove().generateVariants());
		exitStrategies.addAll(new TightenStopWithAge().generateVariants());
		exitStrategies.addAll(new TightenStopWithEMA().generateVariants());
		exitStrategies.addAll(new TimedExit().generateVariants());
	}

	private void createOppositeEntryConditions() {
		ArrayList<EntryCondition> reverseConditions = new ArrayList<EntryCondition>();
		for (EntryCondition ec : entryConditions) {
			ReverseCondition rc = new ReverseCondition(ec);
			reverseConditions.add(rc);
		}
		entryConditions.addAll(reverseConditions);
	}

	private void printPopulation() {
		for (AlgoRunner ar : population) {
			System.out.println(ar.getScorePrint());
		}
	}

	class AlgoRunner implements Runnable, Comparable<AlgoRunner> {
		Algo algo;
		private double score = Double.NaN;
		private double RoI = Double.NaN;
		private double lowestPnL = 0;
		private int nPositions = 0;

		public AlgoRunner(Algo algo) {
			this.algo = algo;
		}

		@Override
		public void run() {
			// Run algo
			BookOfRecord bor = new BookOfRecord();

			List<HistoricalData> hds = new Vector<HistoricalData>();
			for (HistoricalData hd : histories) {
				HistoricalData hdclone = hd.clone();
				hds.add(hdclone);
			}

			Algo.run(algo, hds, bor);

			// Get stats
			Date today = new Date();
			score = bor.returnAbsolute(today);
			RoI = bor.returnPercent(today);
			lowestPnL = bor.lowestPnL();
			nPositions = bor.getAllPositions(today).size();

			System.out.println(getScorePrint());
		}

		public double getScore() {
			return score;
		}

		public double getRoI() {
			return RoI;
		}

		public double getLowestPnL() {
			return lowestPnL;
		}

		public int getnPositions() {
			return nPositions;
		}

		public boolean isEvaluated() {
			return !Double.isNaN(score);
		}

		public String getScorePrint() {
			return toString() + " " + getnPositions() + " " + numberFormat.format(lowestPnL) + " "
					+ numberFormat.format(score) + " " + percentFormat.format(RoI);
		}

		@Override
		public int compareTo(AlgoRunner o) {
			return (int) Math.round((this.getScore() - o.getScore()));
		}

		@Override
		public String toString() {
			return algo.toString();
		}

		public String getGenoType() {
			return algo.getGenoType();
		}

		public AlgoRunner clone() {
			return new AlgoRunner(algo.clone());
		}

	}

}
