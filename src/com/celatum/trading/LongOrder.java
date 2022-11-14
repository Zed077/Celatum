package com.celatum.trading;

import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;

public class LongOrder extends Order {

	public LongOrder(Instrument instrument, String group, Date dateCreated, double priceLevel) {
		super(instrument, group, dateCreated, EntryType.LONG, priceLevel, Double.MAX_VALUE);
	}
	
	protected LongOrder(Instrument instrument, String group, Date dateCreated, EntryType entryType, double priceLevel,
			double limit) {
		super(instrument, group, dateCreated, entryType, priceLevel, limit);
	}

	@Override
	public Position hasExecuted(HistoricalData hd) {
		Position p = null;
		if (entryPrice < hd.askLow.get(0)) {
			// do nothing
		} else if (entryPrice >= hd.askOpen.get(0)) {
			p = new LongPosition(this, hd.askOpen.get(0), hd);
		} else {
			p = new LongPosition(this, entryPrice, hd);
		}
		return p;
	}

	@Override
	protected double validateStop(double currentPrice, double stopPrice, boolean autocorrect) {
		double newStop = stopPrice;
		double space = instrument.marginFactor.getMinControlledRiskStopDistance();
		if (stopPrice > currentPrice - space) {
			newStop = currentPrice - space;
		}

		if (newStop != stopPrice && !autocorrect) {
			throw new RuntimeException(
					"Stop must be inferior to entry - space " + stopPrice + " < " + (currentPrice - space));
		}
		return newStop;
	}

}
