package ec.edu.ups.server;

import ec.edu.ups.cliente.ChatClienteInterface;

public class Chat {
	
	public String name;
	public ChatClienteInterface client;
	
	//constructor
	public Chat(String name, ChatClienteInterface client){
		this.name = name;
		this.client = client;
	}

	
	//getters and setters
	public String getName(){
		return name;
	}
	public ChatClienteInterface getClient(){
		return client;
	}
}
