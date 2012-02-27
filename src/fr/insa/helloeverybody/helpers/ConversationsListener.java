package fr.insa.helloeverybody.helpers;

import java.util.EventListener;

import fr.insa.helloeverybody.models.ConversationMessage;

public interface ConversationsListener extends EventListener {
	public void conversationAdded(long idConversation);
	public void conversationRemoved(long idConversation);
	public void conversationChanged(long idConversation);
	public void newMessage(long idConversation, ConversationMessage newMessage);
}
