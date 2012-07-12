package fr.insa.helloeverybody.smack;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

// Classe capable d'exécuter des Handlers
public class PipelineThread extends Thread {
	
	public static final String TAG = "NetworkThread";
	
	private Handler mHandler;
	
	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler();
		Looper.loop();
	}
	
	public void enqueueRunnable(Runnable r) {
		try {
			mHandler.post(r);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	public void stopThread() {
		mHandler.getLooper().quit();
	}
}
