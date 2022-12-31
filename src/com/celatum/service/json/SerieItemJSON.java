package com.celatum.service.json;

import java.util.ArrayList;
import java.util.Collection;

import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

public class SerieItemJSON {
	private String date;
	private double value;
	
	public SerieItemJSON(SerieItem si) {
		this.date = si.getDate().toString();
		this.value = si.getValue();
	}
	
	public static Collection<SerieItemJSON> serialize(Serie s) {
		ArrayList<SerieItemJSON> res = new ArrayList<>();
		for (SerieItem si : s.getAllEntries()) {
			res.add(new SerieItemJSON(si));
		}
		return res;
	}

	public String getDate() {
		return date;
	}

	public double getValue() {
		return value;
	}
	
}
