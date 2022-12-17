package com.celatum.test;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celatum.data.IGConnector;
import com.celatum.data.Instrument;
import com.celatum.data.WatchlistData;

class IGConnectorTest {
	@BeforeEach
	void setUp() throws Exception {
		IGConnector.connect();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetWatchlists() throws Exception {
		List<WatchlistData> ws = IGConnector.getWatchlists();
		for(WatchlistData w : ws) {
			System.out.println(w);
		}
	}

	@Test
	void testGetWatchlist() throws Exception {
		System.out.println("Live Watchlist\n");
		List<Instrument> is = IGConnector.getWatchlist("test");
		for(Instrument i : is) {
			System.out.println(i);
		}
		
		System.out.println("\nTest Watchlist");
		is = IGConnector.getWatchlist("test");
		for(Instrument i : is) {
			System.out.println(i);
		}
	}
}

/**

Live Watchlist

US 500
Spot Gold
Credit Suisse Group AG (CH)
US Dollar Basket
Oil - US Crude
JPMorgan Chase & Co (All Sessions)
Ryanair Holdings PLC (Euronext Dublin)
Carrefour SA
Amazon.com Inc (All Sessions)
Tesla Motors Inc (All Sessions)
NVIDIA Corp (All Sessions)
Dick's Sporting Goods Inc
Apple Inc (All Sessions)
Microsoft Corp (All Sessions)
Intel Corp (All Sessions)
Tilray Inc (All Sessions)
Germany 40
Molson Coors Brewing Co
Uber Technologies Inc (All Sessions)


Test Watchlist

Credit Suisse Group AG (CH)
US 500
Oil - US Crude
Spot Gold
US Dollar Basket
Ryanair Holdings PLC (Euronext Dublin)
Carrefour SA
Amazon.com Inc (All Sessions)
NVIDIA Corp (All Sessions)

**/