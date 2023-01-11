package com.celatum;

import java.text.NumberFormat;
import java.util.Date;

import com.celatum.algos.Algo;

public class Print {
	private static NumberFormat percentFormat = NumberFormat.getPercentInstance();
	private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
	
	static {
		percentFormat.setMaximumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(0);
	}
	
	public static String shortAlgo(Algo algo, BookOfRecord bor) {
		Date today = new Date();
		double score = bor.returnAbsolute(today);
		double RoI = bor.returnPercent(today);
		double lowestPnL = bor.lowestPnL();
		double nPositions = bor.getAllPositions(today).size();
		
		return algo.toString() + " " + nPositions + " " + numberFormat.format(lowestPnL) + " "
				+ numberFormat.format(score) + " " + percentFormat.format(RoI);
	}
	
	public static void printShortAlgo(Algo algo, BookOfRecord bor) {
		System.out.println(shortAlgo(algo, bor));
	}
}
