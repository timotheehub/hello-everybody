package fr.insa.helloeverybody.smack;

import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;
import fr.insa.helloeverybody.models.Profile;

public class ChatHelper {
	private static final String TAG = "ChatHelper";
	
	private Integer roomCounter = 0;
	private ConcurrentHashMap<String, MultiUserChat> mChatList;
	private ConnectionHelper mConnectionHelper;
	private Profile mUserProfile;
	
	private InvitationListener mInvitationListener;
	
	/**
	 * Création de classe permettant la gestion des différents chats en cours
	 * 
	 * @param localUserProfile : Profile de l'utilisateur du téléphone courant
	 * @param connectionHelper : Objet représentant la connection XMPP
	 */
	public ChatHelper(Profile localUserProfile, ConnectionHelper connectionHelper) {
		mChatList = new ConcurrentHashMap<String, MultiUserChat>();
		mConnectionHelper = connectionHelper;
		mUserProfile = localUserProfile;
		
		mInvitationListener = new InvitationListener() {
			public void invitationReceived(Connection conn, String room, String inviter, String reason, String password, Message message) {
				joinRoom(room);
			}
		};
		
		mConnectionHelper.addInvitationListener(mInvitationListener);
	}
	
	/**
	 * Permet de créer un salon de discussion
	 * @return Identifiant du salon de discussion (roomName)
	 */
	public String createRoom() {
		String roomName = mUserProfile.getJid() + (++roomCounter);
		MultiUserChat muc = mConnectionHelper.createMultiUserChat(roomName + "@" + mConnectionHelper.getConferenceServer());
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
				return true;
			} catch (XMPPException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		
		return false;
	}
}