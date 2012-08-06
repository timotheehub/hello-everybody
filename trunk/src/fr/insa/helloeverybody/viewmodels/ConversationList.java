package fr.insa.helloeverybody.viewmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.insa.helloeverybody.interfaces.ConversationListener;
import fr.insa.helloeverybody.models.Conversation;
import fr.insa.helloeverybody.models.ConversationMessage;
import fr.insa.helloeverybody.models.Profile;

/* Classe qui contient la liste des conversations publiques et en cours
-----------------------------------------------------------------------------*/
public class ConversationList {

	// Constantes
	public static final String TAG = "ConversationsList";
		
	// Singleton
	private static ConversationList instance = null;
	
	// Attributs
	private List<Conversation> hiddenConversationList;
	private List<Conversation> pendingConversationList;
	private List<Conversation> publicConversationList;
	private List<ConversationListener> listenerList;
	
	// Constructeur privé
	private ConversationList() {
		hiddenConversationList = Collections.synchronizedList(
				new LinkedList<Conversation>());
		pendingConversationList = Collections.synchronizedList(
				new LinkedList<Conversation>());
		publicConversationList = Collections.synchronizedList(
					new LinkedList<Conversation>());
		listenerList = Collections.synchronizedList(
					new LinkedList<ConversationListener>());
	}
	
	// Retourne le singleton de maniere protegee
	public static synchronized ConversationList getInstance() {
		if (instance == null) {
			instance = new ConversationList();
		}
		return instance;
	}
	
	
	/* Gestion des conversations et des messages
	-------------------------------------------------------------------------*/
	// Retourne le numéro de page conversation
	public int getPendingIndex(String aRoomName) {
		
		synchronized (pendingConversationList) {
			for (int i = 0; i < pendingConversationList.size(); i++) {
				Conversation conversation = pendingConversationList.get(i);
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	// Retourne le nom de la conversation
	public String getPendingRoomName(int page) {
		
		if ((page < 0) || (page >= pendingConversationList.size())) {
			return null;
		}
		
		return pendingConversationList.get(page).getRoomName();
	}
	
	
	// Retourne la liste des conversation publiques
	public List<Conversation> getPublicRoomList() {
		ArrayList<Conversation> conversationList = 
				new ArrayList<Conversation>(); 
		
		synchronized (publicConversationList) {
			for (Conversation conversation : publicConversationList) {
				conversationList.add(conversation);
			}
		}
		
		return conversationList;
	}
	
	// Retourne la liste des conversations en cours
	public List<Conversation> getPendingRoomList() {
		ArrayList<Conversation> conversationList = 
				new ArrayList<Conversation>(); 
		
		synchronized (pendingConversationList) {
			for (Conversation conversation : pendingConversationList) {
				conversationList.add(conversation);
			}
		}
		
		return conversationList;
	}
	
	// Retourne un profil en fonction de son identifiant
	public Conversation getConversationByName(String aRoomName) {
		
		synchronized (hiddenConversationList) {
			for (Conversation conversation : hiddenConversationList) {
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					return conversation;
				}
			}
		}

		synchronized (pendingConversationList) {
			for (Conversation conversation : pendingConversationList) {
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					return conversation;
				}
			}
		}
		
		synchronized (publicConversationList) {
			for (Conversation conversation : publicConversationList) {
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					return conversation;
				}
			}
		}
		
		return null;
	}
	
	// Ajoute une conversation à ne pas afficher
	public void addHiddenConversation(boolean isPublic, 
				String roomName, String roomSubject) {
		hiddenConversationList.add(
				new Conversation(isPublic, roomName, roomSubject));
	}
	
	// Ajoute une conversation lancée
	public void addPendingConversation(boolean isPublic, String roomName, 
				String roomSubject) {
		pendingConversationList.add(
				new Conversation(isPublic, roomName, roomSubject));
	}
	
	// Ajoute une conversation publique
	public void addPublicConversation(String roomName, String roomSubject) {
		publicConversationList.add(
				new Conversation(true, roomName, roomSubject));
		fireNewPublicConversation(roomName);
	}
	
