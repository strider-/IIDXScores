package cx.ath.strider.iidx;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModeAdapter extends ArrayAdapter<Mode> {
    public ModeAdapter(Context context, int textViewResourceId, ArrayList<Mode> items) {
        super(context, textViewResourceId, items);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	TextView v = (TextView)super.getView(position, convertView, parent);
    	v.setText(getItem(position).ModeName);
    	v.setTextColor(getItem(position).ModeColor);
    	return v;
    }
}
