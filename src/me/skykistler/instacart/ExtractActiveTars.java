package me.skykistler.instacart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.association.tars.UserTransaction;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.DecimalColumn;
import me.skykistler.dsm.table.IntColumn;
import me.skykistler.dsm.table.StringColumn;

public class ExtractActiveTars extends Phase1 {

	public static final String USERS_DPT_CLUSTER_FILE = "facts/users dpt clusters.csv";
	public static final String ALL_BASKETS_REVERSE_ORDER = "facts/baskets reverse order min 1000.csv";
	public static final String ACTIVE_TARS_FOLDER = "processed/active_tars/";

	public HashMap<String, TIntArrayList> departmentClusterUsers;

	@Override
	public void go() {
		groupUsersByDepartmentCluster();

		loadBasketsTable(ALL_BASKETS_REVERSE_ORDER);
		transformBaskets();
		freeBasketsTable();

		extractActiveTars();

		printMemUsage();
	}

	public void groupUsersByDepartmentCluster() {
		System.out.println("Grouping users by department cluster...");
		long before = System.nanoTime();

		CSVTable userDptClusters = new CSVTable(USERS_DPT_CLUSTER_FILE);

		departmentClusterUsers = new HashMap<String, TIntArrayList>();

		StringColumn department_cluster = (StringColumn) userDptClusters.getColumn("department_cluster");
		IntColumn user_id = (IntColumn) userDptClusters.getColumn("user_id");

		int cur_user = -1;
		String cur_cluster;

		int departmentClusters = 0, users = 0;
		for (int i = 0; i < userDptClusters.size(); i++) {

			if (cur_user != user_id.get(i)) {
				cur_cluster = department_cluster.get(i);
				cur_user = user_id.get(i);

				if (!departmentClusterUsers.containsKey(cur_cluster)) {
					departmentClusterUsers.put(cur_cluster, new TIntArrayList());
					departmentClusters++;
				}

				departmentClusterUsers.get(cur_cluster).add(cur_user);
				users++;
			}
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Grouped " + users + " users by " + departmentClusters + " department clusters in " + diff + " seconds");
	}

	@Override
	public void transformBaskets() {
		System.out.println("Transforming baskets...");
		long before = System.nanoTime();

		basketLists = new HashMap<Integer, ArrayList<UserTransaction>>();

		IntColumn user_id = (IntColumn) basketsTable.getColumn("user_id");
		IntColumn order_num = (IntColumn) basketsTable.getColumn("order_number");
		IntColumn product_id = (IntColumn) basketsTable.getColumn("product_id");
		IntColumn days_since = (IntColumn) basketsTable.getColumn("days_to_next_order");

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

				// If moving on to new user, set to days_to_next_order
				// otherwise, increment by next days_to_next_order
				cur_user_days = cur_user != user_id.get(i) ? days_since.get(i) : cur_user_days + days_since.get(i);

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

	public void extractActiveTars() {
		System.out.println("Extracting active Y from baskets...");
		long before = System.nanoTime();

		IntStream.range(0, departmentClusterUsers.keySet().size()).parallel().forEach(i -> processDepartmentCluster(i));

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Extracted active TARS in " + diff + " seconds");

	}

	public void processDepartmentCluster(int department_cluster_index) {
		// Get target department cluster
		String department_cluster = departmentClusterUsers.keySet().toArray(new String[0])[department_cluster_index];

		CSVTable tars = new CSVTable(TarSequencesByDpt.TAR_SEQUENCES_FOLDER + department_cluster + ".csv");
		System.out.println("Working on cluster: " + department_cluster + " with " + tars.size() + " potential TARS...");

		if (tars.size() < 1)
			return;

		HashMap<Integer, HashMap<Integer, ActiveTarRecord>> activeUserTars = new HashMap<Integer, HashMap<Integer, ActiveTarRecord>>();

		DecimalColumn support = (DecimalColumn) tars.getColumn("support");
		DecimalColumn max_intertime = (DecimalColumn) tars.getColumn("max_intertime");
		DecimalColumn median_intratime = (DecimalColumn) tars.getColumn("median_intratime");
		DecimalColumn median_period_occurrences = (DecimalColumn) tars.getColumn("median_period_occurences");
		// IntColumn num_periods = (IntColumn)
		// tars.getColumn("num_periods");

		StringColumn X = (StringColumn) tars.getColumn("X");
		StringColumn Y = (StringColumn) tars.getColumn("Y");

		HashMap<String, Integer> period_occurrences = new HashMap<String, Integer>();

		for (int user_id : departmentClusterUsers.get(department_cluster).toArray()) {
			ArrayList<UserTransaction> baskets = basketLists.get(user_id);
			period_occurrences.clear();

			activeUserTars.put(user_id, new HashMap<Integer, ActiveTarRecord>());

			for (int i = 0; i < tars.size(); i++) {

				String key = X.get(i) + " - " + Y.get(i);
				String[] xItems = X.get(i).split(" ");
				String[] yItems = Y.get(i).split(" ");

				for (UserTransaction basket : baskets) {
					// Skip baskets that are older than max_intertime
					if (basket.getDaysSinceFirstOrder() > max_intertime.get(i))
						continue;

					boolean containsAllX = true;
					for (String x : xItems) {
						if (!basket.getItems().contains(Integer.parseInt(x))) {
							containsAllX = false;
							break;
						}
					}

					// Basket must contain all X
					if (!containsAllX)
						continue;

					// Must be a later basket that contains all Y
					for (UserTransaction basket_y : baskets) {
						int intratime = basket_y.getDaysSinceFirstOrder() - basket.getDaysSinceFirstOrder();
						if (basket == basket_y || intratime < 0 || intratime > median_intratime.get(i))
							continue;

						boolean containsAllY = true;
						for (String y : yItems) {
							if (!basket_y.getItems().contains(Integer.parseInt(y))) {
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

				for (String yStr : yItems) {
					int y = Integer.parseInt(yStr);

					if (!activeUserTars.get(user_id).containsKey(y)) {
						activeUserTars.get(user_id).put(y, new ActiveTarRecord());
						activeUserTars.get(user_id).get(y).item_y = y;
					}

					ActiveTarRecord activeTar = activeUserTars.get(user_id).get(y);
					activeTar.support += support.get(i);

					activeTar.occurrences_left += median_period_occurrences.get(i) - period_occurrences.get(key);
				}

			}
		}

		saveActiveTars(department_cluster, activeUserTars);
	}

	public void saveActiveTars(String cluster, HashMap<Integer, HashMap<Integer, ActiveTarRecord>> activeUserTars) {
		System.out.println("Saving TARS for " + activeUserTars.size() + " users...");

		// Make new table
		CSVTable tarsTable = new CSVTable(ACTIVE_TARS_FOLDER + cluster + ".csv", true);
		tarsTable.addColumn(new IntColumn(tarsTable, "user_id"));
		tarsTable.addColumn(new IntColumn(tarsTable, "y"));
		tarsTable.addColumn(new DecimalColumn(tarsTable, "support"));
		tarsTable.addColumn(new DecimalColumn(tarsTable, "occurrences_left"));

		// Temporary record buffer
		ArrayList<Object> record = new ArrayList<Object>();

		for (Integer user : activeUserTars.keySet())
			for (ActiveTarRecord sequence : activeUserTars.get(user).values()) {

				record.add(user);
				record.add(sequence.item_y);
				record.add(sequence.support);
				record.add(sequence.occurrences_left);

				tarsTable.addRecord(record);

				// Reset for next record
				record.clear();
			}

		tarsTable.save();
	}

	public static void main(String[] args) {
		new ExtractActiveTars();
	}

	private class ActiveTarRecord {
		int item_y;
		int support = 0;
		double occurrences_left = 0;
	}
}
