import java.io.Serializable;

public class Packet implements Serializable{
	//TODO: implement ECHO packet, similar to lab 1
	
	// ID of the client
	private int _ID;
	
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
		this._ID = 0;
	}
	
	public Packet(int ID, Event event)
	{
		this._event = event;
		this._ID = ID;
	}
	
	public Event GetEvent()
	{
		return this._event;
	}
}
