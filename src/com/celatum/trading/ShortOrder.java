package com.celatum.trading;

import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;

public class ShortOrder extends Order {

	public ShortOrder(Instrument instrument, String group, Date dateCreated, double priceLevel) {
		super(instrument, group, dateCreated, EntryType.SHORT, priceLevel, -Double.MAX_VALUE);
	}

	protected ShortOrder(Instrument instrument, String group, Date dateCreated, EntryType entryType, double priceLevel,
			double limit) {
		super(instrument, group, dateCreated, entryType, priceLevel, limit);
	}

	@Override
	public Position hasExecuted(HistoricalData hd) {
		Position p = null;
		if (entryPrice > hd.bidHigh.get(0)) {
			// do nothing
		} else if (entryPrice <= hd.bidOpen.get(0)) {
			p = new ShortPosition(this, hd.bidOpen.get(0), hd);
		} else {
			p = new ShortPosition(this, entryPrice, hd);
		}
		return p;
	}
	
	@Override
	protected double validateStop(double currentPrice, double stopPrice, boolean autocorrect) {
		double newStop = stopPrice;
		double space = instrument.marginFactor.getMinControlledRiskStopDistance();
		if (stopPrice < currentPrice + space) {
			newStop = currentPrice + space;
		}

		if (newStop != stopPrice && !autocorrect) {
			throw new RuntimeException("Stop must be superior to entry + space " + stopPrice + " > " + (currentPrice + space));
		}
		return newStop;
	}

}
