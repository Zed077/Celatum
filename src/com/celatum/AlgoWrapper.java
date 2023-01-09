package com.celatum;

import com.celatum.algos.Algo;
import com.celatum.data.HistoricalData;
import com.celatum.view.Chart;

public class AlgoWrapper implements Runnable {
	private HistoricalData hd;
	private BookOfRecord bor;
	private Algo a;
	private boolean view;

	public AlgoWrapper(Algo a, HistoricalData hd, BookOfRecord bor, boolean view) {
		this.hd = hd;
		this.bor = bor;
		this.a = a;
		this.view = view;
	}

	@Override
	public void run() {
		a.run(hd, bor);
		if (view) {
			Chart.createAndShowGUI(hd, a, bor);
		}
	}

}