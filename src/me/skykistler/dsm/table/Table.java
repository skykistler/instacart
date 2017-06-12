package me.skykistler.dsm.table;

import java.util.ArrayList;

public interface Table {

	public int size();

	public String[] getColumnNames();

	public Column getColumn(String column);

	public String get(String column, int i);

	public void addColumn(Column column);

	public void addRecord(ArrayList<Object> values);

	public default void dirtyPrint(int rows) {
		for (String h : getColumnNames()) {
			System.out.print(h + "   :   ");
		}

		System.out.println();

		for (int i = 0; i < rows; i++) {
			if (size() < i)
				break;

			for (String h : getColumnNames()) {
				System.out.print(get(h, i) + "   :   ");
			}

			System.out.println();
		}
	}
}
