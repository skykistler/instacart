package me.skykistler.dsm.table;

import java.util.ArrayList;

public class StringColumn extends Column {

	private ArrayList<String> data = new ArrayList<String>();

	public StringColumn(Table parent, String title) {
		super(parent, title);
	}

	public String get(int i) {
		return getRaw(i);
	}

	@Override
	public String getRaw(int i) {
		return data.get(i);
	}

	@Override
	public void addRaw(Object value) {
		data.add(value.toString());
	}

	@Override
	public int size() {
		return data.size();
	}

}
