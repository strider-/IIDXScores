package cx.ath.strider.iidx;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

/**
 * Manages application wide settings 
 * @author strider-
 *
 */
public class Settings {
	public static final String DEBUG_TAG = "IIDX";
	public static final String KEY_GEOSCORE = "geoscore";
	public static final String KEY_DEFAULT_STYLE = "default_style";
	public static final String KEY_DEFAULT_MODE = "default_mode";	
	public static final String KEY_SCORE_SORT = "score_sorting";
	public static final String KEY_SCORE_SORT_ORDER = "score_sorting_order";
	public static final String KEY_SSID = "ssid";
	public static final String KEY_SONGLIST_SORT = "sorting";
	public static final String KEY_ARCADE_IN_CONSUMER = "acincs";
	public static final String KEY_REVIVALS = "revivals";
	public static final String KEY_SECTION_HEADERS = "section_headers";
	public static final String KEY_REMOTE_REPOSITORY = "service_host";
	public static final String KEY_LAST_PUSH_DATE = "min_push_date";
	public static final String KEY_SHOW_SPLASHSCREEN = "show_splash";
	
	private SharedPreferences prefs;
	private Context context;
	
	/**
	 * Creates a new Settings instance, with an application context. 
	 * @param context
	 */
	public Settings(Context context) {
		this.context = context;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * Registers a callback to be invoked when any settings change 
	 * @param listener Callback to be registered
	 */
	public void registerSettingsChangeListener(OnSharedPreferenceChangeListener listener) {
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	/**
	 * Unregisters a previous callback
	 * @param listener The callback to be unregistered
	 */
	public void unregisterSettingsChangeListener(OnSharedPreferenceChangeListener listener) {
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	public boolean showSplashScreen() {
		return prefs.getBoolean(KEY_SHOW_SPLASHSCREEN, true);
	}
	
	/**
	 * Gets whether or not to save the location along with new scores
	 * @return true to save scores with user location, otherwise false
	 */
	public boolean getGeoScoreEnabled() {
		return prefs.getBoolean(KEY_GEOSCORE, false);
	}
	
	/**
	 * Gets the unique ID of the style that will be set upon the app starting
	 * @return Unique ID of the style
	 */
	public int getDefaultStyle() {
		return Integer.valueOf(prefs.getString(KEY_DEFAULT_STYLE, "0"));
	}
	
	/**
	 * Gets the unique ID of the mode that will be set upon the app starting
	 * @return Unique ID of the mode
	 */
	public int getDefaultMode() {
		return Integer.valueOf(prefs.getString(KEY_DEFAULT_MODE, "0"));
	}
	
	/**
	 * Gets the field the score list is being sorted by
	 * @return field name of the sort
	 */
	public String getScoreSort() {
		return prefs.getString(KEY_SCORE_SORT, context.getResources().getString(R.string.score_sorting_default));
	}
	
	/**
	 * Sets the field the score list will be sorted by
	 * @param sort field name to sort scores by
	 */
	public void setScoreSort(String sort) {
		prefs.edit()
			.putString(KEY_SCORE_SORT, sort)
		.commit();
	}
	
	/**
	 * Gets whether or not the score list is being sorted in ascending order
	 * @return true if the score list is being sorted in ascending order, false for descending.
	 */
	public boolean isScoreSortOrderAscending() {
		return prefs.getBoolean(KEY_SCORE_SORT_ORDER, false);
	}
	
	/**
	 * Sets the sort order of the score list
	 * @param ascending true to sort ascending, false to sort descending
	 */
	public void setScoreSortOrder(boolean ascending) {
		prefs.edit()
			.putBoolean(KEY_SCORE_SORT_ORDER, ascending)
		.commit();
	}
	
	/**
	 * Gets the SSID of the network for pushing/pulling data
	 * @return Network SSID
	 */
	public String getSsid() {
		return prefs.getString(KEY_SSID, context.getResources().getString(R.string.ssid_default));
	}
	
	/**
	 * Gets the field the song list is being sorted by
	 * @return field name of the sort
	 */
	public String getSongListSort() {
		return prefs.getString(KEY_SONGLIST_SORT, context.getResources().getString(R.string.sorting_default));
	}
	
	/**
	 * Sets the field the song list will be sorted by
	 * @param sort The sorting field
	 */
	public void setSongListSortBy(String sort) {
		prefs.edit()
			.putString(KEY_SONGLIST_SORT, sort)
		.commit();
	}
	
	/**
	 * Gets whether or not arcade style songs are shown in consumer styles
	 * @return true if arcade songs are shown, otherwise false
	 */
	public boolean getShowACinCS() {
		return prefs.getBoolean(KEY_ARCADE_IN_CONSUMER, Boolean.valueOf(context.getResources().getString(R.string.acincs_default)));
	}
	
	/**
	 * Gets whether or not revivals will be included in a song list (CS only)
	 * @return true of revivals are included, otherwise false
	 */
	public boolean getIncludeRevivals() {
		return prefs.getBoolean(KEY_REVIVALS, Boolean.valueOf(context.getResources().getString(R.string.revivals_default)));
	}
	
	/**
	 * Gets whether or not section headers will be shown on the song list
	 * @return true if headers are to be shown, otherwise false
	 */
	public boolean getShowSectionHeaders() {
		return prefs.getBoolean(KEY_SECTION_HEADERS, Boolean.valueOf(context.getResources().getString(R.string.section_headers_default)));
	}
	
	/**
	 * Gets the url of the remote repository for downloading data
	 * @return The url of the remote repository
	 */
	public String getPullAddress() {
    	String host = prefs.getString(KEY_REMOTE_REPOSITORY, context.getResources().getString(R.string.service_host_default));
    	return String.format(context.getResources().getString(R.string.pull_address_format), host);
	}
	
	/**
	 * Gets the url of the remote repository for sending data
	 * @return The url of the remote repository
	 */
	public String getPushAddress() {
    	String host = prefs.getString(KEY_REMOTE_REPOSITORY, context.getResources().getString(R.string.service_host_default));
    	return String.format(context.getResources().getString(R.string.push_address_format), host);
	}	

	/**
	 * Gets the last time data was pushed to the remote repository
	 * @return Date/time as a long
	 */
	public long getMinPushDate() {
		return prefs.getLong(KEY_LAST_PUSH_DATE, System.currentTimeMillis());
	}

	/**
	 * Sets the last date/time data was pushed to the remote repository
	 */
	public void setMinPushDate() {
    	prefs.edit()
    		.putLong(KEY_LAST_PUSH_DATE, System.currentTimeMillis())
		.commit();
	}
	
	/**
	 * Returns the icon for viewing scores
	 * @return Drawable resource
	 */
	public Drawable getViewScoresIcon() {
		return context.getResources().getDrawable(R.drawable.view_scores);
	}
	
	/**
	 * Returns the icon for the score chart
	 * @return Drawable resource
	 */
	public Drawable getScoreChartIcon() {
		return context.getResources().getDrawable(R.drawable.chart);
	}
	
	/**
	 * Returns the icon for new scores
	 * @return Drawable resource
	 */
	public Drawable getNewScoreIcon() {
		return context.getResources().getDrawable(R.drawable.new_score);
	}
}
