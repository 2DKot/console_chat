package juz.chat;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import juz.chat.ChatServer.MsgQueueHandler;

public class ChatBot {

	public ChatBot() throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		// TODO Auto-generated constructor stub
		ExecutorService execSvc = Executors.newCachedThreadPool();
		Properties props = new Properties();
		//props.loadFromXML(new FileInputStream("ChatBot.xml"));
		props.loadFromXML(this.getClass().getClassLoader().getResourceAsStream("ChatBot.xml"));
		
		int msgCount=Integer.parseInt(props.getProperty("msgCount"));
		int botCount=Integer.parseInt(props.getProperty("botCount"));
		long delay = Long.parseLong(props.getProperty("delay"));
		
		InputStream inStream = null;
		PrintStream pStream = System.out;
		for(int botNum=1;botNum<=botCount;botNum++){
			
			inStream = PrepareInputStream(msgCount,botNum);
			try {
				execSvc.execute(new ChatClient(inStream, pStream, delay));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		execSvc.shutdown();		
	}
	
	public InputStream PrepareInputStream(int msgCount, int botNum){
		StringBuffer sb = new StringBuffer("bot" + botNum).append("\r");
		for (int i = 1; i <= msgCount; i++) {
			sb.append("bot" + botNum + "_phrase_" + i).append("\r");
		}
		sb.append("exit\r");
		return new ByteArrayInputStream(sb.toString().getBytes());
	}
	
	public static void main(String[] args) throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		new ChatBot();
	}

}

