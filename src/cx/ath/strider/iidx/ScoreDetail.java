package cx.ath.strider.iidx;

import java.io.Serializable;
import java.util.Date;

public class ScoreDetail implements Serializable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4559545656163238454L;
	public int ScoreID;
	public int DJID;
	public int SongID;
	public int EXScore;
	public int TotalNotes;
	public Date Stamp;
	
	public String getGrade() {
		if(TotalNotes <= 0 || EXScore <= 0)
			return "N/A";
		
		String[] grades = new String[] { "F", "E", "D", "C", "B", "A", "AA", "AAA" };
        int max = TotalNotes * 2;

        for(int i = 8; i > 1; i--)
            if(EXScore >= ((max * i) / 9))
                return grades[i - 1];

        return grades[0];
	}
	
	public int getGradeIconResourceId() {
		String grade = getGrade();
		
		if(grade.equals("AAA"))
			return R.drawable.grade_aaa;
		else if(grade.equals("AA"))
			return R.drawable.grade_aa;
		else if(grade.equals("A"))
			return R.drawable.grade_a;
		else if(grade.equals("B"))
			return R.drawable.grade_b;
		else if(grade.equals("C"))
			return R.drawable.grade_c;
		else if(grade.equals("D"))
			return R.drawable.grade_d;
		else if(grade.equals("E"))
			return R.drawable.grade_e;
		else if(grade.equals("F"))
			return R.drawable.grade_f;
		else
			return R.drawable.icon;
	}

	public float getAccuracy() {
		return ((float)EXScore / (float)(TotalNotes * 2)) * 100;
	}
	
	@Override
	public String toString() {						
		return String.format("%s: %d (%s)", Stamp.toLocaleString(), EXScore, getGrade());
	}
}
