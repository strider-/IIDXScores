package cx.ath.strider.iidx.base;

import android.app.Activity;
import android.view.View;

public abstract class BaseActivity extends Activity {
	
	/**
	 * Generic method of findViewById
	 * @param <T> Type to return, must be a subclass of View.
	 * @param id
	 * @return The view if found, null otherwise.
	 */
	@SuppressWarnings("unchecked")	
	protected <T extends View> T getViewById(int id) {
		return (T)findViewById(id);
	}
}
