package fr.insa.helloeverybody.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import fr.insa.helloeverybody.R;
import fr.insa.helloeverybody.conversations.ConversationActivity;
import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.viewmodels.ConversationList;

public class NotificationCenter implements ConversationListener {

	// Singleton
	private static NotificationCenter instance = null;
	
	// Constantes
	private static final int NOTIFY_NEW_MESSAGE = 1;
	private static final int NOTIFICATION_ICON = R.drawable.ic_launcher;
	
	// Variables
	private NotificationManager notificationManager;
	private Context context;
	
	// Retourne le singleton de maniere protegee
	public static synchronized NotificationCenter getInstance() {
		if (instance == null) {
			instance = new NotificationCenter();
		}
		return instance;
	}
	
	// Initialise le centre de notification
	public void initNotificationCenter(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) 
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		ConversationList.getInstance()
				.addConversationListener(this);
	}
	
	// Désabonne le listener de conversations
	public void destroyNotificationCenter() {
		ConversationList.getInstance()
				.removeConversationListener(this);
	}
	
	// Envoye une notification liée à une conversation
	private void sendConversationNotification(int type, 
			String tickerText, String title, String text, String roomName) {
		
		// Une activité de conversation sera créé en cliquant sur la notification
		Intent notificationIntent = new Intent(context, ConversationActivity.class);
		notificationIntent.putExtra(ConversationActivity.ROOM_NAME_EXTRA, roomName);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, 
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Envoie une notification
		Notification notification = new Notification(NOTIFICATION_ICON,
						tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(context, title, text, contentIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(type, notification);
	}
	
	
	/* Implémentation de l'interface des listeners de conversations
	-------------------------------------------------------------------------*/
	public void onConversationCreationFailed(String roomName) { }

	public void onConversationCreationSucceeded(String roomName) { }

	public void onPublicConversationAdded(String roomName) { }

	public void onConversationRemoved(String roomName) { }

	public void onMemberJoined(String roomName, String jid) { }

	public void onMemberLeft(String roomName, String jid) { }

	public void onInvitationRejected(String roomName, String jid) { }

	// Envoyer une notification de nouveau message
	public void onMessageReceived(String roomName, ConversationMessage newMessage) 
	{
		if (ConversationActivity.getVisbleRoomName() == null) {
			sendConversationNotification(NOTIFY_NEW_MESSAGE, "Nouveaux messages", 
				"Hello Everybody", "Vous avez de nouveaux messages", roomName);
		}
	}

}
