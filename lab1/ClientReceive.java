package notyetdistributed.lab1;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by Suya on 2015-02-12.
 */
public class ClientReceive extends LocalClient implements Runnable {
    private ObjectInputStream _inputStream = null;
    private Thread _t;

    public ClientReceive(ObjectInputStream _inputStream, String name) {
        super(name);
        this._inputStream = _inputStream;
        System.out.println("Created new Thread to listen to Server");
    }

    public void run() {
        Packet packetFromServer = null;

        try {
            while ((packetFromServer = (Packet) this._inputStream.readObject()) != null) {
                System.out.println("Received from Server: " + packetFromServer.GetClientEvent().GetEventCode());
                LocalClient._eventQ.offer(packetFromServer);
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
