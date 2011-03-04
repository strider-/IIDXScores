package cx.ath.strider.iidx;

import java.io.Serializable;
import java.util.Arrays;

public class SongData implements Serializable {
	private static final long serialVersionUID = 7617686558808393723L;

	public SongData() {
		Song = new Song();
		SongInfo = new SongInfo();
		Mode = new Mode();
	}
	
	public Song Song;
	public SongInfo SongInfo;
	public Mode Mode;
	public ScoreDetail[] Scores;
	
	public void sortScores(boolean byEXScore, boolean ascending) {
		Arrays.sort(Scores, new ScoreComparer(byEXScore, ascending));	
	}
	
	@Override
	public String toString() {
		return String.format("%s %s\n%s\n%s",
				SongInfo.Title,
				Mode.Abbr,
				SongInfo.Artist,
				SongInfo.Genre
		);
	}
}
