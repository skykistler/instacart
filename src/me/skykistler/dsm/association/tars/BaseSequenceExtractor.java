package me.skykistler.dsm.association.tars;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import me.skykistler.dsm.association.fptree.ItemSet;

public class BaseSequenceExtractor {

	private ArrayList<ItemSet> frequentItemSetList;
	private TIntObjectHashMap<ItemSet> frequentItemSets = new TIntObjectHashMap<ItemSet>();
	private TIntObjectHashMap<SequenceTree> baseSequences;
	private ArrayList<SequenceTree> baseSequenceList;

	private TIntArrayList toRemove = new TIntArrayList();
	private TIntArrayList encountered = new TIntArrayList();

	public BaseSequenceExtractor(ArrayList<ItemSet> frequentItemSetList) {
		this.frequentItemSetList = frequentItemSetList;

		for (ItemSet set : frequentItemSetList)
			frequentItemSets.put(set.hashCode(), set);

		baseSequences = new TIntObjectHashMap<SequenceTree>(frequentItemSetList.size() * frequentItemSetList.size());
		baseSequenceList = new ArrayList<SequenceTree>();
	}

	public void processUserTransactions(UserTransaction X, List<UserTransaction> Y) {
		TIntArrayList freqSetsInX = getFrequentItemSetsIn(X);

		// If no frequent item sets, can't do anything
		if (freqSetsInX == null)
			return;

		int hash;
		SequenceTree sequence;
		ItemSet freqSetX, freqSetY;
		// for every freqSet in X, scan every Y for every freqSet
		for (int freqHashX : freqSetsInX.toArray()) {
			freqSetX = frequentItemSets.get(freqHashX);

			boolean encounteredSelf = false;
			encountered.reset();

			for (UserTransaction y : Y) {

				// if y has no frequent sets, skip
				if (getFrequentItemSetsIn(y) == null)
					continue;

				for (int freqHashY : getFrequentItemSetsIn(y).toArray()) {
					freqSetY = frequentItemSets.get(freqHashY);

					// if y has frequent item set x, move on to next freqSetX
					// after this y iteration
					if (freqSetX.hashCode() == freqSetY.hashCode())
						encounteredSelf = true;

					// Don't re-encounter a later sequence
					if (encountered.contains(freqSetY.hashCode()))
						continue;

					hash = getHashCode(freqSetX, freqSetY);

					if (!baseSequences.containsKey(hash)) {
						sequence = new SequenceTree(freqSetX, freqSetY);
						baseSequences.put(hash, sequence);
					} else {
						sequence = baseSequences.get(hash);
					}

					sequence.addIntraTime(y.getDaysSinceFirstOrder() - X.getDaysSinceFirstOrder());
					sequence.addInterTime(X);

					encountered.add(freqSetY.hashCode());
				}

				if (encounteredSelf)
					break;
			}
		}

	}

	public void pruneMinSupport(int min_support) {
		for (int s : baseSequences.keys())
			if (baseSequences.get(s).getIntertimes().size() == 0 || baseSequences.get(s).getSupport() < min_support)
				toRemove.add(s);

		for (int id : toRemove.toArray())
			baseSequences.remove(id);

		toRemove.resetQuick();
	}

	public ArrayList<SequenceTree> getSequences() {
		if (baseSequenceList.size() != baseSequences.size()) {
			baseSequenceList.clear();
			baseSequenceList.addAll(baseSequences.valueCollection());
		}

		return baseSequenceList;
	}

	public int size() {
		return baseSequences.size();
	}

	private TIntArrayList getFrequentItemSetsIn(UserTransaction t) {
		if (t.getFrequentItemSets() != null)
			return t.getFrequentItemSets();

		int i = 0;
		for (ItemSet freqSet : frequentItemSetList) {
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
