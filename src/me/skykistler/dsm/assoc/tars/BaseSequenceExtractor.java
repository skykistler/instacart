package me.skykistler.dsm.assoc.tars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.assoc.fptree.ItemSet;

public class BaseSequenceExtractor {

	private ArrayList<ItemSet> frequentItemSets;
	private HashMap<Integer, SequenceTree> baseSequences = new HashMap<Integer, SequenceTree>();

	public BaseSequenceExtractor(ArrayList<ItemSet> frequentItemSets) {
		this.frequentItemSets = frequentItemSets;
	}

	public void processUserTransactions(UserTransaction X, List<UserTransaction> list) {
		ArrayList<ItemSet> freqSetsInX = getFrequentItemSetsIn(X);

		// If no frequent item sets, can't do anything
		if (freqSetsInX == null)
			return;

		// Generate any missing frequent item sets for Y
		for (UserTransaction y : list) {
			getFrequentItemSetsIn(y);
		}

		int hash;
		SequenceTree sequence;
		// for every freqSet in X, scan every Y for every freqSet
		for (ItemSet freqSetX : freqSetsInX) {

			boolean encounteredSelf = false;
			for (UserTransaction y : list) {

				if (y.getFrequentItemSets() == null)
					continue;

				for (ItemSet freqSetY : y.getFrequentItemSets()) {
					if (freqSetX.equals(freqSetY))
						encounteredSelf = true;

					hash = getHashCode(freqSetX, freqSetY);

					if (!baseSequences.containsKey(hash)) {
						sequence = new SequenceTree(freqSetX, freqSetY);
						baseSequences.put(hash, sequence);
					} else {
						sequence = baseSequences.get(hash);
					}

					sequence.addIntraTime(y.getDaysSinceFirstOrder() - X.getDaysSinceFirstOrder());
					sequence.addInterTime(X);
				}

				if (encounteredSelf)
					break;
			}
		}

	}

	public void pruneMinSupport(int sup) {
		TIntArrayList toRemove = new TIntArrayList();

		for (SequenceTree s : baseSequences.values())
			if (s.getSupport() < sup)
				toRemove.add(s.hashCode());

		for (int id : toRemove.toArray())
			baseSequences.remove(id);
	}

	public Collection<SequenceTree> getSequences() {
		return baseSequences.values();
	}

	public int size() {
		return baseSequences.size();
	}

	private ArrayList<ItemSet> getFrequentItemSetsIn(UserTransaction t) {
		if (t.getFrequentItemSets() != null)
			return t.getFrequentItemSets();

		int i = 0;
		for (ItemSet freqSet : frequentItemSets) {
			while (t.getItems().contains(freqSet.get(i))) {
				i++;

				if (i == freqSet.size()) {
					t.addFrequentItemSet(freqSet);
					break;
				}
			}

			i = 0;
		}

		return t.getFrequentItemSets();
	}

	private int getHashCode(ItemSet X, ItemSet Y) {
		return (X.hashCode() << 16) + Y.hashCode();
	}

}
