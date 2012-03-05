package fr.insa.helloeverybody.smack;

public class InternalEvent {
	public String mRoomName;
	public String mMessageCode;
	public Object mContent;
	
	public InternalEvent(String roomName, String messageCode) {
		mRoomName = roomName;
		mMessageCode = messageCode;
	}
	
	public void setContent(Object obj){
		mContent = obj;
	}
}
