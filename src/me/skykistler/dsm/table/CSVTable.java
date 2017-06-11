package me.skykistler.dsm.table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class CSVTable implements Table {

	private File file;

	private String[] columnNames;
	private HashMap<String, Column> data = new HashMap<String, Column>();

	public CSVTable(String name) {
		file = new File("data/" + name);
		load();
	}

	private void load() {
		try {
			CsvReader csv = new CsvReader(file.getAbsolutePath());

			csv.readHeaders();
			columnNames = csv.getHeaders();

			HashMap<String, ArrayList<String>> head = new HashMap<String, ArrayList<String>>();

			// Build temporary head structure
			for (String h : columnNames) {
				head.put(h, new ArrayList<String>());
			}

			// Load raw strings from the first 100 rows
			for (int i = 0; i < 100; i++) {
				boolean hadRow = csv.readRecord();

				if (!hadRow)
					break;

				for (String h : head.keySet())
					head.get(h).add(csv.get(h));
			}

			// Enumerate types from the temporary head structure
			for (String h : head.keySet()) {
				Column typedColumn = getTypedColumn(h, head.get(h));
				data.put(h, typedColumn);
			}

			// Read remaining records into the typed columns
			while (csv.readRecord()) {
				for (String h : getColumns()) {
					getColumn(h).addRaw(csv.get(h));
				}
			}

			csv.close();

			System.gc();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[] getColumns() {
		return data.keySet().toArray(new String[0]);
	}

	@Override
	public int size() {
		return getColumn(columnNames[0]).size();
	}

	@Override
	public String get(String column, int i) {
		return data.get(column).getRaw(i);
	}

	@Override
	public Column getColumn(String column) {
		return data.get(column);
	}

	/**
	 * Mini-factory method to get properly typed column
	 * 
	 * @param name
	 *            column name
	 * @param raw
	 *            string values to compare against
	 * @return appropriately typed Column, or StringColumn is none found
	 */
	private Column getTypedColumn(String name, ArrayList<String> raw) {
		int containsInts = -1;
		int containsDecimals = -1;

		for (String s : raw) {

			if (s.isEmpty()) {
				continue;
			}

			if (s.indexOf('.') > -1) {
				containsInts = 0;
				continue;
			}

			if (containsInts != 0) {
				try {
					Integer.parseInt(s);
					containsInts = 1;
				} catch (NumberFormatException e) {
					containsInts = 0;
				}
			}

			if (containsDecimals != 0) {
				try {
					Double.parseDouble(s);
					containsDecimals = 1;
				} catch (NumberFormatException e) {
					containsDecimals = 0;
				}
			}

		}

		if (containsInts == 1) {
			IntColumn ints = new IntColumn(this, name);
			ints.addRaw(raw);
			return ints;
		}

		if (containsDecimals == 1) {
			DecimalColumn decimals = new DecimalColumn(this, name);
			decimals.addRaw(raw);
			return decimals;
		}

		StringColumn strings = new StringColumn(this, name);
		strings.addRaw(raw);
		return strings;
	}

}
