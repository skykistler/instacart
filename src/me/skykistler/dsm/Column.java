package me.skykistler.dsm;

import java.util.ArrayList;

public class Column {
	private String title;
	private Table parent;
	private ArrayList<String> data = new ArrayList<String>();

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

	public void add(String s) {
		data.add(s);
	}

	public String get(int i) {
		return data.get(i);
	}

	public int size() {
		return data.size();
	}
}
