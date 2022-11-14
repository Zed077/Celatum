package com.celatum.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.HistoricalData;
import com.celatum.data.IGConnector;
import com.celatum.data.Instrument;
import com.celatum.data.Serie;
import com.celatum.maths.ATR;
import com.celatum.maths.BollingerBands;
import com.celatum.maths.RSI;
import com.celatum.maths.SuperTrend;

class BollingerBandsTest {
	private GregorianCalendar gc = new GregorianCalendar();
	private Serie close = new Serie();

	@BeforeEach
	void setUp() throws Exception {
		Date d;
		
		gc.set(2009, 4, 1, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 86.16);
		
		gc.set(2009, 4, 4, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.09);
		
		gc.set(2009, 4, 5, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 88.78);
		
		gc.set(2009, 4, 6, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.32);
		
		gc.set(2009, 4, 7, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.07);
		
		gc.set(2009, 4, 8, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 91.15);
		
		gc.set(2009, 4, 11, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.44);
		
		gc.set(2009, 4, 12, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.18);
		
		gc.set(2009, 4, 13, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 86.93);
		
		gc.set(2009, 4, 14, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 87.68);
		
		gc.set(2009, 4, 15, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 86.96);
		
		gc.set(2009, 4, 18, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.43);
		
		gc.set(2009, 4, 19, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.32);
		
		gc.set(2009, 4, 20, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 88.72);
		
		gc.set(2009, 4, 21, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 87.45);
		
		gc.set(2009, 4, 22, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 87.26);
		
		gc.set(2009, 4, 26, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.50);
		
		gc.set(2009, 4, 27, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 87.90);
		
		gc.set(2009, 4, 28, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.13);
		
		gc.set(2009, 4, 29, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.70);
		
		gc.set(2009, 5, 1, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.90);
		
		gc.set(2009, 5, 2, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.98);
		
		gc.set(2009, 5, 3, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 91.80);
		
		gc.set(2009, 5, 4, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.66);
		
		gc.set(2009, 5, 5, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.68);
		
		gc.set(2009, 5, 8, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.30);
		
		gc.set(2009, 5, 9, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.77);
		
		gc.set(2009, 5, 10, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.54);
		
		gc.set(2009, 5, 11, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 92.95);
		
		gc.set(2009, 5, 12, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 93.20);
		
		gc.set(2009, 5, 15, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 91.07);
		
		gc.set(2009, 5, 16, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.83);
		
		gc.set(2009, 5, 17, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 89.74);
		
		gc.set(2009, 5, 18, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.40);
		
		gc.set(2009, 5, 19, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.74);
		
		gc.set(2009, 5, 22, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 88.02);
		
		gc.set(2009, 5, 23, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 88.09);
		
		gc.set(2009, 5, 24, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 88.84);
		
		gc.set(2009, 5, 25, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.78);
		
		gc.set(2009, 5, 26, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.54);
		
		gc.set(2009, 5, 29, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 91.39);
		
		gc.set(2009, 5, 30, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 90.65);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testBollingerBands() {
		BollingerBands bb = BollingerBands.calc(close, 20, 2);
		bb.println();
		assertEquals(23, bb.lower.size());
		assertEquals(94.15, Math.round(bb.upper.get(0) * 100.0) / 100.0);
		assertEquals(87.95, Math.round(bb.lower.get(0) * 100.0) / 100.0);
	}
	
}
