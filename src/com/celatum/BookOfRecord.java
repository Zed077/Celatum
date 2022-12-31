package com.celatum;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.data.Serie;
import com.celatum.maths.Calc;
import com.celatum.trading.Order;
import com.celatum.trading.Position;

public class BookOfRecord {
	private List<Order> orders = new LinkedList<Order>();
	private List<Position> positions = new LinkedList<Position>();
	public static final double CAPITAL = 250000;
	public static final double ONE_MONTH_EUR_LIBOR = 0.104 / 100.0;
	public static final double ADMIN_FEE = 2.5 / 100.0;
	public static final double DIVISOR = 360;
	public double singlePositionRisk = 0.05; // 5%
	public Serie profitAndLoss = new Serie();
	public Serie capitalUtilisation = new Serie();

	public void addOrder(Order o) {
		if (o.getStopType() == Order.StopType.NONE)
			return;

		double moneyLeft = CAPITAL - totalCapitalRequirement(o.getDateCreated());
		if (moneyLeft <= 0) {
			return;
		}

		// TODO this will not work for instruments with a fractional min deal size
		double size = Math.floor(singlePositionRisk * moneyLeft / Math.abs(o.getPriceLevel() - o.getStop()));

		// Watch out, this operation may change the size. Always refer to the order size
		// thereafter
		o.setSize(size);

		if (o.getCapitalRequirement() > moneyLeft) {
			size = o.getSize() * moneyLeft / o.getCapitalRequirement();
			o.setSize(size);
		}

		if (Double.isNaN(size) || size <= 0) {
			return;
		}

		orders.add(o);
	}

