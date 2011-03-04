package cx.ath.strider.iidx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreAdapter extends BaseAdapter {
	private Context mContext; 
	private ScoreDetail[] scores;
	
	public ScoreAdapter(Context context, ScoreDetail[] scores) {
		this.mContext = context;
		this.scores = scores;
	}
	
	@Override
	public int getCount() {
		return scores.length;
	}

	@Override
	public Object getItem(int position) {
		return scores[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}	

	@Override  // Making the items non-clickable
	public boolean isEnabled(int position) {
		return false; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null) {
			LayoutInflater li = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.score_layout_item, parent, false);
		}
		ScoreDetail sd = scores[position];
		if(sd != null) {
			ImageView grade = (ImageView)v.findViewById(R.id.gradeIcon);
			TextView ex = (TextView)v.findViewById(R.id.tvEXScore);
			TextView ac = (TextView)v.findViewById(R.id.tvAccuracy);
			TextView dt = (TextView)v.findViewById(R.id.tvStamp);				
			
			if(grade != null)
				grade.setImageResource(sd.getGradeIconResourceId());
			if(ex != null)
				ex.setText(String.format("%d", sd.EXScore));
			if(ac != null)
				ac.setText(String.format("%2.1f%%", sd.getAccuracy()));
			if(dt != null)
				dt.setText(sd.Stamp.toLocaleString());
		}
		return v;
	}
}
