package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

/**
 * For a position that is at least positionAge bars old, provided we are loosing
 * money, set the limit at or just above the entry price.
 * 
 * @author cedric.ladde
 *
 */
public class NotGoneMyWay extends ExitStrategy {
	private int positionAge = 3;
	private double distance = 0.1;
	private Serie adp;

	public NotGoneMyWay() {
	}

	public NotGoneMyWay(int positionAge, double distance) {
		this.positionAge = positionAge;
		this.distance = distance;
	}

	@Override
	public void setUp(HistoricalData hd) {
		List<ExitStrategy> ess = algo.getExitStrategies();
		ess.remove(this);
		ess.add(ess.size(), this);

		adp = ATRPercent.calc(hd, 70, ATR.Method.SMA);
		hd.syncReferenceIndex(adp);
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup())) {
			int da = 0;
			for (; da < hd.size(); da++) {
				if (p.getEntryDate().compareTo(hd.midClose.getDate(da)) >= 0)
					break;
			}

			if (da >= positionAge && p.getDeltaPrice() < 0) {
				double newLimit;
				if (p instanceof LongPosition) {
					newLimit = Math.min(p.getLimit(), p.getEntryPrice() * (1 + adp.get(0) * distance));
				} else {
					newLimit = Math.max(p.getLimit(), p.getEntryPrice() * (1 - adp.get(0) * distance));
				}
				p.setLimit(newLimit);
			}
		}

	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new NotGoneMyWay(3, 0));
		set.add(new NotGoneMyWay(5, 0));
		set.add(new NotGoneMyWay(10, 0));
		set.add(new NotGoneMyWay(20, 0));

		set.add(new NotGoneMyWay(3, 0.1));
		set.add(new NotGoneMyWay(5, 0.1));
		set.add(new NotGoneMyWay(10, 0.1));
		set.add(new NotGoneMyWay(20, 0.1));

		set.add(new NotGoneMyWay(3, 0.5));
		set.add(new NotGoneMyWay(5, 0.5));
		set.add(new NotGoneMyWay(10, 0.5));
		set.add(new NotGoneMyWay(20, 0.5));

		return set;
	}

	@Override
	public String toString() {
		return "NGMW/" + positionAge + distance;
	}

	@Override
	public ExitStrategy clone() {
		return new NotGoneMyWay(positionAge, distance);
	}

}
