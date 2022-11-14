package com.celatum.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.Serie;
import com.celatum.maths.RSI;

class RSITest {
	private GregorianCalendar gc = new GregorianCalendar();
	private Serie close = new Serie();

	@BeforeEach
	void setUp() throws Exception {
		Date d;
		
		gc.set(2009, 11, 14, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.34);
		
		gc.set(2009, 11, 15, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.09);
		
		gc.set(2009, 11, 16, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.15);
		
		gc.set(2009, 11, 17, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 43.61);
		
		gc.set(2009, 11, 18, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.33);
		
		gc.set(2009, 11, 21, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.83);
		
		gc.set(2009, 11, 22, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.10);
		
		gc.set(2009, 11, 23, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.42);
		
		gc.set(2009, 11, 24, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.84);
		
		gc.set(2009, 11, 28, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.08);
		
		gc.set(2009, 11, 29, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.89);
		
		gc.set(2009, 11, 30, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.03);
		
		gc.set(2009, 11, 31, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.61);
		
		gc.set(2010, 0, 4, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.28);
		
		gc.set(2010, 0, 5, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.28);
		
		gc.set(2010, 0, 6, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46);
		
		gc.set(2010, 0, 7, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.03);
		
		gc.set(2010, 0, 8, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.41);
		
		gc.set(2010, 0, 11, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.22);
		
		gc.set(2010, 0, 12, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.64);
		
		gc.set(2010, 0, 13, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.21);
		
		gc.set(2010, 0, 14, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.25);
		
		gc.set(2010, 0, 15, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.71);
		
		gc.set(2010, 0, 19, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 46.45);
		
		gc.set(2010, 0, 20, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.78);
		
		gc.set(2010, 0, 21, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 45.35);
		
		gc.set(2010, 0, 22, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.03);
		
		gc.set(2010, 0, 25, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.18);
		
		gc.set(2010, 0, 26, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.22);
		
		gc.set(2010, 0, 27, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 44.57);
		
		gc.set(2010, 0, 28, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 43.42);
		
		gc.set(2010, 0, 29, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 42.66);
		
		gc.set(2010, 1, 1, 0, 0, 0);
		d = gc.getTime();
		close.put(d, 43.13);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testRSI() {
		Serie rsi = RSI.calc(close);
		rsi.println();
		assertEquals(19, rsi.size());
		assertEquals(37.79, Math.round(rsi.get(0) * 100.0) / 100.0);
	}
	
}
