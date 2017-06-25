package me.skykistler.dsm.association.tars;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;

public interface Median {

	public default double median(TDoubleList xList) {
		double[] x = xList.toArray();

		int k = (int) Math.floor((x.length - 1) / 2.0);

		int minIndex;
		double minValue, swap_i;
		for (int i = 0; i <= k; i++) {
			minIndex = i;
			minValue = x[i];

			for (int j = i + 1; j < x.length; j++) {

				if (x[j] < minValue) {
					minIndex = j;
					minValue = x[j];

					swap_i = x[i];
					x[i] = x[minIndex];
					x[minIndex] = swap_i;
				}
			}
		}

		return median_afterSort(xList);
	}

	public default double median(TIntList xList) {
		int[] x = xList.toArray();

		int k = (int) Math.floor((x.length - 1) / 2.0);

		int minIndex, minValue, swap_i;
		for (int i = 0; i <= k; i++) {
			minIndex = i;
			minValue = x[i];

			for (int j = i + 1; j < x.length; j++) {

				if (x[j] < minValue) {
					minIndex = j;
					minValue = x[j];

					swap_i = x[i];
					x[i] = x[minIndex];
					x[minIndex] = swap_i;
				}
			}
		}

		return median_afterSort(xList);
	}

	public default double median_afterSort(TIntList x) {
		int k = (int) Math.floor((x.size() - 1) / 2.0);

		if (k == 0 || x.size() % 2 == 1) {
			return x.get(k);
		} else {
			return (x.get(k - 1) + x.get(k)) / 2.0;
		}
	}

	public default double median_afterSort(TDoubleList x) {
		int k = (int) Math.floor((x.size() - 1) / 2.0);

		if (k == 0 || x.size() % 2 == 1) {
			return x.get(k);
		} else {
			return (x.get(k - 1) + x.get(k)) / 2.0;
		}
	}

}