	public List<Position> getActivePositions(Instrument inst, Date day, String group) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : getActivePositions(inst, day)) {
			if (po.getGroup().equals(group)) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Position> getActivePositions(Instrument inst, Date day) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : getActivePositions(day)) {
			// TODO assumes a position cannot close on the same day it opened
			if (po.getInstrument() == inst) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Position> getActivePositions(Date day) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : positions) {
			// TODO assumes a position cannot close on the same day it opened
			if (po.getEntryDate().compareTo(day) <= 0
					&& (po.getCloseDate() == null || po.getCloseDate().compareTo(day) > 0)) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Position> getClosedPositions(Instrument inst, Date day) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : getClosedPositions(day)) {
			if (po.getInstrument() == inst) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Position> getClosedPositions(Date day) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : positions) {
			if (po.getEntryDate().compareTo(day) <= 0 && po.getCloseDate() != null
					&& po.getCloseDate().compareTo(day) <= 0) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Position> getAllPositions(Date day) {
		ArrayList<Position> result = new ArrayList<Position>();
		for (Position po : positions) {
			if (po.getEntryDate().compareTo(day) <= 0) {
				result.add(po);
			}
		}
		return result;
	}

	public List<Order> getActiveOrders(Instrument inst, Date day, String group) {
		ArrayList<Order> result = new ArrayList<Order>();
		for (Order o : getActiveOrders(inst, day)) {
			if (o.getGroup().equals(group)) {
				result.add(o);
			}
		}
		return result;
	}

	public List<Order> getActiveOrders(Instrument inst, Date day) {
		ArrayList<Order> result = new ArrayList<Order>();
		for (Order o : getActiveOrders(day)) {
			if (o.getInstrument() == inst) {
				result.add(o);
			}
		}
		return result;
	}

	public List<Order> getActiveOrders(Date day) {
		ArrayList<Order> result = new ArrayList<Order>();
		for (Order o : orders) {
			if (o.getDateCreated().compareTo(day) <= 0
					&& (o.getCancelDate() == null || o.getCancelDate().compareTo(day) > 0)) {
				result.add(o);
			}
		}
		return result;
	}

	public List<Order> getCancelledOrders(Date day) {
		ArrayList<Order> result = new ArrayList<Order>();
		for (Order o : orders) {
			if (o.getDateCreated().compareTo(day) <= 0 && o.getCancelDate() != null
					&& o.getCancelDate().compareTo(day) <= 0) {
				result.add(o);
			}
		}
		return result;
	}

	public void cancelAllOrders(Instrument inst, Date day, String group) {
		for (Order o : getActiveOrders(inst, day, group)) {
			o.cancel(day);
		}
	}

	public void processToday(HistoricalData hd) {
		this.checkClosures(hd);
		this.checkCapitalRequirements(hd);
		this.checkEntries(hd);
		this.computeDailyStats(hd.getReferenceDate());
	}

	private void checkCapitalRequirements(HistoricalData hd) {
		double moneyLeft = CAPITAL - totalCapitalRequirement(hd.getReferenceDate());
		if (moneyLeft < 0) {
			for (Order o : this.getActiveOrders(hd.instrument, hd.getReferenceDate())) {
				o.cancel(hd.getReferenceDate());
				moneyLeft = CAPITAL - totalCapitalRequirement(hd.getReferenceDate());
				if (moneyLeft > 0) {
					return;
				}
			}
		}
		// TODO immediately close positions too
//		if (moneyLeft <0) {
//			for (Position p : this.getActivePositions(hd.instrument, hd.getReferenceDate())) {
//				p.cl
//			}
//		}
	}

	private void computeDailyStats(Date referenceDate) {
		// PnL
		double totalCosts = costsToDate(referenceDate);
		double absolutePnL = absolutePnLToDate(referenceDate);
		this.profitAndLoss.put(referenceDate, absolutePnL - totalCosts);

		// Capital Utilisation
		double margins = this.currentMarginRequirement(referenceDate);
		double activePnL = 0;
		for (Position p : getActivePositions(referenceDate)) {
			activePnL += p.getDeltaPrice() * p.getSize() - p.getCosts();
		}
		double cu = margins - activePnL;

		capitalUtilisation.put(referenceDate, cu);
	}

	public void cleanStats() {
		for (Date d : profitAndLoss.getAllDates()) {
			computeDailyStats(d);
		}
	}

	private void checkEntries(HistoricalData hd) {
		for (Order o : getActiveOrders(hd.instrument, hd.getReferenceDate())) {
			Position p = o.hasExecuted(hd);
			if (p != null) {
				o.cancel(hd.getReferenceDate());
				positions.add(p);
			}
		}
	}

	private void checkClosures(HistoricalData hd) {
		for (Position p : getActivePositions(hd.instrument, hd.getReferenceDate())) {
			if (p.getEntryDate().compareTo(hd.getReferenceDate()) > 0) {
				throw new RuntimeException(
						"Order entered in the future " + p.getEntryDate() + " < " + hd.getReferenceDate());
			} else if (p.getEntryDate().compareTo(hd.getReferenceDate()) == 0) {
				// Entered today
//				p.checkClosed();
			} else {
				p.checkClosed();
			}
		}
	}

	public Date dateOfFirstOrder() {
		Date min = new Date();
		for (Order o : orders) {
			if (o.getDateCreated().compareTo(min) < 0) {
				min = o.getDateCreated();
			}
		}
		return min;
	}

	public double currentMarginRequirement(Date referenceDate) {
		double totalMargin = 0;
		for (Order o : getActiveOrders(referenceDate)) {
			totalMargin += o.getMarginRequirement();
		}
		for (Position p : getActivePositions(referenceDate)) {
			totalMargin += p.getMarginRequirement();
		}
		return totalMargin;
	}

	public double totalCapitalRequirement(Date referenceDate) {
		double capReq = 0;
		for (Order o : getActiveOrders(referenceDate)) {
			capReq += o.getCapitalRequirement();
		}
		for (Position p : getAllPositions(referenceDate)) {
			capReq += p.getCapitalRequirement();
		}
		return capReq;
	}

	public double activeCapitalRequirement(Date referenceDate) {
		double capReq = 0;
		for (Order o : getActiveOrders(referenceDate)) {
			capReq += o.getCapitalRequirement();
		}
		for (Position p : getActivePositions(referenceDate)) {
			capReq += p.getCapitalRequirement();
		}
		return capReq;
	}

	public double costsToDate(Date referenceDate) {
		double totalCosts = 0;
		for (Position p : getAllPositions(referenceDate)) {
			totalCosts += p.getCosts();
		}
		return totalCosts;
	}

	public double absolutePnLToDate(Date referenceDate) {
		double res = 0;
		for (Position p : getAllPositions(referenceDate)) {
			res += p.absolutePnL();
		}
		return res;
	}

	public double lowestPnL() {
		double lowestPnL = CAPITAL;
		for (int i = 0; i < profitAndLoss.size(); i++) {
			lowestPnL = Math.min(lowestPnL, profitAndLoss.get(i));
		}
		return lowestPnL;
	}

	public double maxDrawdown() {
		double maxdd = 0;
		for (Position p : positions) {
			maxdd = Math.min(maxdd, p.getLargestDrawdown());
		}
		return maxdd;
	}

	public void printStats(Date referenceDate) {
		System.out.print(this.getStats(referenceDate));
	}
	
	public String getStats(Date referenceDate) {
		String res = 
				getActiveOrders(referenceDate).size() + " Active Orders, " + getCancelledOrders(referenceDate).size()
						+ " Closed Orders, " + getActivePositions(referenceDate).size() + " Active Positions, "
						+ getClosedPositions(referenceDate).size() + " Closed Positions\n";

		NumberFormat nform = NumberFormat.getInstance();
		nform.setMaximumFractionDigits(0);

		// Margin
//		System.out.println("Margin " + nform.format(currentMarginRequirement(referenceDate)));

		// Value active positions
		double returnAbsolute = absolutePnLToDate(referenceDate);
		double returnPercent = returnPercent(referenceDate);
		NumberFormat defaultFormat = NumberFormat.getPercentInstance();
		defaultFormat.setMinimumFractionDigits(1);

		res += "Value " + nform.format(returnAbsolute) + " Costs "
				+ nform.format(costsToDate(referenceDate)) + " Yearly Return " + defaultFormat.format(returnPercent) + "\n";

		// Performance
		res += "Lowest PnL " + nform.format(lowestPnL()) + " Max Drawdown "
				+ nform.format(this.maxDrawdown()) + " Avg Trade Perf " + defaultFormat.format(averagePerformance()) + "\n";

		// Active orders
		res += "\nActive Orders\n";
		for (Order o : getActiveOrders(referenceDate)) {
			res += o + "\n";
		}
		// Active positions
		res += "\nActive Positions\n";
		for (Position p : getActivePositions(referenceDate)) {
			res += p + "\n";
		}
		
		return res;
	}

	public double averagePerformance() {
		double avgP = 0;
		for (Position p : positions) {
			avgP += p.getPerformanceRatio();
		}
		avgP = avgP / (double) positions.size();
		return avgP;
	}

	public double returnPercent(Date referenceDate) {
		// End Value
		double endValue = returnAbsolute(referenceDate) + CAPITAL;

		// Period
		double deltaMillisec = referenceDate.getTime() - dateOfFirstOrder().getTime();
		double deltaYears = deltaMillisec / 31536000000.0;
		
		double returnPercent = Calc.reverseCompound(CAPITAL, endValue, deltaYears);
		return returnPercent;
	}
	
	public double returnAbsolute(Date referenceDate) {
		return absolutePnLToDate(referenceDate) - costsToDate(referenceDate);
	}

}
