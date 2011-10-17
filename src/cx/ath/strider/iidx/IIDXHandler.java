package cx.ath.strider.iidx;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IIDXHandler extends JsonHandler {
	private static final String CREATE_TABLE_DJS = "CREATE TABLE DJs (djid integer, djname text not null, pwd text null, info text null);";
	private static final String CREATE_TABLE_MODES = "CREATE TABLE Modes (modeid integer, modename text not null, abbr text not null);";
	private static final String CREATE_TABLE_SONGINFO = "CREATE TABLE SongInfo (songinfoid integer, styleid integer, title text not null, genre text not null, artist text not null, bpm text not null, notes text null);";
	private static final String CREATE_TABLE_SONGS = "CREATE TABLE Songs (songid integer, modeid integer, songinfoid integer, totalnotes integer, difficulty integer);";
	private static final String CREATE_TABLE_STYLES = "CREATE TABLE Styles (styleid integer, styleorder integer, stylename text not null, theme text null, parentid integer null);";
	private static final String CREATE_TABLE_SCORES = "CREATE TABLE Scores (scoreid integer primary key autoincrement, djid integer, songid integer, exscore integer, arcadescore integer null, stamp integer, lat text null, lon text null);";
	private static final String CREATE_TABLE_CSREVIVALS = "CREATE TABLE CSRevivals (id integer, songinfoid integer, styleid integer)";	
	
	
	private SQLiteDatabase db;
	private Context context;
	private int currentProgress=0, maxProgress=0;
	private String ProgressItem="";
	
	public IIDXHandler(Context context) {
		this.context = context;
		try {			
			context.deleteDatabase(IIDX.DATABASE_NAME);
			
			db = context.openOrCreateDatabase(IIDX.DATABASE_NAME, 0, null);
			db.execSQL(CREATE_TABLE_DJS);
			db.execSQL(CREATE_TABLE_MODES);
			db.execSQL(CREATE_TABLE_SONGINFO);
			db.execSQL(CREATE_TABLE_SONGS);
			db.execSQL(CREATE_TABLE_STYLES);
			db.execSQL(CREATE_TABLE_SCORES);
			db.execSQL(CREATE_TABLE_CSREVIVALS);
			
			createIndexes();			
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "Database Creation or Opening", e);
		}
	}
	
    private void createIndexes() {
    	String[] indexes = new String[] {
            // DJs
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_DJID ON DJs (djid)",
            // Modes
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_MODEID ON Modes (modeid)",
            // Scores
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_SCOREID ON Scores (scoreid)",
            "CREATE INDEX IF NOT EXISTS IDX_DJID ON Scores (djid)",
            "CREATE INDEX IF NOT EXISTS IDX_SONGID ON Scores (songid)",
            // SongInfo
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_SONGINFOID ON SongInfo (songinfoid)",
            "CREATE INDEX IF NOT EXISTS IDX_STYLEID ON SongInfo (styleid)",
            // Songs
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_SONGID ON Songs (songid)",
            "CREATE INDEX IF NOT EXISTS IDX_MODEID ON Songs (modeid)",
            "CREATE INDEX IF NOT EXISTS IDX_SONGINFOID ON Songs (songinfoid)",
            // Styles
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_STYLEID ON Styles (styleid)",
            // CSRevivals
            "CREATE UNIQUE INDEX IF NOT EXISTS IDX_CSRID ON CSRevivals (id)",
            "CREATE INDEX IF NOT EXISTS IDX_CSRSONGINFOID ON CSRevivals (songinfoid)",
            "CREATE INDEX IF NOT EXISTS IDX_CSRSTYLEID ON CSRevivals (styleid)"    			
    	};
    	
    	try {
	    	for(int i=0; i<indexes.length; i++) {
	    		db.execSQL(indexes[i]);
	    	}
    	} catch(SQLException e) {
    		Log.e(Settings.DEBUG_TAG, "Failed to create indexes.", e);
    	}
    }
	
    public boolean fetchData(String address) {
		HttpClient client = new DefaultHttpClient();		
		HttpGet get = new HttpGet(address);
		
		try {
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 10000);			
			HttpResponse response = client.execute(get);

			Log.i(Settings.DEBUG_TAG, "Downloading JSON");			
			String raw = getJsonString(response.getEntity().getContent());
			JSONObject json = new JSONObject(raw);
			
			JSONObject iidx = json.getJSONObject("IIDX");					          
			maxProgress = iidx.getJSONArray("DJs").length() +
						  iidx.getJSONArray("Modes").length() +
						  iidx.getJSONArray("Styles").length() +
						  iidx.getJSONArray("SongInfo").length() +
						  iidx.getJSONArray("Songs").length() +
						  iidx.getJSONArray("Scores").length() +
						  iidx.getJSONArray("CSRevivals").length();
			Log.i(Settings.DEBUG_TAG, "Item Count: " + maxProgress);
			
			populateDJs(iidx.getJSONArray("DJs"));
			populateModes(iidx.getJSONArray("Modes"));
			populateStyles(iidx.getJSONArray("Styles"));
			populateSongInfo(iidx.getJSONArray("SongInfo"));
			populateSongs(iidx.getJSONArray("Songs"));
			populateScores(iidx.getJSONArray("Scores"));
			populateRevivals(iidx.getJSONArray("CSRevivals"));
			
			return true;
		} catch(ClientProtocolException e) {
			Log.e(Settings.DEBUG_TAG, "ClientProtocolException", e);
			context.deleteDatabase(IIDX.DATABASE_NAME);
			return false;
		} catch(SQLException e) {
			Log.e(Settings.DEBUG_TAG, "SQLException", e);
			context.deleteDatabase(IIDX.DATABASE_NAME);
			return false;
		} catch(JSONException e) {
			Log.e(Settings.DEBUG_TAG, "SAXException", e);
			context.deleteDatabase(IIDX.DATABASE_NAME);
			return false;
		} catch (IOException e) {
			Log.e(Settings.DEBUG_TAG, "IOException", e);
			return false;
		} finally {
			Log.i(Settings.DEBUG_TAG, "Data pull complete");
			db.close();
		}
    }
    
    private void incrementProgress(String msg) {
    	currentProgress++;
    	ProgressItem = msg;
    	opu.run();
    }
    private Runnable opu;
    public void onProgressUpdate(Runnable method) {
    	this.opu = method;
    }
    
    private void populateDJs(JSONArray djs) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating DJs");		
    	for(int i=0; i<djs.length(); i++) {
    		incrementProgress("Populating DJs");
    		JSONObject obj = djs.getJSONObject(i);
	    	ContentValues values = new ContentValues();
	    	values.put("djid", obj.getInt("ID"));
	    	values.put("djname", obj.getString("DJName"));
	    	values.put("pwd", obj.getString("Password"));
	    	values.put("info", obj.getString("Info"));
	    	db.insert("DJs", null, values);
    	}
    }
    private void populateModes(JSONArray modes) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating Modes");
    	for(int i=0; i<modes.length(); i++) {
    		incrementProgress("Populating Modes");
    		JSONObject obj = modes.getJSONObject(i);
    		ContentValues values = new ContentValues();
    		values.put("modeid", obj.getInt("ID"));
    		values.put("modename", obj.getString("Mode"));
    		values.put("abbr", obj.getString("Abbr"));
    		db.insert("Modes", null, values);
    	}
    }
    private void populateStyles(JSONArray styles) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating Styles");
    	for(int i=0; i<styles.length(); i++) {
    		incrementProgress("Populating Styles");
    		JSONObject obj = styles.getJSONObject(i);
    		ContentValues values = new ContentValues();
    		values.put("styleid", obj.getInt("ID"));
    		values.put("styleorder", obj.getInt("StyleOrder"));
    		values.put("stylename", obj.getString("StyleName"));
			values.put("theme", obj.getString("Theme"));
			values.put("parentid", obj.get("ParentID") == JSONObject.NULL ? null : obj.getInt("ParentID"));				
    		db.insert("Styles", null, values);
    	}
    }
	private void populateSongInfo(JSONArray songinfo) throws JSONException {
		Log.i(Settings.DEBUG_TAG, "Populating SongInfo");
    	for(int i=0; i<songinfo.length(); i++) {
    		incrementProgress("Populating Song Info");
    		JSONObject obj = songinfo.getJSONObject(i);
			ContentValues values = new ContentValues();
			values.put("songinfoid", obj.getInt("ID"));
			values.put("styleid", obj.getInt("StyleID"));
			values.put("title", obj.getString("Title"));
			values.put("genre", obj.getString("Genre"));
			values.put("artist", obj.getString("Artist"));
			values.put("bpm", obj.getString("BPM"));
			values.put("notes", obj.getString("Notes"));
			db.insert("SongInfo", null, values);	
    	}
	}
    private void populateSongs(JSONArray songs) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating Songs");
    	for(int i=0; i<songs.length(); i++) {
    		incrementProgress("Populating Songs");
    		JSONObject obj = songs.getJSONObject(i);
    		ContentValues values = new ContentValues();
    		values.put("songid", obj.getInt("ID"));
    		values.put("modeid", obj.getInt("ModeID"));
    		values.put("songinfoid", obj.getInt("SongInfoID"));
			values.put("totalnotes", obj.getInt("TotalNotes"));
			values.put("difficulty", obj.getInt("Difficulty"));
    		db.insert("Songs", null, values);
    	}
    }
    private void populateScores(JSONArray scores) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating Scores");
    	for(int i=0; i<scores.length(); i++) {
    		incrementProgress("Populating Scores");
    		JSONObject obj = scores.getJSONObject(i);
    		ContentValues values = new ContentValues();
    		values.put("scoreid", obj.getInt("ID"));
    		values.put("djid", obj.getInt("DJID"));
    		values.put("songid", obj.getInt("SongID"));
			values.put("exscore", obj.getInt("EXScore"));
			values.put("arcadescore", obj.get("ArcadeScore") == JSONObject.NULL ? null : obj.getInt("ArcadeScore"));			
			
			String stamp = obj.getString("Stamp");
			stamp = stamp.substring(6, stamp.length() - 2);			
			values.put("stamp", Long.valueOf(stamp));
			
			values.put("lat", obj.getDouble("Latitude"));
			values.put("lon", obj.getDouble("Longitude"));
    		db.insert("Scores", null, values);
    	}
    }
    private void populateRevivals(JSONArray revivals) throws JSONException {
    	Log.i(Settings.DEBUG_TAG, "Populating Revivals");
    	for(int i=0; i<revivals.length(); i++) {
    		incrementProgress("Populating Revivals");
    		JSONObject obj = revivals.getJSONObject(i);
    		ContentValues values = new ContentValues();
    		values.put("id", obj.getInt("ID"));
    		values.put("songinfoid", obj.getInt("SongInfoID"));
    		values.put("styleid", obj.getInt("StyleID"));
    		db.insert("CSRevivals", null, values);
    	}
    }

	public int getPullProgress() { return currentProgress; }
	public int getMaxProgress() { return maxProgress; }
	public String getPullProgressItem() { return ProgressItem; }
}
