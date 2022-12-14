package com.celatum.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.postgresql.util.PSQLException;

import com.celatum.data.Instrument.Source;
import com.celatum.service.json.AlgoRun;
import com.celatum.service.json.AlgoRunPosition;
import com.celatum.trading.Position;

public class DatabaseConnector {
	private static final SimpleDateFormat PGDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
	private static final SimpleDateFormat PGTIMESTAMPFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
	private static final NumberFormat NF = NumberFormat.getInstance();
	private static Connection connection;
	
	static {
		NF.setMaximumFractionDigits(1);
		
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
					"Abacus2020");
		} catch (Exception e) {
			System.err.println("Cannot connect to the database");
			e.printStackTrace();
			System.exit(-2);
		}
	}

	public static Date getLastUpdatedDate(Instrument inst, Source s) throws Exception {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT day FROM historicaldata WHERE code='" + inst.getCode(s)
				+ "' AND source='" + s + "'ORDER BY day DESC LIMIT 2");

		int i = 0;
		Date maxDate = null;
		while (resultSet.next()) {
			maxDate = resultSet.getDate("day");
			i++;
		}
		System.out.println("getLastUpdatedDate " + s.name() + " " + inst.getName() + ": " + maxDate);

		if (i == 2) {
			return maxDate;
		} else {
			return null;
		}
	}

	/**
	 * Return clean data from the database
	 * 
	 * @param hd
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static void getHistoricalData(HistoricalData hd) throws Exception {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT * FROM historicaldata WHERE code='" + hd.getCode()
				+ "' AND source='" + hd.getSource() + "' ORDER BY day ASC");

		while (resultSet.next()) {
			Date day = resultSet.getDate("day");

			long volume = resultSet.getLong("volume");
			double askHigh = resultSet.getDouble("askhigh");
			double askLow = resultSet.getDouble("asklow");
			double askOpen = resultSet.getDouble("askopen");
			double askClose = resultSet.getDouble("askclose");
			double bidHigh = resultSet.getDouble("bidhigh");
			double bidLow = resultSet.getDouble("bidlow");
			double bidOpen = resultSet.getDouble("bidopen");
			double bidClose = resultSet.getDouble("bidclose");
			int splitCoef = resultSet.getInt("split_coef");

			hd.askOpen.put(day, askOpen);
			hd.askClose.put(day, askClose);
			hd.askHigh.put(day, askHigh);
			hd.askLow.put(day, askLow);
			hd.bidOpen.put(day, bidOpen);
			hd.bidClose.put(day, bidClose);
			hd.bidHigh.put(day, bidHigh);
			hd.bidLow.put(day, bidLow);
			hd.volume.put(day, (double) volume);
			hd.splitCoef.put(day, splitCoef);
		}
		
		// Validation
		if (hd.getSource() == Source.IG_CHART_CODE) {
			combineSatSun(hd);
		} else if (hd.getSource() == Source.AV_CODE) {
//			double ask = hd.askClose.get(hd.askClose.oldestDate());
//			double bid = hd.bidClose.get(hd.askClose.oldestDate());
//			if (ask - bid != hd.instrument.getSpreadPoints()) {
//				throw new RuntimeException("Need to reload AV history for " + hd.instrument.getName() + " as spread ("
//						+ hd.instrument.getSpreadPoints() + ") does not match data (" + (ask - bid) + ")");
//			}
		}
	}

	/**
	 * For IG data only
	 * @param hd
	 */
	private static void combineSatSun(HistoricalData hd) {
		// Go through in ascending order
		Date[] dates = hd.askClose.getAllDates();
		Calendar gc = GregorianCalendar.getInstance();
		
		for (int i = 0; i < dates.length-1; i++) {
			Date day = dates[i];
			Date nextDay = dates[i+1];
			gc.setTime(day);
			if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				// Close remains unchanged
				// Open is the Sunday open
				hd.askOpen.put(nextDay, hd.askOpen.get(day));
				hd.bidOpen.put(nextDay, hd.bidOpen.get(day));
				// Low is the lowest
				hd.askLow.put(nextDay, Math.min(hd.askLow.get(day), hd.askLow.get(nextDay)));
				hd.bidLow.put(nextDay, Math.min(hd.bidLow.get(day), hd.bidLow.get(nextDay)));
				// High is the highest
				hd.askHigh.put(nextDay, Math.max(hd.askHigh.get(day), hd.askHigh.get(nextDay)));
				hd.bidHigh.put(nextDay, Math.max(hd.bidHigh.get(day), hd.bidHigh.get(nextDay)));
				// Volume is sum of both
				hd.volume.put(nextDay, hd.volume.get(day)+hd.volume.get(nextDay));
				// Remove today
				hd.askClose.remove(day);
				hd.askHigh.remove(day);
				hd.askLow.remove(day);
				hd.askOpen.remove(day);
				hd.bidClose.remove(day);
				hd.bidHigh.remove(day);
				hd.bidLow.remove(day);
				hd.bidOpen.remove(day);
				hd.volume.remove(day);
				hd.splitCoef.remove(day);
				i++; // no need to process the Monday
			}
			// US500 data does not have Fridays...
			/*
			gc.setTime(nextDay);
			if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				// Open remains unchanged
				// Close is the Saturday close
				hd.askClose.put(day, hd.askClose.get(nextDay));
				hd.bidClose.put(day, hd.bidClose.get(nextDay));
				// Low is the lowest
				hd.askLow.put(day, Math.min(hd.askLow.get(day), hd.askLow.get(nextDay)));
				hd.bidLow.put(day, Math.min(hd.bidLow.get(day), hd.bidLow.get(nextDay)));
				// High is the highest
				hd.askHigh.put(day, Math.max(hd.askHigh.get(day), hd.askHigh.get(nextDay)));
				hd.bidHigh.put(day, Math.max(hd.bidHigh.get(day), hd.bidHigh.get(nextDay)));
				// Volume is sum of both
				hd.volume.put(day, hd.volume.get(day) + hd.volume.get(nextDay));
				// Remove the next day
				hd.askClose.remove(nextDay);
				hd.askHigh.remove(nextDay);
				hd.askLow.remove(nextDay);
				hd.askOpen.remove(nextDay);
				hd.bidClose.remove(nextDay);
				hd.bidHigh.remove(nextDay);
				hd.bidLow.remove(nextDay);
				hd.bidOpen.remove(nextDay);
				hd.volume.remove(nextDay);
				i++; // no need to process the Saturday
			}
			*/
		}
	}

	public static void saveInstruments(Collection<Instrument> instruments) throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020")) {
			for (Instrument inst : instruments) {
				String AVCode = inst.getCode(Source.AV_CODE);
				if (AVCode == null) {
					AVCode = inst.getCode(Source.IG_CHART_CODE);
				}
				
				Statement statement = connection.createStatement();
				String query = "INSERT INTO public.instruments VALUES ('" + inst.getCode(Source.IG_EPIC) + "', '" + inst.getName() + "', '"
						+ inst.getExpiry() + "', '" + inst.getType() + "', '" + inst.getCode(Source.IG_CHART_CODE) + "', '"
						+ inst.getCode(Source.IG_NEWS_CODE) + "', '" + AVCode + "', '" + inst.isIGDataAvailable() + "', '"
						+ inst.getIGUKMultiplier() + "', '" + inst.marginFactor.getContractSize() + "', '"
						+ inst.marginFactor.getUnit() + "', '" + inst.marginFactor.getMaxPositionSize() + "', '"
						+ inst.marginFactor.getDepositFactorPercent() + "', '"
						+ inst.marginFactor.getMinControlledRiskStopDistance() + "') "
						+ "ON CONFLICT (epic) DO NOTHING";
//				System.out.println(query);
				statement.execute(query);

			}
		} catch (SQLException e) {
			throw e;
		}
	}

	public static void saveInstrumentStatistics(InstrumentStats stats) throws ClassNotFoundException, SQLException {
		Statement statement = connection.createStatement();
		// Save new values
		String query = "UPDATE public.instrumentstatistics SET min_contracts_atr='" + stats.getMinContractsATR()
				+ "', max_contracts_atr='" + stats.getMaxContractsATR() + "', stop_distance_atr='"
				+ stats.getStopDistanceATR() + "', latest_price='" + stats.getLatestPrice() + "', account_size='"
				+ stats.getAccountSize() + "' WHERE instrument_name='" + stats.getInstrumentName() + "'";
//		System.out.println(query);
		statement.executeUpdate(query);
		
		Date now = new Date();
		query = "UPDATE public.instruments SET stats_last_updated='" + PGDATEFORMAT.format(now)
				+ "'::date WHERE name='" + stats.getInstrumentName() + "'";
//		System.out.println(query);
		statement.executeUpdate(query);
		Instrument inst = Instrument.getInstrumentByName(stats.getInstrumentName());
		inst.setLastUpdated(now);
	}
	
	public static void createInstrumentStatisticsShell(Instrument inst) throws ClassNotFoundException, SQLException {
		// Delete existing field if it exists
		Statement statement = connection.createStatement();
				
		// Save new values
		String query = "INSERT INTO public.instrumentstatistics VALUES ('"
				+ inst.getName() + "')"
				+ "ON CONFLICT (instrument_name) DO NOTHING";
//		System.out.println(query);
		statement.executeUpdate(query);
	}
	
	public static InstrumentStats getInstrumentStatistics(Instrument inst, Source s) throws SQLException, ClassNotFoundException {
		Statement statement = connection.createStatement();
		String code = inst.getCode(s);
		String query = "SELECT * FROM public.instrumentstatistics WHERE instrument_code='" + code + "'";
		
		ResultSet resultSet = statement.executeQuery(query);
		InstrumentStats stats = null;
		while (resultSet.next()) {
			String instrumentName = inst.getName();
			String instrumentCode = code;
			double maxContractsATR = resultSet.getDouble("max_contracts_atr");
			double minContractsATR = resultSet.getDouble("min_contracts_atr");
			double accountSize = resultSet.getDouble("account_size");
			double latestPrice = resultSet.getDouble("latest_price");
			double stopDistanceATR = resultSet.getDouble("stop_distance_atr");
			stats = new InstrumentStats(instrumentName, instrumentCode, maxContractsATR, minContractsATR, accountSize,
					latestPrice, stopDistanceATR);
		}
		return stats;
	}

	public static void saveAlgoRun(String algoRunRef, Date runDate, String type, String algoDescription,
			String algoStatistics, Source s) throws SQLException, ClassNotFoundException {
		// Try updating first
		algoDescription = algoDescription.replaceAll("'", "''");
		algoStatistics = algoStatistics.replaceAll("'", "''");

		Statement statement = connection.createStatement();
		String query = "INSERT INTO public.algorun VALUES ('"
				+ algoRunRef + "', '"
				+ PGTIMESTAMPFORMAT.format(runDate) + "'::timestamp, '" 
				+ type + "', '"
				+ algoDescription + "', '"
				+ algoStatistics + "', '"
				+ s + "')";
//		System.out.println(query);
		statement.execute(query);
	}
	
	/**
	 * Return the instrument codes of the saved histories
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static Collection<HistoricalData> getSavedHistories() throws SQLException, ClassNotFoundException {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT DISTINCT code, source FROM public.historicaldata");

		ArrayList<HistoricalData> res = new ArrayList<>();
		while (resultSet.next()) {			
			String code = resultSet.getString("code");
			Source s = Source.valueOf(resultSet.getString("source"));
			HistoricalData hd = new HistoricalData(Instrument.getInstrumentByCode(code), s);
			res.add(hd);
		}
		return res;
	}
	
	public static Collection<AlgoRun> getAlgoRuns() throws SQLException, ClassNotFoundException {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT * FROM public.algorun ORDER BY run_date DESC");

		ArrayList<AlgoRun> res = new ArrayList<>();
		while (resultSet.next()) {			
			String algoRunRef = resultSet.getString("algo_run_ref");
			Date runDate = resultSet.getTimestamp("run_date");
			String type = resultSet.getString("type");
			String algoDescription = resultSet.getString("algo_description");
			String algoStatistics = resultSet.getString("algo_statistics");

			AlgoRun ar = new AlgoRun(algoRunRef, runDate, type, algoDescription, algoStatistics);
			res.add(ar);
		}
		return res;
	}
	
	public static Source getAlgoRunSource(String algoRunRef) throws ClassNotFoundException, SQLException {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT source FROM public.algorun WHERE algo_run_ref='" + algoRunRef + "'");

		String source = null;
		while (resultSet.next()) {			
			source = resultSet.getString("source");
		}
		return Source.valueOf(source);
	}
	
	public static Collection<Instrument> getAlgoRunInstruments(String algoRunRef) throws ClassNotFoundException, SQLException {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(
				"SELECT DISTINCT instrument_name FROM public.algorunpositions WHERE algo_run_ref='" + algoRunRef + "'");

		ArrayList<Instrument> res = new ArrayList<>();
		while (resultSet.next()) {
			String instrument_name = resultSet.getString("instrument_name");
			Instrument inst = Instrument.getInstrumentByName(instrument_name);
			res.add(inst);
		}
		return res;
	}
	
	public static Serie getAlgoRunPnL(String algoRunRef) throws ClassNotFoundException, SQLException {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement
				.executeQuery("SELECT * FROM public.algorunpnl WHERE algo_run_ref='" + algoRunRef + "'");

		Serie res = new Serie();
		while (resultSet.next()) {
			Date date = resultSet.getDate("date");
			double value = resultSet.getDouble("value");
			res.put(date, value);
		}
		return res;
	}
	
	public static Collection<AlgoRunPosition> getAlgoRunPositions(String algoRunRef, String instrumentName) throws ClassNotFoundException, SQLException {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT * FROM public.algorunpositions WHERE algo_run_ref='"
				+ algoRunRef + "' AND instrument_name='" + instrumentName + "'");

		ArrayList<AlgoRunPosition> res = new ArrayList<>();
		while (resultSet.next()) {
			Date entryDate = resultSet.getDate("entry_date");
			double entryPrice = resultSet.getDouble("entry_price");
			Date closeDate = resultSet.getDate("close_date");
			double closePrice = resultSet.getDouble("close_price");
			String entryDesc = resultSet.getString("entry_desc");
			String closeDesc = resultSet.getString("close_desc");

			AlgoRunPosition pos = new AlgoRunPosition(algoRunRef, entryDate, entryPrice, closeDate, closePrice, instrumentName, entryDesc, closeDesc);
			res.add(pos);
		}
		return res;
	}

	public static void saveAlgoRunPositions(String algoRunRef, List<Position> positions) throws SQLException {
		Statement statement = connection.createStatement();
		for (Position p : positions) {
			// Instrument Name
			String instrumentName = p.getInstrument().getName();
			
			// Entry Desc
			String limit = (p.getLimit() == Double.MAX_VALUE) ? "Infinity" : NF.format(p.getLimit());
			String entryDesc = (p.getClass().getSimpleName().equals("LongPosition")) ? "Long " : "Short ";
			entryDesc += p.getSize() + "@" + NF.format(p.getEntryPrice())
			// + " " + p.getGroup()
					+ " stop=" + NF.format(p.getStop()) + " limit=" + limit;

			// Exit Desc
			String closeDesc = (p.getClass().getSimpleName().equals("LongPosition")) ? "-" : "+";
			closeDesc += p.getSize() + "@" + NF.format(p.getClosePrice())
			// + " " + p.getGroup()
					+ " PnL=" + NF.format(p.absolutePnL() - p.getCosts());
			
			
			if (p.isClosed()) {
				String query = "INSERT INTO public.algorunpositions VALUES ('"
						+ algoRunRef + "', '"
						+ PGDATEFORMAT.format(p.getEntryDate()) + "'::date, '" 
						+ p.getEntryPrice() + "', '" 
						+ PGDATEFORMAT.format(p.getCloseDate()) + "'::date, '"
						+ p.getClosePrice() + "', '"
						+ instrumentName + "', '"
						+ entryDesc + "', '"
						+ closeDesc + "')";
//						System.out.println(query);
				statement.execute(query);
			} else {
				String query = "INSERT INTO public.algorunpositions (algo_run_ref, entry_date, entry_price, instrument_name, entry_desc, close_desc) VALUES ('"
						+ algoRunRef + "', '"
						+ PGDATEFORMAT.format(p.getEntryDate()) + "'::date, '" 
						+ p.getEntryPrice() + "', '"
						+ instrumentName + "', '"
						+ entryDesc + "', '"
						+ closeDesc + "')";
//						System.out.println(query);
				statement.execute(query);
			}
		}
	}
	
	public static void saveAlgoRunPnL(String algoRunRef, Serie pnl) throws SQLException {
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020");
		for (SerieItem p : pnl.getAllEntries()) {
			Statement statement = connection.createStatement();
			String query = "INSERT INTO public.algorunpnl VALUES ('" + algoRunRef + "', '"
					+ PGDATEFORMAT.format(p.getDate()) + "'::date, '" + p.getValue() + "')";
//				System.out.println(query);
			statement.execute(query);
		}
	}

	public static void loadInstruments() throws Exception {
		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("SELECT * FROM public.instruments");

		while (resultSet.next()) {
			String name = resultSet.getString("name");
			String epic = resultSet.getString("epic");
			String expiry = resultSet.getString("expiry");

			Instrument inst = Instrument.getInstrumentByName(name);
			inst.setCode(Source.IG_EPIC, epic);
			inst.setExpiry(expiry);
			inst.setCode(Source.IG_CHART_CODE, resultSet.getString("chartCode"));
			inst.setCode(Source.IG_NEWS_CODE, resultSet.getString("newsCode"));
			inst.setType(resultSet.getString("type"));
			inst.setIGDataAvailable(resultSet.getBoolean("ig_data_available"));
			inst.setIGUKMultiplier(resultSet.getInt("ig_uk_multiplier"));
			inst.setCode(Source.AV_CODE, resultSet.getString("av_code"));
			inst.setLastUpdated(resultSet.getDate("stats_last_updated"));
			inst.setSpreadPoints(resultSet.getDouble("spread_points"));
			inst.setCommission(resultSet.getDouble("commission"));
			inst.setCommissionPercent(resultSet.getDouble("commission_percent"));

			double maxPositionSize = resultSet.getDouble("maxPositionSize");
			double depositFactorPercent = resultSet.getDouble("depositFactorPercent");
			int contractSize = resultSet.getInt("contractSize");
			MarginFactorData mf = new MarginFactorData(maxPositionSize, depositFactorPercent, contractSize);

			double minControlledRiskStopDistance = resultSet.getDouble("minControlledRiskStopDistance");
			String unit = resultSet.getString("unit");
			mf.setMinControlledRiskStopDistance(minControlledRiskStopDistance, unit);

			inst.marginFactor = mf;
		}
	}

	public static void updateHistoricalData(HistoricalData hd) throws SQLException {
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020");

		String code = hd.getCode();
		Statement statement = connection.createStatement();

		// Delete required records
		Date lastDate = hd.askClose.oldestDate();
		statement.executeUpdate("DELETE FROM historicaldata WHERE code='" + code + "' AND day >= '"
				+ PGDATEFORMAT.format(lastDate) + "'::date");
//		System.out.println("DELETE FROM historicaldata WHERE code='" + code + "' AND day >= '"
//				+ PGDATEFORMAT.format(lastDate) + "'::date");

		// Insert new records
		for (int i = 0; i < hd.size(); i++) {
			String sql = "INSERT INTO historicaldata (code, day, askhigh, asklow, askopen, askclose, bidhigh, bidlow, bidopen, bidclose, volume, split_coef, source) VALUES ('"
					+ code + "', '" + PGDATEFORMAT.format(hd.askClose.getDate(i)) + "'::date, "
					+ hd.askHigh.get(i) + ", " + hd.askLow.get(i) + ", " + hd.askOpen.get(i) + ", "
					+ hd.askClose.get(i) + ", " + hd.bidHigh.get(i) + ", " + hd.bidLow.get(i) + ", "
					+ hd.bidOpen.get(i) + ", " + hd.bidClose.get(i) + ", " + String.format("%.0f", hd.volume.get(i)) + ", " 
					+ (int) hd.splitCoef.get(i) + ", '" + hd.getSource( ) +"')";
			
			try {
				statement.executeUpdate(sql);
			} catch (PSQLException pex) {
				System.err.println(pex.getMessage());
				System.err.println(sql);
			}
		}
	}

}
