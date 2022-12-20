package com.celatum.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class AlphaVantageConnector {
	private static String CST;
	private static String XSECURITYTOKEN;
	private static String defaultStartDateTime = "2006-01-01T07:00:00";
//	private static String defaultStartDateTime = "2021-11-15T07:00:00";
//	private static String testEndDateTime = "2006-11-15T07:00:00";
	public static SimpleDateFormat IGDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK);
	public static final String LIVEWATCHLIST = "live";
	public static final String TESTWATCHLIST = "test";
	private static final String API_KEY = "0VQDGS574IF8HF9E";
	public static int errorCount = 0;
	private static Date validUntilTime;
	private static double accountAvailable;
	private static double accountBalance;

	/**
	 * Both tokens are initially valid for 6 hours but get extended up to a maximum
	 * of 72 hours while they are in use.
	 */
	public static void connect() {
		if (validUntilTime != null && (new Date()).before(validUntilTime))
			return;

		try {
//			System.out.println("Connect");
			URL url = new URL("https://api.ig.com/gateway/deal/session");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("VERSION", "2");
			conn.setRequestProperty("X-IG-API-KEY", API_KEY);

//			String data = "{\n  \"identifier\": \"zed077\",\n  \"password\": \"z#V!uGge!if-H4t\"\n}";
			String data = "{\n  \"identifier\": \"concombre\",\n  \"password\": \"skdA_22#Qnr32-u\"\n}";

			byte[] out = data.getBytes(StandardCharsets.UTF_8);

			OutputStream stream = conn.getOutputStream();
			stream.write(out);

//			System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

			try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}

				JSONObject jo = new JSONObject(response.toString());
				// Account info
				JSONObject jaccount = jo.getJSONObject("accountInfo");
				accountAvailable = jaccount.getDouble("available");
				accountBalance = jaccount.getDouble("balance");
			}

			CST = conn.getHeaderField("CST");
//			System.out.println("CST : " + CST);
			XSECURITYTOKEN = conn.getHeaderField("X-SECURITY-TOKEN");
