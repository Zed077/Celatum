package com.celatum.service.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.celatum.data.HistoricalData;
import com.celatum.data.SerieItem;

//{ time: '2018-12-22', open: 75.16, high: 82.84, low: 36.16, close: 45.72 }
public class OHCL {
	private Date time;
	double open;
	double high;
	double close;
	double low;

	public String getTime() {
		return time.toString();
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getClose() {
		return close;
	}

	public double getLow() {
		return low;
	}
	
	public static List<OHCL> serialize(HistoricalData history) {
//		System.out.println("Serializing " + history.getEpic() + " " + history.fullSize() + " elements");
		ArrayList<OHCL> result = new ArrayList<>();
		Iterator<SerieItem> O = history.midOpen.getAllEntries().iterator();
		Iterator<SerieItem> H = history.midHigh.getAllEntries().iterator();
		Iterator<SerieItem> C = history.midClose.getAllEntries().iterator();
		Iterator<SerieItem> L = history.midLow.getAllEntries().iterator();
		while (O.hasNext()) {
			OHCL item = new OHCL();
			SerieItem primer = O.next();
			item.setTime(primer.getDate());
			item.open = primer.getValue();
			item.high = H.next().getValue();
			item.close = C.next().getValue();
			item.low = L.next().getValue();

			result.add(item);
		}
//		System.out.println("Serializing complete");
		return result;
	}
}