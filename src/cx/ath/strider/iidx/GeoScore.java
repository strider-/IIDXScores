package cx.ath.strider.iidx;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GeoScore implements LocationListener {
	private LocationManager lm;
    private Location loc;
    private Context context;
    private boolean on;
    private Runnable onGotFix;
    private Address address;
	
    public GeoScore(Context context) {
    	this.context = context;
    	onGotFix = null;
    }
    
    public void onInitialFix(Runnable method) {
		onGotFix=method;
    }
    
    public void Enable() {
    	if(!on) {
	    	lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 100f, this);
	    	on = true;
    	}
    }
    
    public void Disable() {
    	if(on) {
	    	lm.removeUpdates(this);
	    	lm = null;
	    	loc = null;
	    	on = false;
    	}
    }
    public void SetState(boolean enable) {
    	if(enable)
    		Enable();
    	else
    		Disable();
    }
    
	@Override
	public void onLocationChanged(Location arg0) {
		boolean init = loc == null;
		loc = arg0;
		double lat = arg0.getLatitude();
		double lon = arg0.getLongitude();
			
		Log.i("GPS", String.valueOf(lat) + ", " + String.valueOf(lon));
				
		try {
			Geocoder g = new Geocoder(context);
			List<Address> addresses = g.getFromLocation(lat, lon, 1);
			if(addresses.size() > 0)
				address = addresses.get(0);
			else
				address = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		if(init) {
			onGotFix.run();
			init=false;
		}
	}	

	@Override
	public void onProviderDisabled(String provider) {
		loc = null;
		Log.i("GPS", "Disabled [" + provider + "]");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("GPS", "Enabled [" + provider + "]");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("GPS", "Status changed to " + provider + " [" + String.valueOf(status) + "]");
	}
	
	public boolean hasLocation() { return loc != null; }
	public double getLatitude() { return loc == null ? -1 : loc.getLatitude(); }
	public double getLongitude() { return loc == null ? -1 : loc.getLongitude(); }
	public boolean isEnabled() { return on; }
	public Location getLocation() { return loc; }
	public void setLocation(Location location) { loc = location; } 
	public Address getAddress() { return address; }
}
