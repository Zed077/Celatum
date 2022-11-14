package com.celatum.trading;

import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;

public class StopShortOrder extends ShortOrder {

	public StopShortOrder(Instrument instrument, String group, Date dateCreated, double priceLevel) {
		super(instrument, group, dateCreated, EntryType.STOPSHORT, priceLevel, -Double.MAX_VALUE);
	}
	
	@Override
	public Position hasExecuted(HistoricalData hd) {
		Position p = null;
		if (entryPrice < hd.bidLow.get(0)) {
			// do nothing
		} else if (entryPrice >= hd.bidOpen.get(0)) {
			p = new ShortPosition(this, hd.bidOpen.get(0), hd);
		} else {
			p = new ShortPosition(this, entryPrice, hd);
		}
		return p;
	}

}
