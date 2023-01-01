package com.celatum.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.celatum.data.Instrument.Source;

public class AlphaVantageConnector {
	private static SimpleDateFormat AVDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
	private static final String API_KEY = "0VQDGS574IF8HF9E";

	
	/**
	 * https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=Credit%20Suisse&apikey=0VQDGS574IF8HF9E
	 * @param searchString
	 * @return
	 */
	public static List<Object> search(String searchString) {
		return null;
	}
	
	private static HttpURLConnection createConnection(URL url)
			throws IOException, ProtocolException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		return conn;
	}

	/**
	 * https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=IBM&apikey=0VQDGS574IF8HF9E
	 * @param hd
	 * @throws Exception
	 */
	public static void getHistoricalPrices(HistoricalData hd, boolean full) throws Exception {
		System.out.println("\nAV getHistoricalPrices " + hd.instrument.getName());

		String outputSize = (full) ? "full" : "compact";

		// Fetch data
		URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
				+ hd.instrument.getCode(Source.AV_CODE) + "&outputsize=" + outputSize + "&apikey=" + API_KEY);
		HttpURLConnection conn = createConnection(url);
		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

		// Process data
		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			JSONObject jo = new JSONObject(response.toString());
			JSONArray arr = jo.getJSONArray("Time Series (Daily)");

			// Loop through timed entries. IMPORTANT: assumes these are in descending order to apply the split coefficient
			double splitCoefficient = 1;
			Date lastDay = null;
			for (int i = 0; i < arr.length(); i++) {
				// Date
				String timestamp = arr.getString(i);
				Date day = AVDATEFORMAT.parse(timestamp);
				if (lastDay != null && day.after(lastDay)) {
					throw new RuntimeException("AV time series must be in descending order in order to correctly apply the split coefficient");
				}
				
				// Init
				JSONObject d = arr.getJSONObject(i);
				double spread = DataAccessOrchestrator.getInstrumentStatistics(hd.instrument).getBidAskSpreadPercent()/2.0;
				
				// Volume
				int volume = d.getInt("6. volume");

				// Prices
				double askOpen = d.getDouble("1. open") * (1 + spread);
				double askClose = d.getDouble("5. adjusted close") * (1 + spread);
				double askHigh = d.getDouble("2. high") * (1 + spread);
				double askLow = d.getDouble("3. low") * (1 + spread);

				double bidOpen = d.getDouble("1. open") * (1 - spread);
				double bidClose = d.getDouble("5. adjusted close") * (1 - spread);
				double bidHigh = d.getDouble("2. high") * (1 - spread);
				double bidLow = d.getDouble("3. low") * (1 - spread);

				hd.askOpen.put(day, askOpen);
				hd.askClose.put(day, askClose);
				hd.askHigh.put(day, askHigh);
				hd.askLow.put(day, askLow);
				hd.bidOpen.put(day, bidOpen);
				hd.bidClose.put(day, bidClose);
				hd.bidHigh.put(day, bidHigh);
				hd.bidLow.put(day, bidLow);
				hd.volume.put(day, (double) volume);

				// Split coefficient for the next record
				double newSplit = arr.getJSONObject(i).getDouble("8. split coefficient");
				splitCoefficient = splitCoefficient * newSplit;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// Maximum 5 per minute
			Thread.sleep(12500);
			conn.disconnect();
		}
	}

}