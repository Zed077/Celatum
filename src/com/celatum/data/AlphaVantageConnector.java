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
import java.util.NavigableSet;
import java.util.TreeSet;

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
	public static void loadHistoricalPrices(HistoricalData hd, boolean full) throws Exception {
		System.out.print("AV getHistoricalPrices " + hd.instrument.getName() + " ");

		String outputSize = (full) ? "full" : "compact";

		// Fetch data
		URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
				+ hd.instrument.getCode(Source.AV_CODE) + "&outputsize=" + outputSize + "&apikey=" + API_KEY);
		HttpURLConnection conn = createConnection(url);
		System.out.print(conn.getResponseCode() + " " + conn.getResponseMessage());

		// Process data
		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}

			JSONObject jo = new JSONObject(response.toString());
			JSONObject arr = jo.getJSONObject("Time Series (Daily)");
			
			// Order the entries in descending order
			NavigableSet<String> keys = new TreeSet<>(arr.keySet()).descendingSet();

			// Loop through timed entries
			double splitCoefficient = 1;
			Date lastDay = null;
			for (String timestamp : keys) {
				// Date
				Date day = AVDATEFORMAT.parse(timestamp);
				if (lastDay != null && day.after(lastDay)) {
					throw new RuntimeException("AV time series must be in descending order in order to correctly apply the split coefficient");
				}
				
				// Init
				JSONObject d = arr.getJSONObject(timestamp);
				double spread = hd.instrument.getSpreadPoints()/2.0;

				// Prices
				double askOpen = (d.getDouble("1. open") + spread) / splitCoefficient;
				double askHigh = (d.getDouble("2. high") + spread) / splitCoefficient;
				double askLow = (d.getDouble("3. low") + spread) / splitCoefficient;
				double askClose = (d.getDouble("4. close") + spread) / splitCoefficient;

				double bidOpen = (d.getDouble("1. open") - spread) / splitCoefficient;
				double bidHigh = (d.getDouble("2. high") - spread) / splitCoefficient;
				double bidLow = (d.getDouble("3. low") - spread) / splitCoefficient;
				double bidClose = (d.getDouble("4. close") - spread) / splitCoefficient;

				hd.askOpen.put(day, askOpen);
				hd.askClose.put(day, askClose);
				hd.askHigh.put(day, askHigh);
				hd.askLow.put(day, askLow);
				hd.bidOpen.put(day, bidOpen);
				hd.bidClose.put(day, bidClose);
				hd.bidHigh.put(day, bidHigh);
				hd.bidLow.put(day, bidLow);

				// Volume
				double volume = Math.round( ((double) d.getInt("6. volume")) * splitCoefficient );
				hd.volume.put(day, volume);

				// Split coefficient for the next record
				hd.splitCoef.put(day, splitCoefficient);
				double newSplit = d.getDouble("8. split coefficient");
				splitCoefficient = Math.round(splitCoefficient * newSplit);
			}
			
			if (splitCoefficient != 1 && !full) {
				System.out.println(" stock split, reloading full history");
				hd.empty();
				loadHistoricalPrices(hd, true);
			}
			
			System.out.println(" to " + hd.askClose.newestDate());
		} catch (Exception e) {
			System.err.println(url.toString());
			throw e;
		} finally {
			// Maximum 5 per minute
			Thread.sleep(12500);
			conn.disconnect();
		}
	}

}