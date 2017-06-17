package me.skykistler.dsm.table;

import java.util.ArrayList;
import java.util.HashMap;

public interface Table {

	public int size();

	public ArrayList<String> getColumnNames();

	public Column getColumn(String column);

	public String get(String column, int i);

	public void addColumn(Column column);

	public void addRecord(ArrayList<Object> values);

	public default void dirtyPrint(int rows) {
		for (String h : getColumnNames()) {
			System.out.print(h + " : ");
		}

		rows = Math.min(size(), rows);

		System.out.println();

		// Estimate column widths
		HashMap<String, Integer> maxLen = new HashMap<String, Integer>();
		int rowLen;
		for (int i = 0; i < rows; i++) {
			for (String h : getColumnNames()) {

				if (!maxLen.containsKey(h))
					maxLen.put(h, h.length());

				rowLen = get(h, i).length();

				if (maxLen.get(h) < rowLen)
					maxLen.put(h, rowLen);

			}
		}

		for (int i = 0; i < rows; i++) {
			if (size() < i)
				break;

			for (String h : getColumnNames()) {
				String val = get(h, i);

				// Pad to column width
				while (val.length() < maxLen.get(h))
					val = " " + val;

				System.out.print(val + " : ");
			}

			System.out.println();
		}
	}
}
