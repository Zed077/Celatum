package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.ZigZagRelative;

public class HigherHighs extends EntryCondition {
	private Method method = Method.ADP;
	private Serie percent;
	private int period = 70;
	private double breath = 3;

	public enum Method {
		ADP, SDP
	}

	public HigherHighs() {
	}

	public HigherHighs(Method method, int period, double breath) {
		this.method = method;
		this.period = period;
		this.breath = breath;
	}

	public void setUp(HistoricalData hd) {
		switch (method) {
		case ADP:
			percent = Calc.atrPercent(hd, period);
			break;
		case SDP:
			percent = Calc.standardDeviationPercent(hd, period);
			break;
		}
		hd.syncReferenceIndex(percent);
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		ZigZagRelative zz = new ZigZagRelative(hd, percent.get(0) * breath);
		return zz.getHighs().size() >= 2 && zz.getHighs().getItem(0).getValue() > zz.getHighs().getItem(1).getValue();
	}

	@Override
	public String toString() {
		return "HH/" + method + period + breath;
	}

	@Override
	public EntryCondition clone() {
		HigherHighs clone = new HigherHighs(method, period, breath);
		return clone;
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();
		
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 1));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 2));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 3));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 4));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 70, 5.5));

		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 1));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 2));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 3));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 4));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 20, 5.5));

		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 1));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 2));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 3));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 4));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 5));
		set.add(new HigherHighs(HigherHighs.Method.ADP, 200, 5.5));

		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 1));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 2));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 3));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 4));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 70, 5.5));

		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 1));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 2));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 3));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 4));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 20, 5.5));

		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 1));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 1.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 2));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 2.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 3));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 3.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 4));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 4.5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 5));
		set.add(new HigherHighs(HigherHighs.Method.SDP, 200, 5.5));
		
		return set;
	}
}