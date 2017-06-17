package me.skykistler.dsm.association.fptree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import gnu.trove.list.TIntList;

public class FPTree {

	// Unused right now, may be used for FP-Growth
	// private static FPTreeDictionary treeDict = new FPTreeDictionary();
	// private FPTree parent;

	protected HashMap<Integer, FPTree> children = new HashMap<Integer, FPTree>(2);

	protected int item_id;
	protected int support = 0;

	/**
	 * Instantiate a root FPTree
	 */
	public FPTree() {
		this(-1);
	}

	/**
	 * Instantiate an FPTree branch with the specified node ID and parent link
	 * 
	 * @param item_id
	 */
	public FPTree(int item_id) {
		this.item_id = item_id;
		// this.parent = parent;
	}

	/**
	 * Recursively insert items into the tree
	 * 
	 * @param transactionItems
	 */
	public void r_insertTree(TIntList transactionItems) {
		FPTree nextPath;

		// If node needs to be created
		if (!hasChild(transactionItems.get(0))) {
			nextPath = new FPTree(transactionItems.get(0));
			children.put(transactionItems.get(0), nextPath);
			// treeDict.add(nextPath);
		}
		// If node already exists
		else {
			nextPath = getChild(transactionItems.get(0));
		}

		nextPath.incrementSupport();

		// If there are more items remaining, continue recursion
		if (transactionItems.size() > 1)
			nextPath.r_insertTree(transactionItems.subList(1, transactionItems.size()));
	}

	public ArrayList<ItemSet> r_growItemSets(ItemSet parentPrefix, int min_support) {
		// Prepare resulting sets
		ArrayList<ItemSet> frequentItemSets = new ArrayList<ItemSet>();

		ItemSet thisPrefix = null;
		if (!isRoot()) {
			thisPrefix = new ItemSet();

			// Carry over existing prefix
			if (parentPrefix != null)
				thisPrefix.addAll(parentPrefix);

			// Add this node to the prefix
			thisPrefix.add(getItemId());
			thisPrefix.setSupport(support);

			frequentItemSets.add(thisPrefix);
		}

		for (FPTree child : getChildren()) {
			if (child.getSupport() < min_support)
				continue;

			frequentItemSets.addAll(child.r_growItemSets(thisPrefix, min_support));
		}

		return frequentItemSets;
	}

	public void incrementSupport() {
		support++;
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

	public boolean isRoot() {
		return item_id < 0;
	}

}
