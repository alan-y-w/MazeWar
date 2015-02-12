//package notyetdistributed.lab1;
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
			
			// TODO: Get initialization packet from the clients
			do
			{
				packetFromClient = (Packet) this._inStream.readObject();
			} while ((packetFromClient!=null) && 
					(packetFromClient.GetClientEvent().GetEventCode() != ClientEvent.init.GetEventCode()));
			
			System.out.println("Get init packet from Client!");
			MazewarServer._listNames.add(packetFromClient.GetName());

			packetFromClient = null;
			
			// Looping to pull for new packets
			while (( packetFromClient = (Packet) this._inStream.readObject()) != null) {

				System.out.println("From Client: " + packetFromClient.GetClientEvent().GetEventCode());

				MazewarServer._eventQ.offer(packetFromClient);

				/*if (MazewarServer._eventQ.peek() != null) {
					System.out.println("Top of the Q: " + MazewarServer._eventQ.peek().GetClientEvent().GetEventCode());
				}*/
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
}
