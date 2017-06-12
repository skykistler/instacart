package me.skykistler;

import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.assoc.fptree.ItemSet;
import me.skykistler.dsm.assoc.tars.BaseSequenceExtractor;
import me.skykistler.dsm.assoc.tars.SequenceTree;
import me.skykistler.dsm.assoc.tars.TarsMiner;
import me.skykistler.dsm.assoc.tars.UserTransaction;
import me.skykistler.dsm.table.CSVTable;
import me.skykistler.dsm.table.IntColumn;
import me.skykistler.dsm.table.StringColumn;

public class Phase2 extends Phase1 {

	public static final int MIN_SUPPORT_BASE_SEQUENCE = 3;

	public CSVTable frequentItemSetsTable;
	public ArrayList<ItemSet> frequentItemSets;

	public BaseSequenceExtractor sequenceExtractor;
	public TarsMiner tarsMiner;

	@Override
	public void go() {
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		loadBasketsTable();
		transformBaskets();
		freeBasketsTable();

		loadFrequentItemSets();

		extractBaseSequences();

		estimateParameters();

		for (SequenceTree s : sequenceExtractor.getSequences()) {
			System.out.println("X: " + s.getX());
			System.out.println("Y: " + s.getY());
			System.out.println("Support: " + s.getSupport());
			System.out.println("Max Intertime: " + s.getMaxInterTime());

			for (int i : s.getIntertimes().toArray())
				System.out.print(i + " ");

			System.out.println();
			System.out.println();
		}

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
		System.out.println("Loading base sequences...");
		long before = System.nanoTime();

		sequenceExtractor = new BaseSequenceExtractor(frequentItemSets);

		int i, lastSize = 0;
		// for (ArrayList<UserTransaction> userBaskets : basketLists.values())
		ArrayList<UserTransaction> userBaskets = basketLists.get(1);
		for (i = 0; i < userBaskets.size() - 1; i++) {
			sequenceExtractor.processUserTransactions(userBaskets.get(i), userBaskets.subList(i + 1, userBaskets.size()));

			if (sequenceExtractor.size() - lastSize > 100000) {
				lastSize = sequenceExtractor.size();
				System.out.println("Extracted " + sequenceExtractor.size() + " base sequences...");
			}
		}
		sequenceExtractor.pruneMinSupport(MIN_SUPPORT_BASE_SEQUENCE);

		long diff = System.nanoTime() - before;
		diff /= 1000000000;
		System.out.println("Extracted " + sequenceExtractor.size() + " base sequences in " + diff + " seconds");
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
		new Phase2();
	}

}
