package com.celatum.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.postgresql.util.PSQLException;

public class DatabaseConnector {
	private static SimpleDateFormat PGDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

	public static Date getLastUpdatedDate(Instrument inst) throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020")) {
			Statement statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(
					"SELECT day FROM historicaldata WHERE epic='" + inst.getEpic() + "' ORDER BY day DESC LIMIT 2");

			int i = 0;
			Date maxDate = null;
			while (resultSet.next()) {
				maxDate = resultSet.getDate("day");
				i++;
			}
			System.out.println("getLastUpdatedDate " + maxDate);

			if (i == 2) {
				return maxDate;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Return clean data from the database
	 * 
	 * @param hd
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static void getHistoricalData(HistoricalData hd) throws SQLException, ParseException {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020")) {
			Statement statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(
					"SELECT * FROM historicaldata WHERE epic='" + hd.instrument.getEpic() + "' ORDER BY day ASC");

			while (resultSet.next()) {
				Date day = resultSet.getDate("day");

				// Exclude Sundays or Saturdays?? Not that easy
//				Calendar gc = GregorianCalendar.getInstance();
//				gc.setTime(day);
//				if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || gc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) continue;

				int volume = resultSet.getInt("volume");
				double askHigh = resultSet.getDouble("askhigh");
				double askLow = resultSet.getDouble("asklow");
				double askOpen = resultSet.getDouble("askopen");
				double askClose = resultSet.getDouble("askclose");
				double bidHigh = resultSet.getDouble("bidhigh");
				double bidLow = resultSet.getDouble("bidlow");
				double bidOpen = resultSet.getDouble("bidopen");
				double bidClose = resultSet.getDouble("bidclose");

				hd.askOpen.put(day, askOpen);
				hd.askClose.put(day, askClose);
				hd.askHigh.put(day, askHigh);
				hd.askLow.put(day, askLow);
				hd.bidOpen.put(day, bidOpen);
				hd.bidClose.put(day, bidClose);
				hd.bidHigh.put(day, bidHigh);
				hd.bidLow.put(day, bidLow);
				hd.volume.put(day, (double) volume);
			}
		} catch (SQLException e) {
			throw e;
		}
	}

	private static void combineSatSun(HistoricalData hd) {
		hd.resetReferenceIndex();
		for (int i = 0; i < hd.fullSize(); i++) {
			Date day = hd.askClose.getDate(i);
			Calendar gc = GregorianCalendar.getInstance();
			gc.setTime(day);
			switch (gc.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				// Is there a Saturday
				Date sat = hd.askClose.getDate(i + 1);
				gc.setTime(sat);
				if (gc.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					// Combine the two
				} else {
					// Combine with Monday
				}
				break;
			case Calendar.SATURDAY:
				// Combine with Friday
				break;
			}
		}
	}

//	public static void updateInstrument(InstrumentData inst) throws SQLException {
//		instruments = null;
//
//		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
//				"Abacus2020")) {
//			Statement statement = connection.createStatement();
//			String query = "UPDATE public.instruments set epic='" + inst.epic + "', description='" + inst.description
//					+ "' where symbol='" + inst.symbol + "'";
//			System.out.println(query);
//			statement.execute(query);
//		} catch (SQLException e) {
//			throw e;
//		}
//	}

	public static void updateHistoricalData(HistoricalData hd) throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Celatum", "postgres",
				"Abacus2020")) {

			// Delete required records
			Date lastDate = hd.askClose.oldestDate();
			Statement del = connection.createStatement();
			del.executeUpdate("DELETE FROM historicaldata WHERE epic='" + hd.instrument.getEpic() + "' AND day >= '"
					+ PGDATEFORMAT.format(lastDate) + "'::date");
			System.out.println("DELETE FROM historicaldata WHERE epic='" + hd.instrument.getEpic() + "' AND day >= '"
					+ PGDATEFORMAT.format(lastDate) + "'::date");

			// Insert new records
			for (int i = 0; i < hd.size(); i++) {
				Statement insert = connection.createStatement();
				try {
					insert.executeUpdate(
							"INSERT INTO historicaldata (epic, day, askhigh, asklow, askopen, askclose, bidhigh, bidlow, bidopen, bidclose, volume) VALUES ('"
									+ hd.instrument.getEpic() + "', '" + PGDATEFORMAT.format(hd.askClose.getDate(i))
									+ "'::date, " + hd.askHigh.get(i) + ", " + hd.askLow.get(i) + ", "
									+ hd.askOpen.get(i) + ", " + hd.askClose.get(i) + ", " + hd.bidHigh.get(i) + ", "
									+ hd.bidLow.get(i) + ", " + hd.bidOpen.get(i) + ", " + hd.bidClose.get(i) + ", "
									+ hd.volume.get(i) + ")");
				} catch (PSQLException pex) {
					System.out.println(pex.getMessage());
					System.out.println(
							"INSERT INTO historicaldata (epic, day, askhigh, asklow, askopen, askclose, bidhigh, bidlow, bidopen, bidclose, volume) VALUES ('"
									+ hd.instrument.getEpic() + "', '" + PGDATEFORMAT.format(hd.askClose.getDate(i))
									+ "'::date, " + hd.askHigh.get(i) + ", " + hd.askLow.get(i) + ", "
									+ hd.askOpen.get(i) + ", " + hd.askClose.get(i) + ", " + hd.bidHigh.get(i) + ", "
									+ hd.bidLow.get(i) + ", " + hd.bidOpen.get(i) + ", " + hd.bidClose.get(i) + ", "
									+ hd.volume.get(i) + ")");
				}
			}

		} catch (SQLException e) {
			throw e;
		}
	}

}