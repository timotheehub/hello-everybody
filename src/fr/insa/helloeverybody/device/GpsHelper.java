package fr.insa.helloeverybody.device;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GpsHelper {

	private LocationListener locationListener;
	private LocationManager locationManager;
	
	private Location location;
	
	
	public GpsHelper(Context context) {
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network or GPS location provider.
				setLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {}
				    
			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
			
		};
				
		location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(location == null)
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		startListening();
	}
	
	public void startListening() {
		// Register the listener with the Location Manager to receive location updates
		// Notifications will occur every 10min or every 100m
		//TODO: pour les tests le temps et la distance entre chaque notification est de 0
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	public void stopListening() {
		// Remove the listener previously added
		locationManager.removeUpdates(locationListener);
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}
