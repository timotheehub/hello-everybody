package fr.insa.helloeverybody.helpers;

import android.util.Log;

// Classe capabable d'écrire dans le log
public class LogWriter {
	
	public static final Boolean DEBUG = true;
	
	// Ecrit un message dans le log si on est en mode debug
	public static void logIfDebug(String tag, String message) {
		if (DEBUG) {
			Log.d(tag, message);
		}
	}

}
