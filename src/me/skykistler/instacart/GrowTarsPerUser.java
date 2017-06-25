package me.skykistler.instacart;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.IntStream;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.association.fptree.FPTree;
import me.skykistler.dsm.association.fptree.ItemSet;
import me.skykistler.dsm.association.tars.BaseSequenceExtractor;
import me.skykistler.dsm.association.tars.TarSequence;
import me.skykistler.dsm.association.tars.TarsMiner;
import me.skykistler.dsm.association.tars.UserTransaction;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.DecimalColumn;
import me.skykistler.dsm.table.IntColumn;

public class GrowTarsPerUser {
	public static final String BASKETS_FILE = "facts/baskets.csv";
	public static final String ACTIVE_TARS_FOLDER = "processed/active_tars_user/";
	public static final int MIN_SUPPORT = 2;
	public static final int MIN_SUPPORT_BASE_SEQUENCE = 2;

	public CSVTable basketsTable;
	public HashMap<Integer, ArrayList<UserTransaction>> basketLists;

	public GrowTarsPerUser() {
		// try {
		// Thread.sleep(5 * 1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		File outputDir = new File("data/" + ACTIVE_TARS_FOLDER);
		if (outputDir.exists())
			outputDir.delete();
		outputDir.mkdir();

		loadBasketsTable();
		transformBaskets();
		freeBasketsTable();

		@SuppressWarnings("unchecked")
		ArrayList<UserTransaction>[] baskets = basketLists.values().toArray(new ArrayList[basketLists.values().size()]);

		IntStream.range(0, baskets.length).parallel().forEach(i -> growUserTars(baskets[i]));

		printMemUsage();
	}

	public void growUserTars(ArrayList<UserTransaction> userBaskets) {
		ArrayList<ItemSet> frequentItemSets = growFrequentItemSets(constructUserFPTree(userBaskets));
		ArrayList<TarSequence> baseSequences = extractBaseSequences(userBaskets, frequentItemSets);

		if (baseSequences.size() == 0)
			return;

		extractActiveTars(userBaskets, estimateParametersAndFilter(baseSequences));
	}

