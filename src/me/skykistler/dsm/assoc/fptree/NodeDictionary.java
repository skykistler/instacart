package me.skykistler.dsm.assoc.fptree;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeDictionary {

	private HashMap<Integer, ArrayList<Node>> nodeDictionary = new HashMap<Integer, ArrayList<Node>>();

	public void add(Node node) {
		if (!nodeDictionary.containsKey(node.getProduct())) {
			nodeDictionary.put(node.getProduct(), new ArrayList<Node>());
		}

		nodeDictionary.get(node.getProduct()).add(node);
	}
}
