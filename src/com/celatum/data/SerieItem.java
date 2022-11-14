package com.celatum.data;

import java.util.Date;

public class SerieItem implements Comparable<SerieItem> {
	private double value;
	private Date date;
	
	public SerieItem(Date date, double value) {
		this.value = value;
		this.date = date;
	}

	public double getValue() {
		return value;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public int compareTo(SerieItem o) {
		return this.date.compareTo(o.date);
	}
	
	@Override
	public String toString() {
		return date.toString() + " " + value;
	}
	
	@Override
	public boolean equals(Object obj) {
		SerieItem comp = (SerieItem) obj;
		return comp.date.equals(this.date) && comp.value == this.value;
	}
	
}
