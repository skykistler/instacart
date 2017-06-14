package me.skykistler.dsm.table;

import java.util.ArrayList;

public abstract class Column {
	private String title;
	private Table parent;

	public Column(Table parent, String title) {
		this.parent = parent;
		this.title = title;
	}

	public Table getParent() {
		return parent;
	}

	public String getTitle() {
		return title;
	}

	public abstract String getRaw(int i);

	public abstract void addRaw(Object value);

	public void addRaw(ArrayList<Object> raw) {
		for (Object value : raw) {
			addRaw(value);
		}
	}

	public abstract int size();
}
