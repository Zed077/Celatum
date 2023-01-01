package com.celatum.service;

//Import required java libraries
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.celatum.data.DataAccessOrchestrator;
import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.InstrumentStats;
import com.celatum.service.json.AlgoRun;
import com.celatum.service.json.AlgoRunPosition;
import com.celatum.service.json.OHCL;
import com.celatum.service.json.SerieItemJSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//Extend HttpServlet class
public class TestServlet extends HttpServlet {
	
	public void init() throws ServletException {
		
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Routing
		try {
			String method = request.getParameter("method");
			if (method == null)
				return;

			switch (method) {
			case "sizing":
				processSizing(request, response);
				return;
			case "runs":
				processRuns(request, response);
				return;
			case "pnl":
				algoRunPnL(request, response);
				return;
			case "history":
				processHistory(request, response);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processRuns(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String algorunref = request.getParameter("algorunref");
		if (algorunref != null) {
			algoRunInstruments(algorunref, request, response);
		} else {
			Collection<AlgoRun> runs = DataAccessOrchestrator.getRuns();
			respond(response, runs);
		}
	}

	public void algoRunInstruments(String algoRunRef, HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException, IOException {
		String instrumentName = request.getParameter("instrumentname");
		if (instrumentName != null) {
			algoRunPositions(algoRunRef, instrumentName, request, response);
		} else {
			Collection<Instrument> instruments = DataAccessOrchestrator.getAlgoRunInstruments(algoRunRef);
			respond(response, instruments);
		}
	}

	/**
	 * http://localhost:8080/Celatum/HelloWorld?method=pnl&algorunref=runAlgoAggregate_1671615417663_0
	 * @param request
	 * @param response
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public void algoRunPnL(HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException, IOException {
		String algoRunRef = request.getParameter("algorunref");
		Collection<SerieItemJSON> res = SerieItemJSON.serialize(DataAccessOrchestrator.getAlgoRunPnL(algoRunRef));
		respond(response, res);
	}

	/**
	 * http://localhost:8080/Celatum/HelloWorld?method=runs&algorunref=runAlgoAggregate_1671615417663_0&instrumentname=Carrefour%20SA
	 * @param algoRunRef
	 * @param instrumentName
	 * @param request
	 * @param response
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public void algoRunPositions(String algoRunRef, String instrumentName, HttpServletRequest request,
			HttpServletResponse response) throws JsonProcessingException, IOException {
		Collection<AlgoRunPosition> positions = DataAccessOrchestrator.getAlgoRunPositions(algoRunRef, instrumentName);
		respond(response, positions);
	}

	/**
	 * http://localhost:12689/Celatum/HelloWorld?method=sizing
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void processSizing(HttpServletRequest request, HttpServletResponse response) {
		DataAccessOrchestrator.refreshInstrumentStatistics();
		try {
			Collection<Instrument> instruments = DataAccessOrchestrator.getInstruments().values();
			Collection<InstrumentStats> stats = DataAccessOrchestrator.getInstrumentStatistics(instruments);

			String result = new ObjectMapper().writeValueAsString(stats);

			// Set response content type
			response.setContentType("text/html");
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Actual logic goes here.
			PrintWriter out = response.getWriter();
			out.println(result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * http://localhost:8080/Celatum/HelloWorld?method=history&instrumentname=Carrefour%20SA
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processHistory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String instrumentName = request.getParameter("instrumentname");
		String algorunref = request.getParameter("algorunref");
		Instrument inst = DataAccessOrchestrator.getInstruments().get(instrumentName);
		if (inst == null) {
			throw new RuntimeException("Instrument unknown: " + instrumentName);
		}
		
		HistoricalData hist = DataAccessOrchestrator.getHistoricalData(algorunref, inst);
		
		try {
			List<OHCL> firstInstrument = OHCL.serialize(hist);

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

	private void respond(HttpServletResponse response, Object data)
			throws JsonProcessingException, IOException {
		String result = new ObjectMapper().writeValueAsString(data);

		// Set response content type
		response.setContentType("text/html");
		response.setHeader("Access-Control-Allow-Origin", "*");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println(result);
	}
}