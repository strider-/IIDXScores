package cx.ath.strider.iidx.adapter;

import java.util.ArrayList;

import cx.ath.strider.iidx.R;
import cx.ath.strider.iidx.model.SongQuery;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// I'd LOVE to have a character preview with the fast scrolling, but there are some serious
// bugs with getting the ListView to properly refresh the section indexes.
// see: http://code.google.com/p/android/issues/detail?id=9054

public class SongQueryAdapter extends ArrayAdapter<SongQuery> { //implements SectionIndexer {
	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = 2;
	
	private String sort;
	/*
	private HashMap<String, Integer> indexer;
	private String[] sections;
	*/
    public SongQueryAdapter(Context context, int textViewResourceId, ArrayList<SongQuery> items, String sort) {
            super(context, textViewResourceId, items);
            this.sort = sort;
            /*
            getIndexes(sort.startsWith("title"));
            */
    }
    
    public int getPosition(SongQuery song) {
    	for(int i=0; i < getCount(); i++) {
    		if(getItem(i) != null)
	    		if(song.Title.equals(getItem(i).Title)) {
	    			return i;
	    		}
    	}
    	return 0;
    }
    /*
    private void getIndexes(boolean sortByTitle) {
    	indexer = new HashMap<String, Integer>();    	
    	if(sortByTitle) {
	    	for(int i = this.getCount()-1; i>=0; i--) {
	    		indexer.put(getItem(i).Title.substring(0, 1).toUpperCase(), i);
	    	}
    	} else {
	    	for(int i = this.getCount()-1; i>=0; i--) {
	    		indexer.put(String.format("%d\u2605", getItem(i).Difficulty), i);
	    	}
    	}
    	
    	sections = new String[indexer.size()];
    	indexer.keySet().toArray(sections);
    	Arrays.sort(sections);
    }
    */
    @Override
    public int getItemViewType(int position) {
        return this.getItem(position) == null ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }
    
    @Override
    public boolean isEnabled(int position) {
    	return this.getItem(position) != null;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	int type = getItemViewType(position);
    	SongQuery song = getItem(position);
    	View v = convertView;            	
    	
    	if (v == null) {
    		LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		if(type == TYPE_ITEM)
    			v = vi.inflate(R.layout.songlist_layout_item, null);
    		else
    			v = vi.inflate(R.layout.songlist_separator_item, null);
        }
        
        if (type == TYPE_ITEM) {
			TextView tvTitle = (TextView) v.findViewById(R.id.txtTitle);
			TextView tvInfo = (TextView) v.findViewById(R.id.txtInfo);
			tvTitle.setTextColor(song.Mode.ModeColor);
			tvTitle.setText(song.Title);			
			tvInfo.setText(String.format("%d\u2605 - %d Notes, %s BPM", song.Difficulty, song.TotalNotes, song.BPM));
        } else  {
        	((TextView)v.findViewById(R.id.txtSection)).setText(getSectionText(position + 1));
        }
        
        return v;
    }
    
    private String getSectionText(int position) {
    	SongQuery song = getItem(position);
    	if(song != null) {
    		if(sort.equals("title")) {
	    		char t = song.Title.charAt(0);
	    		if(!Character.isLetterOrDigit(t) || Character.isDigit(t))
	    			return "0-9";
	    		else if(t > 122)
	    			return "OTHER";
	    		else
	    			return String.valueOf(Character.toUpperCase(t));
    		} else 
    			return String.format("%d\u2605", song.Difficulty);
    	}
    	return null;
    }
    /*
	@Override
	public int getPositionForSection(int section) {
		return indexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		Log.i(Settings.DEBUG_TAG, "Got Sections");
		return sections;
	}
	*/
}
