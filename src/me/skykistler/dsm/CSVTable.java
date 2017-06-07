package me.skykistler.dsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.csvreader.CsvReader;

public class CSVTable implements Table {

	private File file;
	private String[] headers;
	private int recordCount;
	private HashMap<String, Column> data = new HashMap<String, Column>();

	public CSVTable(String name) {
		file = new File("data/" + name);
		load();
	}

	private void load() {
		try {

			CsvReader test = new CsvReader(file.getAbsolutePath());

			test.readHeaders();
			headers = test.getHeaders();

			for (String h : headers) {
				data.put(h, new Column(this, h));
			}

			while (test.readRecord()) {
				for (String h : getColumns()) {
					getColumn(h).add(test.get(h));
				}
				recordCount++;
			}

			test.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[] getColumns() {
		return (String[]) data.keySet().toArray();
	}

	@Override
	public int size() {
		return recordCount;
	}

	@Override
	public String get(String column, int i) {
		return getColumn(column).get(i);
	}

	@Override
	public Column getColumn(String column) {
		return data.get(column);
	}

}
