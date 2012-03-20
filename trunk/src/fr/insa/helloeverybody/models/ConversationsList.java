package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import fr.insa.helloeverybody.conversations.ConversationsListActions;
import fr.insa.helloeverybody.helpers.ConversationsListener;
import fr.insa.helloeverybody.helpers.DatabaseContactHelper;
import fr.insa.helloeverybody.smack.ChatService;
import fr.insa.helloeverybody.smack.InternalEvent;

public class ConversationsList {

	// Singleton
	private static ConversationsList instance = null;
	
	// Attributes
	private Map<String,Conversation> publicConversations;
	private Map<String,Conversation> pendingConversations;
	
	private List<EventListener> listeners;
	private ChatService mChatService;
	
	private Handler roomJoinedHandler = new Handler() {
		public void handleMessage(Message msg) {
			InternalEvent ie = (InternalEvent) msg.obj;
			if (ie.getMessageCode() == ChatService.EVT_ROOM_JOINED) {
				String roomName = ie.getRoomName();
				
				for(String jid : mChatService.getRoomParticipants(roomName)) {
					Profile profile = ContactsList.getInstance().getProfileByJid(jid);
					if(profile.isUser()) {
						profile = mChatService.fetchProfile(jid);
						profile.setJid(jid);
						ContactsList.getInstance().addProfile(profile);
					} 
					ProfileType previousProfileType = profile.getProfileType();
					profile.setKnown(true);
					DatabaseContactHelper.updateOrInsertContact(profile);
					ContactsList.getInstance().update(profile, previousProfileType);
					addConversationMember(roomName, jid);
				}
			}
		}
	};
	
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
	public void addPendingConversation(boolean isPublic, String roomName, boolean isInviter, String roomTitle) {
		Conversation newPendingConversation = new Conversation(isPublic, roomName, roomTitle);
		pendingConversations.put(roomName,newPendingConversation);
		mChatService.addChatHandler(roomName, new RoomHandler());
		if(isInviter) {
			fireNewConversation(newPendingConversation.getRoomName());
		}
	}
	
	// Ajoute une conversation publique
	public void addPublicConversation(String roomName, String title) {
		Conversation newPublicConversation = new Conversation(true, roomName, title);
		publicConversations.put(roomName,newPublicConversation);
		fireNewPublicConversation(newPublicConversation.getRoomName());
	}
	
	// Ajoute un nouveau membre à la conversation 
	public void addConversationMember(String roomName, String jid) {
		boolean isUser = jid.equals(UserProfile.getInstance().getProfile().getJid());
		if (!isUser) {
			getConversationById(roomName).addMember(ContactsList.getInstance().getProfileByJid(jid));
			fireNewMember(roomName, jid);
		}
	}
	
	// Ajoute un message reçu dans une conversation
	public void addReceivedMessage(String roomName, String jidProfile, String content) {
		Profile profile = ContactsList.getInstance().getProfileByJid(jidProfile);
		if (!profile.isUser()) {
			ConversationMessage newMessage = new ConversationMessage();
			newMessage.setMessage(content);
			newMessage.setContact(profile);
			getConversationById(roomName).addMessage(newMessage);
			fireNewMessage(roomName,newMessage);
		}
	}
	
	// Ajoute un message envoyé dans une conversation
	public void addSendMessage(String roomName, String content) {
		ConversationMessage newMessage = new ConversationMessage();
		newMessage.setMessage(content);
		newMessage.setContact(UserProfile.getInstance().getProfile());
		getConversationById(roomName).addMessage(newMessage);
		fireNewMessage(roomName,newMessage);
	}

	// Supprime un contact d'une conversation
	public void removeConversationMember(String roomName, String jid) {
		getConversationById(roomName).removeMember(ContactsList.getInstance().getProfileByJid(jid));
		fireMemberQuit(roomName, jid);
	}
	
	// Supprime une conversation
	public void removeConversation(String roomName) {
		publicConversations.remove(roomName);
		pendingConversations.remove(roomName);
		fireConversationRemoved(roomName);
	}
	
	
	// Envoie une demande d'invitation au serveur avec reception du roomName
	// sur le handler
	public void sendInvitation(String jid) {
		NewRoomHandler generalHandler = new NewRoomHandler(jid);
		mChatService.addGeneralHandler(generalHandler);
		mChatService.createNewConversation();
	}
	
	public void createPublicGroupConversation(ArrayList<String> jidList, String roomTitle) {
		NewRoomHandler generalHandler = new NewRoomHandler(jidList, roomTitle);
		mChatService.addGeneralHandler(generalHandler);
		mChatService.createNewConversation(roomTitle,true);
	}
	
	// Envoie un message pour une conversation au serveur
	public void sendMessage(String roomName, String content) {
		mChatService.sendMessage(roomName, content);
	}
	
	// Envoie une notification de fermeture de conversation au serveur
	public void sendLeave(String roomName) {
		mChatService.leaveConversation(roomName);
	}

	public void acceptConversation(String roomName, String jid) {
		addPendingConversation(false, roomName, false, null);
		mChatService.joinIntoConversation(roomName);		
	}
	
	public void acceptPublicConversation(String roomName, String roomTitle) {		
		addPendingConversation(true, roomName, false, roomTitle);
		mChatService.joinIntoConversation(roomName);
	}

	public void rejectConversation(String roomName, String jid) {
		mChatService.rejectInvitation(roomName, jid);
	}
	
