import java.io.Serializable;

public class Packet implements Serializable{
	//TODO: implement ECHO packet, similar to lab 1
	
	// ID of the client
	private String _name;
	
	// event of the package
	public static enum Event
	{	
		// TODO: add projectile moves
		QUIT, FORWARD, BACKWARD, TURNLEFT, TURNRIGHT, FIRE	
		
	}
	private Event _event; 
	
	public Packet()
	{
		this._event = null;
		this._name = "";
	}
	
	public Packet(String name, Event event)
	{
		this._event = event;
		this._name = name;
	}
	
	public Event GetEvent()
	{
		return this._event;
	}
	
	public String GetName()
	{
		return this._name;
	}
}
