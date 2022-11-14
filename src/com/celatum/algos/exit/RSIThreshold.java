package com.celatum.algos.exit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.maths.RSI;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

/**
 * Exit a position after X bars
 * 
 * @author cedric.ladde
 *
 */
public class RSIThreshold extends ExitStrategy {
	private int threshold;
	private boolean above;
	private Serie rsi;

	public RSIThreshold() {

	}

	public RSIThreshold(int threshold, boolean above) {
		this.threshold = threshold;
		this.above = above;
	}

	@Override
	public void setUp(HistoricalData hd) {
		rsi = RSI.calc(hd.midClose, 14);
		hd.syncReferenceIndex(rsi);
		
		List<ExitStrategy> ess = algo.getExitStrategies();
		ess.remove(this);
		ess.add(ess.size(), this);
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		for (Position p : bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup())) {
			double rsiv = rsi.get(0);
			
			if ((above && rsiv > threshold) || (!above && rsiv < threshold)) {
				// exit
				double newLimit;
				if (p instanceof LongPosition) {
					newLimit = 0;
				} else {
					newLimit = p.getEntryPrice() * 10;
				}
				p.setLimit(newLimit);
			}
				
		}
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();

		set.add(new RSIThreshold(50, true));
		set.add(new RSIThreshold(70, true));
		set.add(new RSIThreshold(50, false));
		set.add(new RSIThreshold(30, false));

		return set;
	}

	@Override
	public String toString() {
		return "RSIT/" + threshold + above;
	}

	@Override
	public ExitStrategy clone() {
		return new RSIThreshold(threshold, above);
	}

}
