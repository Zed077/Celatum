package com.celatum.trading;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;

public class LongPosition extends Position {

	public LongPosition(Order order, double entryPrice, HistoricalData histData) {
		super(order, entryPrice, histData);
	}

	@Override
	protected boolean hasLimitTriggered() {
		if (getLimit() > histData.bidHigh.get(0)) {
			return false;
		} else if (getLimit() <= histData.bidOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
			close(histData.getReferenceDate(), histData.bidOpen.get(0));
			return true;
		} else {
			close(histData.getReferenceDate(), getLimit());
			return true;
		}
	}

	/**
	 * TODO: case where the order was opened today
	 */
	@Override
	protected boolean hasStopTriggered() {
		switch (getStopType()) {
		case STANDARD:
			if (getStop() < histData.bidLow.get(0)) {
				return false;
			} else if (getStop() >= histData.bidOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
				close(histData.getReferenceDate(), histData.bidOpen.get(0));
				return true;
			} else {
				close(histData.getReferenceDate(), getStop());
				return true;
			}
		case TRAILING:
			// Compute trailing stop before open
			double yts = histData.bidHigh.getMaxSince(getEntryDate(), 1).getValue() - getTrailingDistance();
			// Worse case scenario i.e. stop early. It is possible that in real life the
			// stop would not have triggered
			double wts = histData.bidHigh.get(0) - getTrailingDistance();

			if (yts >= histData.bidOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
				close(histData.getReferenceDate(), histData.bidOpen.get(0));
				return true;
			} else if (wts >= histData.bidLow.get(0)) {
				close(histData.getReferenceDate(), wts);
				return true;
			} else {
				return false;
			}
		case NONE:
			return false;
		}
		throw new RuntimeException("Should never have reached this stage");
	}

	@Override
	public double getDeltaPrice() {
		if (isClosed()) {
			return this.getClosePrice() - this.getEntryPrice();
		} else {
			return histData.bidClose.get(0) - this.getEntryPrice();
		}
	}

	@Override
	public double getCosts() {
		if (isClosed()) {
			double dayDelta = (this.getCloseDate().getTime() - this.getEntryDate().getTime()) / 1000 / 60 / 60 / 24;
			return Math.max(this.getClosePrice(), this.getEntryPrice()) * getSize()
					* (BookOfRecord.ADMIN_FEE + BookOfRecord.ONE_MONTH_EUR_LIBOR) / BookOfRecord.DIVISOR * dayDelta;
		} else {
			double dayDelta = (histData.getReferenceDate().getTime() - this.getEntryDate().getTime()) / 1000 / 60 / 60 / 24;
			return Math.max(histData.bidClose.get(0), this.getEntryPrice()) * getSize()
					* (BookOfRecord.ADMIN_FEE + BookOfRecord.ONE_MONTH_EUR_LIBOR) / BookOfRecord.DIVISOR * dayDelta;
		}
	}

	@Override
	public double getMarginRequirement() {
		if (isClosed()) {
			return 0;
		}
		return histData.bidClose.get(0) * getInstrument().marginFactor.getDepositFactorPercent() * getSize();
	}

	@Override
	public double getRisk() {
		if (isClosed()) {
			return 0;
		}
		return (histData.bidClose.get(0) - getStop()) * getSize();
	}

	@Override
	protected double getDeltaLow() {
		if (isClosed()) {
			return this.getClosePrice() - this.getEntryPrice();
		} else {
			return histData.bidLow.get(0) - this.getEntryPrice();
		} 
	}

	@Override
	public void setStop(double level) {
		this.order.setStopCurrent(histData.bidClose.get(0), level, true);
	}

}
