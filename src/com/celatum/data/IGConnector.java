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

import com.celatum.data.Instrument.Source;

public class IGConnector {
	private static final int TIMEOUT = 61000;
	private static String CST;
	private static String XSECURITYTOKEN;
	private static String defaultStartDateTime = "2006-01-01T07:00:00";
//	private static String defaultStartDateTime = "2021-11-15T07:00:00";
//	private static String testEndDateTime = "2006-11-15T07:00:00";
	public static SimpleDateFormat IGDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK);
	private static IGCredentials currentCredentials;
	public static int errorCount = 0;
	private static Date validUntilTime;
	private static double accountAvailable;
	private static double accountBalance;

	/**
	 * Both tokens are initially valid for 6 hours but get extended up to a maximum
	 * of 72 hours while they are in use.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	static void connect(IGCredentials credentials) throws IOException, InterruptedException {
		// Disconnect if the credentials are different
		if (currentCredentials != null && !credentials.getKey().equals(currentCredentials.getKey())) {
			disconnect();
		}

		if (validUntilTime != null && (new Date()).before(validUntilTime))
			return;

		System.out.print("Connecting to " + credentials.getName() + " ");
		URL url = new URL("https://api.ig.com/gateway/deal/session");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setRequestProperty("VERSION", "2");
		conn.setRequestProperty("X-IG-API-KEY", credentials.getKey());

		String data = credentials.getCredentialString();
		byte[] out = data.getBytes(StandardCharsets.UTF_8);

		try {
			OutputStream stream = conn.getOutputStream();
			stream.write(out);

			System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

			if (conn.getResponseMessage().equals("Unauthorized")) {
				if (errorCount < 1) {
					System.out.print("Retrying in " + TIMEOUT / 1000 + "s ");
					errorCount++;
					Thread.sleep(TIMEOUT);
					connect(credentials);
				} else {
					System.exit(-1);
				}
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}

				JSONObject jo = new JSONObject(response.toString());

				// CH Account info
				if (credentials == IGCredentials.CH_Credentials) {
					JSONObject jaccount = jo.getJSONObject("accountInfo");
					accountAvailable = jaccount.getDouble("available");
					accountBalance = jaccount.getDouble("balance");
				}
			}

			CST = conn.getHeaderField("CST");
//			System.out.println("CST : " + CST);
			XSECURITYTOKEN = conn.getHeaderField("X-SECURITY-TOKEN");
//			System.out.println("X-SECURITY-TOKEN : " + XSECURITYTOKEN);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 350);
			validUntilTime = calendar.getTime();
			currentCredentials = credentials;
		} catch (Exception e) {
			currentCredentials = null;
			throw e;
		} finally {
			conn.disconnect();
		}
	}

	private static void disconnect() {
		validUntilTime = null;
		if (currentCredentials == null) {
			return;
		}

		try {
			System.out.print("Disconnecting from " + currentCredentials.getName() + " ");
			URL url = new URL("https://api.ig.com/gateway/deal/session");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setDoOutput(true);
			conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("VERSION", "1");
			conn.setRequestProperty("X-IG-API-KEY", currentCredentials.getKey());
			conn.setRequestProperty("CST", CST);
			conn.setRequestProperty("X-SECURITY-TOKEN", XSECURITYTOKEN);

			System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

			currentCredentials = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getHistoricalPrices(HistoricalData hd, Date startDate) throws Exception {
		System.out.print("\nIG getHistoricalPrices " + hd.instrument.getName() + ", " + startDate + " ");

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
		URL url = new URL("https://api.ig.com/gateway/deal/prices/" + hd.instrument.getCode(Source.IG_EPIC)
				+ "?resolution=DAY&pageSize=0&from=" + startDateTime + "&to=" + endDateTime);
		HttpURLConnection conn = createConnection(url, "3");

		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
//			System.out.println(response.toString());

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
					hd.splitCoef.put(day, 1);
				} catch (Exception e) {
					System.out.println(arr.getJSONObject(i));
					e.printStackTrace();
				}
			}
			errorCount = 0;
		} catch (IOException e) {
			// Get the detail of the HTTP error
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			JSONObject jo = new JSONObject(response.toString());
			String errorCode = jo.getString("errorCode");
			System.out.println(errorCode);

			// Retry if it is worth it
			if (errorCount < 1 && !errorCode.equals("error.public-api.exceeded-account-historical-data-allowance")) {
				System.out.println("Connection exception, retrying\n" + e.getMessage());
				errorCount++;
				Thread.sleep(TIMEOUT);
				getHistoricalPrices(hd, startDate);
			}
			// Tell calling method we have failed
			else {
				throw e;
			}
		} finally {
			conn.disconnect();
		}
	}

	/**
	 * Add margin factors to the instrument
	 * @param id
	 * @throws Exception
	 */
	static void augmentInstrument(Instrument id) throws Exception {
		System.out.print("augmentInstrument " + id.getName() + " ");

		String epic = id.getCode(Source.IG_EPIC);
		URL url = new URL("https://api.ig.com/gateway/deal/markets/" + epic);
		HttpURLConnection conn = createConnection(url, "3");
		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			if (errorCount == 1) {
				System.out.println(response.toString());
			}

			JSONObject jo = new JSONObject(response.toString());
			JSONObject jins = jo.getJSONObject("instrument");

			// Type
			String type = jins.getString("type");
			id.setType(type);
			if (type.equals("SHARES")) {
				id.setIGUKMultiplier(100);
			}

			// Codes
			if (!jins.isNull("chartCode")) {
				id.setCode(Source.IG_CHART_CODE, jins.getString("chartCode"));
			}
			id.setCode(Source.IG_NEWS_CODE, jins.getString("newsCode"));

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
			id.marginFactor = md;
			errorCount = 0;

		} catch (Exception e) {
			if (errorCount < 1) {
				System.out.println("Connection exception, retrying\n" + e.getMessage());
				errorCount++;
				Thread.sleep(TIMEOUT);
				augmentInstrument(id);
			} else {
				System.err.println(id.getName() + " " + epic + " " + e.getMessage());
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

	static List<Instrument> getWatchlistById(String watchlistId) throws Exception {
//		System.out.println("\ngetWatchlist");

		URL url = new URL("https://api.ig.com/gateway/deal/watchlists/" + watchlistId);
		HttpURLConnection conn = createConnection(url, "1");

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
				Instrument inst = Instrument.getInstrumentByName(name);
				inst.setCode(Source.IG_EPIC, epic);
				inst.setExpiry(expiry);
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

	static List<WatchlistData> getWatchlists() throws Exception {
//		System.out.println("\ngetWatchlist");

		URL url = new URL("https://api.ig.com/gateway/deal/watchlists/");
		HttpURLConnection conn = createConnection(url, "1");

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

	private static HttpURLConnection createConnection(URL url, String version)
			throws IOException, ProtocolException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setRequestProperty("X-IG-API-KEY", currentCredentials.getKey());
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