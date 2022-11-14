package com.celatum.data;

public class Instrument {
	private String name;
	private String epic;
	private String expiry;
	private String type;
	public MarginFactorData marginFactor;

	public Instrument(String name, String epic, String expiry) throws Exception {
		this.name = name;
		this.epic = epic;
		this.expiry = expiry;
		this.marginFactor = IGConnector.getMarginFactor(this);
	}

	public Instrument(String name) {
		this.name = name;
	}

	public void println() {
		System.out.printf("%-30.30s  %-30.30s  %-30.30s  %-30.30s%n", epic, name, expiry,
				marginFactor.getDepositFactorPercent());
	}

	public String getName() {
		return name;
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
}
