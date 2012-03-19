package fr.insa.helloeverybody.conversations;

import java.util.HashMap;

public interface ConversationsListCallbackInterface {
	public void publicGroupsUpdate(HashMap<String, String> groupList);
}
