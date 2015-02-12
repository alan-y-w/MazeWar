//package notyetdistributed.lab1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Queue;

/**
 * Created by Suya on 2015-02-12.
 */
public class ClientReceive implements Runnable {
    private Thread _t;
    private ObjectInputStream _inStream;
    private Queue<Packet> _eventQ;

    public ClientReceive(ObjectInputStream _inStream, Queue<Packet> _eventQ) {
        this._eventQ = _eventQ;
        this._inStream = _inStream;
        System.out.println("Created new Thread to listen to Server");
    }

    public void run() {
        Packet packetFromServer = null;

        try {
            while ((packetFromServer = (Packet) _inStream.readObject()) != null) {
               // System.out.println("Received from Server: " + packetFromServer.GetClientEvent().GetEventCode());
                this._eventQ.offer(packetFromServer);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
