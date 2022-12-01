package com.celatum.service;

//Import required java libraries
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import com.celatum.data.HistoricalData;
import com.celatum.data.IGConnector;
import com.celatum.data.Instrument;
import com.celatum.data.SerieItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//Extend HttpServlet class
public class TestServlet extends HttpServlet {

	private static List<Instrument> instruments;
	private static List<HistoricalData> histories;

	public void init() throws ServletException {
		try {
			// Connect
			IGConnector.connect();

			// Get list of instruments in scope
			instruments = IGConnector.getWatchlist(IGConnector.TESTWATCHLIST);

			// Histories
			histories = new ArrayList<HistoricalData>();
			for (Instrument id : instruments) {
				HistoricalData hd = new HistoricalData(id, false);
				histories.add(hd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			List<OHCL> firstInstrument = serialize(histories.get(0));

			String result = new ObjectMapper().writeValueAsString(firstInstrument);

			// Set response content type
			response.setContentType("text/html");
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Actual logic goes here.
			PrintWriter out = response.getWriter();
			out.println(result);

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		// do nothing.
	}

	private List<OHCL> serialize(HistoricalData history) {
		System.out.println("Serializing " + history.getEpic());
		ArrayList<OHCL> result = new ArrayList<>();
		Iterator<SerieItem> O = history.midOpen.getAllEntries().iterator();
		Iterator<SerieItem> H = history.midHigh.getAllEntries().iterator();
		Iterator<SerieItem> C = history.midClose.getAllEntries().iterator();
		Iterator<SerieItem> L = history.midLow.getAllEntries().iterator();
		while (O.hasNext()) {
			OHCL item = new OHCL();
			SerieItem primer = O.next();
			item.setTime(primer.getDate());
			item.open = primer.getValue();
			item.high = H.next().getValue();
			item.close = C.next().getValue();
			item.low = L.next().getValue();

			result.add(item);
		}
		System.out.println("Serializing complete");
		return result;
	}
}

//{ time: '2018-12-22', open: 75.16, high: 82.84, low: 36.16, close: 45.72 }
class OHCL {
	private Date time;
	double open;
	double high;
	double close;
	double low;

	public String getTime() {
		return time.toString();
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getClose() {
		return close;
	}

	public double getLow() {
		return low;
	}
}