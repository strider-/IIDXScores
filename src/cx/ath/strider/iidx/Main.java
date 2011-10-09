package cx.ath.strider.iidx;

import cx.ath.strider.iidx.adapter.SongQueryAdapter;
import cx.ath.strider.iidx.base.BaseActivity;
import cx.ath.strider.iidx.model.DJ;
import cx.ath.strider.iidx.model.IIDXModel;
import cx.ath.strider.iidx.model.SongData;
import cx.ath.strider.iidx.model.SongQuery;
import android.net.wifi.WifiManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends BaseActivity implements OnSharedPreferenceChangeListener  {	
	private Settings settings;
	
	private ProgressDialog workinDialog, downloadinDialog;
	private boolean goodPublish, goodPull;
	private final Handler mHandler = new Handler(),
	                      uHandler = new Handler();
	private SongData CurrentSong;
	private ActionBarView bar;
	private QuickAction qa;
	private DJ currentDJ;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	bar = getViewById(R.id.actionbar);
    	settings = new Settings(getApplicationContext());
        settings.registerSettingsChangeListener(this);                       
        IIDX.geoScore = new GeoScore(this);
        
        setGeo();
        prepareModel();            
    }
    
    @Override
    public boolean onSearchRequested() {
		bar.showSearch();
    	return true;
    }
    
    @Override
    public void onDestroy() {
    	settings.unregisterSettingsChangeListener(this);
    	IIDX.geoScore.Disable();
    	super.onDestroy();
    }
    
    @Override
    public void onResume() {
    	if(qa != null)
    		qa.dismiss();
    	if(bar.inSearchMode()) {
    		bar.hideSearch();
    		refreshSongList();
    	}
    	super.onResume();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	AppState state = new AppState();    	
    	state.Style = bar.getStyle();
    	state.Mode = bar.getMode();
    	state.DJ = currentDJ;
    	state.Location = IIDX.geoScore.getLocation();
    	return state;
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if(hasFocus)
    		CurrentSong = null;    		
    }
        
    private void setGeo() {    	    	   
    	boolean enabled = settings.getGeoScoreEnabled();
    	
    	if(enabled) {
    		if(!IIDX.geoScore.hasLocation() && this.getLastNonConfigurationInstance() != null)
    			IIDX.geoScore.setLocation(((AppState)this.getLastNonConfigurationInstance()).Location);
        	
    		IIDX.geoScore.onInitialFix(new Runnable() {
        		public void run() {
    				Toast.makeText(Main.this, "Obtained GPS fix", Toast.LENGTH_SHORT).show();
        		}
        	});
    	}
    	
    	IIDX.geoScore.SetState(enabled);
    }
    
    private void prepareModel() {
    	IIDX.model = new IIDXModel(this);
    	TextView tvNoData = getViewById(R.id.tvNoData);
    	
        if(IIDX.model.getDatabaseExists()) {
        	bar.setVisibility(View.VISIBLE);
    		tvNoData.setVisibility(View.GONE);      	
        	dataBind();
        } else {
        	bar.setVisibility(View.GONE);
        	tvNoData.setVisibility(View.VISIBLE);
        }     
    }
    
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("sorting") || key.equals("acincs") || key.equals("revivals") || key.equals("section_headers"))
			refreshSongList();
		else if(key.equals("geoscore"))
			setGeo();
	}
        
    private void dataBind() {    	
    	Runnable refresh = new Runnable() {
    		public void run() { refreshSongList(); }
    	};
    	bar.setStyleAdapter(IIDX.model.getStyles());
    	bar.setModeAdapter(IIDX.model.getModes());    	
    	bar.setOnCancelSearchMode(refresh);
    	
        final ListView lvSongs = getViewById(R.id.lvSongs); 
        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				setCurrentSong((SongQuery)arg0.getItemAtPosition(arg2));
				openDetail(0);
			}        	
		});        
        lvSongs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(final AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				qa = new QuickAction(arg1);												
				setCurrentSong((SongQuery)arg0.getItemAtPosition(arg2));
				
				ActionItem a = new ActionItem();
				a.setTitle(String.valueOf(CurrentSong.Scores.length) + " Score");
				if(CurrentSong.Scores.length != 1)
					a.setTitle(a.getTitle() + "s");
				a.setIcon(Main.this.getResources().getDrawable(R.drawable.view_scores));
				a.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {						
						openDetail(1);
					}					
				});
				qa.addActionItem(a);
				
				if(CurrentSong.Scores.length > 0) {
					ActionItem b = new ActionItem();
					b.setTitle("Score Chart");
					b.setIcon(Main.this.getResources().getDrawable(R.drawable.chart));
					b.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							openDetail(2);
						}					
					});								
					qa.addActionItem(b);
				}
				
				qa.show();
				return true;
			}        	
		});
        
    	bar.setOnSearchChange(new Runnable() {
    		public void run() {
    			if(bar.hasSearchTerm())
    				lvSongs.setAdapter(IIDX.model.search(bar.getSearchTerm()));    			
    		}
    	});
        
        if(this.getLastNonConfigurationInstance() != null) {
        	AppState state = (AppState)this.getLastNonConfigurationInstance();
        	bar.setStyle(state.Style);
        	bar.setMode(state.Mode);
        	currentDJ = state.DJ;        	        	
        } else {
        	bar.setStyle(IIDX.model.getStyleFromID(settings.getDefaultStyle()));
        	bar.setMode(IIDX.model.getModeFromID(settings.getDefaultMode()));
            currentDJ = IIDX.model.getDJs().getItem(0);
        }
        
        bar.setOnChange(refresh);
        refreshSongList();
    }
    private void setCurrentSong(SongQuery song) {
    	CurrentSong = IIDX.model.getSongData(
    				  	song, 
    				  	currentDJ, 
    				  	settings.getScoreSort()
	  	);
    }
    private void openDetail(int tabIndex) {    	
		Intent i = new Intent(Main.this, SongDetail.class);
		i.putExtra("TabIndex", tabIndex);
		i.putExtra("Song", CurrentSong);
		i.putExtra("DJ", currentDJ);
		startActivity(i);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	WifiManager wm = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
    	String currentSsid = wm.getConnectionInfo().getSSID();
    	String homeSsid = settings.getSsid();
    	boolean enabled = wm.isWifiEnabled() && currentSsid != null && currentSsid.equalsIgnoreCase(homeSsid);
    	 
    	enabled=true;
    	menu.findItem(R.id.pull).setEnabled(enabled);
    	menu.findItem(R.id.push).setEnabled(enabled);
    	
    	menu.findItem(R.id.selectDJ).setEnabled(IIDX.model.getDatabaseExists());
    	if(IIDX.model.getDatabaseExists())
    		menu.findItem(R.id.selectDJ).setTitle("DJ " + currentDJ.Name);
    	else    		
    		menu.findItem(R.id.selectDJ).setTitle("Select DJ");
    	
    	menu.findItem(R.id.mnuGPS).setVisible(IIDX.geoScore.isEnabled());
    	
    	return true;
    }
    
    private void refreshSongList() {
    	if(IIDX.model.getDatabaseExists()) {
	    	ListView lvSongs = getViewById(R.id.lvSongs);    	
	    	SongQueryAdapter adapter = (SongQueryAdapter)lvSongs.getAdapter();
	    	SongQuery lastSong = null;
	    	
	    	if(adapter != null && adapter.getCount() > 0) {
	    		lastSong = adapter.getItem(lvSongs.getFirstVisiblePosition());
	    	}
	    	
	    	//lvSongs.setFastScrollEnabled(false);
			lvSongs.setAdapter(adapter = IIDX.model.getSongs(
					bar.getStyle(),
					bar.getMode(),
					settings.getSongListSort(), 
					settings.getShowACinCS(), 
					settings.getIncludeRevivals(),
					settings.getShowSectionHeaders()
			));
			//lvSongs.setFastScrollEnabled(true);
			//LinearLayout grid = (LinearLayout)lvSongs.getParent();
			//grid.setLayoutParams(new FrameLayout.LayoutParams(grid.getWidth()-1, FrameLayout.LayoutParams.FILL_PARENT));
			
			if(lastSong != null)
				lvSongs.setSelection(adapter.getPosition(lastSong));	
    	}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  	
    	switch(item.getItemId()) {
    	case R.id.push:
    		int scoreCount = IIDX.model.getNewScoreCount(settings.getMinPushDate());
    		if(IIDX.model.getDatabaseExists() && scoreCount > 0) {
    			showPromptDialog(
					String.format("Push %d new score(s) to the local service?", scoreCount),
					"Confirm Push",
					pushClickListener
				);    		
    		} else {
    			// no new scores or the local database doesn't exist!
    			showMsgDialog("There's nothing to update!", "No Data");
    		}
    		break;
    	case R.id.pull:
    		if(IIDX.model.getDatabaseExists()) {
    			showPromptDialog(
    				"This will delete ALL current local data. Ok?",
    				"Confirm Pull",
    				pullClickListener
				);
    		} else {
    			// there's nothing to delete, just get the data.
    			pullClickListener.onClick(null, DialogInterface.BUTTON_POSITIVE);
    		}
    		break;
    	case R.id.preferences:
    		Intent i = new Intent(Main.this, Preferences.class);
    		startActivity(i);
    		break;
    	case R.id.selectDJ:
        	AlertDialog.Builder d = new AlertDialog.Builder(this);
        	final ListAdapter la = IIDX.model.getDJs();
        	d.setTitle("Select DJ");    	
        	d.setAdapter(la, new OnClickListener() {
        		public void onClick(DialogInterface di, int index) {
        			currentDJ = (DJ)la.getItem(index);
        		}
        	});
        	d.show();    		
    		break;
    	case R.id.mnuGPS:
    		IIDX.geoScore = new GeoScore(this);
    		setGeo();
    		break;
    	}
    	
    	return true;
    }
    private void showPromptDialog(String Message, String Title, OnClickListener onPositiveClick) {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	dialog.setMessage(Message)
    		  .setTitle(Title)
    		  .setPositiveButton("Yes", onPositiveClick)
    		  .setNegativeButton("No", null)
    		  .setIcon(R.drawable.icon)
    		  .show();
    }
    private void showMsgDialog(String Message, String Title) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(Title)
		 .setMessage(Message)
		 .setNeutralButton("Ok", null)
		 .setIcon(R.drawable.icon)
		 .show();
    }    
        
    private OnClickListener pullClickListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:            	
            	Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            	
            	downloadinDialog = ProgressDialog.show(Main.this, "", "Downloading Data...");

            	workinDialog = new ProgressDialog(Main.this);
            	workinDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        		workinDialog.setMessage("Initializing...");
            	
        		Thread t = new Thread() {
        			public void run() {
        				final IIDXHandler data = new IIDXHandler(Main.this);
        				
        				data.onProgressUpdate(new Runnable() {
        					public void run() {
        						uHandler.post(new Runnable() {
        							public void run() {
    									downloadinDialog.dismiss();    									
    									workinDialog.setMax(data.getMaxProgress());    									
        								workinDialog.setMessage(data.getPullProgressItem());
    									workinDialog.setProgress(data.getPullProgress());   
    									workinDialog.setSecondaryProgress(data.getPullProgress());
    									workinDialog.show();
        							}
        						});
        					}
        				});
        				
        	    		goodPull = data.fetchData(settings.getPullAddress());
        	    		
        	    		mHandler.post(new Runnable(){
        	    			public void run() {        	    				
        	    				workinDialog.dismiss();
        	    				Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        	    				if(goodPull)
        	    					settings.setMinPushDate();
        	    				else
        	    					showMsgDialog("Failed to obtain IIDX data!", "Error");
        	    				prepareModel();
        	    			}
        	    		});
        			}
        		};
        		t.start();
                break;
            }
        }
    };    
    
    private OnClickListener pushClickListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
            	Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        		workinDialog = ProgressDialog.show(Main.this, "", "Uploading scores...");
        		Thread t = new Thread() {
        			public void run() {
        				goodPublish = IIDX.model.updateHost(settings.getPushAddress(), settings.getMinPushDate());
        				
        				mHandler.post(new Runnable() {
        					public void run() {
        						workinDialog.dismiss();
        						Main.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        						if(goodPublish) {
        							settings.setMinPushDate();
        							Toast.makeText(Main.this, "Scores published.", Toast.LENGTH_LONG).show();
        						} else
        							showMsgDialog("Failed to publish scores!", "Error");
        					}
        				});
        			}
        		};
        		t.start();
                break;
            }
        }
    };
}