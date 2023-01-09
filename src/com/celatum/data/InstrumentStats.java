package com.celatum.data;

import com.celatum.maths.ATR;
import com.celatum.maths.ATRPercent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties({ "instrumentCode", "stopLossDistancePercent", "maxLossPercent", "maxContractsStd", "minContractsStd",
		"atrPercent", "maxNotionalStd", "maxNotionalATR", "contractSize", "accountSize", "bidAskSpreadPoints", "commission"})
@JsonPropertyOrder({ "instrumentName", "minContractsATR", "maxContractsATR", "stopDistanceATR", "latestPrice" })
public class InstrumentStats {
	private String instrumentName;
	private String instrumentCode;
	private double stopLossDistancePercent = 0.5 / 100.0;
	private double maxLossPercent = 10.0 / 100.0;
	private double maxContractsStd;
	private double minContractsStd;
	private double maxContractsATR;
	private double minContractsATR;
	private double atrPercent;
	private double maxNotionalStd;
	private double maxNotionalATR;
	private double contractSize;
	private double accountSize;
	private double latestPrice;
	private double stopDistanceATR;

	InstrumentStats(HistoricalData hd) {
		hd.resetReferenceIndex();
		latestPrice = hd.midClose.get(0);
		accountSize = IGConnector.getAccountBalance();

		maxNotionalStd = accountSize * maxLossPercent / stopLossDistancePercent;
		contractSize = hd.instrument.marginFactor.getContractSize();

		maxContractsStd = maxNotionalStd / (latestPrice * contractSize);
		minContractsStd = maxContractsStd / 4;

		atrPercent = ATRPercent.calc(hd, 30, ATR.Method.SMA).get(0);
		maxNotionalATR = accountSize * maxLossPercent / (atrPercent / 4);
		maxContractsATR = maxNotionalATR / (latestPrice * contractSize);
		minContractsATR = maxContractsATR / 4;

		stopDistanceATR = ATR.calc(hd, 30, ATR.Method.SMA).get(0);

		instrumentName = hd.instrument.getName();
		instrumentCode = hd.getCode();
	}
	
	/**
	 * Used to load from database
	 * @param instrumentName
	 * @param instrumentCode
	 * @param maxContractsATR
	 * @param minContractsATR
	 * @param accountSize
	 * @param latestPrice
	 * @param stopDistanceATR
	 */
	InstrumentStats(String instrumentName, String instrumentCode, double maxContractsATR, double minContractsATR,
			double accountSize, double latestPrice, double stopDistanceATR) {
		this.instrumentName = instrumentName;
		this.instrumentCode = instrumentCode;
		this.maxContractsATR = maxContractsATR;
		this.minContractsATR = minContractsATR;
		this.accountSize = accountSize;
		this.latestPrice = latestPrice;
		this.stopDistanceATR = stopDistanceATR;
	}

	private static double round(double value, int precision) {
		return Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision);
	}

	public double getStopLossDistancePercent() {
		return round(stopLossDistancePercent, 1);
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public double getMaxLossPercent() {
		return maxLossPercent;
	}

	public double getMaxContractsStd() {
		return round(maxContractsStd, 1);
	}

	public double getMinContractsStd() {
		return round(minContractsStd, 1);
	}

	public double getMaxContractsATR() {
		return round(maxContractsATR, 1);
	}

	public double getMinContractsATR() {
		return round(minContractsATR, 1);
	}

	public double getAtrPercent() {
		return round(atrPercent * 100.0, 2);
	}

	public double getMaxNotionalStd() {
		return Math.round(maxNotionalStd);
	}

	public double getMaxNotionalATR() {
		return Math.round(maxNotionalATR);
	}

	public double getContractSize() {
		return contractSize;
	}

	public double getAccountSize() {
		return accountSize;
	}

	public double getLatestPrice() {
		return latestPrice;
	}

	public double getStopDistanceATR() {
		return round(stopDistanceATR, 2);
	}

	public String getInstrumentCode() {
		return instrumentCode;
	}
}
