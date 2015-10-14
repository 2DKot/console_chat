package juz.commands;

import juz.chat.ChatServer;

public class UserListCommand implements IChatServerCommand {

	@Override
	public String doIt(String cmd, ChatServer srv) {
		StringBuilder sb = new StringBuilder("User list:\n");
		for (String user : srv.getClientMap().keySet()) {
			sb.append(user).append("\n");
		}
		return sb.toString();
	}
}
