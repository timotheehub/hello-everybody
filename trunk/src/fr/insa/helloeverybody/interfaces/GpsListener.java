package fr.insa.helloeverybody.interfaces;

import android.location.Location;

/* Interface pour être prévenu des changement de location GPS
-----------------------------------------------------------------------------*/
public interface GpsListener {
	public void onLocationUpdated(Location newLocation);
}
