import java.net.*;
import java.io.*;

public class MazewarServerReceive extends MazewarServer implements Runnable {
	private ObjectInputStream _inStream = null;
	private Thread _t;
	
	public MazewarServerReceive(ObjectInputStream in_stream) {
		this._inStream = in_stream;
		System.out.println("Created new Thread to handle client");
	}

	public void run() {

		boolean gotByePacket = false;
		
		try {
			/* stream to read from client */
			Packet packetFromClient;
			
			/* stream to write back to client */
			
			while (( packetFromClient = (Packet) this._inStream.readObject()) != null) {

				System.out.println("From Client: " + packetFromClient.GetClientEvent().GetEventCode());
				
				// alanwu: write to Q
				MazewarServer._eventQ.offer(packetFromClient);
			}
			
		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
	
	public void start ()
    {
		if (_t == null)
		{
		
			_t = new Thread (this);
			_t.start ();
		}
    }

	//abstract private receivePacket()
}
