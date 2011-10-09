package cx.ath.strider.iidx;

import java.util.Date;

import cx.ath.strider.iidx.model.Mode;
import cx.ath.strider.iidx.model.Style;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Adapter;

public class Preferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
			
		Preference mpd = findPreference("min_push_date"),
		           defaultParent = findPreference("defaults_screen");
		
		Settings settings = new Settings(getApplicationContext());
		
		defaultParent.setEnabled(IIDX.model.getDatabaseExists());
		if(IIDX.model.getDatabaseExists()) {		
			long rawDate = settings.getMinPushDate();
						
			mpd.setTitle(String.format(mpd.getTitle().toString(), IIDX.model.getNewScoreCount(rawDate)));
			mpd.setSummary(String.format(mpd.getSummary().toString(), new Date(rawDate).toLocaleString()));			
			setDefaultEntries();
		} else {
			mpd.setTitle("No Local Database");
			mpd.setSummary("New score count & last sync date will be shown here.");		
		}
	}
	
	private void setDefaultEntries() {
		ListPreference defaultStyle = (ListPreference)findPreference("default_style"), 
		   			   defaultMode = (ListPreference)findPreference("default_mode");		
		Adapter styles = IIDX.model.getStyles(), modes = IIDX.model.getModes();
		int styleCount = styles.getCount();
		int modeCount = modes.getCount();		
		String[] styleEntries = new String[styleCount],
		         styleEntryValues = new String[styleCount],
		         modeEntries = new String[modeCount],
		         modeEntryValues = new String[modeCount];
		
		for(int i=0; i<styleCount; i++) {
			Style item = (Style)styles.getItem(i);
			styleEntries[i] = item.toString();
			styleEntryValues[i] = String.valueOf(item.StyleID);
		}
		
		defaultStyle.setEntries(styleEntries);
		defaultStyle.setEntryValues(styleEntryValues);
		defaultStyle.setOnPreferenceChangeListener(onDefaultChange);
		if(defaultStyle.getEntry() != null)
			defaultStyle.setSummary(defaultStyle.getEntry());
		
		for(int i=0; i<modeCount; i++) {
			Mode mode = (Mode)modes.getItem(i);
			modeEntries[i] = mode.ModeName;
			modeEntryValues[i] = String.valueOf(mode.ModeID);
		}
		
		defaultMode.setEntries(modeEntries);
		defaultMode.setEntryValues(modeEntryValues);
		defaultMode.setOnPreferenceChangeListener(onDefaultChange);
		if(defaultMode.getEntry() != null)
			defaultMode.setSummary(defaultMode.getEntry());
	}
	
	private OnPreferenceChangeListener onDefaultChange = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			ListPreference lp = (ListPreference)arg0;
			lp.setSummary(lp.getEntries()[lp.findIndexOfValue((String)arg1)]);
			return true;
		}		
	};
}