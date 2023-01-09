package com.celatum.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

class SerieTest {
	private Serie serie;
	private GregorianCalendar gc = new GregorianCalendar();

	@BeforeEach
	void setUp() throws Exception {
		serie = new Serie();
		
		gc.set(2020-1900, 0, 2);
		serie.put(gc.getTime(), 200);
		
		gc.set(2020-1900, 0, 1);
		serie.put(gc.getTime(), 100);
		
		gc.set(2020-1900, 0, 4);
		serie.put(gc.getTime(), 400);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGet() {
		assertEquals(200, serie.get(1));
		assertEquals(400, serie.get(0));
		assertEquals(100, serie.get(2));
	}

	@Test
	void testGetDate() {
		gc.set(2020-1900, 0, 1);
		assertEquals(gc.getTime(), serie.getDate(2));
		
		gc.set(2020-1900, 0, 4);
		assertEquals(gc.getTime(), serie.getDate(0));
		
		gc.set(2020-1900, 0, 2);
		assertEquals(gc.getTime(), serie.getDate(1));
	}

	@Test
	void testSize() {
		assertEquals(3, serie.size());
	}

	@Test
	void testNewestDate() {
		gc.set(2020-1900, 0, 4);
		assertEquals(gc.getTime(), serie.newestDate());
	}

	@Test
	void testOldestDate() {
		gc.set(2020-1900, 0, 1);
		assertEquals(gc.getTime(), serie.oldestDate());
	}

	@Test
	void testSetReferenceIndex() {
		
		gc.set(2020-1900, 0, 1);
		serie.setReferenceDate(gc.getTime());
		assertEquals(1, serie.size());
		assertEquals(100, serie.get(0));

		serie.resetReferenceIndex();
		assertEquals(3, serie.size());

		gc.set(2020-1900, 0, 2);
		serie.setReferenceDate(gc.getTime());
		assertEquals(2, serie.size());
		assertEquals(200, serie.get(0));
		assertEquals(100, serie.get(1));

		gc.set(2020-1900, 0, 4);
		serie.setReferenceDate(gc.getTime());
		assertEquals(3, serie.size());
		assertEquals(400, serie.get(0));
		assertEquals(200, serie.get(1));
		assertEquals(100, serie.get(2));
	}
	
	@Test
	void testGetMaxSince() {
		gc.set(2020-1900, 0, 4);
		serie.setReferenceDate(gc.getTime());
		
		gc.set(2020-1900, 0, 2);
		SerieItem max = serie.getMaxSince(gc.getTime(), 0);
		assertEquals(400, max.getValue());
		max = serie.getMaxSince(gc.getTime(), 1);
		assertEquals(200, max.getValue());
		

		gc.set(2020-1900, 0, 2);
		serie.setReferenceDate(gc.getTime());
		gc.set(2020-1900, 0, 1);
		max = serie.getMaxSince(gc.getTime(), 0);
		assertEquals(200, max.getValue());
		max = serie.getMaxSince(gc.getTime(), 1);
		assertEquals(100, max.getValue());
	}
	
	@Test
	void head() {
		DataAccessOrchestrator.init();
		Instrument inst = Instrument.getInstrumentByCode("AMZN");
		HistoricalData hd = DataAccessOrchestrator.getHistoricalData(inst, Source.AV_CODE);
		
		int nCandles = 240;
		
		assertTrue(hd.fullSize() > nCandles);
		assertEquals(hd.fullSize(), hd.size());
		
		hd.head(nCandles);
		assertEquals(hd.fullSize(), nCandles);
		assertEquals(hd.size(), nCandles);
		assertEquals(hd.volume.size(), nCandles);
		assertEquals(hd.volume.fullSize(), nCandles);
	}

}
