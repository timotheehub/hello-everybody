package fr.insa.helloeverybody.device;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GpsHelper {
	
	// Temps a partir duquel on prend en compte la nouvelle position, en millisecondes
	private static final int TIME_LIMIT = 1000 * 60 * 1;
	// Distance a partir duquel on prend en compte la nouvelle position, en metres
	private static final int DISTANCE_LIMIT = 100;
	
	private boolean mUpdateUrgent;

	private LocationListener mLocationListener;
	private LocationManager mLocationManager;
	
	private Location mCurrentLocation;
	private Location mLastLocationUpdated;
	private GpsHelperCallbackInterface mCallback;
	
	
	public GpsHelper(Context context, GpsHelperCallbackInterface callback) {
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mCallback = callback;
				
		// Define a listener that responds to location updates
		mLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				
				if(mUpdateUrgent) {
					setLastLocationUpdated(location);
					mCallback.newLocationFound(location);
				}
				
				if(isLocationSignificantlyChanged(location, mLastLocationUpdated)) {
					setLastLocationUpdated(location);
					mCallback.newLocationFound(location);
				}
				
				setLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}
				    
			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
			
		};
				
		mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(mCurrentLocation == null)
			mCurrentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	public void startListening() {
		// Register the listener with the Location Manager to receive location updates
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}
	
	public void stopListening() {
		// Remove the listener previously added
		mLocationManager.removeUpdates(mLocationListener);
	}
	
	public void startListeningNoWait() {
		mUpdateUrgent = true;
		startListening();
	}
	
	public void stopListeningNoWait() {
		mUpdateUrgent = false;
		stopListening();
	}
	
	
	/** Determines if the location has significantly changed to need an update
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	private boolean isLocationSignificantlyChanged(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TIME_LIMIT;
	    boolean isSignificantlyOlder = timeDelta < -TIME_LIMIT;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than time_limit since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than time_limit older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check the distance between these two locations
	    boolean isSignificantlyDistant = location.distanceTo(currentBestLocation) >= DISTANCE_LIMIT;
	    
	    // If the user has moved more than distance_limit, use the new location
	    if(isNewer && isSignificantlyDistant) {
	    	return true;
	    }
	    
	    // Check whether the new location fix is more accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isMoreAccurate = accuracyDelta < 0;
	    
	    // If the new location is more accurate, use it
	    if (isMoreAccurate) {
	        return true;
	    }

	    return false;
	}
	
	
	private void setLastLocationUpdated(Location location) {
		this.mLastLocationUpdated = new Location(location);
	}
	
	
	public Location getLocation() {
		return mCurrentLocation;
	}

	public void setLocation(Location location) {
		this.mCurrentLocation = new Location(location);
	}
	
}
