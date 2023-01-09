package com.celatum.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Instrument.Source;
import com.celatum.trading.LongOrder;
import com.celatum.trading.LongPosition;
import com.celatum.trading.Order;
import com.celatum.data.Serie;
import com.celatum.data.SerieItem;

class PositionTest {
	private Serie serie;
	private GregorianCalendar gc = new GregorianCalendar();
	Instrument i;
	private HistoricalData hd;

	@BeforeEach
	void setUp() throws Exception {
		DataAccessOrchestrator.init();
		i =  Instrument.getInstrumentByCode("AMZN");
		hd = DataAccessOrchestrator.getHistoricalData(i, Source.AV_CODE);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLongCosts0() {
		i.setCommission(0);
		i.setCommissionPercent(0);
		i.setSpreadPoints(0);
		
		// Monday
		gc.set(2022, 11, 12);
		
		Order o = new LongOrder(i, "", gc.getTime(), 100);
		o.setSize(100);
		hd.setReferenceIndex(gc.getTime());
		LongPosition p = new LongPosition(o, 100, hd);
		assertEquals(0, p.getCosts());
		
		// Tuesday
		gc.set(2022, 11, 13);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(1.916666666666667, p.getCosts());
		
		// Wednesday
		gc.set(2022, 11, 14);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(3.833333333333334, p.getCosts());
		
		// Thursday
		gc.set(2022, 11, 15);
		hd.setReferenceIndex(gc.getTime());
		p.close(gc.getTime(), 110);
		assertEquals(5.750000000000001, p.getCosts());
	}
	
	@Test
	void testLongCostsCom1() {
		i.setCommission(1);
		i.setCommissionPercent(0);
		i.setSpreadPoints(0);

		// Monday
		gc.set(2022, 11, 12);

		Order o = new LongOrder(i, "", gc.getTime(), 100);
		o.setSize(100);
		hd.setReferenceIndex(gc.getTime());
		LongPosition p = new LongPosition(o, 100, hd);
		assertEquals(200, p.getCosts());

		// Tuesday
		gc.set(2022, 11, 13);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(201.916666666666667, p.getCosts());
		
		// Wednesday
		gc.set(2022, 11, 14);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(203.833333333333334, p.getCosts());
		
		// Thursday
		gc.set(2022, 11, 15);
		hd.setReferenceIndex(gc.getTime());
		p.close(gc.getTime(), 110);
		assertEquals(205.750000000000001, p.getCosts());
	}

	@Test
	void testLongCostsComP() {
		i.setCommission(0);
		i.setCommissionPercent(0.01);
		i.setSpreadPoints(0);

		// Monday
		gc.set(2022, 11, 12);

		Order o = new LongOrder(i, "", gc.getTime(), 100);
		o.setSize(100);
		hd.setReferenceIndex(gc.getTime());
		LongPosition p = new LongPosition(o, 100, hd);
		assertEquals(100, p.getCosts());

		// Tuesday
		gc.set(2022, 11, 13);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(101.916666666666667, p.getCosts());
		
		// Wednesday
		gc.set(2022, 11, 14);
		hd.setReferenceIndex(gc.getTime());
		p.close(gc.getTime(), 110);
		assertEquals(103.833333333333334, p.getCosts());
		
		// Thursday
		gc.set(2022, 11, 15);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(214.21666666666667, p.getCosts());
	}

	@Test
	void testLongSpread() {
		i.setCommission(0);
		i.setCommissionPercent(0);
		i.setSpreadPoints(1);
		
		// Monday
		gc.set(2022, 11, 12);
		
		Order o = new LongOrder(i, "", gc.getTime(), 100);
		o.setSize(100);
		hd.setReferenceIndex(gc.getTime());
		LongPosition p = new LongPosition(o, 100, hd);
		assertEquals(0, p.getCosts());
		
		// Tuesday
		gc.set(2022, 11, 13);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(1.916666666666667, p.getCosts());
		
		// Wednesday
		gc.set(2022, 11, 14);
		hd.setReferenceIndex(gc.getTime());
		assertEquals(3.833333333333334, p.getCosts());
		
		// Thursday
		gc.set(2022, 11, 15);
		hd.setReferenceIndex(gc.getTime());
		p.close(gc.getTime(), 110);
		assertEquals(5.750000000000001, p.getCosts());
	}
}
