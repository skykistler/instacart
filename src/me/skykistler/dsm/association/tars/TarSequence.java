package me.skykistler.dsm.association.tars;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import me.skykistler.dsm.association.fptree.ItemSet;

public class TarSequence implements Median {
	public static TIntIntHashMap userTimes = new TIntIntHashMap();

	private ItemSet X; // head
	private ItemSet Y; // tail
	private int hash_id = -1;
	private int support;

	private int lastHeadTime = -1;
	private TIntArrayList interTimes = new TIntArrayList(2); // distances
																// between
																// head times
	private TIntArrayList intraTimes = new TIntArrayList(2); // distances
																// between head
																// and tail

	private int numPeriods, minPeriods;

	private double medianIntertime = -1, maxIntertime, medianIntratime;
	private double medianPeriodOccurences, minPeriodOccurences;

	public TarSequence(ItemSet X, ItemSet Y) {
		this.X = X;
		this.Y = Y;
		hash_id = getHashCode();
	}

	public void addIntraTime(int d) {
		intraTimes.add(d);
		support++;
	}

	public void addInterTime(UserTransaction t) {
		if (lastHeadTime == -1)
			lastHeadTime = t.getDaysSinceFirstOrder();
		else
			interTimes.add(t.getDaysSinceFirstOrder() - lastHeadTime);

		// if (userTimes.containsKey(hash_id))
		// interTimes.add(t.getDaysSinceFirstOrder() - userTimes.get(hash_id));
		// else
		// interTimes.add(Integer.MAX_VALUE);
		//
		// userTimes.put(hash_id, t.getDaysSinceFirstOrder());
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

	public int getSupport() {
		return support;
	}

	public double getMedianInterTime() {
		if (medianIntertime < 0)
			medianIntertime = median(interTimes);

		return medianIntertime;
	}

	public double getMedianIntraTime() {
		TIntArrayList toMedian = new TIntArrayList(intraTimes.size());
		for (int i : intraTimes.toArray()) {
			if (i < maxIntertime)
				toMedian.add(i);
		}

		medianIntratime = median(toMedian);

		return medianIntratime;
	}

	public boolean hasSatisfyingIntratime() {
		for (int i : intraTimes.toArray())
			if (i < maxIntertime)
				return true;

		return false;
	}

	public boolean hasSatisfyingIntertime() {
		for (int i : interTimes.toArray())
			if (i < maxIntertime)
				return true;

		return false;
	}

	public void setMaxInterTime(double max) {
		maxIntertime = max;
	}

	public double getMaxInterTime() {
		return maxIntertime;
	}

	public void setNumPeriods(int p) {
		numPeriods = p;
	}

	public int getNumPeriods() {
		return numPeriods;
	}

	public void setMedianPeriodOccurences(double median) {
		medianPeriodOccurences = median;
	}

	public double getMedianPeriodOccurences() {
		return medianPeriodOccurences;
	}

	public void setMinPeriodOccurences(double median) {
		minPeriodOccurences = (int) Math.floor(median);
	}

	public double getMinPeriodOccurences() {
		return minPeriodOccurences;
	}

	public void setMinPeriods(int p) {
		minPeriods = p;
	}

	public double getMinPeriods() {
		return minPeriods;
	}

	@Override
	public int hashCode() {
		return hash_id;
	}

	private int getHashCode() {
		return (X.hashCode() << 16) + Y.hashCode();
	}
}