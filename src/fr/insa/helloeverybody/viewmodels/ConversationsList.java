package fr.insa.helloeverybody.viewmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.Profile;

/* Classe qui contient la liste des conversations publiques et en cours
-----------------------------------------------------------------------------*/
public class ConversationsList {

	// Constantes
	public static final String TAG = "ConversationsList";
		
	// Singleton
	private static ConversationsList instance = null;
	
	// Attributs
	private Map<String,Conversation> publicConversationMap;
	private Map<String,Conversation> pendingConversationMap;
	private List<ConversationListener> listenerList;
	
	// Constructeur privé
	private ConversationsList() {
		publicConversationMap = Collections.synchronizedMap(new HashMap<String,Conversation>());
		pendingConversationMap = Collections.synchronizedMap(new HashMap<String,Conversation>()); 
		listenerList = Collections.synchronizedList(new LinkedList<ConversationListener>());
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ConversationsList getInstance() {
		if (instance == null) {
			instance = new ConversationsList();
		}
		return instance;
	}
	
	
	/* Gestion des conversations et des messages
	-------------------------------------------------------------------------*/
	// Retourne la liste des conversation publiques
	public List<Conversation> getPublicRoomList() {
		return new ArrayList<Conversation>(publicConversationMap.values());
	}
	
	// Retourne la liste des conversations en cours
	public List<Conversation> getPendingRoomList() {
		return new ArrayList<Conversation>(pendingConversationMap.values());
	}
	
	// Retourne un profil en fonction de son identifiant
	public Conversation getConversationById(String roomName) {
		if (publicConversationMap.containsKey(roomName)) {
			return publicConversationMap.get(roomName);
		}

		if (pendingConversationMap.containsKey(roomName)) {
			return pendingConversationMap.get(roomName);
		}
		
		return null;
	}
	
	// Ajoute une conversation lancée
	public void addPendingConversation(boolean isPublic, String roomName, 
				boolean isInviter, String roomSubject) {
		Conversation newPendingConversation = new Conversation(isPublic, roomName, roomSubject);
		pendingConversationMap.put(roomName, newPendingConversation);
		
		// Aller à la conversation si on a créé le salon
		if (isInviter) {
			fireNewPendingConversation(newPendingConversation.getRoomName());
		}
	}
	
	// Ajoute une conversation publique
	public void addPublicConversation(String roomName, String roomSubject) {
		Conversation newPublicConversation = new Conversation(true, roomName, roomSubject);
		publicConversationMap.put(roomName,newPublicConversation);
		fireNewPublicConversation(newPublicConversation.getRoomName());
	}
	
	// Signale l'échec de la création d'une conversation
	public void notifyConversationCreationFailure(boolean isPublic, String roomName) {
		fireCreationConversationFail();
	}
	
	// Supprime une conversation
	public void removeConversation(String roomName) {
		publicConversationMap.remove(roomName);
		pendingConversationMap.remove(roomName);
		fireConversationRemoved(roomName);
	}
	
	// Ajoute un nouveau membre à la conversation 
	public void addConversationMember(String roomName, String jid) {
		Profile newMemberProfile = ContactsList.getInstance().getProfileByJid(jid);
		if ((newMemberProfile == null) || (newMemberProfile.isLocalUser())) {
			return;
		}
		
		getConversationById(roomName).addMember(newMemberProfile);
		fireNewMember(roomName, jid);
	}
	
	// Supprime un contact d'une conversation
	public void removeConversationMember(String roomName, String jid) {
		Profile oldMemberProfile = ContactsList.getInstance().getProfileByJid(jid);
		if (oldMemberProfile == null) {
			return;
		}
		
		getConversationById(roomName).removeMember(oldMemberProfile);
		fireMemberQuit(roomName, jid);
	}
	
	// Ajoute un message reçu dans une conversation
	public void addReceivedMessage(String roomName, String jidProfile, String content) {
		Profile senderProfile = ContactsList.getInstance().getProfileByJid(jidProfile);
		if ((senderProfile == null) || (senderProfile.isLocalUser())) {
			return;
		}

		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(senderProfile);
		getConversationById(roomName).addMessage(newMessage);
		fireReceivedMessage(roomName, newMessage);
	}
	
	// Ajoute un message envoyé dans une conversation
	public void addSendMessage(String roomName, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(LocalUserProfile.getInstance().getProfile());
		getConversationById(roomName).addMessage(newMessage);
	}
	
	// Retourne le nombre de conversations non lues
	public int getUnreadConversationCount(){
		int unreadCount = 0;
		for (Conversation conversation : pendingConversationMap.values()){
			if (conversation.getNbUnreadMessages() > 0)
				unreadCount++;
		}
		
		return unreadCount;
	}
	
	
	/* Gestion des listeners des événements liés aux conversations
	-------------------------------------------------------------------------*/
	public void addConversationListener(ConversationListener listener) {
		listenerList.add(listener);
	}
	
	public void removeConversationListener(ConversationListener listener) {
		listenerList.remove(listener);
	}
	
	private void fireReceivedMessage(String roomName, ConversationMessage newMessage) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onMessageReceived(roomName, newMessage);
			}
		}
	}

	private void fireNewPendingConversation(String roomName) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onPendingConversationAdded(roomName);
			}
		}
	}
	
	private void fireNewPublicConversation(String roomName) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onPublicConversationAdded(roomName);
			}
		}
	}
	
	private void fireCreationConversationFail() {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onCreationConversationFailed();
			}
		}
	}
	
	private void fireConversationRemoved(String roomName) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onConversationRemoved(roomName);
			}
		}
	}
	
	private void fireNewMember(String roomName, String jid) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onMemberJoined(roomName, jid);
			}
		}
	}
	
	private void fireMemberQuit(String roomName, String jid) {
		synchronized (listenerList) {
			for(ConversationListener listener : listenerList){
				listener.onMemberLeft(roomName, jid);
			}
		}
	}
}

