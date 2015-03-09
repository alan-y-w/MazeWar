//package notyetdistributed.lab1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;

/**
 * Created by Suya on 2015-02-12.
 */
public class ClientReceive implements Runnable {
    private Thread _t;
    private ObjectInputStream _inStream;
    private ObjectOutputStream _outStream;
    private Queue<Packet> _eventQ;
    private String _myName;

    public ClientReceive(ObjectInputStream _inStream, ObjectOutputStream _outStream, Queue<Packet> _eventQ, String _name) {
        this._eventQ = _eventQ;
        this._inStream = _inStream;
        this._outStream = _outStream;
        this._myName = _name;
        System.out.println("Created new Thread to listen to Server");
    }

    public void run() {
    	// send my own name
    	System.out.println("Send init packet to Server: "+ this._myName);
    	try {
    		Packet myPacket = LocalClient.GetSequenceNumber(new Packet(this._myName, ClientEvent.init));
			System.out.println("Obtain seq number: "+ myPacket.seqNumber);
    		_outStream.writeObject(myPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	// enqueue packets
    	Packet packetFromPeer = null;
        try {
			while ((packetFromPeer = (Packet) _inStream.readObject()) != null) {
			    // System.out.println("Received from Server: " + packetFromServer.GetClientEvent().GetEventCode());
			    System.out.println("Received a packet!");
				this._eventQ.offer(packetFromPeer);
			}
		} catch (ClassNotFoundException | IOException e) {
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
