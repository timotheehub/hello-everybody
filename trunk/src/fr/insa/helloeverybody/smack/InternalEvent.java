package fr.insa.helloeverybody.smack;

public class InternalEvent {
	private String mRoomName;
	private int mMessageCode;
	private Object mContent;
	
	public InternalEvent(String roomName, int messageCode) {
		mRoomName = roomName;
		mMessageCode = messageCode;
	}
	
	public InternalEvent(String roomName, int messageCode, Object obj) {
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
	
	public void setMessageCode(int messageCode) {
		mMessageCode = messageCode;
	}
	
	public String getRoomName() {
		return mRoomName;
	}
	
	public int getMessageCode() {
		return mMessageCode;
	}
	
	public Object getContent() {
		return mContent;
	}
}
