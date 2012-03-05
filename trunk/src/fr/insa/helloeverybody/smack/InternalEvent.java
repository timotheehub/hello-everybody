package fr.insa.helloeverybody.smack;

public class InternalEvent {
	private String mRoomName;
	private String mMessageCode;
	private Object mContent;
	
	public InternalEvent(String roomName, String messageCode) {
		mRoomName = roomName;
		mMessageCode = messageCode;
	}
	
	public InternalEvent(String roomName, String messageCode, Object obj) {
		mRoomName = roomName;
		mMessageCode = messageCode;
		mContent = obj;
	}
	
	public void setContent(Object obj) {
		mContent = obj;
	}
	
	public void setRoomName(String roomName) {
		mRoomName = roomName;
	}
	
	public void setMessageCode(String messageCode) {
		mMessageCode = messageCode;
	}
	
	public String getRoomName() {
		return mRoomName;
	}
	
	public String getMessageCode() {
		return mMessageCode;
	}
	
	public Object getContent() {
		return mContent;
	}
}
