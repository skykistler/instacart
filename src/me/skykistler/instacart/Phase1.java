package me.skykistler.instacart;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.association.fptree.FPTree;
import me.skykistler.dsm.association.fptree.ItemSet;
import me.skykistler.dsm.association.tars.UserTransaction;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.IntColumn;
import me.skykistler.dsm.table.StringColumn;

public class Phase1 {

	public static final String ALL_BASKETS_FILE = "facts/baskets min 1000.csv";
	public static final String FREQUENT_ITEM_SETS_FILE = "processed/frequent items.csv";
	public static final int MIN_SUPPORT = 1000;

	public CSVTable basketsTable;
	public HashMap<Integer, ArrayList<UserTransaction>> basketLists;

	public FPTree root;
	public ArrayList<ItemSet> itemSets;

	public Phase1() {
		go();
	}

	public void go() {
		// try {
		// Thread.sleep(5 * 1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		loadBasketsTable();
		transformBaskets();
		freeBasketsTable();

		constructFPTree();
		growFrequentItemSets();
		sortFrequentItemSets();
		saveFrequentItemSets();

		printMemUsage();
	}

	public void loadBasketsTable() {
		loadBasketsTable(ALL_BASKETS_FILE);
	}

	public void loadBasketsTable(String fileName) {
		System.out.println("Loading baskets from " + fileName + " ...");
		long before = System.nanoTime();

		basketsTable = new CSVTable(fileName);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;

		basketsTable.dirtyPrint(10);
		System.out.println("Loaded " + basketsTable.size() + " records in " + diff + " seconds");
	}

	public void transformBaskets() {
		System.out.println("Transforming baskets...");
		long before = System.nanoTime();

		basketLists = new HashMap<Integer, ArrayList<UserTransaction>>();

		IntColumn user_id = (IntColumn) basketsTable.getColumn("user_id");
		IntColumn order_num = (IntColumn) basketsTable.getColumn("order_number");
		IntColumn product_id = (IntColumn) basketsTable.getColumn("product_id");
		IntColumn days_since = (IntColumn) basketsTable.getColumn("days_since_prior_order");

		int cur_user = -1;
		int cur_user_days = 0;
		int cur_order = -1;
		UserTransaction currentTransaction = null;

		int basketsSize = 0;
		for (int i = 0; i < basketsTable.size(); i++) {
			// If we've hit the next user/order, add the current transaction and
			// reset
			if (user_id.get(i) != cur_user || order_num.get(i) != cur_order) {

				// Add the previously current user's record
				if (currentTransaction != null) {
					if (basketLists.get(cur_user) == null)
						basketLists.put(cur_user, new ArrayList<UserTransaction>());

					basketLists.get(cur_user).add(currentTransaction);
					basketsSize++;
				}

				// If moving on to new user, set to 0
				// otherwise, increment by next days_since
				cur_user_days = cur_user != user_id.get(i) ? 0 : cur_user_days + days_since.get(i);

				cur_user = user_id.get(i);
				cur_order = order_num.get(i);

				currentTransaction = new UserTransaction(cur_user, new TIntArrayList(), cur_user_days);
			}

			currentTransaction.getItems().add(product_id.get(i));
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Transformed " + basketsSize + " baskets for " + basketLists.keySet().size() + " users in " + diff + " seconds");
	}

	public void freeBasketsTable() {
		basketsTable = null;
		System.gc();
	}

	public void constructFPTree() {
		System.out.println("Constructing FP-tree...");
		long before = System.nanoTime();

		root = new FPTree();

		int i = 0;
		for (ArrayList<UserTransaction> userBasket : basketLists.values())
			for (UserTransaction basket : userBasket) {
				root.r_insertTree(basket.getItems());

				i++;
				if (i % 100000 == 0)
					System.out.println("Seeded " + i + " baskets...");
			}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Seeded " + i + " baskets in " + diff + " seconds.");

		System.out.println("Top level has " + root.getChildren().size() + " children");
	}

	public void growFrequentItemSets() {
		System.out.println("Growing item sets...");
		long before = System.nanoTime();

		itemSets = root.r_growItemSets(null, MIN_SUPPORT, 0, 0);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Mined " + itemSets.size() + " frequent item sets in " + diff + " seconds");
	}

	public void sortFrequentItemSets() {
		System.out.println("Sorting item sets by frequency...");

		itemSets.sort(new Comparator<ItemSet>() {

			@Override
			public int compare(ItemSet o1, ItemSet o2) {
				if (o1.getSupport() == o2.getSupport())
					return 0;

				// Sort in descending order
				return o1.getSupport() > o2.getSupport() ? -1 : 1;
			}

		});
	}

	public void saveFrequentItemSets() {
		System.out.println("Saving frequent item sets...");

		// Make new table
		CSVTable itemSetsTable = new CSVTable(FREQUENT_ITEM_SETS_FILE, true);
		itemSetsTable.addColumn(new IntColumn(itemSetsTable, "support"));
		itemSetsTable.addColumn(new StringColumn(itemSetsTable, "items"));

		// Temporary record buffers
		ArrayList<Object> record = new ArrayList<Object>();
		StringBuilder items = new StringBuilder();

		for (ItemSet itemSet : itemSets) {
			// Support value
			record.add(itemSet.getSupport());

			// Construct space-separated item list
			for (int item : itemSet.toArray()) {
				if (items.length() > 0)
					items.append(" ");
				items.append(item);
			}

			// Item list value
			record.add(items.toString());

			itemSetsTable.addRecord(record);

			// Reset for next record
			record.clear();
			items.setLength(0);
		}

		itemSetsTable.save();
	}

	public void printMemUsage() {
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryL = runtime.totalMemory() - runtime.freeMemory();
		usedMemoryL /= 1024;
		double usedMemory = usedMemoryL / 1024;
		usedMemory /= 1024;
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumFractionDigits(2);
		System.out.println(formatter.format(usedMemory) + "GB of memory used");
	}

	public void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Phase1();
	}

}
