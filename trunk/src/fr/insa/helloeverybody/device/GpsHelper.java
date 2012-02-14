package fr.insa.helloeverybody.device;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GpsHelper {

	private LocationListener mLocationListener;
	private LocationManager mLocationManager;
	
	private Location mCurrentLocation;
	private GpsHelperCallbackInterface mCallback;
	
	public GpsHelper(Context context, GpsHelperCallbackInterface callback) {
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mCallback = callback;
				
		// Define a listener that responds to location updates
		mLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				setLocation(location);
				mCallback.newLocationFound(location);
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
		// Notifications will occur every 10min or every 100m
		//TODO: pour les tests le temps et la distance entre chaque notification est de 0
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}
	
	public void stopListening() {
		// Remove the listener previously added
		mLocationManager.removeUpdates(mLocationListener);
	}
	
	public Location getLocation() {
		return mCurrentLocation;
	}

	public void setLocation(Location location) {
		this.mCurrentLocation = location;
	}
}
