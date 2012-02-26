package fr.insa.helloeverybody.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;


public class ChatService {
	private Handler mHandler;
	private ConnectThread mConnectThread;
	//private ConnectedThread mConnectedThread;
	//private AcceptThread mAcceptThread;
	private XMPPConnection connection;
	
	public ChatService(Handler mHandler){
		this.mHandler=mHandler;
	}
	
	/*public void doStart(int serverPort){
		mAcceptThread=new AcceptThread(serverPort);
		mAcceptThread.start();
	}*/
	
	public void doConnect(String targetAddr, int targetPort, String login, String pwd, String service){
		mConnectThread=new ConnectThread(targetAddr, targetPort, login, pwd, service);
		mConnectThread.start();
	}
	
	public void setConnection(XMPPConnection connetion){
		this.connection=connetion;
		if (connection != null){
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				                public void processPacket(Packet packet) {
				                    Message message = (Message) packet;
				                    if (message.getBody() != null) {
				                        //String fromName = StringUtils.parseBareAddress(message.getFrom());				                      
				                        //messages.add(fromName + ":");
				                        //messages.add(message.getBody());
				                        mHandler.obtainMessage(2,message.getBody()).sendToTarget();
				                    }
				                }
				            }, filter);
		}
	}
	
	/*public void doConnected(){
		mConnectedThread=new ConnectedThread();
		mConnectedThread.start();
	}*/
	
	public void write(String to, String text){
		//mConnectedThread.write(msg);
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(text);
		connection.sendPacket(msg);
		mHandler.obtainMessage(2,text).sendToTarget();
		
	}
	
	/*private class AcceptThread extends Thread{
		ServerSocket serversocket=null;
		
		public AcceptThread(int serverPort){
			try{
				serversocket=new ServerSocket(serverPort);
			}
			catch(IOException e){
				
			}
		}
		
		public void run(){
			Socket socket=null;
			while(true){
				try{
					socket=serversocket.accept();
					mHandler.obtainMessage(1,"connection established").sendToTarget();
				}
				catch(IOException e){
					
				}
				doConnected(socket);
			}
		}
	}*/

	private class ConnectThread extends Thread{
		String targetAddr;
		int targetPort;
		String login;
		String pwd;
		String service;
	
		public ConnectThread(String targetAddr, int targetPort, String login, String pwd, String service){
			this.targetAddr=targetAddr;
			this.targetPort=targetPort;
			this.login=login;
			this.pwd=pwd;
			this.service=service;
		}
	
		public void run(){
			
			/*Socket socket=null;
			try{
				mHandler.obtainMessage(1,"Connecting...").sendToTarget();
				socket=new Socket(targetAddr, targetPort);
				mHandler.obtainMessage(1,"Connection established").sendToTarget();
			}
			catch(IOException e){
			
			}
			doConnected(socket);*/
			
			
			ConnectionConfiguration connConfig=new ConnectionConfiguration(targetAddr, targetPort, service);
			XMPPConnection connection = new XMPPConnection(connConfig);
		    try {
		    	mHandler.obtainMessage(2,"connecting").sendToTarget();
		    	connection.connect();
		    	connection.login(login, pwd);
		    	mHandler.obtainMessage(2,"connection established").sendToTarget();
		    	// Set the status to available
		    	Presence presence = new Presence(Presence.Type.available);
		    	connection.sendPacket(presence);
		    	setConnection(connection);
		    } catch (XMPPException ex) {
		    	//xmppClient.setConnection(null);
		    }

		}
	}
	
	/*private class ConnectedThread extends Thread{
		
		public ConnectedThread(){
			try{
				inStream=new DataInputStream(socket.getInputStream());
				outStream=new DataOutputStream(socket.getOutputStream());
			}
			catch(IOException e){
				
			}
		}
		
		public void run(){
			String msg;
			while(true){
				try{
					msg=inStream.readUTF();
					mHandler.obtainMessage(1, msg).sendToTarget();
				}
				catch(IOException e){
					
				}
			}
		}
		
		public void write(String msg){
			try{
				outStream.writeUTF(msg);
				mHandler.obtainMessage(2,msg).sendToTarget();
			}
			catch(IOException e){
				
			}
		}
	}*/
}