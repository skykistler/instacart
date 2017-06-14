package me.skykistler.dsm.assoc.tars;

import gnu.trove.list.array.TIntArrayList;
import me.skykistler.dsm.assoc.fptree.ItemSet;

public class UserTransaction {

	private int user_id;
	private TIntArrayList items;
	private int daysSinceFirstOrder;

	private TIntArrayList frequentItemSets;

	public UserTransaction(int user_id, TIntArrayList items, int daysSinceFirstOrder) {
		this.user_id = user_id;
		this.items = items;
		this.daysSinceFirstOrder = daysSinceFirstOrder;
	}

	public int getUserId() {
		return user_id;
	}

	public int getDaysSinceFirstOrder() {
		return daysSinceFirstOrder;
	}

	public TIntArrayList getItems() {
		return items;
	}

	public TIntArrayList getFrequentItemSets() {
		return frequentItemSets;
	}

	public void addFrequentItemSet(ItemSet freqSet) {
		if (frequentItemSets == null)
			frequentItemSets = new TIntArrayList(2);

		if (freqSet != null)
			frequentItemSets.add(freqSet.hashCode());
	}
}
