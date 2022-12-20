package com.celatum.data;

import java.util.TreeMap;

public class Instrument implements Comparable<Instrument> {
	private static TreeMap<String, Instrument> instrumentCache = new TreeMap<>();
	private String name;
	private String epic;
	private String expiry;
	private String type;
	private String chartCode; // e.g. CSGN
	private String newsCode; // e.g. CSGN.VX
	private String avCode;
	private boolean ig_data_available = true;
	private int ig_uk_multiplier = 1;
	public MarginFactorData marginFactor;

	private Instrument(String name, String epic, String expiry) {
		this.name = name;
		this.epic = epic;
		this.expiry = expiry;
	}

	public static Instrument getInstrument(String name, String epic, String expiry) {
		Instrument result = instrumentCache.get(name);
		if (result == null) {
			result = new Instrument(name, epic, expiry);
			instrumentCache.put(name, result);
		}
		return result;
	}

	static TreeMap<String, Instrument> getInstrumentCache() {
		return instrumentCache;
	}

	public void println() {
		System.out.printf("%-30.30s  %-30.30s  %-30.30s  %-30.30s%n", epic, name, expiry,
				marginFactor.getDepositFactorPercent());
	}

	public String getName() {
		return name;
	}

	public void setChartCode(String chartCode) {
		this.chartCode = chartCode;
	}

	public void setNewsCode(String newsCode) {
		this.newsCode = newsCode;
	}

	public String getEpic() {
		return epic;
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

	public String getChartCode() {
		return chartCode;
	}

	public String getNewsCode() {
		return newsCode;
	}

	public String getAVCode() {
		return avCode;
	}

	public void setAVCode(String avCode) {
		this.avCode = avCode;
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

	@Override
	public int compareTo(Instrument o) {
		return this.name.compareTo(o.name);
	}
}