//			System.out.println("X-SECURITY-TOKEN : " + XSECURITYTOKEN);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 350);
			validUntilTime = calendar.getTime();

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=Credit%20Suisse&apikey=0VQDGS574IF8HF9E
	 * @param searchString
	 * @return
	 */
	public static List<Object> search(String searchString) {
		return null;
	}

	public static void getDailyAdjusted100(HistoricalData hd, Date startDate) throws Exception {
		System.out.println("\ngetHistoricalPrices " + hd.instrument.getName() + ", " + startDate);

		// Establish start date
		String startDateTime;
		if (startDate == null) {
			startDateTime = defaultStartDateTime;
		} else {
			startDateTime = IGDATEFORMAT.format(startDate);
		}

		// Create end date time
		String endDateTime = IGDATEFORMAT.format(new Date());

		// Fetch Data
		// https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=IBM&interval=5min&apikey=demo
		URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
				+ hd.instrument.getEpic() + "?resolution=DAY&pageSize=0&from=" + startDateTime + "&to=" + endDateTime);
		HttpURLConnection conn = createGetConnection(url, "3");

		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			System.out.println(response.toString());

			JSONObject jo = new JSONObject(response.toString());
			JSONArray arr = jo.getJSONArray("prices");

			// Loop through timed entries
			for (int i = 0; i < arr.length(); i++) {
				String timestamp = arr.getJSONObject(i).getString("snapshotTimeUTC");
				Date day = IGDATEFORMAT.parse(timestamp);

				int volume = arr.getJSONObject(i).getInt("lastTradedVolume");

				JSONObject openPrice = arr.getJSONObject(i).getJSONObject("openPrice");
				JSONObject closePrice = arr.getJSONObject(i).getJSONObject("closePrice");
				JSONObject highPrice = arr.getJSONObject(i).getJSONObject("highPrice");
				JSONObject lowPrice = arr.getJSONObject(i).getJSONObject("lowPrice");

				if (openPrice.isNull("ask") && closePrice.isNull("ask") && highPrice.isNull("ask")
						&& lowPrice.isNull("ask") && openPrice.isNull("bid") && closePrice.isNull("bid")
						&& highPrice.isNull("bid") && lowPrice.isNull("bid")) {
					System.out.println("Skipping " + arr.getJSONObject(i));
					continue;
				}

				try {
					double askOpen = openPrice.getDouble("ask");
					double askClose = closePrice.getDouble("ask");
					double askHigh = highPrice.getDouble("ask");
					double askLow = lowPrice.getDouble("ask");
					double bidOpen = openPrice.getDouble("bid");
					double bidClose = closePrice.getDouble("bid");
					double bidHigh = highPrice.getDouble("bid");
					double bidLow = lowPrice.getDouble("bid");

					hd.askOpen.put(day, askOpen);
					hd.askClose.put(day, askClose);
					hd.askHigh.put(day, askHigh);
					hd.askLow.put(day, askLow);
					hd.bidOpen.put(day, bidOpen);
					hd.bidClose.put(day, bidClose);
					hd.bidHigh.put(day, bidHigh);
					hd.bidLow.put(day, bidLow);
					hd.volume.put(day, (double) volume);
				} catch (Exception e) {
					System.out.println(arr.getJSONObject(i));
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			if (errorCount <= 1) {
				System.out.println("Connection exception, retrying\n" + e.getMessage());
				errorCount++;
				Thread.sleep(61000);
				getDailyAdjusted100(hd, startDate);
			} else {
				throw e;
			}
		} finally {
			conn.disconnect();
		}
	}

	public static MarginFactorData getMarginFactor(Instrument id) throws Exception {
//		System.out.println("\ngetMarginFactor " + id.getName());

		URL url = new URL("https://api.ig.com/gateway/deal/markets/" + id.getEpic());
		HttpURLConnection conn = createGetConnection(url, "3");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
//			System.out.println(response.toString());

			JSONObject jo = new JSONObject(response.toString());
			JSONObject jins = jo.getJSONObject("instrument");

			// Type
			String type = jins.getString("type");
			id.setType(type);

			// Contract size
			int contractSize = 1;
			if (!jins.isNull("contractSize")) {
				contractSize = Integer.valueOf(jins.getString("contractSize"));
			}

			// Min distances
			JSONObject jmind = jo.getJSONObject("dealingRules").getJSONObject("minControlledRiskStopDistance");
			double distance = jmind.getDouble("value");
			String unit = jmind.getString("unit");

			// Margin
			JSONArray arr = jins.getJSONArray("marginDepositBands");
			double max = arr.getJSONObject(0).getDouble("max");
			double margin = arr.getJSONObject(0).getDouble("margin");

			// TODO Commodities hack
			if (type.equals("COMMODITIES")) {
				margin = 10; // in percent
			}

			MarginFactorData md = new MarginFactorData(max, margin / 100, contractSize);
			md.setMinControlledRiskStopDistance(distance, unit);
			return md;

		} catch (Exception e) {
			if (errorCount <= 1) {
				System.out.println("Connection exception, retrying\n" + e.getMessage());
				errorCount++;
				Thread.sleep(61000);
				return getMarginFactor(id);
			} else {
				System.err.println(id.getName() + " " + id.getEpic());
				throw e;
			}
		} finally {
			conn.disconnect();
		}
	}

