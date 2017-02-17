package com.example.videotransfer.thread;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GoThread extends Thread {
	
	private String ip;
	private int port;
	
	public GoThread(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void run() {
		super.run();
		// 获取IP 的过程
		try {
			System.out.println("ip"+ port);
			Socket socket = new Socket();
			socket.setReuseAddress(true);
			socket.connect((new InetSocketAddress(ip, port)), 5000);
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(new String("BROFIST"));
			oos.close();
			os.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
