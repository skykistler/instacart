package me.skykistler.dsm.table;

import gnu.trove.list.array.TIntArrayList;

public class IntColumn extends Column {

	private TIntArrayList data = new TIntArrayList();

	public IntColumn(Table parent, String title) {
		super(parent, title);
	}

	public int get(int i) {
		return data.get(i);
	}

	@Override
	public String getRaw(int i) {
		return get(i) + "";
	}

	@Override
	public void addRaw(String value) {
		if (value.isEmpty())
			data.add(Integer.MIN_VALUE);
		else
			data.add(Integer.parseInt(value));
	}

	@Override
	public int size() {
		return data.size();
	}

}
