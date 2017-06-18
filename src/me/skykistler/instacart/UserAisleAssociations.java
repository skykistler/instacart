package me.skykistler.instacart;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.association.fptree.FPTree;
import me.skykistler.dsm.association.fptree.ItemSet;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.IntColumn;
import me.skykistler.dsm.table.StringColumn;

public class UserAisleAssociations {

	public static final String USER_AISLES_FILE = "facts/user aisles.csv";
	public static final String FREQUENT_AISLE_SETS_FILE = "processed/frequent_aisles.csv";
	public static final int MIN_SUPPORT = 50;
	public static final int CLUSTER_SIZE = 2;

	public CSVTable userAislesTable;
	public HashMap<Integer, TIntArrayList> userAisles = new HashMap<Integer, TIntArrayList>();

	public FPTree root;
	public ArrayList<ItemSet> itemSets;

	public UserAisleAssociations() {
		loadUserAislesTable();
		transformAisleProfiles();
		freeUserAislesTable();

		constructFPTree();
		growFrequentItemSets();

		sortFrequentItemSets();
		saveFrequentItemSets();

		printMemUsage();
	}

	public void loadUserAislesTable() {
		System.out.println("Loading user aisles...");
		long before = System.nanoTime();

		userAislesTable = new CSVTable(USER_AISLES_FILE);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;

		userAislesTable.dirtyPrint(10);
		System.out.println("Loaded " + userAislesTable.size() + " records in " + diff + " seconds");
	}

	public void transformAisleProfiles() {
		System.out.println("Transforming user aisles...");
		long before = System.nanoTime();

		IntColumn user_id = (IntColumn) userAislesTable.getColumn("user_id");
		IntColumn aisle_id = (IntColumn) userAislesTable.getColumn("aisle_id");

		int size = 0, user, aisle;
		for (int i = 0; i < userAislesTable.size(); i++) {
			user = user_id.get(i);
			aisle = aisle_id.get(i);

			if (userAisles.get(user) == null) {
				userAisles.put(user, new TIntArrayList());
				size++;
			}

			userAisles.get(user).add(aisle);
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Transformed " + size + " user aisle profiles in " + diff + " seconds");
	}

	public void freeUserAislesTable() {
		userAislesTable = null;
		System.gc();
	}

	public void constructFPTree() {
		System.out.println("Constructing FP-tree...");
		long before = System.nanoTime();

		root = new FPTree();

		int i = 0;
		for (TIntArrayList aisleProfile : userAisles.values()) {
			root.r_insertTree(aisleProfile);

			i++;
			if (i % 100000 == 0)
				System.out.println("Seeded " + i + " user aisle profiles...");
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Seeded " + i + " user aisle profiles in " + diff + " seconds.");

		System.out.println("Top level has " + root.getChildren().size() + " children");
	}

	public void growFrequentItemSets() {
		System.out.println("Growing aisle sets...");
		long before = System.nanoTime();

		itemSets = root.r_growItemSets(null, MIN_SUPPORT, CLUSTER_SIZE, CLUSTER_SIZE);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Mined " + itemSets.size() + " frequent aisle sets in " + diff + " seconds");
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
		System.out.println("Saving frequent aisle sets...");

		// Make new table
		CSVTable itemSetsTable = new CSVTable(FREQUENT_AISLE_SETS_FILE, true);
		itemSetsTable.addColumn(new IntColumn(itemSetsTable, "support"));

		// Aisle columns
		for (int i = 1; i <= CLUSTER_SIZE; i++)
			itemSetsTable.addColumn(new StringColumn(itemSetsTable, "aisle_id_" + i));

		// Temporary record buffers
		ArrayList<Object> record = new ArrayList<Object>();

		for (ItemSet itemSet : itemSets) {
			if (itemSet.size() != CLUSTER_SIZE)
				continue;

			// Support value
			record.add(itemSet.getSupport());

			// Cluster aisles
			for (int i = 0; i < CLUSTER_SIZE; i++)
				record.add(itemSet.get(i));

			itemSetsTable.addRecord(record);

			// Reset for next record
			record.clear();
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

	public static void main(String[] args) {
		new UserAisleAssociations();
	}
}
