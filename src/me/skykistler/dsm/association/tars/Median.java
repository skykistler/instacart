package me.skykistler.dsm.association.tars;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public interface Median {

	public default double median(TIntArrayList x) {
		TIntArrayList sortedCopy = new TIntArrayList(x.size());

		// Remove 'breakpoints' constructed as MIN_VALUE or MAX_VALUE
		for (int i : x.toArray())
			if (i != Integer.MIN_VALUE && i != Integer.MAX_VALUE)
				sortedCopy.add(i);

		sortedCopy.sort();

		x = sortedCopy;

		int middle = x.size() / 2;
		if (middle == 0 || x.size() % 2 == 1) {
			return x.get(middle);
		} else {
			return (x.get(middle - 1) + x.get(middle)) / 2.0;
		}
	}

	public default double median(TDoubleArrayList x) {
		TDoubleArrayList sortedCopy = new TDoubleArrayList(x.size());

		// Remove 'breakpoints' constructed as NaN
		for (double d : x.toArray())
			if (!Double.isNaN(d))
				sortedCopy.add(d);

		sortedCopy.sort();

		x = sortedCopy;

		int middle = x.size() / 2;
		if (middle == 0 || x.size() % 2 == 1) {
			return x.get(middle);
		} else {
			return (x.get(middle - 1) + x.get(middle)) / 2.0;
		}
	}

	public default double median_presorted(TDoubleList x) {
		int middle = x.size() / 2;
		if (middle == 0 || x.size() % 2 == 1) {
			return x.get(middle);
		} else {
			return (x.get(middle - 1) + x.get(middle)) / 2.0;
		}
	}

}
