import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class SequencerReceive implements Runnable {
	private Thread _t;
	private ObjectInputStream _inStream;
    private ObjectOutputStream _outStream;
    
    public SequencerReceive(ObjectInputStream _inStream, ObjectOutputStream _outStream)
    {
    	this._inStream = _inStream;
    	this._outStream = _outStream;
    }
    
    public void run() {
    	Packet packetFromPeer = null;
    	try {
			while ((packetFromPeer = (Packet) _inStream.readObject()) != null) {
				if (packetFromPeer.GetClientEvent().GetEventCode() ==5)
				{
					packetFromPeer.seqNumber = Sequencer.Get();
				}
				else
				{
					packetFromPeer.seqNumber = Sequencer.Add();
				}
			    System.out.println("Assign Sequence Number: " +  packetFromPeer.seqNumber);
			    _outStream.writeObject(packetFromPeer);
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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

