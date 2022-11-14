package com.celatum.trading;

import com.celatum.BookOfRecord;
import com.celatum.data.HistoricalData;

public class ShortPosition extends Position {

	public ShortPosition(Order order, double entryPrice, HistoricalData histData) {
		super(order, entryPrice, histData);
	}

	@Override
	protected boolean hasLimitTriggered() {
		if (getLimit() < histData.askLow.get(0)) {
			return false;
		} else if (getLimit() >= histData.askOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
			close(histData.getReferenceDate(), histData.askOpen.get(0));
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
			if (getStop() > histData.askHigh.get(0)) {
				return false;
			} else if (getStop() <= histData.askOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
				close(histData.getReferenceDate(), histData.askOpen.get(0));
				return true;
			} else {
				close(histData.getReferenceDate(), getStop());
				return true;
			}
		case TRAILING:
			// Compute trailing stop before open
			double yts = histData.askLow.getMinSince(getEntryDate(), 1).getValue() - getTrailingDistance();
			// Worse case scenario i.e. stop early. It is possible that in real life the
			// stop would not have triggered
			double wts = histData.askLow.get(0) - getTrailingDistance();

			if (yts <= histData.askOpen.get(0) && getEntryDate().compareTo(histData.getReferenceDate()) < 0) {
				close(histData.getReferenceDate(), histData.askOpen.get(0));
				return true;
			} else if (wts <= histData.askHigh.get(0)) {
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
			return this.getEntryPrice() - this.getClosePrice();
		} else {
			return this.getEntryPrice() - histData.askClose.get(0);
		}
	}

	@Override
	public double getCosts() {
		if (isClosed()) {
			double dayDelta = (this.getCloseDate().getTime() - this.getEntryDate().getTime()) / 1000 / 60 / 60 / 24;
			return Math.max(this.getClosePrice(), this.getEntryPrice()) * getSize()
					* (BookOfRecord.ADMIN_FEE - BookOfRecord.ONE_MONTH_EUR_LIBOR) / BookOfRecord.DIVISOR * dayDelta;
		} else {
			double dayDelta = (histData.getReferenceDate().getTime() - this.getEntryDate().getTime()) / 1000 / 60 / 60 / 24;
			return Math.max(histData.askClose.get(0), this.getEntryPrice()) * getSize()
					* (BookOfRecord.ADMIN_FEE - BookOfRecord.ONE_MONTH_EUR_LIBOR) / BookOfRecord.DIVISOR * dayDelta;
		}
		
	}

	@Override
	public double getMarginRequirement() {
		if (isClosed()) {
			return 0;
		}
		return histData.askClose.get(0) * getInstrument().marginFactor.getDepositFactorPercent() * getSize();
	}

	@Override
	public double getRisk() {
		if (isClosed()) {
			return 0;
		}
		return (getStop() - histData.askClose.get(0)) * getSize();
	}

	@Override
	protected double getDeltaLow() {
		if (isClosed()) {
			return this.getEntryPrice() - this.getClosePrice();
		} else {
			return this.getEntryPrice() - histData.askHigh.get(0);
		}
		
	}

	@Override
	public void setStop(double level) {
		this.order.setStopCurrent(histData.askClose.get(0), level, true);
	}

}