	public void loadBasketsTable() {
		System.out.println("Loading baskets from " + BASKETS_FILE + " ...");
		long before = System.nanoTime();

		basketsTable = new CSVTable(BASKETS_FILE, "NA", false);

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

	public FPTree constructUserFPTree(ArrayList<UserTransaction> userBaskets) {
		long before = System.nanoTime();

		FPTree user_fpTree = new FPTree();

		int i = 0;
		for (UserTransaction basket : userBaskets) {
			user_fpTree.r_insertTree(basket.getItems());
			i++;
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Seeded " + i + " baskets in " + diff + " seconds.");

		return user_fpTree;
	}

	public ArrayList<ItemSet> growFrequentItemSets(FPTree root) {
		long before = System.nanoTime();

		ArrayList<ItemSet> itemSets = root.r_growItemSets(null, MIN_SUPPORT, 0, 0);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Mined " + itemSets.size() + " frequent item sets in " + diff + " seconds");

		return itemSets;
	}

	public void sortFrequentItemSets(ArrayList<ItemSet> itemSets) {
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

	public ArrayList<TarSequence> extractBaseSequences(ArrayList<UserTransaction> userBaskets, ArrayList<ItemSet> itemSets) {
		long before = System.nanoTime();

		BaseSequenceExtractor sequenceExtractor = new BaseSequenceExtractor(itemSets);

		for (int i = 0; i < userBaskets.size() - 1; i++)
			sequenceExtractor.processUserTransactions(userBaskets.get(i), userBaskets.subList(i + 1, userBaskets.size()));

		sequenceExtractor.pruneMinSupport(MIN_SUPPORT_BASE_SEQUENCE);
		ArrayList<TarSequence> baseSequences = sequenceExtractor.getSequences();

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Extracted " + sequenceExtractor.size() + " base sequences in " + diff + " seconds");

		return baseSequences;
	}

	public ArrayList<TarSequence> estimateParametersAndFilter(ArrayList<TarSequence> baseSequences) {
		long before = System.nanoTime();

		TarsMiner tarsMiner = new TarsMiner(baseSequences);

		tarsMiner.estimateParameters();
		tarsMiner.filterSequences();

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Estimated parameters in " + diff + " seconds");

		return tarsMiner.getSequences();
	}

	public void extractActiveTars(ArrayList<UserTransaction> userBaskets, ArrayList<TarSequence> tars) {
		HashMap<Integer, ActiveTarRecord> activeUserTars = new HashMap<Integer, ActiveTarRecord>();

		try {
			HashMap<String, Integer> period_occurrences = new HashMap<String, Integer>();

			period_occurrences.clear();

			for (int i = 0; i < tars.size(); i++) {
				TarSequence tar = tars.get(i);

				String key = tar.getX().toString() + " - " + tar.getY().toString();
				int[] xItems = tar.getX().toArray();
				int[] yItems = tar.getY().toArray();

				for (UserTransaction basket : userBaskets) {
					// Skip baskets that are older than max_intertime
					if (basket.getDaysSinceFirstOrder() > tar.getMaxInterTime())
						continue;

					boolean containsAllX = true;
					for (int x : xItems) {
						if (!basket.getItems().contains(x)) {
							containsAllX = false;
							break;
						}
					}

					// Basket must contain all X
					if (!containsAllX)
						continue;

					// Must be a later basket that contains all Y
					for (UserTransaction basket_y : userBaskets) {
						int intratime = basket_y.getDaysSinceFirstOrder() - basket.getDaysSinceFirstOrder();
						if (basket == basket_y || intratime < 0 || intratime > tar.getMedianIntraTime())
							continue;

						boolean containsAllY = true;
						for (int y : yItems) {
							if (!basket_y.getItems().contains(y)) {
								containsAllY = false;
								break;
							}
						}

						if (!containsAllY)
							continue;

						if (!period_occurrences.containsKey(key))
							period_occurrences.put(key, 0);

						// Increment existing occurrences
						int existing = period_occurrences.get(key);
						period_occurrences.put(key, existing + 1);
					}
				}

				if (!period_occurrences.containsKey(key))
					continue;

				for (int y : yItems) {

					if (!activeUserTars.containsKey(y)) {
						activeUserTars.put(y, new ActiveTarRecord());
						activeUserTars.get(y).item_y = y;
					}

					ActiveTarRecord activeTar = activeUserTars.get(y);
					activeTar.support += tar.getSupport();

					activeTar.occurrences_left += tar.getMedianPeriodOccurences() - period_occurrences.get(key);
				}

			}

			int user_id = userBaskets.get(0).getUserId();
			saveActiveTars(user_id, activeUserTars);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveActiveTars(int user_id, HashMap<Integer, ActiveTarRecord> activeUserTars) {
		System.out.println("Saving " + activeUserTars.size() + " TARS...");

		// Make new table
		CSVTable tarsTable = new CSVTable(ACTIVE_TARS_FOLDER + user_id + ".csv", true);
		tarsTable.addColumn(new IntColumn(tarsTable, "user_id"));
		tarsTable.addColumn(new IntColumn(tarsTable, "y"));
		tarsTable.addColumn(new DecimalColumn(tarsTable, "support"));
		tarsTable.addColumn(new DecimalColumn(tarsTable, "occurrences_left"));

		// Temporary record buffer
		ArrayList<Object> record = new ArrayList<Object>();

		for (ActiveTarRecord sequence : activeUserTars.values()) {

			record.add(user_id);
			record.add(sequence.item_y);
			record.add(sequence.support);
			record.add(sequence.occurrences_left);

			tarsTable.addRecord(record);

			// Reset for next record
			record.clear();
		}

		tarsTable.save();
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
		new GrowTarsPerUser();
	}

	private class ActiveTarRecord {
		int item_y;
		int support = 0;
		double occurrences_left = 0;
	}
}
