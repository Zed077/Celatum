package com.celatum.algos.entry;

import java.util.HashSet;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;

public class NoViolentMoveDown extends EntryCondition {
	private int days = 5;
	private Method method = Method.ATR;
	private Serie atr;
	private Serie adp;
	private Serie sdp;
	private static final int PERIOD = 70;
	private static final double DEV_BREATH = 3;

	public enum Method {
		ATR, ADP, SDP
	}

	public NoViolentMoveDown() {
	}

	public NoViolentMoveDown(int days) {
		this.days = days;
	}

	public NoViolentMoveDown(Method method, int days) {
		this.days = days;
		this.method = method;
	}

	public void setUp(HistoricalData hd) {
		switch (method) {
		case ATR:
			atr = Calc.atr(hd, PERIOD);
			hd.syncReferenceIndex(atr);
			break;
		case ADP:
			adp = Calc.atrPercent(hd, PERIOD);
			hd.syncReferenceIndex(adp);
			break;
		case SDP:
			sdp = Calc.standardDeviationPercent(hd, PERIOD);
			hd.syncReferenceIndex(sdp);
			break;
		}
	}

	public boolean canEnter(HistoricalData hd, BookOfRecord bor) {
		double highest = 0;
		for (int i = 0; i < days; i++) {
			highest = Math.max(highest, hd.midHigh.get(i));
		}

		switch (method) {
		case ATR:
			return highest - hd.midClose.get(0) < DEV_BREATH * atr.get(0);
		case ADP:
			double adpPercent = DEV_BREATH * adp.get(0);
			return hd.midClose.get(0) / highest > 1 - adpPercent;
		case SDP:
			double sdpPercent = DEV_BREATH * sdp.get(0);
			return hd.midClose.get(0) / highest > 1 - sdpPercent;
		}
		
		throw new RuntimeException("Should not have executed this code");
	}

	@Override
	public String toString() {
		return "NVMD/" + method + days;
	}

	public EntryCondition clone() {
		return new NoViolentMoveDown(method, days);
	}

	@Override
	public Set<EntryCondition> generateVariants() {
		HashSet<EntryCondition> set = new HashSet<EntryCondition>();
		
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 3));
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 4));
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 5));
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ADP, 10));

//		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ATR, 3));
//		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ATR, 5));
//		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.ATR, 10));

		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.SDP, 3));
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.SDP, 5));
		set.add(new NoViolentMoveDown(NoViolentMoveDown.Method.SDP, 10));
		
		return set;
	}
}
