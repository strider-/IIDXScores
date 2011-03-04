package cx.ath.strider.iidx;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Score {
	public int ScoreID;
	public int DJID;
	public int SongID;
	public int EXScore;
	public int ArcadeScore = -1;
	public Date Stamp;
	public double Latitude;
	public double Longitude;
	
	/*public String toXmlElement() {
		if(ArcadeScore <= 0)
			return String.format(
					"<Scores><ScoreID>%d</ScoreID><DJID>%d</DJID><SongID>%d</SongID><EXScore>%d</EXScore><Stamp>%s</Stamp><Latitude>%f</Latitude><Longitude>%f</Longitude></Scores>",
					ScoreID, DJID, SongID, EXScore, Stamp.toLocaleString(), Latitude, Longitude
			);
		else
			return String.format(
					"<Scores><ScoreID>%d</ScoreID><DJID>%d</DJID><SongID>%d</SongID><EXScore>%d</EXScore><ArcadeScore>%d</ArcadeScore><Stamp>%s</Stamp><Latitude>%f</Latitude><Longitude>%f</Longitude></Scores>",
					ScoreID, DJID, SongID, EXScore, ArcadeScore, Stamp.toLocaleString(), Latitude, Longitude
			);						
	}*/
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("ScoreID", ScoreID);
		obj.put("DJID", DJID);
		obj.put("SongID", SongID);
		obj.put("EXScore", EXScore);
		if(ArcadeScore > 0)
			obj.put("ArcadeScore", ArcadeScore);
		obj.put("Stamp", Stamp.getTime());
		obj.put("Latitude", Latitude);
		obj.put("Longitude", Longitude);
		return obj;
	}
}
