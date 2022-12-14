package com.celatum.data;

import java.util.Date;

import com.celatum.BookOfRecord;
import com.celatum.algos.Algo;

public class AlgoRunData {
	protected int id;
	private String description;
	private Date runDatetime;
	private Date dataFirstDate;
	private Date dataLastDate;
	private String stats;

	public AlgoRunData(Algo algo, BookOfRecord bookOfRecord) {
		this.description = algo.toString();
		this.runDatetime = new Date();
		this.dataFirstDate = bookOfRecord.profitAndLoss.oldestDate();
		this.dataLastDate = bookOfRecord.profitAndLoss.newestDate();
		this.stats = bookOfRecord.getStats(dataFirstDate);
	}
	
	public AlgoRunData(String description, Date runDatetime, Date dataFirstDate, Date dataLastDate, String stats) {
		this.description = description;
		this.runDatetime = runDatetime;
		this.dataFirstDate = dataFirstDate;
		this.dataLastDate = dataLastDate;
		this.stats = stats;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Date getRunDatetime() {
		return runDatetime;
	}

	public Date getDataFirstDate() {
		return dataFirstDate;
	}

	public Date getDataLastDate() {
		return dataLastDate;
	}

	public String getStats() {
		return stats;
	}

}
