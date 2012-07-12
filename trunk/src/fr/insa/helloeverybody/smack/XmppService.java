package fr.insa.helloeverybody.smack;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/*
 *  Service pour les interactions avec le serveur XMPP
 */
public class XmppService extends Service {
	
	// Constantes
	public static final String TAG = "XmppService";
	
	// Binder du server
	private final IBinder mBinder = new LocalBinder();

	// Classe pour le binder
	public class LocalBinder extends Binder {
		public XmppService getService() {
            return XmppService.this;
        }
    }
	
	// Créer le server
	@Override
	public void onCreate () {
		super.onCreate();
		NetworkThread.createThread();
	}
	
	// Configure le service de façon à ce qu'il ne soit pas arrêter de façon non explicite
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
	    return START_STICKY;
	}
	
	// Détruit le service
	@Override
	public void onDestroy () {
		NetworkThread.removeThread();
		super.onDestroy();
	}

	// Retourne le binder du service
	@Override
	public IBinder onBind(Intent arg0) {		
		return mBinder;
	}
}
