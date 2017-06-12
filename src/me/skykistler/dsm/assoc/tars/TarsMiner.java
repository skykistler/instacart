package me.skykistler.dsm.assoc.tars;

import java.util.ArrayList;

import gnu.trove.list.array.TDoubleArrayList;

public class TarsMiner implements Median {

	private ArrayList<SequenceTree> sequences;

	public TarsMiner(BaseSequenceExtractor sequenceExtractor) {
		sequences = sequenceExtractor.getSequences();
	}

	// public void extractTars(baskets) {
	// ArrayList<Tars> sequences = extractBaseSequences(baskets);
	//
	// params = parametersEstimation(sequences, baskets);
	// sequences = sequenceFiltering(baskets, sequences, params);
	//
	// tarsTree = buildTarsTree(baskets, sequences, params);
	//
	// tars = extractTarsFromTree(tree);
	//
	// return tars;
	// }

	public void estimateParameters() {
		setMaxIntertimes();
	}

	public void sequenceFiltering() {
		// Given each sequence, remove sequences that don't satisfy their
		// assigned parameter values
	}

	public void setMaxIntertimes() {
		TDoubleArrayList medianIntertimes = new TDoubleArrayList(sequences.size());
		for (SequenceTree s : sequences)
			medianIntertimes.add(s.getMedianInterTime());

		double binSize = binSize(medianIntertimes);
		double bins = Math.ceil((medianIntertimes.max() - medianIntertimes.min()) / binSize);

		System.out.println("Intertime bin size: " + binSize);
		System.out.println("Intertime bins: " + bins);

		TDoubleArrayList bin_medianIntertimes = new TDoubleArrayList((int) (sequences.size() / bins));
		ArrayList<SequenceTree> cluster = new ArrayList<SequenceTree>((int) (sequences.size() / bins));

		for (int b = 0; b < bins; b++) {
			double bin_min = medianIntertimes.min() + b * binSize;
			double bin_max = bin_min + binSize;

			for (SequenceTree s : sequences)
				if (s.getMedianInterTime() >= bin_min && s.getMedianInterTime() < bin_max) {
					bin_medianIntertimes.add(s.getMedianInterTime());
					cluster.add(s);
				}

			if (cluster.size() < 1)
				continue;

			double max_intertime = median(bin_medianIntertimes);
			for (SequenceTree s : cluster)
				s.setMaxInterTime(max_intertime);

			System.out.println("Set max intertime to " + max_intertime + " for " + cluster.size() + " sequences");

			bin_medianIntertimes.resetQuick();
			cluster.clear();
		}
	}

	public double binSize(TDoubleArrayList x) {
		x.sort();

		int n = Math.floorDiv(x.size(), 2);
		double q1 = x.get(n / 2);
		double q3 = x.get(n + n / 2);
		double icr = q3 - q1;

		return Math.max(1 + Math.log(x.size()) / Math.log(2), Math.floor(2 * icr / Math.cbrt(x.size())));
	}

	// public void getActiveTars(baskets, t, T) {
	// That = new ArrayList<Tars>();
	// Yam = L.clone();
	//
	// baskets.sortDescending();
	//
	// for (int j = 0; j < baskets.size(); j++) {
	// int i = j - 1;
	// int aprev = Tj - Ti;
	//
	// for (X in basket[i]) {
	// for (Y in basket[j]) {
	//
	// y = X -> Y;
	// if (Yam.contains(y) || a1 <= aprev <= a2) {
	//
	// if (That.contains(y)) {
	// y.Q <- Qy + 1;
	// y.L <- Ti;
	//
	// if (y.Q > q) {
	// That.remove(y); // failing
	// Yam.remove(y);
	// }
	//
	//
	// if (y.L - Ti > q . (a1 - a2)) {
	// Yam.remove(y); // passing
	// }
	// } else {
	// That.push(y);
	// y.Q = 1;
	// y.L = Ti;
	// }
	//
	// }
	//
	// if (Yam.empty()) return That;
	// }
	// }
	// }
	//
	// return That;
	// }

}
