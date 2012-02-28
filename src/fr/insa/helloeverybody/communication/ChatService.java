package fr.insa.helloeverybody.communication;

import java.util.Map;
import java.util.jar.Attributes;

import android.os.Handler;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.util.StringUtils;

import fr.insa.helloeverybody.models.Profile;


public class ChatService {
	private Handler mHandler;
	private XMPPConnection mConnection;
	private String targetAddr;
	private int targetPort;
	private String targetService;
	private final static int MESSAGE_IN=1; // message entrant
	private final static int MESSAGE_OUT=2; //message sortant
	private final static int MESSAGE_SYS=3; //message d'information systeme
	
	public ChatService(Handler mHandler, String serverAddr, int serverPort, String serverService){
		this.mHandler=mHandler;
		targetAddr=serverAddr;
		targetPort=serverPort;
		targetService=serverService;
	}
	
	public void doConnect(){
		ConnectionConfiguration connConfig=new ConnectionConfiguration(targetAddr, targetPort, targetService);
		XMPPConnection connection = new XMPPConnection(connConfig);
		try{
			connection.connect();
			setConnection(connection);
		}
		catch(XMPPException e){
			setConnection(null);
		}
	}
	
	//Methode pour creer un compte sur le serveur
	public void doRegistrate(String login, String pwd, Profile profile){
		RegistrateThread registrateThread = new RegistrateThread(login,pwd,profile);
		registrateThread.start();
	}
	
	//Methode pour se logger sur le serveur, l'utilisateur peut ensuite ecrire et recevoir
	//les messages
	public void doLogin(String login, String pwd){
		LoginThread loginThread=new LoginThread(login, pwd);
		loginThread.start();
	}
	
	public void setConnection(XMPPConnection connection){
		this.mConnection=connection;
		if (mConnection != null){
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			mConnection.addPacketListener(new PacketListener() {
				                public void processPacket(Packet packet) {
				                    Message message = (Message) packet;
				                    if (message.getBody() != null) {
				                        //String fromName = StringUtils.parseBareAddress(message.getFrom());				                      
				                        //messages.add(fromName + ":");
				                        //messages.add(message.getBody());
				                        mHandler.obtainMessage(MESSAGE_IN,message.getBody()).sendToTarget();
				                    }
				                }
				            }, filter);
		}
	}
	
	
	public void write(String to, String text){
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);
		mConnection.sendPacket(msg);
		mHandler.obtainMessage(MESSAGE_OUT,text).sendToTarget();
		
	}
	

	private class LoginThread extends Thread{
		String login;
		String pwd;
	
		public LoginThread(String login, String pwd){
			this.login=login;
			this.pwd=pwd;
		}
	
		public void run(){		
		    try {
		    	mHandler.obtainMessage(MESSAGE_OUT,"connecting").sendToTarget();
		    	//si la connexion n'a pas ete etablie, on connecte 
		    	if(mConnection==null){
		    		doConnect();
		    	}
		    	mConnection.login(login, pwd);
		    	mHandler.obtainMessage(MESSAGE_OUT,"connection established").sendToTarget();
		    	
		    	// Set the status to available
		    	Presence presence = new Presence(Presence.Type.available);
		    	mConnection.sendPacket(presence);
		    	
		    } catch (XMPPException ex) {
		    	setConnection(null);
		    }
		}
	}
	
	//Thread pour creer un nouveau compte
	private class RegistrateThread extends Thread{
		Profile userProfile;
		String login;
		String pwd;
		
		public RegistrateThread(String login, String pwd, Profile userProfile){
			this.userProfile=userProfile;
			this.login=login;
			this.pwd=pwd;
		}
		
		public void run(){
			Attributes attributes=new Attributes();
			attributes.putValue("login", login);
			attributes.putValue("pwd", pwd);
			doConnect();
			AccountManager accountManager=new AccountManager(mConnection);
			try{
				if(accountManager.supportsAccountCreation()){
					accountManager.createAccount(login, pwd);
					mHandler.obtainMessage(MESSAGE_OUT,"Account Created").sendToTarget();
				}
			}
			catch(XMPPException e){
				setConnection(null);
			}
		}
	}
	
}