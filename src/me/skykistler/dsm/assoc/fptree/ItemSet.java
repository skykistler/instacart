package me.skykistler.dsm.assoc.fptree;

import gnu.trove.list.array.TIntArrayList;

public class ItemSet extends TIntArrayList {

	private int support = -1;

	public void setSupport(int sup) {
		support = sup;
	}

	public int getSupport() {
		return support;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			if (i > 0)
				sb.append(" ");

			sb.append(get(i));
		}

		return sb.toString();
	}
}
