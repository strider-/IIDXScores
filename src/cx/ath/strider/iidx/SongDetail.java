package cx.ath.strider.iidx;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class SongDetail extends TabActivity {
	private Button btnAddScore, btnSort, btnSortDirection;
	private ListView lvScores;
	private TextView lblLocation, txtSpacer, tvSong, tvSong2;
	private SharedPreferences prefs;
	private ScoreAdapter sa;
	private EditText txtEXScore, txtArcadeScore, txtGreats, txtJustGreats;
	private ChartView cv;
	private SongQuery[] songModes;
	private SongData workingSong;
	private DJ currentDJ;
	
	private Runnable r = new Runnable() {
		public void run() {
			if(btnAddScore != null) {
	   			btnAddScore.setEnabled(IIDX.geoScore.hasLocation());   			
	   			Toast.makeText(SongDetail.this, "Obtained GPS fix", Toast.LENGTH_SHORT).show();
	   			if(IIDX.geoScore.getAddress() != null) {
	   				setAddressText();
	   			}
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.songdetail);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		IIDX.geoScore.onInitialFix(r);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);			
		
		txtEXScore = (EditText)findViewById(R.id.txtEXScore);
		txtArcadeScore = (EditText)findViewById(R.id.txtArcadeScore);
		txtGreats = (EditText)findViewById(R.id.txtGreats);
		txtJustGreats = (EditText)findViewById(R.id.txtJustGreats);
		lvScores = (ListView)findViewById(R.id.lvScores);													
		btnSort = (Button)findViewById(R.id.btnSort);
		btnSortDirection = (Button)findViewById(R.id.btnSortDirection);		
		tvSong  = (TextView)findViewById(R.id.tvSong);
		tvSong2 = (TextView)findViewById(R.id.tvSong2);		
		txtSpacer = (TextView)findViewById(R.id.txtSpacer);				   		
   		btnAddScore = (Button)findViewById(R.id.btnAddScore);
   		lblLocation = (TextView)findViewById(R.id.lblLocation);
   		
   		cv = new ChartView(this);
		((LinearLayout)findViewById(R.id.scoreChart))
			.addView(cv, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		TabHost mTabHost = getTabHost();
		mTabHost.addTab(mTabHost
				.newTabSpec("tab1")
				.setIndicator("New Score", getResources().getDrawable(R.drawable.new_score))
				.setContent(R.id.tbNewScore)
		);
   		mTabHost.addTab(mTabHost
   				.newTabSpec("tab2")
   				.setIndicator("View Scores", getResources().getDrawable(R.drawable.view_scores))
   				.setContent(R.id.llScores)
		);
   		mTabHost.addTab(mTabHost
   				.newTabSpec("tab3")
   				.setIndicator("Score Chart", getResources().getDrawable(R.drawable.chart))
   				.setContent(R.id.scoreChart)
		);   		
   		
   		if(IIDX.geoScore.isEnabled()) {
   			btnAddScore.setEnabled(IIDX.geoScore.hasLocation());
   		}
   		
   		if(IIDX.geoScore.getAddress() != null)
   			setAddressText();   			   		   		
   		
   		mTabHost.setCurrentTab(this.getIntent().getExtras().getInt("TabIndex"));
		currentDJ = (DJ)getIntent().getExtras().getSerializable("DJ");
		SongData currentSongData = (SongData)getIntent().getExtras().getSerializable("Song");
		
		songModes = IIDX.model.getSongModes(currentSongData);
		setSong(currentSongData);   		
	}
    
	private void setSong(SongData song) {	
		workingSong = song;
		
		int modeColor = song.Mode.ModeColor,
		    deepModeColor = (modeColor & 0xfefefefe) >> 1;

	    this.setTitle(String.format("DJ %s", currentDJ.Name));
		tvSong.setText(song.toString());
		tvSong.setTextColor(modeColor);
		tvSong2.setText(song.toString());
		tvSong2.setTextColor(modeColor);
		txtSpacer.setBackgroundColor(deepModeColor);
		((TextView)findViewById(R.id.tvSeparator)).setBackgroundColor(deepModeColor);
		btnSort.setBackgroundColor(deepModeColor);
		btnSortDirection.setBackgroundColor(deepModeColor);
		
   		bindScores();   		
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if(menu.size() == 0) {
	    	for(SongQuery song : songModes) {
	    		String title = String.format("%d\u2605 %s", song.Difficulty, song.Mode.ModeName);
	    		menu.add(Menu.NONE, song.Mode.ModeID, song.Mode.ModeID, title);
	    	}
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	for(SongQuery song : songModes) {
    		if(item.getItemId() == song.Mode.ModeID) {
    			this.setSong(IIDX.model.getSongData(song, currentDJ, getScoreSort()));
    			break;
    		}
    	}
    	return true;
    }
    
	public void addScore(View v) {
		String exText = txtEXScore.getText().toString(),
		       acText = txtArcadeScore.getText().toString();
	
		if(exText == null || exText.length() == 0)
			return;
		
		int ex = Integer.parseInt(exText);
		int ac = (acText == null || acText.length() == 0) ? 0 : Integer.parseInt(acText);
		
		double lat = IIDX.geoScore.getLatitude(), 
		       lon = IIDX.geoScore.getLongitude();										
		
		boolean added = IIDX.model.addScore(workingSong, currentDJ, ex, ac, lat, lon);
		
		if(!added) {						
			AlertDialog.Builder d = new AlertDialog.Builder(SongDetail.this);
			d.setMessage("Failed to add score!");
			d.setNeutralButton("Ok", null);
			d.show();
		} else
			Toast.makeText(SongDetail.this, "Score Added.", Toast.LENGTH_LONG).show();
			SongDetail.this.finish();		
	}
	
	public void generateEXScore(View v) {
		String jgText = txtJustGreats.getText().toString(),
	    	   gText = txtGreats.getText().toString();

		if(jgText == null || jgText.length() == 0 || gText == null || gText.length() == 0)
			return;
		
		int jg = Integer.parseInt(jgText);
		int g = Integer.parseInt(gText);
		txtEXScore.setText(String.valueOf((jg * 2) + g));
		txtJustGreats.setText("");
		txtGreats.setText("");
		txtEXScore.requestFocus();		
	}
	
	public void toggleSort(View v) {				
		if(getScoreSort().equals("exscore")) {
			prefs.edit().putString("score_sorting", "stamp").commit();
		} else {
			prefs.edit().putString("score_sorting", "exscore").commit();
		}		
				
		bindScores();
	}
	public void toggleSortOrder(View v) {
		if(getScoreSortOrder()) {
			prefs.edit().putBoolean("score_sorting_order", false).commit();
		} else {
			prefs.edit().putBoolean("score_sorting_order", true).commit();
		}
		
		bindScores();
	}
	private void bindScores() {
		boolean byEXScore = getScoreSort().equals("exscore"),
		        asc = getScoreSortOrder();
		workingSong.sortScores(byEXScore, asc);
   		sa = new ScoreAdapter(this, workingSong.Scores);
   		lvScores.setAdapter(sa);
   		btnSort.setText(byEXScore ? "EX Score" : "Date/Time");
   		btnSortDirection.setText(asc ? "Ascending" : "Descending");
   		
   		if(sa.getCount() == 0) {
   			((TextView)findViewById(R.id.txtNoScores)).setVisibility(View.VISIBLE);
   			btnSort.setVisibility(View.GONE);
   			txtSpacer.setVisibility(View.GONE);
   			btnSortDirection.setVisibility(View.GONE);
   		} else {
   			((TextView)findViewById(R.id.txtNoScores)).setVisibility(View.GONE);
   			btnSort.setVisibility(View.VISIBLE);
   			txtSpacer.setVisibility(View.VISIBLE);
   			btnSortDirection.setVisibility(View.VISIBLE);   			
   		}
   		   		
   		if(workingSong.Scores.length == 0) {
   			cv.setVisibility(View.GONE);
   			((TextView)findViewById(R.id.txtNoChart)).setVisibility(View.VISIBLE);
   		} else {
			cv.setMaxValue(workingSong.Song.TotalNotes * 2);
			cv.setScores(workingSong.Scores);
			cv.setChartColor(workingSong.Mode.ModeColor);
   			cv.setVisibility(View.VISIBLE);
   			((TextView)findViewById(R.id.txtNoChart)).setVisibility(View.GONE);			
   		}   		
	}
	private String getScoreSort() {
		return prefs.getString("score_sorting", getResources().getString(R.string.score_sorting_default));
	}
	private boolean getScoreSortOrder() {
		return prefs.getBoolean("score_sorting_order", false);
	}
	private void setAddressText() {
		lblLocation.setText(String.format("%s, %s",
			IIDX.geoScore.getAddress().getLocality(),
			IIDX.geoScore.getAddress().getAdminArea()
		));
	}	
}