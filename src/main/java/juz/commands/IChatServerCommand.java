package juz.commands;

import juz.chat.ChatServer;

public interface IChatServerCommand {
	public String doIt(String cmd, ChatServer srv);
}
