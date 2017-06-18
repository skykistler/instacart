package me.skykistler.dsm.association.tars;

import java.util.ArrayList;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class TarsMiner implements Median {

	private ArrayList<TarSequence> sequences;

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
		estimateMaxIntertimes();
		estimateMinPeriodOccurences();
		estimateMinPeriods();
	}

	public void filterSequences() {
		ArrayList<TarSequence> toRemove = new ArrayList<TarSequence>();

		for (TarSequence seq : sequences) {
			if (!seq.hasSatisfyingIntratime())
				toRemove.add(seq);
			else if (!seq.hasSatisfyingIntertime())
				toRemove.add(seq);
		}

		sequences.removeAll(toRemove);
	}

	public void estimateMaxIntertimes() {
		TDoubleArrayList medianIntertimes = new TDoubleArrayList(sequences.size());
		for (TarSequence s : sequences)
			medianIntertimes.add(s.getMedianInterTime());

		double binSize = binSize(medianIntertimes);
		double min = medianIntertimes.min();
		double bins = Math.ceil((medianIntertimes.max() - min) / binSize);

		if (bins == 0 || binSize == 0) {
			for (TarSequence s : sequences)
				s.setMaxInterTime(s.getMedianInterTime());
			return;
		}

		System.out.println("Intertime bin size: " + binSize);
		System.out.println("Intertime bins: " + bins);

		TDoubleArrayList bin_medianIntertimes = new TDoubleArrayList((int) (sequences.size() / bins));
		ArrayList<TarSequence> cluster = new ArrayList<TarSequence>((int) (sequences.size() / bins));

		for (int b = 0; b < bins; b++) {
			double bin_min = min + b * binSize;
			double bin_max = bin_min + binSize;

			for (TarSequence s : sequences)
				if (s.getMedianInterTime() >= bin_min && s.getMedianInterTime() < bin_max) {
					bin_medianIntertimes.add(s.getMedianInterTime());
					cluster.add(s);
				}

			if (cluster.size() < 1)
				continue;

			double max_intertime = median(bin_medianIntertimes);
			for (TarSequence s : cluster)
				s.setMaxInterTime(max_intertime);

			System.out.println("Set max intertime to " + max_intertime + " for " + cluster.size() + " sequences");

			bin_medianIntertimes.resetQuick();
			cluster.clear();
		}
	}

	public void getTemporallyCompliantPeriods() {
		// Generate list of start/end times of periods based on max intertime
		// If next sequence instance occurred after max_intertime, split period
	}

	public void estimateMinPeriodOccurences() {
		// For each sequence, build list of occurrences in each period

		TIntArrayList periodOccurences = new TIntArrayList();
		TDoubleArrayList medianPeriodOccurences = new TDoubleArrayList();

		int cur_period_occurrences, i;
		double median;
		for (TarSequence seq : sequences) {
			cur_period_occurrences = 1;

			for (i = 0; i < seq.getIntertimes().size(); i++) {

				// increment occurrences if next intertime < max_intertime
				if (seq.getIntertimes().get(i) <= seq.getMaxInterTime()) {
					cur_period_occurrences++;
				}
				// otherwise, add cur_period_occurences to median list
				else {
					periodOccurences.add(cur_period_occurrences);
					cur_period_occurrences = 1;
				}
			}
			// Add the last count
			periodOccurences.add(cur_period_occurrences);

			// Infer number of temporally compliant periods from the list size
			seq.setNumPeriods(periodOccurences.size());

			median = median(periodOccurences);
			seq.setMedianPeriodOccurences(median);
			medianPeriodOccurences.add(median);

			periodOccurences.resetQuick();
		}

		// groupSimilar(median_period_occurence)
		// Set each groups minimum period occurrence to the group's median
		// period occurrence

		// double binSize = binSize(medianPeriodOccurences);
		// double min = medianPeriodOccurences.min();
		// double bins = Math.ceil((medianPeriodOccurences.max() - min) /
		// binSize);
		//
		// System.out.println("Q bin size: " + binSize);
		// System.out.println("Q bins: " + bins);
		//
		// TDoubleArrayList bin_medianPeriodOccurences = new
		// TDoubleArrayList((int) (sequences.size() / bins));
		// ArrayList<TarSequence> cluster = new ArrayList<TarSequence>((int)
		// (sequences.size() / bins));
		//
		// for (int b = 0; b < bins; b++) {
		// double bin_min = min + b * binSize;
		// double bin_max = bin_min + binSize;
		//
		// for (TarSequence s : sequences)
		// if (s.getMedianPeriodOccurences() >= bin_min &&
		// s.getMedianPeriodOccurences() < bin_max) {
		// bin_medianPeriodOccurences.add(s.getMedianPeriodOccurences());
		// cluster.add(s);
		// }
		//
		// if (cluster.size() < 1)
		// continue;
		//
		// double min_period_occurences = median(bin_medianPeriodOccurences);
		// for (TarSequence s : cluster)
		// s.setMinPeriodOccurences(min_period_occurences);
		//
		// System.out.println("Set min period occurences to " +
		// min_period_occurences + " for " + cluster.size() + " sequences");
		//
		// bin_medianPeriodOccurences.resetQuick();
		// cluster.clear();
		// }
	}

	public void estimateMinPeriods() {

	}

	public double binSize(TDoubleArrayList x) {
		x.sort();

		int n = (int) Math.floor(x.size() / 2.0);

		if (n == 0)
			return x.get(0);

		double q1 = median_presorted(x.subList(0, n));
		double q3 = median_presorted(x.subList(n, x.size()));
		double icr = q3 - q1;

		return Math.min(1 + Math.log(x.size()) / Math.log(2), Math.floor(2 * icr / Math.cbrt(x.size())));
	}

	public ArrayList<TarSequence> getSequences() {
		return sequences;
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
