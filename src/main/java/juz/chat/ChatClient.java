package juz.chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatClient implements Runnable{

	private int port = 8888;
	private Thread senderThread;
	private InputStream inStream;
	private volatile static boolean socketIsAlive = true;
	private PrintStream pStream;
	public Socket socket;
	public BufferedReader brSocketIn;
	public PrintWriter pwSocketOut;
	public BufferedReader bSysInReader;
	public long delay;
	private String host = "localhost";
	
	
	
	public static void main(String[] args){
		try {
			new ChatClient(System.in, System.out).run();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The client thread console input message
	 */
	static class Sender implements Runnable {
		private ChatClient chatClient;
		

		public Sender(ChatClient chatClient) {
			this.chatClient = chatClient;
		}

		public void run() {
			try {
				String msg;

				while (socketIsAlive) {
					if((msg = chatClient.bSysInReader.readLine())!=null){
						chatClient.pwSocketOut.println(msg);
						
						if (msg.trim().equals("exit")) {
							chatClient.pwSocketOut.close();
							chatClient.bSysInReader.close();
							break;
						}
					}
					Thread.sleep(chatClient.delay);
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}
	
	public ChatClient(InputStream inStream,PrintStream pStream) throws UnknownHostException, IOException{
		this(inStream,pStream,0);
	}
	
	
	public ChatClient(InputStream inStream,PrintStream pStream,long delay) throws UnknownHostException, IOException {
			Properties props = new Properties();
			//props.loadFromXML(new FileInputStream("ChatClient.xml"));
			props.loadFromXML(this.getClass().getClassLoader().getResourceAsStream("ChatClient.xml"));
			
			port = Integer.parseInt(props.getProperty("port"));
			host = props.getProperty("host");
		
			this.inStream = inStream;
			this.pStream = pStream;
			this.delay = delay;
			bSysInReader = new BufferedReader(new InputStreamReader(this.inStream));
			
			
			socket = new Socket(host, port);
			senderThread = new Thread(new Sender(this),"Поток отправки сообщений");

			brSocketIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			pwSocketOut = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		senderThread.start();
		String msg;
		try {
			while ((msg = brSocketIn.readLine()) != null) {
				pStream.println(msg);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			socketIsAlive = false;
		}
		socketIsAlive = false;

	}
}
