package com.celatum.trading;

import java.text.NumberFormat;
import java.util.Date;

import com.celatum.data.HistoricalData;
import com.celatum.data.Instrument;

public abstract class Order {
	public enum EntryType {
		LONG, STOPLONG, SHORT, STOPSHORT
	}

	public enum StopType {
		STANDARD, TRAILING, NONE
	}

	protected String group;
	protected Date dateCreated;
	protected Date dateCancelled;
	protected EntryType entryType;
	protected Instrument instrument;
	protected double entryPrice;
	protected double size;
	protected double limit;
	protected double stop;
	protected double trailingDistance;
	protected StopType stopType = StopType.NONE;
	private static NumberFormat nf = NumberFormat.getInstance();
	
	static {
		nf.setMaximumFractionDigits(2);
	}

	protected Order(Instrument instrument, String group, Date dateCreated, EntryType entryType, double priceLevel,
			double limit) {
		this.group = group;
		this.dateCreated = dateCreated;
		this.entryType = entryType;
		this.entryPrice = priceLevel;
		this.instrument = instrument;
		this.limit = limit;
	}

	public void cancel(Date day) {
		if (dateCancelled != null)
			throw new RuntimeException("Order was already cancelled on " + dateCancelled + " - " + day + "\n" + this);
		this.dateCancelled = day;
	}

	public Date getCancelDate() {
		return this.dateCancelled;
	}

	public String getGroup() {
		return group;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setStop(double stopPrice) {
		validateStop(entryPrice, stopPrice, false);
		this.stop = stopPrice;
		this.stopType = StopType.STANDARD;
	}

	/**
	 * Adjust the stop price if it is inferior to proposed entry price
	 * @param stopPrice
	 */
	public void setStopCorrect(double stopPrice) {
		this.stop = validateStop(entryPrice, stopPrice, true);
		this.stopType = StopType.STANDARD;
	}

	void setStopCurrent(double currentPrice, double stopPrice, boolean autocorrect) {
		this.stop = validateStop(currentPrice, stopPrice, autocorrect);
		this.stopType = StopType.STANDARD;
	}

	protected abstract double validateStop(double currentPrice, double stopPrice, boolean autoCorrect);

	public double getStop() {
		return stop;
	}

	public StopType getStopType() {
		return stopType;
	}

	public void setTrailingStop(double level, double distance) {
		double mind = instrument.marginFactor.getMinControlledRiskStopDistance();
		if (distance <= mind) {
			throw new RuntimeException("Trailing stop level must be > minRiskDistance - " + level + " > " + mind);
		}
		this.stop = level;
		this.trailingDistance = distance;
		this.stopType = StopType.TRAILING;
	}

	public double getPriceLevel() {
		return entryPrice;
	}

	public void setPriceLevel(double priceLevel) {
		this.entryPrice = priceLevel;
	}

	public double getSize() {
		return size;
	}

	/**
	 * Size limited to Tier 0 deposit band
	 * 
	 * @param size
	 */
	public void setSize(double size) {
		this.size = Math.min(size, instrument.marginFactor.getMaxPositionSize());
	}

	public void setLimit(double limit) {
		this.limit = limit;
	}

	public double getLimit() {
		return limit;
	}

	public double getMarginRequirement() {
		return entryPrice * size * instrument.marginFactor.getDepositFactorPercent();
	}

	public double getCapitalRequirement() {
		return getMarginRequirement() + Math.abs(entryPrice - stop) * size;
	}

	public void println() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		String ll = (limit == Double.MAX_VALUE || limit == 0) ? "none" :  nf.format(limit);
		
		return dateCreated.toString() + " " + group + " type " + this.entryType
				+ " size " + size + " level " + nf.format(this.entryPrice) + " stop " + nf.format(getStop())
				+ " distance " + nf.format(this.trailingDistance) + " limit " + ll;
	}

	public abstract Position hasExecuted(HistoricalData hd);

}
