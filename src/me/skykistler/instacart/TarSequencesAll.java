package me.skykistler.instacart;

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

public class TarSequencesAll extends Phase1 {

	public static final int MIN_SUPPORT_BASE_SEQUENCE = 50;
	public static final String BASE_SEQUENCES_FILE = "processed/base sequences.csv";

	public CSVTable frequentItemSetsTable;
	public ArrayList<ItemSet> frequentItemSets;
	public ArrayList<TarSequence> baseSequences;

	public BaseSequenceExtractor sequenceExtractor;
	public TarsMiner tarsMiner;

	@Override
	public void go() {
		// wait(15);

		loadBasketsTable();
		transformBaskets();
		freeBasketsTable();

		// wait(5);

		loadFrequentItemSets();

		extractBaseSequences();

		estimateParameters();

		sortBaseSequences();
		saveBaseSequences();

		printMemUsage();
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

	public void sortBaseSequences() {
		System.out.println("Sorting base sequences by frequency...");

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

	public void saveBaseSequences() {
		System.out.println("Saving base sequences...");

		// Make new table
		CSVTable baseSequencesTable = new CSVTable(BASE_SEQUENCES_FILE, true);
		baseSequencesTable.addColumn(new IntColumn(baseSequencesTable, "support"));
		baseSequencesTable.addColumn(new DecimalColumn(baseSequencesTable, "max_intertime"));
		baseSequencesTable.addColumn(new StringColumn(baseSequencesTable, "X"));
		baseSequencesTable.addColumn(new StringColumn(baseSequencesTable, "Y"));

		// Temporary record buffers
		ArrayList<Object> record = new ArrayList<Object>();
		StringBuilder items = new StringBuilder();

		for (TarSequence sequence : baseSequences) {
			// Support value
			record.add(sequence.getSupport());
			record.add(sequence.getMaxInterTime());

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

	public void estimateParameters() {
		System.out.println("Estimating TARS parameters...");
		long before = System.nanoTime();

		tarsMiner = new TarsMiner(sequenceExtractor);
		tarsMiner.estimateParameters();

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Estimated parameters in " + diff + " seconds");
	}

	public static void main(String[] args) {
		new TarSequencesAll();
	}

}
