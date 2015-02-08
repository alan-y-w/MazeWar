import java.io.Serializable;

public class Packet implements Serializable{
	//TODO: implement ECHO packet, similar to lab 1
	
	// ID of the client
	private String _name;
	
	// event of the package
	public enum Event
	{	
		// TODO: add projectile moves
		QUIT(0), FORWARD(1), BACKWARD(2), TURNLEFT(3), TURNRIGHT(4), FIRE(5);
		
		private int _val;
		
		private Event(int value) {
			this._val = value;
		}
		
		public String toString(){
		       return Integer.toString(this._val);
		}
		
//		public Event StringtoEvent(String str)
//		{
//			return new Event()
//		}
		
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
}
