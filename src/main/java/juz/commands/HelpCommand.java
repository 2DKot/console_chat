package juz.commands;

import javax.activation.CommandMap;

import juz.chat.ChatServer;

public class HelpCommand implements IChatServerCommand{

	@Override
	public String doIt(String cmd, ChatServer srv) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder("Available commands:\n");
		for (Object strCmd : CommandFactory.getCommandMap().keySet() ) {
			sb.append(strCmd).append("\n");
		}
		return sb.toString();
	}
}
