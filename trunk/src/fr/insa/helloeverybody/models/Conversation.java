package fr.insa.helloeverybody.models;

import java.util.ArrayList;
import java.util.List;



public class Conversation {
	
	// Attributs
	private boolean isPublic = false;
	private String roomName;
	private String title;
	private String destID; //A qui les messages doivent etre envoyes
	private List<Profile> members = new ArrayList<Profile>();
	private List<ConversationMessage> messages = new ArrayList<ConversationMessage>();
	private int nbUnreadMessages = 0;
	private boolean open=false;
	
	
	
	// Constructeurs
	public Conversation() {
	}
	

	public Conversation(boolean isPublic, String roomName) {
		this.roomName = roomName;
		this.isPublic = isPublic;
	}
	
	public Conversation(boolean isPublic, String roomName, String title) {
		this.roomName = roomName;
		this.isPublic = isPublic;
		this.title = title;
	}

	public void setDestID(String destID){
		this.destID=destID;
	}
		
	public String getDestID(){
		return destID;
	}
	
	public void addMembersById(List<Long> idsProfile) {
		for (Long id : idsProfile) {
			members.add(ContactsList.getInstance().getProfileById(id));
		}
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
	
	public ArrayList<String> getMembersIDs(){
		ArrayList<String> mIDs=new ArrayList<String>();
		for(Profile member:members)
			mIDs.add(member.getId().toString());
		return mIDs;
	}
	
	
	// Liste des messages
	public void addMessage(ConversationMessage message) {
		messages.add(message);
		if(!this.open){
			this.addUnreadMessage();
			System.out.println("msg unread "+this.nbUnreadMessages);
		}
	}
	
	public boolean removeMember(ConversationMessage message) {
		return messages.remove(message);
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
			title = "";
			int i;
			for (i = 0 ; i < members.size()-1 ; i++) {
				title += members.get(i).getFullName() + ", ";
			}
			if (!members.isEmpty()) {
				title += members.get(i).getFullName();
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getNbUnreadMessages() {
		return nbUnreadMessages;
	}

	public void setNbUnreadMessages(int nbUnreadMessages) {
		this.nbUnreadMessages = nbUnreadMessages;
	}
	
	public void addUnreadMessage() {
		this.nbUnreadMessages = this.nbUnreadMessages+1;
	}
	
	public List<ConversationMessage> getMessages() {
		return messages;
	}
	
	public boolean isEmpty() {
		return members.isEmpty();
	}
	
	public boolean isOpen(){
		return this.open;
	}
}
