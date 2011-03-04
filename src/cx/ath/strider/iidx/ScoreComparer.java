package cx.ath.strider.iidx;

import java.util.Comparator;

public class ScoreComparer implements Comparator<ScoreDetail> {
	private boolean exSort, asc;
	
	public ScoreComparer(boolean sortByEXScore, boolean ascending) {
		exSort = sortByEXScore;
		asc = ascending;
	}
	
	@Override
	public int compare(ScoreDetail object1, ScoreDetail object2) {
		if(!asc) {
			ScoreDetail temp = object1;
			object1 = object2;
			object2 = temp;
		}
		
		if(exSort) {
			if(object1.EXScore > object2.EXScore)
				return 1;
			else if(object1.EXScore < object2.EXScore)
				return -1;
			else
				return 0;			
		} else {
			return object1.Stamp.compareTo(object2.Stamp);
		}
	}
	
}