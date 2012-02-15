package fr.insa.helloeverybody.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

public class ChatService {
	private Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private AcceptThread mAcceptThread;
	
	public ChatService(Handler mHandler){
		this.mHandler=mHandler;
	}
	
	public void doStart(int serverPort){
		mAcceptThread=new AcceptThread(serverPort);
		mAcceptThread.start();
	}
	
	public void doConnect(String targetAddr, int targetPort){
		mConnectThread=new ConnectThread(targetAddr, targetPort);
		mConnectThread.start();
	}
	
	public void doConnected(Socket socket){
		mConnectedThread=new ConnectedThread(socket);
		mConnectedThread.start();
	}
	
	public void write(String msg){
		mConnectedThread.write(msg);
	}
	
	private class AcceptThread extends Thread{
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
	}

	private class ConnectThread extends Thread{
		String targetAddr;
		int targetPort;
	
		public ConnectThread(String targetAddr, int targetPort){
			this.targetAddr=targetAddr;
			this.targetPort=targetPort;
		}
	
		public void run(){
			Socket socket=null;
			try{
				mHandler.obtainMessage(1,"Connecting...").sendToTarget();
				socket=new Socket(targetAddr, targetPort);
				mHandler.obtainMessage(1,"Connection established").sendToTarget();
			}
			catch(IOException e){
			
			}
			doConnected(socket);
		}
	}
	
	private class ConnectedThread extends Thread{
		Socket socket=null;
		DataInputStream inStream=null;
		DataOutputStream outStream=null;
		
		public ConnectedThread(Socket socket){
			this.socket=socket;
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
	}
}