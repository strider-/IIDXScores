package cx.ath.strider.iidx;

import java.util.Timer;
import java.util.TimerTask;

import cx.ath.strider.iidx.base.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class SplashScreen extends BaseActivity {
	private final static int DELAY_IN_SECONDS = 5;
	
	private SplashDelayTask delay;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.splash);
    	    	
		delay = new SplashDelayTask();
		delay.start(DELAY_IN_SECONDS);
	}
	
	private void done() {		
		startActivity(new Intent(SplashScreen.this, Main.class));		
		finish();		
	}
	
	private class SplashDelayTask extends TimerTask {
		Timer t;
		boolean showSplash;
		
		public SplashDelayTask() {
			t = new Timer();
			showSplash = new Settings(getApplicationContext()).showSplashScreen();
		}
		
		public void start(int seconds) {
			RelativeLayout layout = getViewById(R.id.splash_layout);
			layout.setVisibility(showSplash ? View.VISIBLE : View.GONE);
			int milliseconds = showSplash ? 1000 : 0;
			t.schedule(this, milliseconds * seconds);
		}
		
		@Override
		public void run() {
			done();
			t.cancel();
		}		
	}
}
