package cx.ath.strider.iidx.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cx.ath.strider.iidx.IIDX;
import cx.ath.strider.iidx.JsonHandler;
import cx.ath.strider.iidx.R;
import cx.ath.strider.iidx.Settings;
import cx.ath.strider.iidx.adapter.ModeAdapter;
import cx.ath.strider.iidx.adapter.SongQueryAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;  
import android.widget.ArrayAdapter;

public class IIDXModel extends JsonHandler {
	private SQLiteDatabase db;
	private Context context;
	private boolean exists;
	
	public IIDXModel(Context context) {
		this.context = context;
		exists = context.getDatabasePath(IIDX.DATABASE_NAME).exists();
	}
	
	public boolean getDatabaseExists() {
		return exists;
	}
	
	public ArrayAdapter<Style> getStyles() {
		if(!exists || !open())
			return null;
		Cursor c = null;
		
		try {
			c = db.query("Styles", null,null,null,null,null,"styleorder");
			ArrayAdapter<Style> aa = new ArrayAdapter<Style>(this.context, android.R.layout.select_dialog_item);
			aa.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			if(c.moveToFirst())
				do {
					Style s = new Style();
					s.StyleID = c.getInt(c.getColumnIndexOrThrow("styleid"));
					s.StyleName = c.getString(c.getColumnIndexOrThrow("stylename"));
					s.ParentID = c.getInt(c.getColumnIndexOrThrow("parentid"));
					s.Theme = c.getString(c.getColumnIndexOrThrow("theme"));
					s.StyleOrder = c.getInt(c.getColumnIndexOrThrow("styleorder"));
					aa.add(s);
				} while(c.moveToNext());
			c.close();
			return aa;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving Styles from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}
	public ModeAdapter getModes() {	
		if(!exists || !open())
			return null;
		
		Cursor c = null;
		try {
			c = db.query("Modes",null,null,null,null,null,"modeid");
			ArrayList<Mode> aa = new ArrayList<Mode>();
			if(c.moveToFirst())
				do {
					Mode m = new Mode();
					m.ModeID = c.getInt(c.getColumnIndexOrThrow("modeid"));
					m.ModeName = c.getString(c.getColumnIndexOrThrow("modename"));
					m.Abbr = c.getString(c.getColumnIndexOrThrow("abbr"));
					setModeResources(m);
					
					aa.add(m);
				} while(c.moveToNext());
			c.close();
			
			ModeAdapter ma = new ModeAdapter(this.context, android.R.layout.select_dialog_item, aa);
			ma.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			return ma;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving Modes from SQLite", e);
			return null;
		} finally {
			if(c!=null)
				c.close();
			this.close();
		}
	}
	public ArrayAdapter<DJ> getDJs(){		
		if(!exists || !open())
			return null;
		
		Cursor c = null;
		try {
			c = db.query("DJs",null,null,null,null,null,"djid");
			ArrayAdapter<DJ> aa = new ArrayAdapter<DJ>(this.context, android.R.layout.select_dialog_item);
			aa.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			if(c.moveToFirst())
				do {
					DJ d = new DJ();
					d.DJID = c.getInt(c.getColumnIndexOrThrow("djid"));
					d.Name = c.getString(c.getColumnIndexOrThrow("djname"));
					aa.add(d);
				} while(c.moveToNext());
			c.close();						
			return aa;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving DJs from SQLite", e);
			return null;
		} finally {
			if(c!=null)
				c.close();
			this.close();
		}
	}
	public SongQueryAdapter getSongs(Style style, Mode mode, String sort, boolean ACinCS, boolean IncludeRevivals, boolean IncludeSectionHeaders) {
		if(style == null || mode == null || !exists || !open())
			return null;
		
		Cursor c = null;
		StringBuffer qBuffer = new StringBuffer();
		
		qBuffer.append(String.format(
            "select songid, title, difficulty, totalnotes, bpm from songs a " +
            "inner join songinfo b on a.songinfoid = b.songinfoid " +
            "where (styleid=%d or styleid=%d) and modeid=%d ", 
            style.StyleID, ACinCS ? style.ParentID : -1, mode.ModeID
    	));
		
        if(IncludeRevivals) {
        	qBuffer.append(String.format(
                "union " +
                "select songid, title, difficulty, totalnotes, bpm from songs a " +
                "left outer join csrevivals b on a.songinfoid = b.songinfoid " +
                "left outer join songinfo c on c.songinfoid= a.songinfoid " +
                "where b.styleid in (%d, %d) and modeid=%d ", 
                style.StyleID, ACinCS ? style.ParentID : -1, mode.ModeID
            ));
        }
		
        qBuffer.append(String.format("order by %s COLLATE NOCASE", sort));       
        
		try {
			c = db.rawQuery(qBuffer.toString(), null);	
			ArrayList<SongQuery> aa = new ArrayList<SongQuery>();
			char ph = 0, t;
			boolean digits=false, other=false;
			
			if(c.moveToFirst()) {				
				do {			
					String title = c.getString(c.getColumnIndexOrThrow("title"));
					int difficulty = c.getInt(c.getColumnIndexOrThrow("difficulty"));
					
					if(IncludeSectionHeaders)
						if(sort.equals("title")) {
							int i=0;
							while(!Character.isLetterOrDigit(t = title.charAt(i)))
								i++;											
							
							if(Character.isDigit(t) && !digits) {  //digits
								aa.add(null);
								digits=true;
							} else if(((t >= 65 && t <= 90) || (t >= 97 && t <= 122)) && Character.toLowerCase(t) != ph) { //letters								
								aa.add(null);
								ph = Character.toLowerCase(t);
							} else if(t > 122 && !other) { // other								
								aa.add(null);
								other=true;
							}
						} else {
							if(difficulty != ph) {
								aa.add(null);
								ph = (char)difficulty;
							}
						}
					
					SongQuery s = new SongQuery();
					s.SongID = c.getInt(c.getColumnIndexOrThrow("songid"));
					s.Title = title;
					s.Difficulty = difficulty; 
					s.TotalNotes = c.getInt(c.getColumnIndexOrThrow("totalnotes"));
					s.BPM = c.getString(c.getColumnIndexOrThrow("bpm"));					
					s.Mode = mode;
					aa.add(s);
				} while(c.moveToNext());
			}
			c.close();
			
			return new SongQueryAdapter(this.context, R.layout.songlist_layout_item, aa, sort);
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed querying for Songs from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}	
	public SongQuery[] getSongModes(SongData song) {
		if(!exists || !open())
			return null;
		Cursor c = null;
		
		try {
			String sql = String.format(
				"select songid, s.modeid, m.abbr, m.modename, totalnotes, difficulty from songs s " +
				"JOIN modes m ON m.modeID = s.modeID " +
				"where songinfoid=%d",
				song.SongInfo.SongInfoID
			);
			
			int i=0;
			c = db.rawQuery(sql, null);
			SongQuery[] songs = new SongQuery[c.getCount()];
			if(c.moveToFirst()) {				
				do {
					SongQuery sq = new SongQuery();
					sq.BPM = song.SongInfo.BPM;
					sq.Title = song.SongInfo.Title;
					sq.Difficulty = c.getInt(c.getColumnIndex("difficulty"));
					sq.SongID = c.getInt(c.getColumnIndex("songid"));					
					sq.TotalNotes = c.getInt(c.getColumnIndex("totalnotes"));
					sq.Mode.ModeID = c.getInt(c.getColumnIndex("modeid"));
					sq.Mode.ModeName = c.getString(c.getColumnIndex("modename"));
					sq.Mode.Abbr = c.getString(c.getColumnIndex("abbr"));
					setModeResources(sq.Mode);
					songs[i++] = sq;
				} while(c.moveToNext());
			}
			c.close();			
			return songs;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving SongData from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}
	public SongData getSongData(SongQuery song, DJ dj, String scoreSort) {		
		if(!exists || !open())
			return null;
		Cursor c = null;
		
		String q1 = String.format(
			"select * from songs a " +
			"inner join songinfo b on a.songinfoid = b.songinfoid " +
			"inner join modes c on a.modeid = c.modeid " +
			"where songid=%d", song.SongID
		);
		String q2 = String.format(
			"select scoreid, a.songid, exscore, stamp, totalnotes " +
            "from scores a join songs b on a.songid = b.songid " +
            "where djid=%d and a.songid=%d order by %s desc",
			dj.DJID, song.SongID, scoreSort
		);
		
		try {			
			c = db.rawQuery(q1, null);
			c.moveToFirst();
			SongData s = new SongData();
			
			s.Song.SongID = c.getInt(c.getColumnIndexOrThrow("songid"));
			s.Song.ModeID = c.getInt(c.getColumnIndexOrThrow("modeid"));
			s.Song.SongInfoID = c.getInt(c.getColumnIndexOrThrow("songinfoid"));
			s.Song.TotalNotes = c.getInt(c.getColumnIndexOrThrow("totalnotes"));
			s.Song.Difficulty = c.getInt(c.getColumnIndexOrThrow("difficulty"));
			
			s.SongInfo.SongInfoID = c.getInt(c.getColumnIndexOrThrow("songinfoid"));			
			s.SongInfo.Artist = c.getString(c.getColumnIndexOrThrow("artist"));
			s.SongInfo.BPM = c.getString(c.getColumnIndexOrThrow("bpm"));
			s.SongInfo.Genre = c.getString(c.getColumnIndexOrThrow("genre"));
			s.SongInfo.Notes = c.getString(c.getColumnIndexOrThrow("notes"));	
			s.SongInfo.StyleID = c.getInt(c.getColumnIndexOrThrow("styleid"));
			s.SongInfo.Title = c.getString(c.getColumnIndexOrThrow("title"));
			
			s.Mode.ModeID = c.getInt(c.getColumnIndexOrThrow("modeid"));
			s.Mode.Abbr = c.getString(c.getColumnIndexOrThrow("abbr"));
			s.Mode.ModeName = c.getString(c.getColumnIndexOrThrow("modename"));		
			setModeResources(s.Mode);
			
			c.close();
						
			c = db.rawQuery(q2, null);
			s.Scores = new ScoreDetail[c.getCount()];
			int i=0;
			if(c.moveToFirst())
				do {
					ScoreDetail sd = new ScoreDetail();						
					sd.DJID = dj.DJID;
					sd.ScoreID = c.getInt(c.getColumnIndexOrThrow("scoreid"));
					sd.SongID = c.getInt(c.getColumnIndexOrThrow("songid"));
					sd.EXScore = c.getInt(c.getColumnIndexOrThrow("exscore"));
					sd.TotalNotes = c.getInt(c.getColumnIndexOrThrow("totalnotes"));
					sd.Stamp = new Date(c.getLong(c.getColumnIndexOrThrow("stamp")));
					s.Scores[i++] = sd;
				} while(c.moveToNext());
			
			c.close();
			return s;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving SongData from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}
	public boolean addScore(SongData song, DJ dj, int EXScore, int ArcadeScore, double lat, double lon) {
		if(!exists || !open())
			return false;
		
		try {
			ContentValues values = new ContentValues();
			values.put("djid", dj.DJID);
			values.put("songid", song.Song.SongID);
			values.put("exscore", EXScore);
			values.put("arcadescore", ArcadeScore == 0 ? null : ArcadeScore);			
			values.put("stamp", System.currentTimeMillis());
			values.put("lat", lat);
			values.put("lon", lon);
			return db.insert("Scores", null, values) != -1;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed inserting Score into SQLite", e);
			return false;
		} finally {
			this.close();
		}
	}
	public int getNewScoreCount(long since) {		
		if(!exists || !open())
			return -1;		

		Cursor c = null;
		String q = String.format("select count(scoreid) from scores where stamp >= %d", since);
		
		try {
			c = db.rawQuery(q, null);
			c.moveToFirst();
			return c.getInt(0);
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving new score count.", e);
			return -1;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}
	private void setModeResources(Mode mode) {		
		switch(mode.ModeID) {
		case 1:
			mode.ModeColorResource = R.color.color_normal;
			break;
		case 2:
			mode.ModeColorResource = R.color.color_hyper;
			break;
		case 3:
			mode.ModeColorResource = R.color.color_another;
			break;
		default:
			mode.ModeColorResource = R.color.color_white;			
			break;
		}		
		mode.ModeColor = context.getResources().getColor(mode.ModeColorResource);
	}
	public Drawable getModeIcon(Mode mode) {
		switch(mode.ModeID) {
		case 1:
			return context.getResources().getDrawable(R.drawable.normal);
		case 2:
			return context.getResources().getDrawable(R.drawable.hyper);
		case 3:
			return context.getResources().getDrawable(R.drawable.another);
		default:
			return null;
		}
	}
 	public Style getStyleFromID(int StyleID) {
		if(!exists || !open())
			return null;
		Cursor c = null;
		
		try {
			c = db.rawQuery(String.format("select * from styles where styleid = %d", StyleID), null);
			if(c.moveToFirst()) {
				Style s = new Style();
				s.StyleID = c.getInt(c.getColumnIndexOrThrow("styleid"));
				s.StyleName = c.getString(c.getColumnIndexOrThrow("stylename"));
				s.ParentID = c.getInt(c.getColumnIndexOrThrow("parentid"));
				s.Theme = c.getString(c.getColumnIndexOrThrow("theme"));
				s.StyleOrder = c.getInt(c.getColumnIndexOrThrow("styleorder"));
				c.close();
				return s;
			} else
				return null;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving Style from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}		
	}
	public Mode getModeFromID(int ModeID) {
		if(!exists || !open())
			return null;
		Cursor c = null;
		
		try {
			c = db.rawQuery(String.format("select * from modes where modeid = %d", ModeID), null);
			if(c.moveToFirst()) {
				Mode m = new Mode();
				m.ModeID = c.getInt(c.getColumnIndexOrThrow("modeid"));
				m.ModeName = c.getString(c.getColumnIndexOrThrow("modename"));
				m.Abbr = c.getString(c.getColumnIndexOrThrow("abbr"));
				setModeResources(m);
				return m;
			} else
				return null;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving Mode from SQLite", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}		
	}	
	public boolean updateHost(String pushAddress, long since) {		
		if(!exists || !open())
			return false;
		
		StringBuffer buffer = new StringBuffer();
		Cursor c = null;
		
		try {	
			String q = String.format("select * from scores where stamp >= %d", since);
			JSONArray scores = new JSONArray();
			
			c = db.rawQuery(q, null);
			if(c.moveToFirst()) {
				do {
					Score s = new Score();
					s.ArcadeScore = c.getInt(c.getColumnIndexOrThrow("arcadescore"));
					s.DJID = c.getInt(c.getColumnIndexOrThrow("djid"));
					s.EXScore = c.getInt(c.getColumnIndexOrThrow("exscore"));
					s.ScoreID = c.getInt(c.getColumnIndexOrThrow("scoreid"));
					s.SongID = c.getInt(c.getColumnIndexOrThrow("songid"));
					s.Stamp = new Date(c.getLong(c.getColumnIndexOrThrow("stamp")));
					s.Latitude = c.getDouble(c.getColumnIndexOrThrow("lat"));
					s.Longitude = c.getDouble(c.getColumnIndexOrThrow("lon"));
					try {
						scores.put(s.toJSONObject());
					} catch(JSONException ex) {
						Log.e(Settings.DEBUG_TAG, "JSONException", ex);
					}
				} while(c.moveToNext());				
			}
			buffer.append(scores.toString());
			
			return postDocument(pushAddress, buffer);
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed retrieving scores for data push", e);
			return false;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}
	}
	
	public SongQueryAdapter search(String titleQuery) {
		if(!exists || !open())
			return null;
		
		Cursor c = null;
		String query = String.format(
            "select songid, title, difficulty, totalnotes, bpm, modeid from songs a " +
            "inner join songinfo b on a.songinfoid = b.songinfoid " +
            "where title like '%%%s%%' order by title", titleQuery 
		);
		
		try {
			c = db.rawQuery(query, null);
			ArrayList<SongQuery> aa = new ArrayList<SongQuery>();
			if(c.moveToFirst())
				do {
					SongQuery s = new SongQuery();
					s.SongID = c.getInt(c.getColumnIndexOrThrow("songid"));
					s.Title = c.getString(c.getColumnIndexOrThrow("title"));
					s.Difficulty = c.getInt(c.getColumnIndexOrThrow("difficulty")); 
					s.TotalNotes = c.getInt(c.getColumnIndexOrThrow("totalnotes"));
					s.BPM = c.getString(c.getColumnIndexOrThrow("bpm"));
					
					s.Mode = new Mode();
					s.Mode.ModeID = c.getInt(c.getColumnIndex("modeid"));
					setModeResources(s.Mode);
					
					aa.add(s);
				} while(c.moveToNext());
			c.close();
			
			return new SongQueryAdapter(this.context, R.layout.songlist_layout_item, aa, "title");
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed to query Songs from SQLite for search", e);
			return null;
		} finally {
			if(c != null)
				c.close();
			this.close();
		}	
	}
	
	private boolean postDocument(String pushAddress, StringBuffer document) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(pushAddress);
		// HttpPost post = new HttpPost("http://10.0.2.2:65220/Data/PushScores");
		
		try {
			post.setEntity(new StringEntity(document.toString()));
			HttpResponse response = client.execute(post);
			
			String json = getJsonString(response.getEntity().getContent());
			JSONObject result = new JSONObject(json);
			
			return result.getBoolean("Result");
		} catch(ClientProtocolException e) {
			Log.e(Settings.DEBUG_TAG, "ClientProtocolException", e);
			return false;
		} catch(IOException e) {
			Log.e(Settings.DEBUG_TAG, "IOException", e);
			return false;
		} catch(JSONException e) {
			Log.e(Settings.DEBUG_TAG, "JSONException", e);
			return false;
		}
	}
	
	private boolean open() {		
		try {
			db = this.context.openOrCreateDatabase(IIDX.DATABASE_NAME, 0, null);
			return db.isOpen();
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Failed to open IIDX Database", e);
			return false;
		}
	}
	private void close() {
		if(db != null && db.isOpen())
			db.close();
	}
}
