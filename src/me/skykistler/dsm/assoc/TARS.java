package me.skykistler.dsm.assoc;

public class TarsMiner {

	public void extractTars(baskets) {
		ArrayList<Tars> sequences = extractBaseSequences(baskets);
		
		params = parametersEstimation(sequences, baskets);
		sequences = sequenceFiltering(baskets, sequences, params);
		
		tarsTree = buildTarsTree(baskets, sequences, params);
		
		tars = extractTarsFromTree(tree);
		
		return tars;
	}

	public void parametersEstimation(baskets, sequences) {
		
	}

	public void getActiveTars(baskets, t, T) {
		That = new ArrayList<Tars>();
		Y = L.clone();
	}
	
	

}

public class Tars {

	S = sequence;
	
	As = new ArrayList<a>();
	
}

public class Sequence {
	X = new int[x];
	Y = new int[y];
	
	a = Tl - Th; // time length of sequence
}