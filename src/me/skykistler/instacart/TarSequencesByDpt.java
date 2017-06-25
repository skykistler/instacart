package me.skykistler.instacart;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.association.fptree.ItemSet;
import me.skykistler.dsm.association.tars.BaseSequenceExtractor;
import me.skykistler.dsm.association.tars.TarSequence;
import me.skykistler.dsm.association.tars.TarsMiner;
import me.skykistler.dsm.association.tars.UserTransaction;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.DecimalColumn;
import me.skykistler.dsm.table.IntColumn;
import me.skykistler.dsm.table.StringColumn;

public class TarSequencesByDpt extends Phase1 {

	public static final String BASKETS_FOLDER = "facts/baskets/";
	public static final String TAR_SEQUENCES_FOLDER = "processed/tars/";
	public static final int MIN_SUPPORT_BASE_SEQUENCE = 0;

	public CSVTable frequentItemSetsTable;
	public ArrayList<ItemSet> frequentItemSets;
	public ArrayList<TarSequence> baseSequences;

	public BaseSequenceExtractor sequenceExtractor;
	public TarsMiner tarsMiner;

	@Override
	public void go() {
		loadFrequentItemSets();

		File basketsFolder = new File("data/" + BASKETS_FOLDER);
		String[] department_clusters = basketsFolder.list();
		for (String cluster : department_clusters) {
			loadCluster(cluster);
			extractBaseSequences();

			if (sequenceExtractor.getSequences().size() > 0) {
				estimateParameters();

				sortTarSequences();
				saveTarSequences(cluster);
			}

			printMemUsage();
		}

	}

	public void loadCluster(String filename) {
		loadBasketsTable(BASKETS_FOLDER + filename);
		transformBaskets();
		freeBasketsTable();
	}

	public void loadFrequentItemSets() {
		System.out.println("Loading frequent item sets...");
		long before = System.nanoTime();

		frequentItemSetsTable = new CSVTable(FREQUENT_ITEM_SETS_FILE);
		IntColumn supports = (IntColumn) frequentItemSetsTable.getColumn("support");
		StringColumn items = (StringColumn) frequentItemSetsTable.getColumn("items");

		frequentItemSets = new ArrayList<ItemSet>();
		TIntArrayList itemsBuffer = new TIntArrayList();

		for (int i = 0; i < frequentItemSetsTable.size(); i++) {
			ItemSet freqSet = new ItemSet();
			freqSet.setSupport(supports.get(i));

			String[] rawItems = items.getRaw(i).split(" ");
			for (String item : rawItems) {
				itemsBuffer.add(Integer.parseInt(item));
			}

			freqSet.addAll(itemsBuffer);
			frequentItemSets.add(freqSet);

			itemsBuffer.clear();
		}

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Loaded " + frequentItemSets.size() + " frequent item sets in " + diff + " seconds");
	}

	public void extractBaseSequences() {
		System.out.println("Extracting base sequences...");
		long before = System.nanoTime();

		sequenceExtractor = new BaseSequenceExtractor(frequentItemSets);

		int i, lastSize = 0, users = 0;
		for (ArrayList<UserTransaction> userBaskets : basketLists.values()) {

			for (i = 0; i < userBaskets.size() - 1; i++)
				sequenceExtractor.processUserTransactions(userBaskets.get(i), userBaskets.subList(i + 1, userBaskets.size()));

			users++;
			TarSequence.userTimes.clear();

			if (users - lastSize == 10000) {
				lastSize = users;
				System.out.println("Extracted " + sequenceExtractor.size() + " base sequences from " + users + " users...");

				printMemUsage();
			}
		}

		sequenceExtractor.pruneMinSupport(MIN_SUPPORT_BASE_SEQUENCE);
		baseSequences = sequenceExtractor.getSequences();

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Extracted " + sequenceExtractor.size() + " base sequences in " + diff + " seconds");
	}

	public void estimateParameters() {
		System.out.println("Estimating TARS parameters...");
		long before = System.nanoTime();

		tarsMiner = new TarsMiner(sequenceExtractor.getSequences());
		tarsMiner.estimateParameters();

		tarsMiner.filterSequences();
		baseSequences = tarsMiner.getSequences();

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Estimated parameters in " + diff + " seconds");
	}

	public void sortTarSequences() {
		System.out.println("Sorting TAR sequences by frequency...");

		baseSequences.sort(new Comparator<TarSequence>() {

			@Override
			public int compare(TarSequence o1, TarSequence o2) {
				if (o1.getSupport() == o2.getSupport())
					return 0;

				// Sort in descending order
				return o1.getSupport() > o2.getSupport() ? -1 : 1;
			}

		});
	}

	public void saveTarSequences(String filename) {
		System.out.println("Saving TAR " + baseSequences.size() + " sequences...");

		// Make new table
		CSVTable baseSequencesTable = new CSVTable(TAR_SEQUENCES_FOLDER + filename, true);
		baseSequencesTable.addColumn(new DecimalColumn(baseSequencesTable, "support"));
		baseSequencesTable.addColumn(new DecimalColumn(baseSequencesTable, "max_intertime"));
		baseSequencesTable.addColumn(new DecimalColumn(baseSequencesTable, "median_intratime"));
		baseSequencesTable.addColumn(new DecimalColumn(baseSequencesTable, "median_period_occurences"));
		baseSequencesTable.addColumn(new IntColumn(baseSequencesTable, "num_periods"));
		baseSequencesTable.addColumn(new StringColumn(baseSequencesTable, "X"));
		baseSequencesTable.addColumn(new StringColumn(baseSequencesTable, "Y"));

		// Temporary record buffers
		ArrayList<Object> record = new ArrayList<Object>();
		StringBuilder items = new StringBuilder();

		for (TarSequence sequence : baseSequences) {
			// Support value
			record.add(sequence.getSupport());
			record.add(sequence.getMaxInterTime());
			record.add(sequence.getMedianIntraTime());
			record.add(sequence.getMedianPeriodOccurences());
			record.add(sequence.getNumPeriods());

			// Construct space-separated item list for X
			for (int item : sequence.getX().toArray()) {
				if (items.length() > 0)
					items.append(" ");
				items.append(item);
			}

			// Add items to record
			record.add(items.toString());
			items.setLength(0);

			// Construct space-separated item list
			for (int item : sequence.getY().toArray()) {
				if (items.length() > 0)
					items.append(" ");
				items.append(item);
			}

			// Item list value
			record.add(items.toString());

			baseSequencesTable.addRecord(record);

			// Reset for next record
			record.clear();
			items.setLength(0);
		}

		baseSequencesTable.save();
	}

	public static void main(String[] args) {
		new TarSequencesByDpt();
	}

}
