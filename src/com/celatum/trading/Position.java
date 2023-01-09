package com.celatum.trading;

import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;
import com.celatum.trading.Order.StopType;

/**
 * TODO does not work in a temporal fashion i.e. can't get PnL for a specific
 * date as it stand Solution is to associate the HistoricalData object to the
 * position when the position is created.
 * 
 * @author cedric.ladde
 *
 */
public abstract class Position {
	protected HistoricalData histData;
	protected Order order;
	private double entryPrice;
	private Date entryDate;
	private double closePrice;
	private Date closeDate;
	private double largestDrawdown;
	private double splitCoef;
	private static NumberFormat nf = NumberFormat.getInstance();
	
	static {
		nf.setMaximumFractionDigits(2);
	}

	public Position(Order order, double entryPrice, HistoricalData hd) {
		this.order = order;
		this.entryPrice = entryPrice;
		this.entryDate = hd.getReferenceDate();
		this.histData = hd;
		this.splitCoef = hd.splitCoef.get(0);
	}

	/**
	 * Auto-correct stop if not feasible
	 * 
	 * @param level
	 */
	public abstract void setStop(double level);

	public void setTrailingStop(double level, double distance) {
		this.order.setTrailingStop(level, distance);
	}

	public double getTrailingDistance() {
		return order.trailingDistance;
	}

	public void setLimit(double limit) {
		this.order.setLimit(limit);
	}

	public double getLimit() {
		return this.order.getLimit();
	}

	public String getGroup() {
		return order.getGroup();
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public Instrument getInstrument() {
		return order.getInstrument();
	}

	public double getSize() {
		return order.getSize();
	}

	public double getStop() {
		return order.getStop();
	}

	public StopType getStopType() {
		return order.getStopType();
	}

	public boolean checkClosed() {
		if (this.closeDate != null)
			throw new RuntimeException("Order already closed " + histData.getReferenceDate() + "\n" + this);

		boolean result = false;
		if (hasStopTriggered()) {
			result = true;
		} else if (hasLimitTriggered()) {
			result = true;
		}

		// Compute largest drawdown
		double currentdd = getDeltaLow() * order.getSize() - getCosts();
		this.largestDrawdown = Math.min(largestDrawdown, currentdd);

		return result;
	}

	public double getPerformanceRatio() {
		// By definition the largest draw down is <= 0
		double returnPercent = (getDeltaPrice() * order.getSize() - getCosts()) / (getEntryPrice() * order.getSize());
		double painPercent = -largestDrawdown / (getEntryPrice() * order.getSize());
		double yearlyDuration = ((double) getDaysActive()) / 365.0 + 1;

		return returnPercent / (1 + painPercent) / yearlyDuration;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public double getLargestDrawdown() {
		return largestDrawdown;
	}

	/**
	 * Number of calendar days since this position was opened
	 * 
	 * @return
	 */
	public long getDaysActive() {
		long diff;
		if (isClosed()) {
			diff = getCloseDate().getTime() - getEntryDate().getTime();
		} else {
			diff = histData.getReferenceDate().getTime() - getEntryDate().getTime();
		}
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public void close(Date d, double price) {
		this.closeDate = d;
		this.closePrice = price;
	}

	public boolean isClosed() {
		if (closeDate == null || closeDate.compareTo(histData.getReferenceDate()) > 0) {
			return false;
		}
		return true;
	}

	public void println() {
		System.out.println(this);
	}

	@Override
	public String toString() {
		String ll = (getLimit() == Double.MAX_VALUE || getLimit() == 0) ? "none" :  nf.format(getLimit());
		
		return entryDate + " " + this.getClass().getSimpleName() + " " + order.group + " size " + order.size + " entry " + nf.format(this.entryPrice) + " stop "
				+ nf.format(this.getStop()) + " limit " + ll + " exit " + nf.format(this.closePrice) + " on "
				+ this.closeDate + " PnL " + nf.format(absolutePnL() - getCosts());
	}

	protected abstract boolean hasLimitTriggered();

	protected abstract boolean hasStopTriggered();

	public abstract double getDeltaPrice();

	public double getCosts() {
		// Used for US stocks. Minimum fee of 15 USD. Charged both ways
		double commission = 0;
		if (getInstrument().getCommission() > 0) {
			commission = Math.max(getInstrument().getCommission() * this.getSize() / this.splitCoef * 2, 30);
		}

		// Used for other markets. Minimum fee of 10 EUR / GBP
		double commissionPercent = 0;
		if (getInstrument().getCommissionPercent() > 0) {
			commissionPercent = Math.max(getEntryPrice() * getSize() * getInstrument().getCommissionPercent(), 10);
			if (isClosed()) {
				commissionPercent += Math.max(getClosePrice() * getSize() * getInstrument().getCommissionPercent(), 10);
			}
		}

		return commission + commissionPercent;
	}

	public abstract double getMarginRequirement();

	public double getCapitalRequirement() {
		return getMarginRequirement() + getCosts() + getRisk() - absolutePnL();
	}

	public double absolutePnL() {
		return getDeltaPrice() * getSize();
	}

	public abstract double getRisk();

	protected abstract double getDeltaLow();
}
