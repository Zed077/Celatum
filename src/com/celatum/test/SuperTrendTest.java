package com.celatum.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Serie;
import com.celatum.data.Instrument.Source;
import com.celatum.maths.ATR;
import com.celatum.maths.SuperTrend;

class SuperTrendTest {
	private GregorianCalendar gc = new GregorianCalendar();
	private HistoricalData hd;

	@BeforeEach
	void setUp() throws Exception {
		DataAccessOrchestrator.init();
		
		Serie high = new Serie();
		Serie low = new Serie();
		Serie close = new Serie();
		Date d;

		gc.set(2020, 0, 1, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1757.95);
		low.put(d, 1753.6);
		close.put(d, 1755.9);

		gc.set(2020, 0, 2, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1758.1);
		low.put(d, 1755.4);
		close.put(d, 1756.5);

		gc.set(2020, 0, 3, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1758);
		low.put(d, 1755.05);
		close.put(d, 1756.85);

		gc.set(2020, 0, 4, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1758.55);
		low.put(d, 1755.55);
		close.put(d, 1757.8);

		gc.set(2020, 0, 5, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1759.05);
		low.put(d, 1757);
		close.put(d, 1759);

		gc.set(2020, 0, 6, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1763);
		low.put(d, 1758.85);
		close.put(d, 1761);

		gc.set(2020, 0, 7, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1767);
		low.put(d, 1759.4);
		close.put(d, 1767);

		gc.set(2020, 0, 8, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1768.5);
		low.put(d, 1765.55);
		close.put(d, 1767.8);

		gc.set(2020, 0, 9, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1773);
		low.put(d, 1767.55);
		close.put(d, 1771);

		gc.set(2020, 0, 10, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772.55);
		low.put(d, 1769.65);
		close.put(d, 1772);

		gc.set(2020, 0, 11, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772);
		low.put(d, 1768.5);
		close.put(d, 1769);

		gc.set(2020, 0, 12, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772.1);
		low.put(d, 1764.75);
		close.put(d, 1772.1);

		gc.set(2020, 0, 13, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1774.95);
		low.put(d, 1771.1);
		close.put(d, 1773);

		gc.set(2020, 0, 14, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772.9);
		low.put(d, 1769);
		close.put(d, 1769.8);

		gc.set(2020, 0, 15, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1770.7);
		low.put(d, 1767);
		close.put(d, 1768);

		gc.set(2020, 0, 16, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1771.95);
		low.put(d, 1767.7);
		close.put(d, 1769.7);

		gc.set(2020, 0, 17, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1770.7);
		low.put(d, 1769.25);
		close.put(d, 1769.9);

		gc.set(2020, 0, 18, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772);
		low.put(d, 1769.7);
		close.put(d, 1770.9);

		gc.set(2020, 0, 19, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772.6);
		low.put(d, 1770.35);
		close.put(d, 1771.8);

		gc.set(2020, 0, 20, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1772);
		low.put(d, 1770.65);
		close.put(d, 1771);

		gc.set(2020, 0, 21, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1773.9);
		low.put(d, 1770.5);
		close.put(d, 1772.65);

		gc.set(2020, 0, 22, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1773);
		low.put(d, 1772.5);
		close.put(d, 1772.65);

		gc.set(2020, 0, 23, 0, 0, 0);
		d = gc.getTime();
		high.put(d, 1773.55);
		low.put(d, 1772.6);
		close.put(d, 1773);

		hd = HistoricalData.getEmptyHistoricalData(Instrument.getInstrumentByCode("IX.D.SPTRD.IFM.IP"), Source.IG_EPIC);
		hd.midHigh = high;
		hd.midClose = close;
		hd.midLow = low;
		hd.askHigh = high;
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSuperTrend() {
		Serie atr = ATR.calc(hd, 10, ATR.Method.SMA);
		atr.println();
		assertEquals(14, atr.size());
		assertEquals(3.4, Math.round(atr.get(3) * 100.0) / 100.0);
		assertEquals(3.94, Math.round(atr.get(6) * 100.0) / 100.0);
		assertEquals(4.38, Math.round(atr.get(9) * 100.0) / 100.0);

		Serie st = SuperTrend.calc(hd, 10, 3, ATR.Method.SMA);
		st.println();
		assertEquals(13, st.size());
		assertEquals(1780.32, Math.round(st.get(0) * 100.0) / 100.0);
		assertEquals(1780.87, Math.round(st.get(1) * 100.0) / 100.0);
		assertEquals(1781, Math.round(st.get(11) * 100.0) / 100.0);
	}

}
