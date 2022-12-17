package com.celatum.data;

public class MarginFactorData {
	private double maxPositionSize;
	private double depositFactorPercent;
	private double minControlledRiskStopDistance = 0;
	private int contractSize;

	public MarginFactorData(double maxPositionSize, double depositFactorPercent, int contractSize) {
		this.maxPositionSize = maxPositionSize;
		this.depositFactorPercent = depositFactorPercent;
		this.contractSize = contractSize;
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

	public int getContractSize() {
		return contractSize;
	}

	void setMinControlledRiskStopDistance(double minControlledRiskStopDistance, String unit) {
		if (unit.equals("POINTS")) {
			this.minControlledRiskStopDistance = minControlledRiskStopDistance;
		}
	}
}
