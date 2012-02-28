package fr.insa.helloeverybody.device;

import java.util.TimerTask;

import android.os.HandlerThread;
import android.util.Log;


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
				Log.v("GPS Timer Task", "listening");
				super.onLooperPrepared();
			}
			
		};
		
		mHandlerThread.start();
	}
	

	public void quit() {
		if(mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}
	}
	
}
