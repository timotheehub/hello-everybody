package fr.insa.helloeverybody.device;

import java.util.TimerTask;

import android.os.HandlerThread;


public class GpsTimerTaskStartListening extends TimerTask {
	
	private GpsHelper mGpsHelper;
	private HandlerThread mHandlerThread;
	
	
	public GpsTimerTaskStartListening (GpsHelper gpsHelper) {
		mGpsHelper = gpsHelper;
		mHandlerThread = null;
	}


	@Override
	public void run() {
		mHandlerThread = new HandlerThread("GpsListening") {

			@Override
			protected void onLooperPrepared() {
				mGpsHelper.startListening();
				super.onLooperPrepared();
			}
			
		};
		
		mHandlerThread.start();
	}
	
	
	@Override
	public boolean cancel() {
		mGpsHelper.stopListening();
		quit();
		return super.cancel();
	}
	

	public void quit() {
		if(mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}
	}
	
}
