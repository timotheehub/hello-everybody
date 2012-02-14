package fr.insa.helloeverybody.device;

import android.location.Location;

public interface GpsHelperCallbackInterface {
	public void newLocationFound(Location loc);
}
