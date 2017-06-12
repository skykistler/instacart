package me.skykistler.dsm.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class CSVTable implements Table {

	private File file;

	private String[] columnNames = new String[0];
	private HashMap<String, Column> data = new HashMap<String, Column>();

	public CSVTable(String name) {
		this(name, false);
	}

	public CSVTable(String name, boolean overwrite) {
		file = new File("data/" + name);

		if (!overwrite)
			load();
	}

	private void load() {
		try {
			CsvReader csv = new CsvReader(file.getAbsolutePath());

			csv.readHeaders();
			columnNames = csv.getHeaders();

			HashMap<String, ArrayList<Object>> head = new HashMap<String, ArrayList<Object>>();

			// Build temporary head structure
			for (String h : columnNames) {
				head.put(h, new ArrayList<Object>());
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
				addColumn(typedColumn);
			}

			// Read remaining records into the typed columns
			while (csv.readRecord()) {
				for (String h : getColumnNames()) {
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

	public void save() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			StringBuilder line = new StringBuilder();
			int h;

			// Print column names
			for (h = 0; h < columnNames.length; h++) {
				line.append(columnNames[h]);

				if (h != columnNames.length - 1)
					line.append(",");
			}

			// Write line and reset
			writer.write(line.toString());
			writer.newLine();
			line.setLength(0);

			for (int i = 0; i < size(); i++) {

				// Append each column value
				for (h = 0; h < columnNames.length; h++) {
					line.append(get(columnNames[h], i));

					if (h != columnNames.length - 1)
						line.append(",");
				}

				// Write line and reset
				writer.write(line.toString());
				writer.newLine();
				line.setLength(0);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int size() {
		return getColumn(columnNames[0]).size();
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public Column getColumn(String column) {
		return data.get(column);
	}

	@Override
	public String get(String column, int i) {
		return data.get(column).getRaw(i);
	}

	@Override
	public void addColumn(Column column) {
		data.put(column.getTitle(), column);
		columnNames = data.keySet().toArray(new String[0]);
	}

	@Override
	public void addRecord(ArrayList<Object> values) {
		for (int i = 0; i < columnNames.length; i++) {
			getColumn(columnNames[i]).addRaw(values.get(i));
		}
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
	private Column getTypedColumn(String name, ArrayList<Object> raw) {
		int containsInts = -1;
		int containsDecimals = -1;

		for (Object value : raw) {
			String s = value.toString();

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
