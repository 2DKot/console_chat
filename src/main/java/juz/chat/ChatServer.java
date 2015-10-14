package juz.chat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import juz.commands.CommandFactory;
import juz.commands.IChatServerCommand;

public class ChatServer {
	private int port;
	private int msgQueueMaxLength;
	private static ConcurrentHashMap<String, Socket> clientMap = new ConcurrentHashMap<String, Socket>();
	private ExecutorService execSvc;
	private ServerSocket server;
	private ArrayBlockingQueue<String> msgQueue;
	private String userNamePattern;
	private String commandPattern;
	private String commandMapFileName;
	public int loginTryCount;
	public Object userExitCommand;

	private void setPort(int port) {
		this.port = port;
	}

	public void init(String config) throws InvalidPropertiesFormatException,
			FileNotFoundException, IOException {

		Properties props = new Properties();
		props.loadFromXML(this.getClass().getClassLoader().getResourceAsStream(config));
		//props.loadFromXML(new FileInputStream(config));

		this.port = Integer.parseInt(props.getProperty("port"));
		this.msgQueueMaxLength = Integer.parseInt(props
				.getProperty("msgQueueMaxLength"));
		this.userNamePattern = props.getProperty("userNamePattern");
		this.commandPattern = props.getProperty("commandPattern");
		this.commandMapFileName = props.getProperty("commandMapFileName");
		this.loginTryCount = Integer.parseInt(props
				.getProperty("loginTryCount"));
		this.userExitCommand = props.getProperty("userExitCommand");

		msgQueue = new ArrayBlockingQueue<String>(this.msgQueueMaxLength);
		
		CommandFactory.initCommandMap(this.commandMapFileName);
	}

	public int getPort() {
		return port;
	}

	public ConcurrentHashMap<String, Socket> getClientMap() {
		return clientMap;
	}

	public ArrayBlockingQueue<String> getMsgQueue() {
		return msgQueue;
	}

	public static void main(String[] args)
			throws InvalidPropertiesFormatException, FileNotFoundException,
			IOException {
		new ChatServer("ChatServer.xml");
	}

	public ChatServer(String configFileName)
			throws InvalidPropertiesFormatException, FileNotFoundException,
			IOException {
		this.init(configFileName);

		try {
			server = new ServerSocket(port);
			System.out.println("Server has started!");

			execSvc = Executors.newCachedThreadPool();

			// remove first message from overflowed queue
			execSvc.execute(new MsgQueueHandler(msgQueue));

			Socket client = null;
			while (true) {
				client = server.accept(); // receive client connections
				execSvc.execute(new ChatListener(client, this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static class MsgQueueHandler implements Runnable {
		private ArrayBlockingQueue<String> msgQueue;

		public MsgQueueHandler(ArrayBlockingQueue<String> msgQueue) {
			this.msgQueue = msgQueue;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!(Thread.currentThread().isInterrupted())) {
				if (msgQueue.remainingCapacity() == 0) {
					try {
						msgQueue.take();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
			}
		}
	}

	static class ChatListener implements Runnable {
		private Socket socket;
		private BufferedReader bReader;
		private PrintWriter pWriter;
		private String userName;
		private ChatServer chatServer;

		public ChatListener(Socket socket, ChatServer chatServer)
				throws IOException {
			this.socket = socket;
			this.bReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			this.chatServer = chatServer;
		}

		// Broadcasts message for all clients
		private void sendAllMessage(String msg) {
			System.out.println(msg);

			for (Socket clientSocket : clientMap.values()) {
				if (!(clientSocket.isClosed())) {
					try {
						pWriter = new PrintWriter(
								clientSocket.getOutputStream(), true);
					} catch (IOException e) {
						e.printStackTrace();
					}
					pWriter.println(msg);
				}
			}
			// Log message to Queue
			try {
				
				this.chatServer.msgQueue.put(msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void sendPersonalMessage(String msg, Socket clientSocket)
				throws IOException {
			// TODO Auto-generated method stub
			PrintWriter pWriter = new PrintWriter(
					clientSocket.getOutputStream(), true);
			pWriter.println(msg);
		}

		@Override
		public void run() {
			String msg = null;
			// TODO Auto-generated method stub
			try {
				if (initUserName()) {

					while ((msg = bReader.readLine()) != null) {
						if (msg.trim().equals(chatServer.userExitCommand)) {
							clientMap.remove(this.userName);
							bReader.close();
							pWriter.close();
							socket.close();
							msg = this.userName + " leave the chat!";
							sendAllMessage(msg);
							break;
						} else {
							if (msg.matches(chatServer.commandPattern)) {
								try {
									IChatServerCommand srvCmd = CommandFactory
											.getCommand(msg, this.chatServer);
									sendPersonalMessage(
											srvCmd.doIt(msg, this.chatServer),
											this.socket);
								} catch (InstantiationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								msg = this.userName + " says:" + msg;
								sendAllMessage(msg);
							}
						}
					}
				} else {
					bReader.close();
					socket.close();
				}
			} catch (SocketException e) {
				//e.printStackTrace();
				try {
					clientMap.remove(this.userName);
					bReader.close();
					pWriter.close();
					socket.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					//e2.printStackTrace();
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}

		}

		private boolean initUserName() throws IOException {
			sendPersonalMessage("UserName:", this.socket);
			int countDown = chatServer.loginTryCount;
			String userName = null;
			while (countDown > 0) {
				if ((userName = bReader.readLine()) != null) {
					if (userName.matches(chatServer.userNamePattern)) {
						if (clientMap.get(userName) == null) {
							this.userName = userName;
							clientMap.put(this.userName, this.socket);
							StringBuffer sb = new StringBuffer();
							for(String msg:this.chatServer.msgQueue){
								sb.append(msg).append("\n");
							}
							sendPersonalMessage(sb.toString(),this.socket);
							sendPersonalMessage("Welcome, ".concat(userName),
									this.socket);
							
							sendAllMessage(userName
									.concat(" has entered the chat."));
							return true;
						} else {
							sendPersonalMessage(
									userName.concat(" already exists. Try another."),
									this.socket);
						}
					} else {
						if (countDown == 1) {
							sendPersonalMessage("Bye.", this.socket);
						} else {
							sendPersonalMessage(
									"UserName must match regex pattern "
											.concat(chatServer.userNamePattern)
											.concat(". Try again."),
									this.socket);
						}
					}
					countDown--;
				}
			}
			return false;
		}
	}
}
