package com.example.videotransfer.thread;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class OwnerThread extends Thread {
	private String ipAddr;
	
	private Handler handler;
	private int port;
	
	public OwnerThread(Handler handler, int port) {
		super();
		this.handler = handler;
		this.port = port;
	}

	/**
	 * @return the ipAddr
	 */
	public String getIpAddr() {
		return ipAddr;
	}

	/**
	 * @param ipAddr the ipAddr to set
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	@Override
	public void run() {
		super.run();
		// 获取IP 的过程
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			Socket client = serverSocket.accept();
			ObjectInputStream objectInputStream = new ObjectInputStream(
					client.getInputStream());
			Object object = objectInputStream.readObject();
			if (object.getClass().equals(String.class)
					&& ((String) object).equals("BROFIST")) {
				Log.d("P2P", "Client IP address: " + client.getInetAddress());
				setIpAddr(client.getInetAddress()+"");
			}
			objectInputStream.close();
			client.close();
			serverSocket.close();
			Bundle bundle = new Bundle();
			bundle.putString("ip", client.getInetAddress()+"");
			Message msg = new Message();
			msg.what = 0x01;
			msg.setData(bundle);
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
