package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
	
	// Attributs
	private boolean isPublic = false;
	private String roomName;
	private String roomTitle;
	private List<Profile> members = new ArrayList<Profile>();
	private List<ConversationMessage> messages = new ArrayList<ConversationMessage>();
	private int nbUnreadMessages = 0;
	
	// Constructeurs
	public Conversation() { }
	
	public Conversation(boolean isPublic, String roomName) {
		this.roomName = roomName;
		this.isPublic = isPublic;
	}
	
	public Conversation(boolean isPublic, String roomName, String roomSubject) {
		this.roomName = roomName;
		this.isPublic = isPublic;
		this.roomTitle = roomSubject;
	}

	// Liste des participants 
	public void addMember(Profile profile) {
		members.add(profile);
		generateTitle();
	}
	
	public boolean removeMember(Profile profile) {
		if (members.remove(profile)) {
			generateTitle();
			return true;
		} else {
			return false;
		}
	}
	
	public List<Profile> getMembers(){
		return members;
	}
	
	public ArrayList<String> getMemberJidList(){
		ArrayList<String> memberJidList = new ArrayList<String>();
		for (Profile member : members) {
			memberJidList.add(member.getJid());
		}
		return memberJidList;
	}
	
	// Liste des messages
	public void addMessage(ConversationMessage message) {
		messages.add(message);
		this.addUnreadMessage();
	}
	
	// Getters et setters
	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public void generateTitle() {
		if (!isPublic) {
			roomTitle = "";
			int i;
			for (i = 0 ; i < members.size()-1 ; i++) {
				roomTitle += members.get(i).getFullName() + ", ";
			}
			if (!members.isEmpty()) {
				roomTitle += members.get(i).getFullName();
			}
		}
	}

	public String getRoomSubject() {
		return roomTitle;
	}

	public void setRoomSubject(String roomSubject) {
		this.roomTitle = roomSubject;
	}

	public int getNbUnreadMessages() {
		return nbUnreadMessages;
	}

	public void setNbUnreadMessages(int nbUnreadMessages) {
		this.nbUnreadMessages = nbUnreadMessages;
	}
	
	public void addUnreadMessage() {
		this.nbUnreadMessages = this.nbUnreadMessages + 1;
	}
	
	public List<ConversationMessage> getMessages() {
		return messages;
	}
	
	public boolean isEmpty() {
		return members.isEmpty();
	}
}
