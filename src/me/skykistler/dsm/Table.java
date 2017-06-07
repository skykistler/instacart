package me.skykistler.dsm;

public interface Table {

	public String[] getColumns();

	public int size();

	public String get(String column, int i);

	public Column getColumn(String column);

	public default void dirtyPrint(int rows) {
		for (String h : getColumns()) {
			System.out.print(h + "   :   ");
		}

		for (int i = 0; i < rows; i++) {
			if (size() < i)
				break;

			for (String h : getColumns()) {
				System.out.print(get(h, i) + "   :   ");
			}

			System.out.println();
		}
	}
}
