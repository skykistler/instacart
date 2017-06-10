package me.skykistler.dsm.assoc.fptree;

public class Node {

	private Node parent;

	private int product_id;
	private int support;

	public Node(int product_id, Node parent) {
		this.product_id = product_id;
		this.parent = parent;

		this.support = 1;
	}

	public int getProduct() {
		return product_id;
	}
}
