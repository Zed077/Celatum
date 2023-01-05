package com.celatum.service.json;

import java.util.Date;

public class AlgoRunPosition {
	private String algoRunRef;
	private Date entryDate;
	private double entryPrice;
	private Date closeDate;
	private double closePrice;
	private String entryDesc;
	private String closeDesc;
	private String instrumentName;

	public AlgoRunPosition(String algoRunRef, Date entryDate, double entryPrice, Date closeDate, double closePrice,
			String instrumentName, String entryDesc, String closeDesc) {
		this.algoRunRef = algoRunRef;
		this.entryDate = entryDate;
		this.entryPrice = entryPrice;
		this.closeDate = closeDate;
		this.closePrice = closePrice;
		this.instrumentName = instrumentName;
		this.entryDesc = entryDesc;
		this.closeDesc = closeDesc;
	}

	public String getAlgoRunRef() {
		return algoRunRef;
	}

	public String getEntryDate() {
		return entryDate.toString();
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public String getCloseDate() {
		if (closeDate == null) {
			return null;
		} else {
			return closeDate.toString();
		}
	}

	public double getClosePrice() {
		return closePrice;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public String getEntryDesc() {
		return entryDesc;
	}

	public String getCloseDesc() {
		return closeDesc;
	}

}
