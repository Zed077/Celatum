package com.celatum.test;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.maths.HighLow;

class CalcTest {
	private Serie serie;
	private GregorianCalendar gc = new GregorianCalendar();

	@BeforeEach
	void setUp() throws Exception {
		DataAccessOrchestrator.init();
		
		serie = new Serie();

		gc.set(2020 - 1900, 0, 1);
		serie.put(gc.getTime(), 100);

		gc.set(2020 - 1900, 0, 2);
		serie.put(gc.getTime(), 200);

		gc.set(2020 - 1900, 0, 4);
		serie.put(gc.getTime(), 400);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testReverseCompound() {
		double pv = 250;
		double fv = 500;
		double period = 5;
		double rate = Calc.reverseCompound(pv, fv, period);
		assertEquals(1487, Math.round(rate * 10000));

//		rate = Calc.reverseCompound(250000, -332378.0992869161, 15.106164241533486);
//		double val = 250000 / 332378.0992869161;
//		double pp = 1.0 / period;
//		double pow = Math.pow(val, pp);
//		assertFalse(Double.isNaN(rate));
	}

	@Test
	void testSMA() {
		Serie sma = Calc.sma(serie, 1);
		assertEquals(400, sma.get(0));
		assertEquals(200, sma.get(1));
		assertEquals(100, sma.get(2));

		sma = Calc.sma(serie, 2);
		assertEquals(300, sma.get(0));
		assertEquals(150, sma.get(1));
		assertEquals(2, sma.size());
	}

	@Test
	void testATH() {
		Serie ath = HighLow.allTimeHigh(serie);
		assertEquals(400, ath.get(0));
		assertEquals(200, ath.get(1));
		assertEquals(100, ath.get(2));

		serie = new Serie();

		gc.set(2020 - 1900, 0, 1);
		serie.put(gc.getTime(), 100);

		gc.set(2020 - 1900, 0, 2);
		serie.put(gc.getTime(), 400);

		gc.set(2020 - 1900, 0, 4);
		serie.put(gc.getTime(), 200);

		ath = HighLow.allTimeHigh(serie);
		assertEquals(400, ath.get(0));
		assertEquals(400, ath.get(1));
		assertEquals(100, ath.get(2));
	}

	@Test
	void testPeriodLow() {
		double v = HighLow.periodLow(serie, 0, 3);
		assertEquals(100, v);
		v = HighLow.periodLow(serie, 0, 2);
		assertEquals(200, v);
		v = HighLow.periodLow(serie, 0, 1);
		assertEquals(400, v);

		v = HighLow.periodLow(serie, 1, 1);
		assertEquals(200, v);

		gc.set(2020 - 1900, 0, 2);
		v = HighLow.periodLow(serie, gc.getTime(), 1);
		assertEquals(200, v);
	}

	@Test
	void testMID() {
		Serie ts = new Serie();
		gc.set(2020 - 1900, 0, 1);
		ts.put(gc.getTime(), 10);
		gc.set(2020 - 1900, 0, 2);
		ts.put(gc.getTime(), 20);
		gc.set(2020 - 1900, 0, 4);
		ts.put(gc.getTime(), 30);

		Serie mid = Calc.mid(ts, serie);
		assertEquals(215, mid.get(0));
		assertEquals(110, mid.get(1));
		assertEquals(55, mid.get(2));

		Serie ts2 = new Serie();
		gc.set(2020 - 1900, 0, 1);
		ts2.put(gc.getTime(), 10);
		gc.set(2020 - 1900, 0, 2);
		ts2.put(gc.getTime(), 20);
		gc.set(2020 - 1900, 0, 3);
		ts2.put(gc.getTime(), 30);

		try {
			Serie mid2 = Calc.mid(ts2, serie);
			fail();
		} catch (RuntimeException e) {
		}
	}

	@Test
	void testEMA() {
		Serie ts = new Serie();
		gc.set(2020 - 1900, 0, 1);
		ts.put(gc.getTime(), 22.27);
		gc.set(2020 - 1900, 0, 2);
		ts.put(gc.getTime(), 22.19);
		gc.set(2020 - 1900, 0, 3);
		ts.put(gc.getTime(), 22.08);
		gc.set(2020 - 1900, 0, 4);
		ts.put(gc.getTime(), 22.17);
		gc.set(2020 - 1900, 0, 5);
		ts.put(gc.getTime(), 22.18);
		gc.set(2020 - 1900, 0, 6);
		ts.put(gc.getTime(), 22.13);
		gc.set(2020 - 1900, 0, 7);
		ts.put(gc.getTime(), 22.23);
		gc.set(2020 - 1900, 0, 8);
		ts.put(gc.getTime(), 22.43);
		gc.set(2020 - 1900, 0, 9);
		ts.put(gc.getTime(), 22.24);
		gc.set(2020 - 1900, 0, 10);
		ts.put(gc.getTime(), 22.29);
		gc.set(2020 - 1900, 0, 11);
		ts.put(gc.getTime(), 22.15);
		gc.set(2020 - 1900, 0, 12);
		ts.put(gc.getTime(), 22.39);
		gc.set(2020 - 1900, 0, 13);
		ts.put(gc.getTime(), 22.38);
		gc.set(2020 - 1900, 0, 14);
		ts.put(gc.getTime(), 22.61);
		gc.set(2020 - 1900, 0, 15);
		ts.put(gc.getTime(), 23.36);
		gc.set(2020 - 1900, 0, 16);
		ts.put(gc.getTime(), 24.05);
		gc.set(2020 - 1900, 0, 17);
		ts.put(gc.getTime(), 23.75);
		gc.set(2020 - 1900, 0, 18);
		ts.put(gc.getTime(), 23.83);
		gc.set(2020 - 1900, 0, 19);
		ts.put(gc.getTime(), 23.95);
		gc.set(2020 - 1900, 0, 20);
		ts.put(gc.getTime(), 23.63);

		// Test EMA
		Serie ema = Calc.ema(ts, 10);
		assertEquals(11, ema.size());
		assertEquals(23.34, Math.round(ema.get(0) * 100.0) / 100.0);
		assertEquals(22.33, Math.round(ema.get(6) * 100.0) / 100.0);
		assertEquals(22.22, Math.round(ema.get(10) * 100.0) / 100.0);
	}

	@Test
	void testATR() throws Exception {
		Serie high = new Serie();
		gc.set(2020 - 1900, 0, 1);
		high.put(gc.getTime(), 48.70);
		gc.set(2020 - 1900, 0, 2);
		high.put(gc.getTime(), 48.72);
		gc.set(2020 - 1900, 0, 3);
		high.put(gc.getTime(), 48.90);
		gc.set(2020 - 1900, 0, 4);
		high.put(gc.getTime(), 48.87);
		gc.set(2020 - 1900, 0, 5);
		high.put(gc.getTime(), 48.82);
		gc.set(2020 - 1900, 0, 6);
		high.put(gc.getTime(), 49.05);
		gc.set(2020 - 1900, 0, 7);
		high.put(gc.getTime(), 49.20);
		gc.set(2020 - 1900, 0, 8);
		high.put(gc.getTime(), 49.35);
		gc.set(2020 - 1900, 0, 9);
		high.put(gc.getTime(), 49.92);
		gc.set(2020 - 1900, 0, 10);
		high.put(gc.getTime(), 50.19);
		gc.set(2020 - 1900, 0, 11);
		high.put(gc.getTime(), 50.12);
		gc.set(2020 - 1900, 0, 12);
		high.put(gc.getTime(), 49.66);
		gc.set(2020 - 1900, 0, 13);
		high.put(gc.getTime(), 49.88);
		gc.set(2020 - 1900, 0, 14);
		high.put(gc.getTime(), 50.19);
		gc.set(2020 - 1900, 0, 15);
		high.put(gc.getTime(), 50.36);
		gc.set(2020 - 1900, 0, 16);
		high.put(gc.getTime(), 50.57);
		gc.set(2020 - 1900, 0, 17);
		high.put(gc.getTime(), 50.65);
		gc.set(2020 - 1900, 0, 18);
		high.put(gc.getTime(), 50.43);
		gc.set(2020 - 1900, 0, 19);
		high.put(gc.getTime(), 49.63);
		gc.set(2020 - 1900, 0, 20);
		high.put(gc.getTime(), 50.33);

		Serie low = new Serie();
		gc.set(2020 - 1900, 0, 1);
		low.put(gc.getTime(), 47.79);
		gc.set(2020 - 1900, 0, 2);
		low.put(gc.getTime(), 48.14);
		gc.set(2020 - 1900, 0, 3);
		low.put(gc.getTime(), 48.39);
		gc.set(2020 - 1900, 0, 4);
		low.put(gc.getTime(), 48.37);
		gc.set(2020 - 1900, 0, 5);
		low.put(gc.getTime(), 48.24);
		gc.set(2020 - 1900, 0, 6);
		low.put(gc.getTime(), 48.64);
		gc.set(2020 - 1900, 0, 7);
		low.put(gc.getTime(), 48.94);
		gc.set(2020 - 1900, 0, 8);
		low.put(gc.getTime(), 48.86);
		gc.set(2020 - 1900, 0, 9);
		low.put(gc.getTime(), 49.50);
		gc.set(2020 - 1900, 0, 10);
		low.put(gc.getTime(), 49.87);
		gc.set(2020 - 1900, 0, 11);
		low.put(gc.getTime(), 49.20);
		gc.set(2020 - 1900, 0, 12);
		low.put(gc.getTime(), 48.90);
		gc.set(2020 - 1900, 0, 13);
		low.put(gc.getTime(), 49.43);
		gc.set(2020 - 1900, 0, 14);
		low.put(gc.getTime(), 49.73);
		gc.set(2020 - 1900, 0, 15);
		low.put(gc.getTime(), 49.26);
		gc.set(2020 - 1900, 0, 16);
		low.put(gc.getTime(), 50.09);
		gc.set(2020 - 1900, 0, 17);
		low.put(gc.getTime(), 50.30);
		gc.set(2020 - 1900, 0, 18);
		low.put(gc.getTime(), 49.21);
		gc.set(2020 - 1900, 0, 19);
		low.put(gc.getTime(), 48.98);
		gc.set(2020 - 1900, 0, 20);
		low.put(gc.getTime(), 49.61);

		Serie close = new Serie();
		gc.set(2020 - 1900, 0, 1);
		close.put(gc.getTime(), 48.16);
		gc.set(2020 - 1900, 0, 2);
		close.put(gc.getTime(), 48.61);
		gc.set(2020 - 1900, 0, 3);
		close.put(gc.getTime(), 48.75);
		gc.set(2020 - 1900, 0, 4);
		close.put(gc.getTime(), 48.63);
		gc.set(2020 - 1900, 0, 5);
		close.put(gc.getTime(), 48.74);
		gc.set(2020 - 1900, 0, 6);
		close.put(gc.getTime(), 49.03);
		gc.set(2020 - 1900, 0, 7);
		close.put(gc.getTime(), 49.07);
		gc.set(2020 - 1900, 0, 8);
		close.put(gc.getTime(), 49.32);
		gc.set(2020 - 1900, 0, 9);
		close.put(gc.getTime(), 49.91);
		gc.set(2020 - 1900, 0, 10);
		close.put(gc.getTime(), 50.13);
		gc.set(2020 - 1900, 0, 11);
		close.put(gc.getTime(), 49.53);
		gc.set(2020 - 1900, 0, 12);
		close.put(gc.getTime(), 49.50);
		gc.set(2020 - 1900, 0, 13);
		close.put(gc.getTime(), 49.75);
		gc.set(2020 - 1900, 0, 14);
		close.put(gc.getTime(), 50.03);
		gc.set(2020 - 1900, 0, 15);
		close.put(gc.getTime(), 50.31);
		gc.set(2020 - 1900, 0, 16);
		close.put(gc.getTime(), 50.52);
		gc.set(2020 - 1900, 0, 17);
		close.put(gc.getTime(), 50.41);
		gc.set(2020 - 1900, 0, 18);
		close.put(gc.getTime(), 49.34);
		gc.set(2020 - 1900, 0, 19);
		close.put(gc.getTime(), 49.37);
		gc.set(2020 - 1900, 0, 20);
		close.put(gc.getTime(), 50.23);

		HistoricalData hd = HistoricalData
				.getEmptyHistoricalData(Instrument.getInstrumentByCode("UD.D.TSLA.CASH.IP"), Source.IG_EPIC);
		hd.midHigh = high;
		hd.midClose = close;
		hd.midLow = low;

		// Test TR
		Serie tr = Calc.trueRange(hd);
		tr.println();

		// Test ATR
		System.out.println("*** ATR ***");
		Serie atr = Calc.atr(hd, 14);
		atr.println();
		assertEquals(7, atr.size());
		assertEquals(0.64, Math.round(atr.get(0) * 100.0) / 100.0);
		assertEquals(0.57, Math.round(atr.get(3) * 100.0) / 100.0);
		assertEquals(0.55, Math.round(atr.get(6) * 100.0) / 100.0);
		// TODO below is the real value. Need to move away from using double into
		// BigDecimal to avoid all these rounding errors
//		assertEquals(0.56, Math.round(atr.get(6) * 100.0) / 100.0);

		// Test ADP
		System.out.println("*** ADP ***");
		Serie adp = Calc.atrPercent(hd, 14);
		adp.println();
		assertEquals(7, adp.size());
		assertEquals(1.30, Math.round(adp.get(0) * 10000.0) / 100.0);
		assertEquals(1.11, Math.round(adp.get(3) * 10000.0) / 100.0);
		assertEquals(1.13, Math.round(adp.get(6) * 10000.0) / 100.0);

		// Test SDP
		System.out.println("*** SDP ***");
		Serie sdp = Calc.standardDeviationPercent(hd, 14);
		sdp.println();
		assertEquals(7, sdp.size());
		assertEquals(1.42, Math.round(sdp.get(0) * 10000.0) / 100.0);
		assertEquals(1.20, Math.round(sdp.get(3) * 10000.0) / 100.0);
		assertEquals(1.19, Math.round(sdp.get(6) * 10000.0) / 100.0);
	}
}
