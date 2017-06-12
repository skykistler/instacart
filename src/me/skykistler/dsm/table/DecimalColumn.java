package me.skykistler.dsm.table;

import gnu.trove.list.array.TDoubleArrayList;

public class DecimalColumn extends Column {

	public DecimalColumn(Table parent, String title) {
		super(parent, title);
	}

	private TDoubleArrayList data = new TDoubleArrayList();

	@Override
	public String getRaw(int i) {
		return data.get(i) + "";
	}

	@Override
	public void addRaw(Object value) {
		String raw = value.toString();

		if (raw.isEmpty())
			data.add(Double.NaN);
		else
			data.add(Double.parseDouble(raw));
	}

	@Override
	public int size() {
		return data.size();
	}

}
