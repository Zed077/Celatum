package com.celatum.data;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Item 0 is newest
 * 
 * @author cedric.ladde
 *
 */
public class Serie implements Cloneable {
	/**
	 * In Ascending order
	 */
	private TreeMap<Date, SerieItem> dataset = new TreeMap<Date, SerieItem>();
	/**
	 * In Descending order
	 */
	private ArrayList<SerieItem> cache = new ArrayList<SerieItem>(10000);
	private int referenceIndex = 0;
	private int hashCode = 0;

	public void put(SerieItem si) {
		dataset.put(si.getDate(), si);
		if (cache.size() == 0) {
			cache.add(si);
		} else if (si.getDate().compareTo(cache.get(0).getDate()) > 0) {
			cache.add(0, si);
		} else if (si.getDate().compareTo(cache.get(cache.size() - 1).getDate()) < 0) {
			cache.add(si);
		} else {
			cacheValues();
		}
		hashCode = 0;
	}

	public void put(Date date, double value) {
		SerieItem si = new SerieItem(date, value);
		this.put(si);
	}
	
	public void removeOldest() {
		dataset.remove(dataset.firstEntry().getKey());
		cacheValues();
	}

	/**
	 * Return value for this entry
	 * @param i
	 * @return
	 */
	public double get(int i) {
		return cache.get(index(i)).getValue();
	}
	
	public double get(Date d) {
		return dataset.get(d).getValue();
	}

	public Date getDate(int i) {
		return cache.get(index(i)).getDate();
	}

	/**
	 * Not synchronised!! Only use after processing as read only
	 * @return dates in ascending order
	 */
	public Date[] getAllDates() {
		return dataset.keySet().toArray(new Date[dataset.size()]);
	}
	
	/**
	 * 
	 * @return all entries in ascending date order
	 */
	public Collection<SerieItem> getAllEntries() {
		return dataset.values();
	}

	public SerieItem getItem(int i) {
		return cache.get(index(i));
	}

	public int size() {
		if (referenceIndex < 0) {
			return 0;
		}
		return dataset.size() - referenceIndex;
	}

	public int fullSize() {
		return dataset.size();
	}

	/**
	 * TODO Should take into account the reference
	 * 
	 * @return
	 */
	public Date newestDate() {
		return dataset.lastKey();
	}

	/**
	 * TODO Should take into account the reference
	 * 
	 * @return
	 */
	public Date oldestDate() {
		return dataset.firstKey();
	}

	public SerieItem getMaxSince(Date d, int lastIndex) {
		SerieItem max = null;
		for (int i = lastIndex; i < this.size(); i++) {
			SerieItem cursor = this.getItem(i);
			if (cursor.getDate().compareTo(d) >= 0 && (max == null || cursor.getValue() > max.getValue())) {
				max = cursor;
			}
		}
		return max;
	}

	public SerieItem getMinSince(Date d, int lastIndex) {
		SerieItem min = null;
		for (int i = lastIndex; i < this.size(); i++) {
			SerieItem cursor = this.getItem(i);
			if (cursor.getDate().compareTo(d) >= 0 && (min == null || cursor.getValue() < min.getValue())) {
				min = cursor;
			}
		}
		return min;
	}

	public void setReferenceDate(Date referenceDate) {
		Entry<Date, SerieItem> entry = dataset.floorEntry(referenceDate);
		if (entry != null) {
			SerieItem refItem = entry.getValue();
			this.referenceIndex = cache.indexOf(refItem);
		} else {
			this.referenceIndex = -1;
		}
	}

	public void removeReferenceIndex() {
		referenceIndex = 0;
	}

	private void cacheValues() {
		cache = new ArrayList<SerieItem>(10000);
		NavigableMap<Date, SerieItem> dmap = dataset.descendingMap();
		for (SerieItem si : dmap.values()) {
			cache.add(si);
		}
	}

	private int index(int i) {
		return i + referenceIndex;
	}

	public void println() {
		System.out.println("Size = " + size());
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		for (int i = 0; i < cache.size(); i++) {
			System.out.println("[" + i + "] " + cache.get(i).getDate() + " - " + nf.format(cache.get(i).getValue()));
		}
	}

	@Override
	public String toString() {
		String res = "Size = " + size() + "\n";
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		for (int i = 0; i < cache.size(); i++) {
			res += "[" + i + "] " + cache.get(i).getDate() + " - " + nf.format(cache.get(i).getValue()) + "\n";
		}
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		Serie comp = (Serie) obj;
		for (int i = 0; i < cache.size(); i++) {
			if (!comp.cache.get(i).equals(this.cache.get(i)))
				return false;
		}
		
		return true;
	}

	@Override
	public Serie clone() {
		Serie clone = new Serie();
		clone.cache = this.cache;
		clone.dataset = this.dataset;
		return clone;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0 ) {
			hashCode = cache.hashCode();
		}
		return hashCode;
	}
}
