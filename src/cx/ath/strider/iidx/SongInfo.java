package cx.ath.strider.iidx;

import java.io.Serializable;

public class SongInfo implements Serializable {
	private static final long serialVersionUID = 4799089717241308730L;
	public int SongInfoID;
	public int StyleID;
	public String Title;
	public String Genre;
	public String Artist;
	public String BPM;
	public String Notes;
	
	@Override
	public String toString() {
		return Title;
	}
}
