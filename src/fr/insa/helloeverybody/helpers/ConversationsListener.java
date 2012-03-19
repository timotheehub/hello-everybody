package fr.insa.helloeverybody.helpers;

import java.util.EventListener;

import fr.insa.helloeverybody.models.ConversationMessage;

public interface ConversationsListener extends EventListener {
	public void creationConversationFailed();
	public void conversationAdded(String roomName);
	public void conversationPublicAdded(String roomName);
	public void conversationRemoved(String roomName);
	public void newMember(String roomName, String jid);
	public void memberQuit(String roomName, String jid);
	public void rejectedInvitation(String roomName, String jid);
	public void newMessage(String roomName, ConversationMessage newMessage);
}
