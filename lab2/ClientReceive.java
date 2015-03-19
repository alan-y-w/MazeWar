//package notyetdistributed.lab1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Queue;

/**
 * Created by Suya on 2015-02-12.
 */
public class ClientReceive implements Runnable {
    private Thread _t;
    private ObjectInputStream _inStream;
    public static Hashtable<ObjectInputStream, String> DictOfNameOutStreams = new Hashtable<ObjectInputStream ,String>();
	private String PeerName = null;
    public ClientReceive(ObjectInputStream _inStream ) {
        this._inStream = _inStream;
//        System.out.println("Created new Thread to listen to Server");
    }

    public void run() {
    	// stays in the loop to enqueue incoming packets
    	Packet packetFromPeer = null;
        try {
        	synchronized(this){
				while ((packetFromPeer = (Packet) _inStream.readObject()) != null) {
						LocalClient._eventQ.offer(packetFromPeer);
						
						if (PeerName == null)
							PeerName = packetFromPeer.GetName();
						//if (!ClientReceive.DictOfNameOutStreams.containsKey(_inStream))
						//{
							//ClientReceive.DictOfNameOutStreams.put(_inStream, packetFromPeer.GetName());
						//}
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("ClientReceive Exception!");
			//String clientName = ClientReceive.DictOfNameOutStreams(_inStream);
			Client clientToRemove = Client.DictOfClients.get(PeerName);
			clientToRemove.maze.removeClient(clientToRemove);
			Client.DictOfClients.remove(PeerName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
