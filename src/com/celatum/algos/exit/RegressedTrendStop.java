package com.celatum.algos.exit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;
import com.celatum.maths.LinearRegression;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Position;

public class RegressedTrendStop extends ExitStrategy {
	private int period = 50;
	private double rateOfChangePercent = 0.1;

	public RegressedTrendStop() {
	}

	public RegressedTrendStop(int period, double rateOfChangePercent) {
		this.period = period;
		this.rateOfChangePercent = rateOfChangePercent;
	}

	@Override
	public void managePositions(HistoricalData hd, BookOfRecord bor) {
		List<Position> positions = bor.getActivePositions(hd.instrument, hd.getReferenceDate(), algo.getGroup());
		ArrayList<Position> longs = new ArrayList<Position>();
		ArrayList<Position> shorts = new ArrayList<Position>();
		
		for (Position p : positions) {
			if (p instanceof LongPosition) {
				longs.add(p);
			} else {
				shorts.add(p);
			}
		}
		
		manageLongs(hd, bor, longs);
		manageShorts(hd, bor, shorts);
	}

	private void manageLongs(HistoricalData hd, BookOfRecord bor, List<Position> positions) {
		if (positions.size() == 0)
			return;

		// Regression over last 50 days
		LinearRegression lr = new LinearRegression(hd.midClose, period);
		boolean trendUp = lr.getPercentYearlyRateOfChange() >= rateOfChangePercent;

		for (Position p : positions) {
			if (!trendUp) {
				double currentStop = p.getStop();
				double proposedStop = hd.bidLow.get(0);
				double newStop = Math.max(currentStop, proposedStop);
				p.setStop(newStop);
			}
		}
	}

	private void manageShorts(HistoricalData hd, BookOfRecord bor, List<Position> positions) {
		if (positions.size() == 0)
			return;

		// Regression over last 50 days
		LinearRegression lr = new LinearRegression(hd.midClose, period);
		boolean trendUp = lr.getPercentYearlyRateOfChange() <= -rateOfChangePercent;

		for (Position p : positions) {
			if (!trendUp) {
				double currentStop = p.getStop();
				double proposedStop = hd.askHigh.get(0);
				double newStop = Math.min(currentStop, proposedStop);
				p.setStop(newStop);
			}
		}
	}

	@Override
	public String toString() {
		return "RTS/" + period + rateOfChangePercent;
	}

	@Override
	public ExitStrategy clone() {
		RegressedTrendStop clone = new RegressedTrendStop(period, rateOfChangePercent);
		return clone;
	}

	@Override
	public void setUp(HistoricalData hd) {
	}

	@Override
	public Set<ExitStrategy> generateVariants() {
		HashSet<ExitStrategy> set = new HashSet<ExitStrategy>();
		
		set.add(new RegressedTrendStop(20, 0.1));
		set.add(new RegressedTrendStop(50, 0.1));
		set.add(new RegressedTrendStop(70, 0.1));
		set.add(new RegressedTrendStop(200, 0.1));
		
		set.add(new RegressedTrendStop(20, 0.05));
		set.add(new RegressedTrendStop(50, 0.05));
		set.add(new RegressedTrendStop(70, 0.05));
		set.add(new RegressedTrendStop(200, 0.05));
		
		set.add(new RegressedTrendStop(20, 0.2));
		set.add(new RegressedTrendStop(50, 0.2));
		set.add(new RegressedTrendStop(70, 0.2));
		set.add(new RegressedTrendStop(200, 0.2));
		
		return set;
	}

}
