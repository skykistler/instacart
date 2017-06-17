package me.skykistler.dsm.association.tars;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import me.skykistler.dsm.association.fptree.ItemSet;

public class SequenceTree implements Median {
	public static TIntIntHashMap userTimes = new TIntIntHashMap();

	private ItemSet X; // head
	private ItemSet Y; // tail
	private int hash_id = -1;
	private int support;

	private TIntArrayList interTimes = new TIntArrayList(2); // distances
																// between
																// occurences
	private TIntArrayList intraTimes = new TIntArrayList(2); // distances
																// between head
																// and tail

	private double medianInterTime = -1, maxInterTime;

	public SequenceTree(ItemSet X, ItemSet Y) {
		this.X = X;
		this.Y = Y;
		hash_id = getHashCode();
	}

	public void addIntraTime(int d) {
		intraTimes.add(d);
		support++;
	}

	public void addInterTime(UserTransaction t) {
		if (userTimes.containsKey(hash_id))
			interTimes.add(t.getDaysSinceFirstOrder() - userTimes.get(hash_id));

		userTimes.put(hash_id, t.getDaysSinceFirstOrder());
	}

	public int getSupport() {
		return support;
	}

	public double getMedianInterTime() {
		if (medianInterTime < 0) {
			medianInterTime = median(interTimes);

			interTimes = null;
		}

		return medianInterTime;
	}

	public void setMaxInterTime(double max) {
		maxInterTime = max;
	}

	public double getMaxInterTime() {
		return maxInterTime;
	}

	public TIntArrayList getIntertimes() {
		return interTimes;
	}

	public ItemSet getX() {
		return X;
	}

	public ItemSet getY() {
		return Y;
	}

	@Override
	public int hashCode() {
		return hash_id;
	}

	private int getHashCode() {
		return (X.hashCode() << 16) + Y.hashCode();
	}
}