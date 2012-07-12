package fr.insa.helloeverybody.device;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import fr.insa.helloeverybody.interfaces.GpsListener;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;

/* Classe capable de donner la position GPS
 * TODO(performance): Implémenter un minimum d'intervalle pour les téléchargements
 * TODO(architecture): Utiliser les timers de façon à ce qu'il y ait des mises à jours régulières
-----------------------------------------------------------------------------*/
public class GpsHelper {
	
	// Singleton
	private static GpsHelper instance = null;
	
	// Taux à partir duquel la précision est considéré comme meilleure
	private static final float ACCURACY_RATE = 0.9f;
	
	// Précision en mètres par défaut si aucune précision n'est trouvée
	private static final float DEFAULT_ACCURACY = 200.0f;
	
	// Temps pour le timer du GPS, en millisecondes
	private final long EXECUTION_TIME = 1000 * 60;
	private final long TIME_BETWEEN_TWO_EXECUTIONS = 1000 * 60 * 5;
	
	// Variable
	private LocationListener mLocationListener;
	private LocationManager mLocationManager;
	private Location mCurrentLocation;
	private ArrayList<GpsListener> mListeners;
	private Timer mStartTimer;
	private Timer mStopTimer;
	
	// Constructeur privé
	private GpsHelper() {
		mListeners = new ArrayList<GpsListener>();
		mLocationListener = new LocationChangeListener();
	}
	
	// Retourne le singleton
	public static synchronized GpsHelper getInstance() {
		if (instance == null) {
			instance = new GpsHelper();
		}
		return instance;
	}
	
	// Fixe de la valeur de la position GPS
	public void initLocationManager(Context context) {
		mLocationManager = (LocationManager) 
				context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	// Ajoute un listener
	public void addListener(GpsListener listener) {
		
		// Ajouter le listener
		mListeners.add(listener);
		
		// Demander de mettre le GPS à jour si c'est le premier listener
		if (mListeners.size() == 1) {
			mCurrentLocation = null;
			mStartTimer = new Timer(true);
			mStopTimer = new Timer(true);
			StartUpdateGpsTimerTask startGpsTask =
					new StartUpdateGpsTimerTask();
			StopUpdateGpsTimerTask stopGpsTask =
					new StopUpdateGpsTimerTask(startGpsTask);
			mStartTimer.schedule(startGpsTask, 0,
					EXECUTION_TIME + TIME_BETWEEN_TWO_EXECUTIONS);
			mStopTimer.schedule(stopGpsTask, EXECUTION_TIME,
					EXECUTION_TIME + TIME_BETWEEN_TWO_EXECUTIONS);
		}
	}
	
	// Supprime un listener
	public void removeListener(GpsListener listener) {
		
		// Supprimer le listener
		mListeners.remove(listener);
		
		// Arrêter les mises à jour de GPS s'il n'y a plus de listener
		if ((mListeners.size() == 0)
				&& (mStartTimer != null) && (mStopTimer != null)) { 
			mStartTimer.cancel();
			mStopTimer.cancel();
			mStartTimer = null;
			mStopTimer = null;
		}
	}
	
	// Détermine si la position de l'utilisateur 
	// a beaucoup changé depuis la dernière mise à jour  
	private boolean isLocationSignificantlyChanged(
				Location oldLocation, Location newLocation) {

        // Retourne vrai si on n'a pas de position GPS
	    if (oldLocation == null) {
	        return true;
	    }

	    // Vérifie que la nouvelle position est plus récente
	    if (newLocation.getTime() < oldLocation.getTime()) {
	    	return false;
	    }
	    
	    // Calcule les précisions des position
	    float newLocationAccuracy = (newLocation.getAccuracy() == 0.0f) ?
	    		DEFAULT_ACCURACY : newLocation.getAccuracy();
	    float oldLocationAccuracy = (oldLocation.getAccuracy() == 0.0f) ?
	    		DEFAULT_ACCURACY : oldLocation.getAccuracy();
	    
	    // Retourne vrai si la différence de distance est suffisamment grande
	    if (newLocation.distanceTo(oldLocation) >= 
	    		newLocationAccuracy + oldLocationAccuracy) {
	    	return true;
	    }
	    
	    // Retourne vrai si la nouvelle distance est de meilleure qualité
	    return (newLocationAccuracy < 
	    			oldLocationAccuracy * ACCURACY_RATE);
	}
	
	// Retourne la position actuelle
	public Location getLocation() {
		return mCurrentLocation;
	}

	// Fixe la valeur de la position actuelle
	public void setLocation(Location location) {
		this.mCurrentLocation = new Location(location);
	}
	
	// Commence les mises à jour GPS  
	private void startGpsUpdates() {
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}
	
	// Arrête les mises à jour GPS
	private void stopGpsUpdates() {
		mLocationManager.removeUpdates(mLocationListener);
	}
	
	
	/* Classe qui gère les changements de localisation GPS
	-------------------------------------------------------------------------*/
	private class LocationChangeListener implements LocationListener {
		
		// Met à jour la localisation GPS
		public void onLocationChanged(Location location) {
			if (isLocationSignificantlyChanged(mCurrentLocation, location)) {
				setLocation(location);
				for (GpsListener listener : mListeners) {
					listener.onLocationUpdated(location);
				}
			}
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}
			    
		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
		
	}
	
	
	/* Classe pour mettre à jour la position GPS périodiquement
	-------------------------------------------------------------------------*/
	private class StartUpdateGpsTimerTask extends TimerTask {

        private HandlerThread mHandlerThread;
        
        @Override
        public void run() {
            mHandlerThread = new HandlerThread("GpsListening") {

                @Override
                protected void onLooperPrepared() {
                	startGpsUpdates();
                    super.onLooperPrepared();
                }
            };
            
            mHandlerThread.start();
        }
        
        @Override
        public boolean cancel() {
            return super.cancel();
        }
		
        public void quit() {
        	stopGpsUpdates();
            if (mHandlerThread != null) {
                mHandlerThread.quit();
                mHandlerThread = null;
            }
        }
	}
	
	
	/* Classe pour stopper la mise à jour de la position GPS périodiquement
	-------------------------------------------------------------------------*/
	private class StopUpdateGpsTimerTask extends TimerTask {
		
		private StartUpdateGpsTimerTask mStartGpsTask;
                
        public StopUpdateGpsTimerTask(StartUpdateGpsTimerTask startGpsTask) {
        	mStartGpsTask = startGpsTask;
        }
                
        @Override
        public void run() {
        	mStartGpsTask.quit();
        }
	}
}
