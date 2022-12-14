package com.celatum.algos;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import com.celatum.BookOfRecord;
import com.celatum.algos.entry.EntryCondition;
import com.celatum.algos.exit.ExitStrategy;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;
import com.celatum.maths.ZigZagRelative;
import com.celatum.view.AbstractPlot;
import com.celatum.view.PlotSerie;
import com.celatum.view.PlotZigZag;

public abstract class Algo {
	public ArrayList<AbstractPlot> plots = new ArrayList<AbstractPlot>();
	private ArrayList<EntryCondition> entryConditions = new ArrayList<EntryCondition>();
	private ArrayList<ExitStrategy> exitStrategies = new ArrayList<ExitStrategy>();
	private Serie signals;
	private String group;

	@SuppressWarnings("unchecked")
	private void initialise(HistoricalData hd, BookOfRecord bor) {
		group = this.getName() + " - " + hd.instrument.getName();
		hd.resetReferenceIndex();
		this.setUp(hd, bor);

		for (EntryCondition ac : entryConditions) {
			ac.setUp(hd);
		}
		for (ExitStrategy ac : (List<ExitStrategy>) exitStrategies.clone()) {
			ac.setUp(hd);
		}
	}

	public void run(HistoricalData hd, BookOfRecord bor) {
		this.initialise(hd, bor);

		for (int i = minPeriods(); i < hd.fullSize(); i++) {
			hd.setReferenceIndex(i);
			bor.processToday(hd);

			manageOrders(hd, bor);
			managePositions(hd, bor);

			if (canEnter(hd, bor)) {
				processToday(hd, bor);
			}
		}
	}

	public static void run(Algo algo, List<HistoricalData> hds, BookOfRecord bor) {
		TreeSet<Date> allDates = new TreeSet<Date>();
		ArrayList<Algo> algos = new ArrayList<Algo>();
		for (HistoricalData hd : hds) {
			Algo aa = algo.clone();
			algos.add(aa);
			aa.initialise(hd, bor);

			Date[] dds = hd.askHigh.getAllDates();
			for (int i = 0; i < dds.length; i++) {
				allDates.add(dds[i]);
			}
		}

		for (Date d : allDates) {
			for (int i = 0; i < hds.size(); i++) {
				Algo aa = algos.get(i);
				HistoricalData hd = hds.get(i);

				if (hd.setReferenceIndex(d) < aa.minPeriods()) {
					continue;
				}

				bor.processToday(hd);

				aa.manageOrders(hd, bor);
				aa.managePositions(hd, bor);

				if (aa.canEnter(hd, bor)) {
					aa.processToday(hd, bor);
				}
			}
		}
	}

	public static void run(List<Algo> algos, List<HistoricalData> hds, BookOfRecord bor) {
		TreeSet<Date> allDates = new TreeSet<Date>();
		ArrayList<List<Algo>> aclones = new ArrayList<List<Algo>>();
		for (HistoricalData hd : hds) {
			aclones.add(cloneInit(algos, hd, bor));

			Date[] dds = hd.askHigh.getAllDates();
			for (int i = 0; i < dds.length; i++) {
				allDates.add(dds[i]);
			}
		}

		for (Date d : allDates) {
			for (int i = 0; i < hds.size(); i++) {
				List<Algo> la = aclones.get(i);
				HistoricalData hd = hds.get(i);

				for (Algo aa : la) {
					if (hd.setReferenceIndex(d) < aa.minPeriods()) {
						continue;
					}

					bor.processToday(hd);

					aa.manageOrders(hd, bor);
					aa.managePositions(hd, bor);

					if (aa.canEnter(hd, bor)) {
						aa.processToday(hd, bor);
					}
				}
			}
		}
	}
	
	private static List<Algo> cloneInit(List<Algo> algos, HistoricalData hd, BookOfRecord bor) {
		List<Algo> clone = new ArrayList<Algo>(algos.size());
		for (Algo a : algos) {
			Algo ac = a.clone();
			clone.add(ac);
			ac.initialise(hd, bor);
		}
		return clone;
	}

	public abstract Algo getInstance();
	
	public Algo clone() {
		Algo clone = this.getInstance();
		clone.entryConditions.clear();
		clone.exitStrategies.clear();
		
		for (EntryCondition ec : this.entryConditions) {
			clone.addAlgoComponent(ec.clone());
		}
		
		for (ExitStrategy es : this.exitStrategies) {
			clone.addAlgoComponent(es.clone());
		}

		return clone;
	}

	protected void plot(Serie s, String title) {
		this.plots.add(new PlotSerie(s, title));
	}

	protected void plot(Serie s, String title, Color c) {
		this.plots.add(new PlotSerie(s, title, c));
	}

	protected void plot(Serie s, String title, Color c, boolean dashed) {
		this.plots.add(new PlotSerie(s, title, c, dashed));
	}

	public void plot(SerieItem si) {
		if (signals == null) {
			signals = new Serie();
			this.plots.add(new PlotSerie(signals, "Signals", Color.GREEN));
		}
		signals.put(si);
	}

	protected void plot(ZigZagRelative z) {
		this.plots.add(new PlotZigZag(z));
	}

	public void addAlgoComponent(AlgoComponent algoComponent) {
		algoComponent.linkAlgo(this);
		if (algoComponent instanceof EntryCondition) {
			entryConditions.add((EntryCondition) algoComponent);
		} else if (algoComponent instanceof ExitStrategy) {
			exitStrategies.add((ExitStrategy) algoComponent);
		}
	}

	boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		for (EntryCondition ec : entryConditions) {
			if (!ec.canEnter(hd, bor)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Executed post entry filters
	 * 
	 * @param hd
	 * @param bor
	 */
	protected abstract void processToday(HistoricalData hd, BookOfRecord bor);

	/**
	 * Executed prior to entry filters
	 * 
	 * @param hd
	 * @param bor
	 */
	protected abstract void manageOrders(HistoricalData hd, BookOfRecord bor);

	protected void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (ExitStrategy es : exitStrategies) {
			es.managePositions(hd, bor);
		}
	}

	protected abstract void setUp(HistoricalData hd, BookOfRecord bor);

	protected abstract int minPeriods();

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public List<EntryCondition> getEntryConditions() {
		return this.entryConditions;
	}

	public List<ExitStrategy> getExitStrategies() {
		return exitStrategies;
	}

	public String getGroup() {
		return group;
	}

	public String getGenoType() {
		TreeSet<String> genotype = new TreeSet<String>();
		for (EntryCondition ec : getEntryConditions()) {
			genotype.add(ec.getName());
		}
		for (ExitStrategy es : getExitStrategies()) {
			genotype.add(es.getName());
		}

		String type = "";
		for (String s : genotype) {
			type += s;
		}

		return type;
	}
	
	@Override
	public String toString() {
		String s = getName();
		for (EntryCondition ec : getEntryConditions()) {
			s += "-" + ec.toString();
		}
		for (ExitStrategy es : getExitStrategies()) {
			s += "--" + es.toString();
		}
		return s;
	}

}
