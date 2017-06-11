package me.skykistler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.assoc.fptree.FPTree;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.IntColumn;

public class Main {

	public CSVTable baskets;
	public ArrayList<TIntList> basket_lists = new ArrayList<TIntList>();

	public FPTree root;
	public ArrayList<TIntArrayList> itemSets;

	public Main() {
		// try {
		// Thread.sleep(5 * 1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		loadBaskets();
		transformBaskets();

		baskets = null;
		System.gc();

		constructFPTree();
		growFrequentItemSets();
		saveFrequentItemSets();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryL = runtime.totalMemory() - runtime.freeMemory();
		usedMemoryL /= 1024;
		double usedMemory = usedMemoryL / 1024;
		usedMemory /= 1024;
		System.out.println(usedMemory + "GB of memory used");
	}

	public void loadBaskets() {
		System.out.println("Loading baskets...");
		long before = System.nanoTime();

		baskets = new CSVTable("baskets min 2000.csv");

		long diff = System.nanoTime() - before;
		baskets.dirtyPrint(10);
		System.out.println("Loaded " + baskets.size() + " records in " + diff / 1000000000 + " seconds");
	}

	public void transformBaskets() {
		System.out.println("Transforming baskets...");
		long before = System.nanoTime();

		IntColumn user_id = (IntColumn) baskets.getColumn("user_id");
		IntColumn order_num = (IntColumn) baskets.getColumn("order_number");
		IntColumn product_id = (IntColumn) baskets.getColumn("product_id");

		int cur_user = -1;
		int cur_order = -1;
		TIntList cur_list = null;

		for (int i = 0; i < baskets.size(); i++) {
			if (cur_user != user_id.get(i) || cur_order != order_num.get(i)) {
				if (cur_list != null)
					basket_lists.add(cur_list);

				cur_list = new TIntArrayList();
				cur_user = user_id.get(i);
				cur_order = order_num.get(i);
			}

			cur_list.add(product_id.get(i));
		}

		long diff = System.nanoTime() - before;
		System.out.println("Transformed " + basket_lists.size() + " baskets in " + diff / 1000000000 + " seconds");
	}

	public void constructFPTree() {
		System.out.println("Constructing FP-tree...");
		long before = System.nanoTime();

		root = new FPTree(-1, null);

		for (int i = 0; i < basket_lists.size(); i++) {
			TIntList basket = basket_lists.get(i);
			root.r_insertTree(basket);

			if (i > 0 && i % 100000 == 0)
				System.out.println("Seeded " + i + " baskets...");
		}

		long diff = System.nanoTime() - before;
		System.out.println("Seeded " + basket_lists.size() + " baskets in " + diff / 1000000000 + " seconds");
	}

	public void growFrequentItemSets() {
		System.out.println("Growing item sets...");
		long before = System.nanoTime();

		itemSets = root.r_growItemSets(null);

		long diff = System.nanoTime() - before;
		System.out.println("Mined " + itemSets.size() + " frequent item sets in " + diff / 1000000000 + " seconds");
	}

	public void saveFrequentItemSets() {
		System.out.println("Saving frequent item sets...");

		try {
			FileWriter output = new FileWriter("data/frequent items.txt");

			StringBuilder line = new StringBuilder();

			for (TIntList itemSet : itemSets) {
				for (int item : itemSet.toArray()) {
					line.append(item + " ");
				}

				line.append("\n");
				output.write(line.toString());
				line.setLength(0);
			}

			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Main();
	}

}
