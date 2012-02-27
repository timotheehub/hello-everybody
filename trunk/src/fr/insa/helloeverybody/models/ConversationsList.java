package fr.insa.helloeverybody.models;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;

import fr.insa.helloeverybody.helpers.ConversationsListener;

public class ConversationsList {

	// Singleton
	private static ConversationsList instance = null;
	
	// Attributes
	private List<Conversation> publicConversations;
	private List<Conversation> privateConversations;
	private List<Conversation> pendingConversations;
	
	private List<EventListener> listeners;
	
	// Constructeur privee
	private ConversationsList() {
		publicConversations = Collections.synchronizedList(new LinkedList<Conversation>());
		privateConversations = Collections.synchronizedList(new LinkedList<Conversation>()); 
		pendingConversations = Collections.synchronizedList(new LinkedList<Conversation>()); 
		listeners = Collections.synchronizedList(new LinkedList<EventListener>()); 
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ConversationsList getInstance() {
		if (instance == null) {
			instance = new ConversationsList();
		}
		return instance;
	}
	
	// TODO : Ajouter une conversation privée lancée 
	public void addLaunchedPrivateConversation() {
		
	}
	
	// TODO : Ajouter une conversation publique lancée
	public void addLaunchedPublicConversation() {
		
	}
	
	// TODO : Ajouter une conversation publique
	public void addPublicConversation() {
	
	}
	
	// Ajoute un message dans une conversation
	public void addConversationMessage(long idConversation, long idProfile, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(ContactsList.getInstance().getProfileById(idProfile));
		getConversationById(idConversation).addMessage(newMessage);
	}
	
	// Retourne la liste des conversation publiques
	public List<Conversation> getPublicList() {
		return publicConversations;
	}
	
	// Retourne la liste des conversations privées
	public List<Conversation> getPrivateList() {
		return privateConversations;
	}
	
	// Retourne la liste des conversations en cours
	public List<Conversation> getOpenList() {
		return pendingConversations;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Conversation getConversationById(Long id) {
		for (Conversation conversation : publicConversations) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		for (Conversation conversation : privateConversations) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		for (Conversation conversation : pendingConversations) {
			if (conversation.getId().equals(id)) {
				return conversation;
			}
		}
		
		return null;
	}
	
	public void addConversationsListener(ConversationsListener listener) {
		listeners.add(listener);
	}
	
	public void removeConversationsListener(ConversationsListener listener) {
		listeners.remove(listener);
	}
	
	public void fireNewMessage(long idConversation, ConversationMessage newMessage) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).newMessage(idConversation, newMessage);
		}
	}
}