//	public static InstrumentData getInstrument(String symbol) throws Exception {
//		System.out.println("\ngetEpic " + symbol);
//
//		URL url = new URL("https://api.ig.com/gateway/deal/markets?searchTerm=" + symbol);
//		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		conn.setRequestMethod("GET");
//		conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
//		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//		conn.setRequestProperty("X-IG-API-KEY", "4a1a04fac44886e0811604368b983212d01ca4ce");
//		conn.setRequestProperty("VERSION", "1");
//		conn.setRequestProperty("CST", CST);
//		conn.setRequestProperty("X-SECURITY-TOKEN", XSECURITYTOKEN);
//
//		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());
//
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
//			StringBuilder response = new StringBuilder();
//			String responseLine = null;
//			while ((responseLine = br.readLine()) != null) {
//				response.append(responseLine.trim());
//			}
//			System.out.println(response.toString());
//
//			JSONObject jo = new JSONObject(response.toString());
//			JSONArray arr = jo.getJSONArray("markets");
//
//			InstrumentData data = new InstrumentData(symbol);
//			data.epic = arr.getJSONObject(0).getString("epic");
//			data.description = arr.getJSONObject(0).getString("instrumentName");
//			data.println();
//			conn.disconnect();
//			return data;
//
//		} catch (Exception e) {
//			conn.disconnect();
//			throw e;
//		}
//	}

	public static List<Instrument> getWatchlist(String watchlistName) throws Exception {
		List<WatchlistData> ws = AlphaVantageConnector.getWatchlists();
		for (WatchlistData w : ws) {
			if (w.getName().equals(watchlistName)) {
				return getWatchlistById(w.getId());
			}
		}
		return null;
	}

	private static List<Instrument> getWatchlistById(String watchlistId) throws Exception {
//		System.out.println("\ngetWatchlist");

		URL url = new URL("https://api.ig.com/gateway/deal/watchlists/" + watchlistId);
		HttpURLConnection conn = createGetConnection(url, "1");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
//			System.out.println(response.toString());

			JSONObject jo = new JSONObject(response.toString());
			JSONArray arr = jo.getJSONArray("markets");

			// Need to replace method argument with InstrumentData object
			Vector<Instrument> instruments = new Vector<Instrument>();
			// Loop through timed entries
			for (int i = 0; i < arr.length(); i++) {
				String name = arr.getJSONObject(i).getString("instrumentName");
				String epic = arr.getJSONObject(i).getString("epic");
				String expiry = arr.getJSONObject(i).getString("expiry");
				Instrument inst = Instrument.getInstrument(name, epic, expiry);
//				inst.println();
				instruments.add(inst);
			}
			return instruments;

		} catch (Exception e) {
			throw e;
		} finally {
			conn.disconnect();
		}
	}

	public static List<WatchlistData> getWatchlists() throws Exception {
//		System.out.println("\ngetWatchlist");

		URL url = new URL("https://api.ig.com/gateway/deal/watchlists/");
		HttpURLConnection conn = createGetConnection(url, "1");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
//			System.out.println(response.toString());

			JSONObject jo = new JSONObject(response.toString());
			JSONArray arr = jo.getJSONArray("watchlists");

			// Need to replace method argument with InstrumentData object
			Vector<WatchlistData> ws = new Vector<WatchlistData>();
			// Loop through timed entries
			for (int i = 0; i < arr.length(); i++) {
				boolean defaultSystemWatchlist = arr.getJSONObject(i).getBoolean("defaultSystemWatchlist");
				boolean editable = arr.getJSONObject(i).getBoolean("editable");
				String id = arr.getJSONObject(i).getString("id");
				String name = arr.getJSONObject(i).getString("name");
				WatchlistData w = new WatchlistData(defaultSystemWatchlist, editable, id, name);
//				System.out.println(w);
				ws.add(w);
			}
			return ws;

		} catch (Exception e) {
			throw e;
		} finally {
			conn.disconnect();
		}
	}

	private static HttpURLConnection createGetConnection(URL url, String version)
			throws IOException, ProtocolException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setRequestProperty("X-IG-API-KEY", API_KEY);
		conn.setRequestProperty("VERSION", version);
		conn.setRequestProperty("CST", CST);
		conn.setRequestProperty("X-SECURITY-TOKEN", XSECURITYTOKEN);
		return conn;
	}

	public static double getAccountAvailable() {
		return accountAvailable;
	}

	public static double getAccountBalance() {
		return accountBalance;
	}

}