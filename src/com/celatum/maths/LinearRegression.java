package com.celatum.maths;

import java.util.Date;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

public class LinearRegression {
	private SimpleRegression regression;
	private Serie pastRegression = new Serie();
	private double firstValue;

	public LinearRegression(Serie serie, int periods) {
		if (periods < 3)
			throw new RuntimeException("Linear regression period must be >= 3 - " + periods);
		
		firstValue = serie.get(periods - 1);

		regression = new SimpleRegression();
		for (int i = 0; i < periods; i++) {
			SerieItem si = serie.getItem(i);
			double time = si.getDate().getTime();
			double price = si.getValue();
			regression.addData(time, price);
		}

		for (int i = 0; i < periods; i++) {
			SerieItem si = serie.getItem(i);
			double value = regression.predict(si.getDate().getTime());
			pastRegression.put(si.getDate(), value);
		}
	}

	public Serie getPastRegression() {
		return pastRegression;
	}
	
	public double predict(Date d) {
		return regression.predict(d.getTime());
	}
	
	/**
	 * @return rate of change per year as an absolute value
	 */
	public double getAbsoluteYearlyRateOfChange() {
		// Rate of change per millisec
		double rmillisec = regression.getSlope();
		double ryear =  rmillisec * 1000*60*60*24*365;
		return ryear;
	}
	
	public double getAbsoluteDailyRateOfChange() {
		// Rate of change per millisec
		double rmillisec = regression.getSlope();
		double rday =  rmillisec * 1000*60*60*24;
		return rday;
	}
	
	/**
	 * @return rate of change per year as a percentage
	 */
	public double getPercentYearlyRateOfChange() {
		// Rate of change per millisec
		double rmillisec = regression.getSlope();
		double ryear =  rmillisec * 1000*60*60*24*365;
		return ryear / firstValue;
	}
	
	public double getIntercept() {
		return regression.getIntercept();
	}
	
	public double getMeanSquareError() {
		return regression.getMeanSquareError();
	}
	
	public double getSlope() {
		return regression.getSlope();
	}
}