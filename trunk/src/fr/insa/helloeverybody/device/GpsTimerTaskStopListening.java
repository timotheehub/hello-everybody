package fr.insa.helloeverybody.device;

import java.util.TimerTask;


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
	}

}
