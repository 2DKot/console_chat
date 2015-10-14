package juz.commands;

import juz.chat.ChatServer;

public class NotFoundCommand implements IChatServerCommand{
	@Override
	public String doIt(String cmd, ChatServer srv) {
		// TODO Auto-generated method stub
		return "Unknown command ".concat(cmd);
	}
}
