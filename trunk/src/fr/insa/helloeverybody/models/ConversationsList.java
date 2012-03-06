package fr.insa.helloeverybody.models;

import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.smack.ChatService;

public class ConversationsList {

	// Singleton
	private static ConversationsList instance = null;
	
	// Attributes
	private Map<String,Conversation> publicConversations;
	private Map<String,Conversation> pendingConversations;
	
	private List<EventListener> listeners;
	private ChatService mChatService;
	
	// Constructeur privee
	private ConversationsList() {
		publicConversations = Collections.synchronizedMap(new HashMap<String,Conversation>());
		pendingConversations = Collections.synchronizedMap(new HashMap<String,Conversation>()); 
		listeners = Collections.synchronizedList(new LinkedList<EventListener>());
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ConversationsList getInstance() {
		if (instance == null) {
			instance = new ConversationsList();
		}
		return instance;
	}
	
	/** GESTION DES EVENEMENTS DE MODIFICATION DU MODELE */
	// Ajoute une conversation lancée
	public void addPendingConversation(boolean isPublic, String jidProfile, String title) {
		Conversation newPendingConversation = new Conversation(false, jidProfile, title);
		pendingConversations.put(jidProfile,newPendingConversation);
		fireNewConversation(newPendingConversation.getRoomName());
		
	}
	
	// Ajoute une conversation publique
	public void addPublicConversation(String roomName, List<String> idsProfile, String title) {
		Conversation newPublicConversation = new Conversation(true, roomName, title);
		publicConversations.put(roomName,newPublicConversation);
		fireNewConversation(newPublicConversation.getRoomName());
	}
	
	// Ajoute un message dans une conversation
	public void addReceivedMessage(String roomName, String jidProfile, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		Profile profile = ContactsList.getInstance().getProfileByJid(jidProfile);
		newMessage.setContact(profile);
		getConversationById(roomName).addMessage(newMessage);
		fireNewMessage(roomName,newMessage);
	}
	
	public void addSendMessage(String roomName, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(UserProfile.getInstance().getProfile());
		getConversationById(roomName).addMessage(newMessage);
		fireNewMessage(roomName,newMessage);
	}
	
	// Retourne la liste des conversation publiques
	public Map<String,Conversation> getPublicList() {
		return publicConversations;
	}
	
	// Retourne la liste des conversations en cours
	public Map<String,Conversation> getPendingList() {
		return pendingConversations;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Conversation getConversationById(String roomName) {
		for (Entry<String,Conversation> conversation : publicConversations.entrySet()) {
			if (conversation.getKey().equals(roomName)) {
				return conversation.getValue();
			}
		}
		
		for (Entry<String,Conversation> conversation : pendingConversations.entrySet()) {
			if (conversation.getKey().equals(roomName)) {
				return conversation.getValue();
			}
		}
		
		return null;
	}
	
	public int getUnreadConversationscount(){
		int unreadCount=0;
		for (Entry<String,Conversation> conv : publicConversations.entrySet()){
			if(conv.getValue().getNbUnreadMessages()>0)
				unreadCount++;
		}
		return unreadCount;
	}
	
	public void addConversationsListener(ConversationsListener listener) {
		listeners.add(listener);
	}
	
	public void removeConversationsListener(ConversationsListener listener) {
		listeners.remove(listener);
	}
	
	public void fireNewMessage(String roomName, ConversationMessage newMessage) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).newMessage(roomName, newMessage);
		}
	}

	public void fireNewConversation(String roomName) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).conversationAdded(roomName);
		}
	}

	/** GESTION DES EVENEMENTS PROVENANT DU CHAT */
	public void connectChat(ChatService mChatService) {
		this.mChatService = mChatService;
	}
	
	/** GESTION DES EVENEMENTS PROVENANT DU CHAT */
	public void disconnectChat(ChatService mChatService) {
		this.mChatService = null;
	}
}
