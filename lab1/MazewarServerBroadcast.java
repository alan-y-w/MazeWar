import java.io.IOException;
import java.io.ObjectOutputStream;


public class MazewarServerBroadcast extends MazewarServer implements Runnable{

	private Thread _t;
	private Packet _currPacket;
	
	public MazewarServerBroadcast()
	{
		//alanwu: nothing to do here
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while ((_currPacket = MazewarServer._eventQ.poll()) != null)
		{
			for (ObjectOutputStream stm : MazewarServer._listOutputs)
			{
				try {
					stm.writeObject(_currPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