	public void inviteMembers(String roomName, List<String> jids) {	
		for(String userJID:jids){
			//TODO: tester....
			Log.w("Debug Invitation", "Demande d'invitation : "+userJID+" "+roomName);
			mChatService.inviteToConversation(roomName, userJID);
		}
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
		if (publicConversations.containsKey(roomName)) {
				return publicConversations.get(roomName);
		}

		if (pendingConversations.containsKey(roomName)) {
				return pendingConversations.get(roomName);
		}
		
		return null;
	}
	
	public int getUnreadConversationscount(){
		int unreadCount=0;
		for (Entry<String,Conversation> conv : pendingConversations.entrySet()){
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
	
	public void fireNewPublicConversation(String roomName) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).conversationPublicAdded(roomName);
		}
	}
	
	public void fireCreationConversationFail() {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).creationConversationFailed();
		}
	}
	
	public void fireConversationRemoved(String roomName) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).conversationRemoved(roomName);
		}
	}
	
	public void fireNewMember(String roomName, String jid) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).newMember(roomName, jid);
		}
	}
	
	public void fireMemberQuit(String roomName, String jid) {
		for(EventListener listener : listeners){
			((ConversationsListener) listener).memberQuit(roomName, jid);
		}
	}

	/** GESTION DES EVENEMENTS PROVENANT DU CHAT */
	public void connectChat(ChatService mChatService) {
		this.mChatService = mChatService;
		mChatService.addGeneralHandler(roomJoinedHandler);
	}
	
	public void disconnectChat(ChatService mChatService) {
		this.mChatService = null;
	}
	
	public void registerHandler(Handler h) {
		mChatService.addGeneralHandler(h);
	}
	
	public void removeHandler(Handler h) {
		mChatService.removeGeneralHandler(h);
	}
	
	// Handler pour recuperer le nom du salon, une fois cree et inviter le contact
	private class NewRoomHandler extends Handler {
		private String jid;
		private ArrayList<String> jidList;
		private String roomTitle;
		private boolean isGroupChat;
		
		public NewRoomHandler(String jid) {
			this.jid = jid;
			isGroupChat = false;
		}
		
		public NewRoomHandler (ArrayList<String> jidList, String roomTitle) {
			this.jidList = jidList;
			this.roomTitle = roomTitle;
			isGroupChat = true;
		}
		
		@Override
		public void handleMessage(Message msg) {
			InternalEvent ev = (InternalEvent) msg.obj;
			String subject = (String)ev.getContent();
			
			switch(ev.getMessageCode()) {
				case ChatService.EVT_NEW_ROOM:
					if (subject != null) {
						ConversationsListActions convListAct = ConversationsListActions.getInstance(null);
						
						if (convListAct != null) {
							convListAct.askRegisterPublicRoom(ev.getRoomName(), subject);
						} else {
							Log.d("ConvList", "Can't update public group ! ConversationListActions is NULL");
						}
					} else
						Log.d("ConvList", "Can't update public group ! Subject is NULL");
					
					if (isGroupChat) {
						for(String toInvite:jidList) {
							mChatService.inviteToConversation(ev.getRoomName(), toInvite);
						}
						addPendingConversation(true, ev.getRoomName(), true, roomTitle);
					}
					else {
						mChatService.inviteToConversation(ev.getRoomName(), jid);
						addPendingConversation(false, ev.getRoomName(), true, null);
					}				
					mChatService.removeGeneralHandler(this);
					break;
					
				case ChatService.EVT_NEW_ROOM_FAIL:
					fireCreationConversationFail();
					break;
					
				default:
					break;
			}
		}
	}
	
	// Handler pour recuperer le nom du salon, une fois cree et inviter le contact
	private class RoomHandler extends Handler {
		
		public RoomHandler() {
		}
		
		@Override
		public void handleMessage(Message msg) {
			InternalEvent ev = (InternalEvent) msg.obj;
			switch (ev.getMessageCode()) {
			case ChatService.EVT_INV_REJ :
				if (getConversationById(ev.getRoomName()).isEmpty()) {
					removeConversation(ev.getRoomName());
					mChatService.removeChatHandler(ev.getRoomName());
				} else {
					// TODO Notification du refus
				}
			 	break;
			case ChatService.EVT_MSG_RCV :
				org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) ev.getContent();
				addReceivedMessage(ev.getRoomName(), message.getFrom(), message.getBody());
				Log.e("TEST", message.getFrom().toString());
				break;
			case ChatService.EVT_MSG_SENT :
				addSendMessage(ev.getRoomName(), (String) ev.getContent());
				break;
			case ChatService.EVT_NEW_MEMBER :
				addConversationMember(ev.getRoomName(), (String) ev.getContent());
				break;
			case ChatService.EVT_MEMBER_QUIT :
				String member = (String) ev.getContent();
				boolean isUser = member.equals(UserProfile.getInstance().getProfile().getJid());
				// TODO Demander le retour du JID
				if (!isUser) {
					removeConversationMember(ev.getRoomName(), member);
				}
				if (getConversationById(ev.getRoomName()).isEmpty() || isUser) {
					removeConversation(ev.getRoomName());
					mChatService.removeChatHandler(ev.getRoomName());
				}
				break;
			 default :
			}
		}
	}

	public ChatService getChatService() {
		return this.mChatService;
	}
}

