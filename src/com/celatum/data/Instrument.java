package com.celatum.data;

import java.util.Date;
import java.util.TreeMap;

public class Instrument implements Comparable<Instrument> {
	/**
	 * chartCode; // e.g. CSGN
	 * newsCode; // e.g. CSGN.VX
	 * @author cedric.ladde
	 *
	 */
	public enum Source {
		IG_CHART_CODE, IG_NEWS_CODE, IG_EPIC, AV_CODE
	}

	private static TreeMap<String, Instrument> name_instrumentCache = new TreeMap<>();
	private static TreeMap<String, Instrument> code_instrumentCache = new TreeMap<>();
	private static TreeMap<String, Source> code_sourceCache = new TreeMap<>();
	private TreeMap<Source, String> codes = new TreeMap<>();
	private String name;
	private String expiry;
	private String type;
	private boolean ig_data_available = true;
	private int ig_uk_multiplier = 1;
	private Date lastUpdated;
	public MarginFactorData marginFactor;

	private Instrument(String name) {
		this.name = name;
	}

	/**
	 * Creates an empty instrument if the instrument does not already exist
	 * @param name
	 * @return
	 */
	public static Instrument getInstrumentByName(String name) {
		name = name.replaceAll("'", "").replaceAll("&", "n").replaceAll("€", "E");

		Instrument result = name_instrumentCache.get(name);
		if (result == null) {
			result = new Instrument(name);
			name_instrumentCache.put(name, result);
		}
		return result;
	}
	
	/**
	 * Returns null of the instrument does not exist
	 * @param code
	 * @return
	 */
	public static Instrument getInstrumentByCode(String code) {
		return code_instrumentCache.get(code);
	}
	
	public static Source getSource(String code) {
		return code_sourceCache.get(code);
	}

	static TreeMap<String, Instrument> getInstrumentCache() {
		return name_instrumentCache;
	}

	public void println() {
		System.out.printf("%-30.30s  %-30.30s  %-30.30s  %-30.30s%n", codes.get(Source.IG_EPIC), name, expiry,
				marginFactor.getDepositFactorPercent());
	}

	public String getName() {
		return name;
	}
	
	public void setCode(Source s, String code) {
		codes.put(s, code);
		code_instrumentCache.put(code, this);
		code_sourceCache.put(code, s);
	}
	
	public String getCode(Source s) {
		return codes.get(s);
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getExpiry() {
		return expiry;
	}

	public String getType() {
		return type;
	}

	void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return getName();
	}

	public boolean isIGDataAvailable() {
		return ig_data_available;
	}

	public void setIGDataAvailable(boolean ig_data_available) {
		this.ig_data_available = ig_data_available;
	}

	public int getIGUKMultiplier() {
		return ig_uk_multiplier;
	}

	public void setIGUKMultiplier(int ig_uk_multiplier) {
		this.ig_uk_multiplier = ig_uk_multiplier;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public int compareTo(Instrument o) {
		return this.name.compareTo(o.name);
	}
}