	// Signale le succès de la création d'une conversation 
	public void notifyConversationCreationSuccess(String roomName) {
		fireConversationCreationSucceeded(roomName);
	}
	
	// Signale l'échec de la création d'une conversation
	public void notifyConversationCreationFailure(String roomName) {
		fireConversationCreationFailed(roomName);
	}
	
	// Supprime une conversation
	public void removeConversation(String aRoomName) {

		// Supprimer la conversation
		synchronized (hiddenConversationList) {
			Iterator<Conversation> iterator = hiddenConversationList.iterator();
			while (iterator.hasNext()) {
				Conversation conversation = iterator.next();
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					iterator.remove();
				}
			}
		}
		
		synchronized (pendingConversationList) {
			Iterator<Conversation> iterator = pendingConversationList.iterator();
			while (iterator.hasNext()) {
				Conversation conversation = iterator.next();
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					iterator.remove();
				}
			}
		}
		
		synchronized (publicConversationList) {
			Iterator<Conversation> iterator = publicConversationList.iterator();
			while (iterator.hasNext()) {
				Conversation conversation = iterator.next();
				String roomName = conversation.getRoomName();
				if ((roomName != null) && (roomName.equals(aRoomName))) {
					iterator.remove();
				}
			}
		}
		
		// Signaler la suppression de la conversation
		fireConversationRemoved(aRoomName);
	}
	
	// Ajoute un nouveau membre à la conversation 
	public void addConversationMember(String roomName, String jid) {
		Profile newMemberProfile = ContactList.getInstance().getProfileByJid(jid);
		if ((newMemberProfile == null) || (newMemberProfile.isLocalUser())) {
			return;
		}
		
		getConversationByName(roomName).addMember(newMemberProfile);
		fireNewMember(roomName, jid);
	}
	
	// Supprime un contact d'une conversation
	public void removeConversationMember(String roomName, String jid) {
		Profile oldMemberProfile = ContactList.getInstance().getProfileByJid(jid);
		if (oldMemberProfile == null) {
			return;
		}
		
		getConversationByName(roomName).removeMember(oldMemberProfile);
		fireMemberQuit(roomName, jid);
	}
	
	// Ajoute un message reçu dans une conversation
	public void addReceivedMessage(String roomName, String jidProfile, String content) {
		Profile senderProfile = ContactList.getInstance().getProfileByJid(jidProfile);
		if ((senderProfile == null) || (senderProfile.isLocalUser())) {
			return;
		}

		// Créer le message
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(senderProfile);
		Conversation conversation = getConversationByName(roomName);
		
		// Marquer la conversation comme courante au lieu de caché
		synchronized (hiddenConversationList) {
			if (hiddenConversationList.contains(conversation)) {
				hiddenConversationList.remove(conversation);
				pendingConversationList.add(conversation);
			}
		}
		
		// Mettre la conversation en première position
		synchronized (pendingConversationList) {
			if (pendingConversationList.contains(conversation)) {
				pendingConversationList.remove(conversation);
				pendingConversationList.add(0, conversation);
			}
		}
		
		// Ajouter le message
		conversation.addMessage(newMessage);
		fireReceivedMessage(roomName, newMessage);
	}
	
	// Ajoute un message envoyé dans une conversation
	public void addSendMessage(String roomName, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(LocalUserProfile.getInstance().getProfile());
		getConversationByName(roomName).addMessage(newMessage);
	}
	
	// Retourne le nombre de conversations non lues
	public int getUnreadConversationCount(){
		int unreadCount = 0;
		
		synchronized (pendingConversationList) {
			for (Conversation conversation : pendingConversationList){
				if (conversation.getNbUnreadMessages() > 0) {
					unreadCount++;
				}
			}
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

	private void fireConversationCreationSucceeded(String roomName) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onConversationCreationSucceeded(roomName);
			}
		}
	}
	
	private void fireConversationCreationFailed(String roomName) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onConversationCreationFailed(roomName);
			}
		}
	}
	
	private void fireReceivedMessage(String roomName, ConversationMessage newMessage) {
		synchronized (listenerList) {
			for (ConversationListener listener : listenerList){
				listener.onMessageReceived(roomName, newMessage);
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

