package com.celatum.maths;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.celatum.data.HistoricalData;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

public class Calc {
	
	public static Serie sma(Serie source, int nPeriods) {
		return SMA.calc(source, nPeriods);
	}

	public static Serie ema(Serie source, int nPeriods) {
		return EMA.calc(source, nPeriods);
	}

	public static Serie mid(Serie a, Serie b) {
		return Mid.calc(a, b);
	}

	public static Serie trueRange(HistoricalData hd) {
		return TrueRange.calc(hd, TrueRange.Method.Absolute);
	}

	public static Serie atrPercent(HistoricalData hd, int nPeriods) {
		return ATRPercent.calc(hd, nPeriods, ATR.Method.SMA);
	}

	public static Serie standardDeviationPercent(HistoricalData hd, int nPeriods) {
		return SDP.calc(hd, nPeriods);
	}

	/**
	 * 
	 * @param hd
	 * @param nPeriods
	 * @return Average True Range
	 */
	public static Serie atr(HistoricalData hd, int nPeriods) {
		return ATR.calc(hd, nPeriods, ATR.Method.Smoothed);
	}

	public static double twoPointsRegression(SerieItem p1, SerieItem p2, SerieItem offset, Date projectionDate) {
		// Regress highs, offset to low
		double alpha = (p1.getValue() - p2.getValue()) / (p1.getDate().getTime() - p2.getDate().getTime());
		double beta = offset.getValue() - alpha * offset.getDate().getTime();
		return alpha * projectionDate.getTime() + beta;
	}

	public static double twoPointsRegression(SerieItem p1, SerieItem p2, Date projectionDate) {
		// Regress highs, no offset
		double alpha = (p1.getValue() - p2.getValue()) / (p1.getDate().getTime() - p2.getDate().getTime());
		double beta = p1.getValue() - alpha * p1.getDate().getTime();
		return alpha * projectionDate.getTime() + beta;
	}
	
	public static long daysDifference(Date start, Date end) {
		long diff = end.getTime() - start.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	/**
	 * ATR length is 10 Based on close price
	 * 
	 * @param source
	 * @param nPeriods
	 * @param multiplier
	 * @return
	 */
	public static Serie[] keltnerChannel(HistoricalData hd, int nPeriods, double multiplier) {
		return KeltnerChannel.calc(hd, nPeriods, multiplier);
	}

	public static double reverseCompound(double startValue, double endValue, double period) {
		return Math.pow(endValue / startValue, 1.0 / period) - 1;
	}
}
