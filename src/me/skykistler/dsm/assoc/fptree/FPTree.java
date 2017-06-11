package me.skykistler.dsm.assoc.fptree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class FPTree {

	private static FPTreeDictionary treeDict = new FPTreeDictionary();

	private FPTree parent;
	private HashMap<Integer, FPTree> children = new HashMap<Integer, FPTree>();

	private int item_id;
	private int support = 1;

	public FPTree(int item_id, FPTree parent) {
		this.item_id = item_id;
		this.parent = parent;
	}

	public void r_insertTree(TIntList transaction_items) {
		FPTree nextPath;

		if (hasChild(transaction_items.get(0))) {
			nextPath = getChild(transaction_items.get(0));
			nextPath.increment();

		} else {
			nextPath = new FPTree(transaction_items.get(0), this);

			children.put(transaction_items.get(0), nextPath);
			treeDict.add(nextPath);
		}

		if (transaction_items.size() > 1)
			nextPath.r_insertTree(transaction_items.subList(1, transaction_items.size()));
	}

	public ArrayList<TIntArrayList> r_growItemSets(TIntArrayList prefix) {
		// recursively enumerate all paths with at least min_support
		// add current prefix to resulting children prefixes

		TIntArrayList newPrefix = new TIntArrayList();
		if (prefix != null)
			newPrefix.addAll(prefix);

		if (getItemId() > 0)
			newPrefix.add(getItemId());

		ArrayList<TIntArrayList> itemSets = new ArrayList<TIntArrayList>();
		itemSets.add(newPrefix);

		for (FPTree child : getChildren()) {
			if (child.getSupport() < 10)
				continue;

			itemSets.addAll(child.r_growItemSets(newPrefix));
		}

		return itemSets;
	}

	public boolean hasChild(int id) {
		return children.containsKey(id);
	}

	public FPTree getChild(int id) {
		return children.get(id);
	}

	public Collection<FPTree> getChildren() {
		return children.values();
	}

	public int getItemId() {
		return item_id;
	}

	public int getSupport() {
		return support;
	}

	public void increment() {
		support++;
	}

}
