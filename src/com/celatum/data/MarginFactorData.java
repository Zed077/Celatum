package com.celatum.data;

public class MarginFactorData {
	private double maxPositionSize;
	private double depositFactorPercent;
	private double minControlledRiskStopDistance = 0;

	public MarginFactorData(double maxPositionSize, double depositFactorPercent) {
		this.maxPositionSize = maxPositionSize;
		this.depositFactorPercent = depositFactorPercent;
	}

	public double getMaxPositionSize() {
		return maxPositionSize;
	}

	public double getDepositFactorPercent() {
		return depositFactorPercent;
	}

	public double getMinControlledRiskStopDistance() {
		return minControlledRiskStopDistance;
	}

	void setMinControlledRiskStopDistance(double minControlledRiskStopDistance, String unit) {
		if (unit.equals("POINTS")) {
			this.minControlledRiskStopDistance = minControlledRiskStopDistance;
		}
	}
}
