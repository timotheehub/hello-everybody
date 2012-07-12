package fr.insa.helloeverybody.smack;

// Gère un unique thread pour la connection XMPP
public class NetworkThread {

	public static final String TAG = "NetworkThread";
	
	private static PipelineThread mNetworkThread;

	// Crée le thread réseau
	public static void createThread() {
		mNetworkThread = new PipelineThread();
		mNetworkThread.start();
	}
	
	// Supprimer le thread réseau 
	public static void removeThread() {
		mNetworkThread = null;
	}
	
	// Empile une action dans la file d'attente du thread réseau
	public static boolean enqueueRunnable(Runnable runnable) {
		// Vérifie que le thread n'est pas nul
		if (mNetworkThread == null) {
			return false;
		}
		
		// Empile l'action
		mNetworkThread.enqueueRunnable(runnable);
		return true;
	}
}
