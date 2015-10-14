package juz.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import juz.chat.ChatServer;

public class CommandFactory {
	private static Properties commandMap = new Properties();
	
	public static Properties getCommandMap() {
		return commandMap;
	}

	public static void initCommandMap(String fileName) throws InvalidPropertiesFormatException, FileNotFoundException, IOException{
		commandMap.loadFromXML(CommandFactory.class.getClassLoader().getResourceAsStream(fileName));
		//commandMap.loadFromXML(new FileInputStream(fileName));
	}
		
	public static IChatServerCommand getCommand(String cmdName, ChatServer srv) throws InstantiationException, IllegalAccessException{
		Class c;
		try {
			String className;
			
			if((className = commandMap.getProperty(cmdName))!=null){
				c = Class.forName(className);
				if (c != null) {
					return (IChatServerCommand) c.newInstance();
				} else {
					return new NotFoundCommand();
				}
			}
			return new NotFoundCommand();
		} catch (ClassNotFoundException e) {
			return new NotFoundCommand();
		}
	}
}