package com.celatum.service.json;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "runDate", "type", "algoDescription", "algoStatistics", "algoRunRef" })
public class AlgoRun {
	private String algoRunRef;
	private Date runDate;
	private String type;
	private String algoDescription;
	private String algoStatistics;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");

	public AlgoRun(String algoRunRef, Date runDate, String type, String algoDescription, String algoStatistics) {
		this.algoRunRef = algoRunRef;
		this.runDate = runDate;
		this.type = type;
		this.algoDescription = algoDescription;
		this.algoStatistics = algoStatistics;
	}

	public String getAlgoRunRef() {
		return algoRunRef;
	}

	public String getRunDate() {
		return DATE_FORMAT.format(runDate);
	}

	public String getType() {
		return type;
	}

	public String getAlgoDescription() {
		return algoDescription;
	}

	public String getAlgoStatistics() {
		return algoStatistics;
	}
	
}
