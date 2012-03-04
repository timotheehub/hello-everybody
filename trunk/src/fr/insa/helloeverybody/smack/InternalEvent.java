package fr.insa.helloeverybody.smack;

public class InternalEvent {
	public String mRoomName;
	public String mMessageCode;
	
	public InternalEvent(String roomName, String messageCode) {
		mRoomName = roomName;
		mMessageCode = messageCode;
	}
}
