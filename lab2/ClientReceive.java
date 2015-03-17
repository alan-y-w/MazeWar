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

    public ClientReceive(ObjectInputStream _inStream ) {
        this._inStream = _inStream;
//        System.out.println("Created new Thread to listen to Server");
    }

    public void run() {
    	// stays in the loop to enqueue incoming packets
    	Packet packetFromPeer = null;
        try {
			while (true) {
				
				packetFromPeer = (Packet) _inStream.readObject();
				synchronized(this){
					LocalClient._eventQ.offer(packetFromPeer);
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
