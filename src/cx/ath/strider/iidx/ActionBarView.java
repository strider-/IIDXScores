package cx.ath.strider.iidx;

import cx.ath.strider.iidx.model.Mode;
import cx.ath.strider.iidx.model.Style;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

public class ActionBarView extends RelativeLayout {
	private Button btnABStyle, btnABMode, btnABSort;
	private EditText txtABSearch;
	private ImageView sepOne, sepTwo;
	private ListAdapter styleAdapter, modeAdapter;
	private Style currentStyle;
	private Mode currentMode;
	private Runnable onChange, onSearchChange, onCancelSearchMode;
	private SharedPreferences prefs;
	private boolean searchMode;
	private String currentSearch;
	private Animation slide;
	private Animation.AnimationListener listener;
	private InputMethodManager kbd;
	
	public ActionBarView(Context context) {		
		this(context, null);
	}
	public ActionBarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}	
	public ActionBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(!this.isInEditMode()) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.actionbar, this);
			
			kbd = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			listener = new Animation.AnimationListener() {	
				@Override
				public void onAnimationRepeat(Animation animation) {
					int color = searchMode ? 0xFFFFFFCC : (currentMode.ModeColor & 0xfefefefe) >> 1;
					int viewState = searchMode ? View.GONE : View.VISIBLE;
					
					btnABStyle.setVisibility(viewState);
					btnABMode.setVisibility(viewState);
					btnABSort.setVisibility(viewState);
					sepOne.setVisibility(viewState);
					sepTwo.setVisibility(viewState);
					
					if(searchMode) {
						txtABSearch.setVisibility(View.VISIBLE);									
						showSoftKeyboard();			
					} else {
						txtABSearch.setVisibility(View.GONE);
						txtABSearch.setText(null);				
					}
					
					currentSearch = null;
					ActionBarView.this.setBackgroundColor(color);					
				}
				@Override
				public void onAnimationStart(Animation animation) { }
				@Override
				public void onAnimationEnd(Animation animation) { }
			};		
			slide = AnimationUtils.loadAnimation(context, R.anim.actionbar_slide);
			slide.setAnimationListener(listener);
		
			btnABStyle = (Button)findViewById(R.id.btnABStyle);
			btnABMode = (Button)findViewById(R.id.btnABMode);
			btnABSort = (Button)findViewById(R.id.btnABSort);		
					
			sepOne = (ImageView)findViewById(R.id.borderone);
			sepTwo = (ImageView)findViewById(R.id.bordertwo);
			
			btnABStyle.setOnClickListener(onStyleClick);
			btnABMode.setOnClickListener(onModeClick);
			btnABSort.setOnClickListener(onSortClick);
			
			txtABSearch = (EditText)findViewById(R.id.txtABSearch);
			txtABSearch.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) { }
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					currentSearch = s.toString();					
					onSearchChange.run();
				}
			});
			
			setSortText();
		} else {
			this.setBackgroundColor(0xFFAABBCC);
		}
	}
	
	private OnClickListener onStyleClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(styleAdapter != null) {
		    	AlertDialog.Builder d = new AlertDialog.Builder(ActionBarView.this.getContext());		    	
		    	d.setTitle("Select Style");    	
		    	d.setAdapter(styleAdapter, new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface di, int index) {
		    			setStyle((Style)styleAdapter.getItem(index));
		    		}
		    	});
		    	d.show();
			}
		}		
	};	
	private OnClickListener onModeClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(modeAdapter != null) {
				QuickAction qa = new QuickAction(v);
				
				for(int i=0; i<modeAdapter.getCount(); i++) {
					final Mode m = (Mode)modeAdapter.getItem(i);
					ActionItem item = new ActionItem();
					item.setIcon(IIDX.model.getModeIcon(m));
					item.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							setMode(m);
						}
					});
					qa.addActionItem(item);
				}
				
				qa.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
				qa.show();					
			}
		}		
	};
	private OnClickListener onSortClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// the preference has a listener set up in Main for preference changes, no need to call onChange
			if(getSort().equals("title")) {
				prefs.edit().putString("sorting", "difficulty,title").commit();
			} else {
				prefs.edit().putString("sorting", "title").commit();
			}
			
			setSortText();
		}
	};
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK && searchMode) {
			boolean hidKbd = kbd.hideSoftInputFromWindow(getWindowToken(), 0);
			if(!hasSearchTerm() || !hidKbd) {
				hideSearch();				
			}
			return !hidKbd;
		}
		return super.dispatchKeyEventPreIme(event);
	}
	public void showSearch() {
		if(!searchMode) {
			searchMode = true;						
			this.startAnimation(slide);
		} else
			showSoftKeyboard();
	}
	public void hideSearch() {
		if(searchMode) {
			searchMode = false;
			this.startAnimation(slide);
			onCancelSearchMode.run();
		}
	}
	public boolean hasSearchTerm() {
		if(!searchMode)
			return false;
		return currentSearch != null && currentSearch.trim().length() > 0;
	}
	private void showSoftKeyboard() {
		txtABSearch.requestFocus();		
		kbd.showSoftInput(txtABSearch, 0);
	}
	private void setSortText() {
		btnABSort.setText(getSort().equals("title") ? "a-z" : "\u2605");
	}
	private String getSort() {		
		return prefs.getString("sorting", getContext().getResources().getString(R.string.sorting_default));
	}
	public void setOnChange(Runnable r) {
		onChange = r;
	}
	public void setOnSearchChange(Runnable r) {
		onSearchChange = r;
	}
	public void setOnCancelSearchMode(Runnable r) {
		onCancelSearchMode = r;
	}
	public void setStyleAdapter(ListAdapter adapter) {
		styleAdapter = adapter;
		setStyle((Style)styleAdapter.getItem(0));
	}
	public void setModeAdapter(ListAdapter adapter) {
		modeAdapter = adapter;
		setMode((Mode)modeAdapter.getItem(0));
	}
	public Style getStyle() { return currentStyle; }
	public void setStyle(Style style) { 
		if(style != null) {
			currentStyle = style;
			btnABStyle.setText(currentStyle.toString());
		}
		if(onChange != null)
			onChange.run();
	}
	public boolean inSearchMode() { return searchMode; }
	public String getSearchTerm() { return currentSearch; }
	public Mode getMode()   { return currentMode;  }
	public void setMode(Mode mode) { 
		if(mode != null) {
			currentMode = mode;
			this.setBackgroundColor((currentMode.ModeColor & 0xfefefefe) >> 1);
			sepOne.setImageResource(currentMode.ModeColorResource);
			sepTwo.setImageResource(currentMode.ModeColorResource);
			btnABMode.setText(currentMode.Abbr);
		}
		if(onChange != null)
			onChange.run();
	}
}
