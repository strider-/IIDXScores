package cx.ath.strider.iidx.model;


public class SongQuery {
	public int SongID;
	public String Title;
	public int Difficulty;
	public int TotalNotes;
	public String BPM;
	public Mode Mode;
	
	public SongQuery() {
		this.Mode = new Mode();
	}
	
	@Override
	public String toString() {
		return String.format("%d\u2605 - %s", Difficulty, Title);
	}
}
