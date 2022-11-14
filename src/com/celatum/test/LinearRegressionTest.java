package com.celatum.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.Serie;
import com.celatum.maths.LinearRegression;

class LinearRegressionTest {
	private GregorianCalendar gc = new GregorianCalendar();

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSlope() {
		Serie serie = new Serie();

		gc.set(2020 - 1900, 0, 1);
		serie.put(gc.getTime(), 10);
		
		gc.set(2020 - 1900, 5, 15);
		serie.put(gc.getTime(), 15);

		gc.set(2020 - 1900, 11, 31);
		serie.put(gc.getTime(), 20);
		
		LinearRegression lr = new LinearRegression(serie, 3);
		
		assertEquals(10, Math.round(lr.getAbsoluteYearlyRateOfChange()));
		assertEquals(1, Math.round(lr.getPercentYearlyRateOfChange()));
	}

	@Test
	void testSlope2() {
		Serie serie = new Serie();

		gc.set(2020 - 1900, 0, 1);
		serie.put(gc.getTime(), 10);
		
		gc.set(2020 - 1900, 5, 15);
		serie.put(gc.getTime(), 12.5);

		gc.set(2020 - 1900, 11, 31);
		serie.put(gc.getTime(), 15);
		
		LinearRegression lr = new LinearRegression(serie, 3);
		
		assertEquals(5, Math.round(lr.getAbsoluteYearlyRateOfChange()));
		assertEquals(0.5, Math.round(lr.getPercentYearlyRateOfChange()*10)/10.0);
	}
	
}
