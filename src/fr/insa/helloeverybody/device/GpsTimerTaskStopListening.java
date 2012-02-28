package fr.insa.helloeverybody.device;

import java.util.TimerTask;

import android.util.Log;


public class GpsTimerTaskStopListening extends TimerTask {
	
	private GpsHelper mGpsHelper;
	private GpsTimerTaskStartListening mGpsTimerTask;
	
	
	public GpsTimerTaskStopListening (GpsHelper gpsHelper,
			GpsTimerTaskStartListening gpsTimerTask) {
		mGpsHelper = gpsHelper;
		mGpsTimerTask = gpsTimerTask;
	}
	
	
	@Override
	public void run() {
		mGpsTimerTask.quit();
		mGpsHelper.stopListening();
		Log.v("GPS Timer Task", "stop listening");
	}

}
