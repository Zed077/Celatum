package com.celatum.algos;

import com.celatum.data.HistoricalData;

public abstract class AlgoComponent {

	protected Algo algo;
	
	public abstract void setUp(HistoricalData hd);
	
	public void linkAlgo(Algo algo) {
		this.algo = algo;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public abstract AlgoComponent clone();
}
