package me.skykistler.dsm.assoc.tars;

import java.util.Arrays;

public class TarsMiner {

	public void extractTars(baskets) {
		ArrayList<Tars> sequences = extractBaseSequences(baskets);
		
		params = parametersEstimation(sequences, baskets);
		sequences = sequenceFiltering(baskets, sequences, params);
		
		tarsTree = buildTarsTree(baskets, sequences, params);
		
		tars = extractTarsFromTree(tree);
		
		return tars;
	}

	public void parametersEstimation(baskets, sequences) {
		// find max intertime parameter
		//		median intertime for each sequence
		//		histogram sequences by median intertime (freedman-diaconis)
		// 		set max intertime for each sequence, as the median intertime in each histo bucket
		
		// find min period occurrences parameter
		// 		break down periods of each sequence based on max intertime
		//		set minimum period occurrence for each sequence, as the median occurrence count in each period
		// 		histogram sequences by minimum period occurrence (freedman-diaconis)
		//		set minimum period occurrence for each sequence, as the median occurrence in each histo bucket
		
		// find min amount of periods parameter
		//		get periods for each sequence based on max intermine, minimum occurence account
		//		calculate per-period support as period support / total support
		//		histogram sequences by period support
		//		set sequence min amount of periods, as the median number of periods in each histo bucket
	}

	public void sequenceFiltering() {
		// Given each sequence, remove sequences that don't satisfy their
		// assigned parameter values
	}

	public void getActiveTars(baskets, t, T) {
		That = new ArrayList<Tars>();
		Yam = L.clone();
		
		baskets.sortDescending();
		
		for (int j = 0; j < baskets.size(); j++) {
			int i = j - 1;
			int aprev = Tj - Ti;
			
			for (X in basket[i]) {
				for (Y in basket[j]) {
					
					y = X -> Y;
					if (Yam.contains(y) || a1 <= aprev <= a2) {
						
						if (That.contains(y)) {
							y.Q <- Qy + 1;
							y.L <- Ti;
							
							if (y.Q > q) {
								That.remove(y); // failing
								Yam.remove(y);
							}

							
							if (y.L - Ti > q . (a1 - a2)) {
								Yam.remove(y); // passing
							}
						} else {
							That.push(y);
							y.Q = 1;
							y.L = Ti;
						}
						
					}
					
					if (Yam.empty()) return That;
				}
			}
		}
		
		return That;
	}

	public double binSize(double[] x) {
		Arrays.parallelSort(x);

		int n = Math.floorDiv(x.length, 2);
		double q1 = x[n / 2];
		double q3 = x[n + n / 2];
		double icr = q3 - q1;

		return Math.max(1 + Math.log(x.length) / Math.log(2), Math.floor(2 * icr / Math.cbrt(x.length)));
	}

}
