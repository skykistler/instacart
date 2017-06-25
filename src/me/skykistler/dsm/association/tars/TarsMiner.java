package me.skykistler.dsm.association.tars;

import java.util.ArrayList;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class TarsMiner implements Median {

	private ArrayList<TarSequence> baseSequences;

	public TarsMiner(ArrayList<TarSequence> baseSequences) {
		this.baseSequences = baseSequences;
	}

	public ArrayList<TarSequence> getSequences() {
		return baseSequences;
	}

	public void estimateParameters() {
		estimateMaxIntertimes();
		estimateMinPeriodOccurences();
		estimateMinPeriods();
	}

	public void filterSequences() {
		ArrayList<TarSequence> toRemove = new ArrayList<TarSequence>();

		for (TarSequence seq : baseSequences) {
			if (!seq.hasSatisfyingIntratime())
				toRemove.add(seq);
			else if (!seq.hasSatisfyingIntertime())
				toRemove.add(seq);
		}

		baseSequences.removeAll(toRemove);
	}

	private void estimateMaxIntertimes() {
		// goal is find optimal distance between occurrences that differentiates
		// start of next period
		// use the median intertime of sequences with similar typical intertimes

		// so this finds a middle ground between a user's periods of consumption
		// vs. a user's specific sequence period of consumption

		// this approach attempts to optimize the trade-off between signal of
		// a specific sequence's recurrence vs. noise or missing recurrences

		TDoubleArrayList medianIntertimes = new TDoubleArrayList(baseSequences.size());
		for (TarSequence s : baseSequences)
			medianIntertimes.add(s.getMedianInterTime());

		double binSize = binSize(medianIntertimes);
		double min = medianIntertimes.min();
		double bins = Math.ceil((medianIntertimes.max() - min) / binSize);

		if (bins == 0 || binSize == 0) {
			for (TarSequence s : baseSequences)
				s.setMaxInterTime(s.getMedianInterTime());
			return;
		}

		System.out.println("Intertime bin size: " + binSize);
		System.out.println("Intertime bins: " + bins);

		TDoubleArrayList bin_medianIntertimes = new TDoubleArrayList((int) (baseSequences.size() / bins));
		ArrayList<TarSequence> cluster = new ArrayList<TarSequence>((int) (baseSequences.size() / bins));

		for (int b = 0; b < bins; b++) {
			double bin_min = min + b * binSize;
			double bin_max = bin_min + binSize;

			for (TarSequence s : baseSequences)
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

	private void getTemporallyCompliantPeriods() {
		// Generate list of start/end times of periods based on max intertime
		// If next sequence instance occurred after max_intertime, split period
	}

	private void estimateMinPeriodOccurences() {
		// goal is to filter out periods with low occurrence count
		// compared to sequences with similar number of occurrences per
		// intra-time period

		// For each sequence, build list of occurrences in each period

		TIntArrayList periodOccurences = new TIntArrayList();
		TDoubleArrayList medianPeriodOccurences = new TDoubleArrayList();

		int cur_period_occurrences, i;
		double median;
		for (TarSequence seq : baseSequences) {
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
		// TDoubleArrayList((int) (baseSequences.size() / bins));
		// ArrayList<TarSequence> cluster = new ArrayList<TarSequence>((int)
		// (baseSequences.size() / bins));
		//
		// for (int b = 0; b < bins; b++) {
		// double bin_min = min + b * binSize;
		// double bin_max = bin_min + binSize;
		//
		// for (TarSequence s : baseSequences)
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

	private void estimateMinPeriods() {
		// goal is to filter sequences that have relatively few recurrences
		// compared to sequences with similar occurrences:periods ratio
	}

	private double binSize(TDoubleArrayList x) {
		x.sort();

		int n = (int) Math.floor(x.size() / 2.0);

		if (n == 0)
			return x.get(0);

		double q1 = median_afterSort(x.subList(0, n));
		double q3 = median_afterSort(x.subList(n, x.size()));
		double icr = q3 - q1;

		return Math.min(1 + Math.log(x.size()) / Math.log(2), Math.floor(2 * icr / Math.cbrt(x.size())));
	}

}
