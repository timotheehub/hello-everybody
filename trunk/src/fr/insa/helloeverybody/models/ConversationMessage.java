package fr.insa.helloeverybody.models;


public class ConversationMessage {
	
	private Profile contact;
	private String message;
	
	public Profile getContact() {
		return contact;
	}
	public void setContact(Profile contact) {
		this.contact = contact;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
