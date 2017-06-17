package me.skykistler.dsm.association.tars;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public interface Median {

	public default double median(TIntArrayList x) {
		int middle = x.size() / 2;
		if (middle == 0 || x.size() % 2 == 1) {
			return x.get(middle);
		} else {
			return (x.get(middle - 1) + x.get(middle)) / 2.0;
		}
	}

	public default double median(TDoubleArrayList x) {
		int middle = x.size() / 2;
		if (middle == 0 || x.size() % 2 == 1) {
			return x.get(middle);
		} else {
			return (x.get(middle - 1) + x.get(middle)) / 2.0;
		}
	}

}
