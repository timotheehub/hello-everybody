package fr.insa.helloeverybody.helpers;

import java.util.EventListener;

import fr.insa.helloeverybody.models.ConversationMessage;

public interface ConversationsListener extends EventListener {
	public void conversationAdded(String roomName);
	public void conversationRemoved(String roomName);
	public void conversationChanged(String roomName);
	public void newMessage(String roomName, ConversationMessage newMessage);
}
