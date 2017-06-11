package me.skykistler.dsm.assoc.fptree;

import java.util.ArrayList;
import java.util.HashMap;

public class FPTreeDictionary {

	private HashMap<Integer, ArrayList<FPTree>> treeDict = new HashMap<Integer, ArrayList<FPTree>>();

	public void add(FPTree tree) {
		if (!treeDict.containsKey(tree.getItemId())) {
			treeDict.put(tree.getItemId(), new ArrayList<FPTree>());
		}

		treeDict.get(tree.getItemId()).add(tree);
	}
}
