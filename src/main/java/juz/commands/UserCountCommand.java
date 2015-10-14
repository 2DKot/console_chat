package juz.commands;

import juz.chat.ChatServer;

public class UserCountCommand implements IChatServerCommand{

	@Override
	public String doIt(String cmd, ChatServer srv) {
		// TODO Auto-generated method stub
		return "User count: ".concat(Integer.toString(srv.getClientMap().size()));
	}
	
}
