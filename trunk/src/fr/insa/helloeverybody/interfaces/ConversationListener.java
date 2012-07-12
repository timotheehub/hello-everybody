package fr.insa.helloeverybody.interfaces;

import fr.insa.helloeverybody.models.ConversationMessage;

/* Interface pour être prévenu des événements relatifs aux conversations
-----------------------------------------------------------------------------*/
public interface ConversationListener {
	public void onCreationConversationFailed();
	public void onPendingConversationAdded(String roomName);
	public void onPublicConversationAdded(String roomName);
	public void onConversationRemoved(String roomName);
	public void onMemberJoined(String roomName, String jid);
	public void onMemberLeft(String roomName, String jid);
	public void onInvitationRejected(String roomName, String jid);
	public void onMessageReceived(String roomName, ConversationMessage newMessage);
}
