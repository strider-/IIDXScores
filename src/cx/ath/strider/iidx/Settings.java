package cx.ath.strider.iidx;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Settings {
	private SharedPreferences prefs;
	private Context context;
	
	public Settings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public void registerSettingsChangeListener(OnSharedPreferenceChangeListener listener) {
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	public void unregisterSettingsChangeListener(OnSharedPreferenceChangeListener listener) {
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	public boolean getGeoScoreEnabled() {
		return prefs.getBoolean("geoscore", false);
	}
	
	public int getDefaultStyle() {
		return Integer.valueOf(prefs.getString("default_style", "0"));
	}
	
	public int getDefaultMode() {
		return Integer.valueOf(prefs.getString("default_mode", "0"));
	}
	
	public String getScoreSort() {
		return prefs.getString("score_sorting", context.getResources().getString(R.string.score_sorting_default));
	}
	
	public void setScoreSort(String sort) {
		prefs.edit()
			.putString("score_sorting", sort)
		.commit();
	}
	
	public boolean isScoreSortOrderAscending() {
		return prefs.getBoolean("score_sorting_order", false);
	}
	
	public void setScoreSortOrder(boolean ascending) {
		prefs.edit()
			.putBoolean("score_sorting_order", ascending)
		.commit();
	}
	
	public String getSsid() {
		return prefs.getString("ssid", context.getResources().getString(R.string.ssid_default));
	}
	
	public String getSongListSort() {
		return prefs.getString("sorting", context.getResources().getString(R.string.sorting_default));
	}
	public void setSongListSort(String sort) {
		prefs.edit()
			.putString("sorting", sort)
		.commit();
	}
	
	public boolean getShowACinCS() {
		return prefs.getBoolean("acincs", Boolean.valueOf(context.getResources().getString(R.string.acincs_default)));
	}
	
	public boolean getIncludeRevivals() {
		return prefs.getBoolean("revivals", Boolean.valueOf(context.getResources().getString(R.string.revivals_default)));
	}
	
	public boolean getShowSectionHeaders() {
		return prefs.getBoolean("section_headers", Boolean.valueOf(context.getResources().getString(R.string.section_headers_default)));
	}
	
	public String getPullAddress() {
    	String host = prefs.getString("service_host", context.getResources().getString(R.string.service_host_default));
    	return String.format(context.getResources().getString(R.string.pull_address_format), host);
	}
	
	public String getPushAddress() {
    	String host = prefs.getString("service_host", context.getResources().getString(R.string.service_host_default));
    	return String.format(context.getResources().getString(R.string.push_address_format), host);
	}	
	
	public long getMinPushDate() {
		return prefs.getLong("min_push_date", System.currentTimeMillis());
	}
	
	public void setMinPushDate() {
    	prefs.edit()
    		.putLong("min_push_date", System.currentTimeMillis())
		.commit();
	}
}
