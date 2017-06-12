package me.skykistler.dsm.assoc.tars;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import me.skykistler.dsm.assoc.fptree.ItemSet;

public class SequenceTree implements Median {
	private static TIntObjectHashMap<TIntIntHashMap> userTimes = new TIntObjectHashMap<TIntIntHashMap>();

	private ItemSet X;
	private ItemSet Y;
	private int hash_id = -1;

	private TIntArrayList interTimes = new TIntArrayList(2);
	private TIntArrayList intraTimes = new TIntArrayList(2);

	private double medianInterTime = -1, maxInterTime;

	public SequenceTree(ItemSet X, ItemSet Y) {
		this.X = X;
		this.Y = Y;
		hash_id = (X.hashCode() << 16) + Y.hashCode();
	}

	public void addIntraTime(int d) {
		intraTimes.add(d);
	}

	public void addInterTime(UserTransaction t) {
		if (userTimes.containsKey(t.getUserId()) && userTimes.get(t.getUserId()).containsKey(hash_id)) {
			interTimes.add(t.getDaysSinceFirstOrder() - userTimes.get(t.getUserId()).get(hash_id));
		}

		if (!userTimes.containsKey(t.getUserId())) {
			userTimes.put(t.getUserId(), new TIntIntHashMap());
		}

		userTimes.get(t.getUserId()).put(hash_id, t.getDaysSinceFirstOrder());
	}

	public int getSupport() {
		return intraTimes.size();
	}

	public double getMedianInterTime() {
		if (medianInterTime < 0)
			medianInterTime = median(interTimes);

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
}