package com.celatum.trading;

import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;

public class StopLongOrder extends LongOrder {

	public StopLongOrder(Instrument instrument, String group, Date dateCreated, double priceLevel) {
		super(instrument, group, dateCreated, EntryType.STOPLONG, priceLevel, Double.MAX_VALUE);
	}

	@Override
	public Position hasExecuted(HistoricalData hd) {
		Position p = null;
		if (entryPrice > hd.askHigh.get(0)) {
			// do nothing
		} else if (entryPrice <= hd.askOpen.get(0)) {
			p = new LongPosition(this, hd.askOpen.get(0), hd);
		} else {
			p = new LongPosition(this, entryPrice, hd);
		}
		return p;
	}

}
