//package notyetdistributed.lab1;

import java.io.Serializable;

public class Packet implements Serializable{
	//TODO: implement ECHO packet, similar to lab 1
	
	// ID of the client
	private String _name;
	
	// Sequence number
	public long seqNumber = -1;
	
	// event of the package

	private ClientEvent _event; 
	
	public Packet()
	{
		this._event = null;
		this._name = "";
	}
	
	public Packet(String name, ClientEvent event)
	{
		this._event = event;
		this._name = name;
	}
	
	public ClientEvent GetClientEvent()
	{
		return this._event;
	}
	
	// Return the ID of the packet to/from client 
	public String GetName()
	{
		return this._name;
	}
}
