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
		Yam = L.clone();
		
		baskets.sortDescending()
		
		for (int j = 0; j < baskets.size(); j++) {
			int i = j - 1;
			int aprev = Tj - Ti;
			
			for (X in basket[i]) {
				for (Y in basket[j]) {
					
					y = X -> Y;
					if (Yam.contains(y) || a1 <= aprev <= a2) {
						
						if (That.contains(y)) {
							y.Q <- Qy + 1;
							y.L <- Ti;
							
							if (y.Q > q) {
								That.remove(y); // failing
								Yam.remove(y);
							}

							
							if (y.L - Ti > q . (a1 - a2)) {
								Yam.remove(y); // passing
							}
						} else {
							That.push(y);
							y.Q = 1;
							y.L = Ti;
						}
						
					}
					
					if (Yam.empty()) return That;
				}
			}
		}
		
		return That;
	}
	
	

}

public class Tars {

	S = sequence;
	
	As = new ArrayList<a>();
	
}

public class Sequence {
	X = new int[x];
	Y = new int[y];
	
	a = Tl - Th; // intratime time length of sequence
	sig = Th - Th-1; // intertime, time since last occurrence
}