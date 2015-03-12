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
    	
    	// stays in the loop to enqueue incoming packets
    	Packet packetFromPeer = null;
        try {
			while (true) {
				
					packetFromPeer = (Packet) _inStream.readObject();
					synchronized(this){
						LocalClient._eventQ.offer(packetFromPeer);
						if (packetFromPeer.seqNumber > LocalClient._curSeqNumber )
			    		{
			    			LocalClient._curSeqNumber = packetFromPeer.seqNumber;
			    		}
				}
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
