package fr.insa.helloeverybody.smack;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.os.Handler;
import android.util.Log;
import fr.insa.helloeverybody.models.Profile;

public class ChatHelper {
	private static final String TAG = "ChatHelper";
	
	private Integer roomCounter = 0;
	private ConcurrentHashMap<String, MultiUserChat> mChatList;
	private ConnectionHelper mConnectionHelper;
	private Profile mUserProfile;
	
	private ConcurrentHashMap<String, Handler> mChatHandlerMap;
	
	private InvitationListener mInvitationListener;
	
	/**
	 * Création de classe permettant la gestion des différents chats en cours
	 * 
	 * @param localUserProfile : Profile de l'utilisateur du téléphone courant
	 * @param connectionHelper : Objet représentant la connection XMPP
	 */
	public ChatHelper(Profile localUserProfile, ConnectionHelper connectionHelper) {
		mChatList = new ConcurrentHashMap<String, MultiUserChat>();
		mChatHandlerMap = new ConcurrentHashMap<String, Handler>();
		mConnectionHelper = connectionHelper;
		mUserProfile = localUserProfile;
		
		mInvitationListener = new InvitationListener() {
			public void invitationReceived(Connection conn, String room, String inviter, String reason, String password, Message message) {
				joinRoom(room);
			}
		};
		
		mConnectionHelper.addInvitationListener(mInvitationListener);
	}
	
	
	private void sendMessageToHandler(Handler handler, int id, Object message) {
		handler.obtainMessage(id, message).sendToTarget();
	}
	
	private void sendMessageToHandlers(Set<Handler> handlerSet, int id, Object message) {
		for (Iterator<Handler> iterator = handlerSet.iterator(); iterator.hasNext();) {
			Handler handler = (Handler) iterator.next();
			handler.obtainMessage(id, message).sendToTarget();
		}
	}
	
	private void sendEventToChat(String roomName, Object message) {
		sendMessageToHandler(mChatHandlerMap.get(roomName), ChatService.CHAT_EVENT, message);
	}
	
	
	/**
	 * Permet d'associer un Handler a un salon de discussion(roomName) 
	 */
	public void registrateHandler(Handler handler, String roomName){
		mChatHandlerMap.put(roomName, handler);
	}
	
	public void setListenersToMuc(final MultiUserChat muc){
		muc.addMessageListener(new PacketListener(){
			public void processPacket(Packet pck){
				Message msg = (Message)pck;
				InternalEvent event = new InternalEvent(muc.getRoom(),ChatService.EVT_MSG_RCV);
				event.setContent(msg);
				sendEventToChat(muc.getRoom(),event);
			}
		});
		
		muc.addParticipantStatusListener(new DefaultParticipantStatusListener(){
			@Override
			public void joined(String participant){
				super.joined(participant);
				InternalEvent event = new InternalEvent(muc.getRoom(),ChatService.EVT_NEW_MEMBER);
				event.setContent(participant);
				sendEventToChat(muc.getRoom(),event);
			}
			
			@Override
			public void left(String participant){
				super.left(participant);
				InternalEvent event = new InternalEvent(muc.getRoom(),ChatService.EVT_MEMBER_QUIT);
				event.setContent(participant);
				sendEventToChat(muc.getRoom(),event);
			}
		});
	}
	
	/**
	 * Permet de créer un salon de discussion
	 * @return Identifiant du salon de discussion (roomName)
	 */
	public String createRoom() {
		String roomName = mUserProfile.getJid() + (++roomCounter);
		MultiUserChat muc = mConnectionHelper.createMultiUserChat(roomName);
		setListenersToMuc(muc);
		Boolean creationSuccess = false;
		
		try {
			muc.create(mUserProfile.getFullName());
			muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			creationSuccess = true;
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		if (creationSuccess) {
			mChatList.put(roomName, muc);
			return roomName;
		} else {
			return null;
		}
	}
	
	public Boolean joinRoom(String roomName) {
		MultiUserChat muc = mConnectionHelper.createMultiUserChat(roomName);
		setListenersToMuc(muc);
		Boolean joinSuccess = false;
		
		try {
			muc.join(mUserProfile.getFullName());
			joinSuccess = true;
		} catch (XMPPException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		if (joinSuccess) {
			mChatList.put(roomName, muc);
		}
		
		return joinSuccess;
	}
	
	public Boolean inviteUserToRoom(String roomName, String userJid) {
		MultiUserChat muc = mChatList.get(roomName);
		
		if (muc != null) {
			muc.invite(userJid + "@" + mConnectionHelper.getServerDomain(), null);
			return true;
		}
		
		return false;
	}
	
	public Boolean sendMessageToRoom(String roomName, String message) {
		MultiUserChat muc = mChatList.get(roomName);
		
		if (muc != null) {
			try {
				muc.sendMessage(message);
				InternalEvent event = new InternalEvent(roomName,ChatService.EVT_MSG_SENT);
				event.setContent(message);
				sendEventToChat(roomName, event);
				return true;
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		
		return false;
	}
}