//package notyetdistributed.lab1;

import java.io.IOException;
import java.io.ObjectOutputStream;


public class MazewarServerBroadcast extends MazewarServer implements Runnable{

	private Thread _t, _missileTickThread;
	private Packet _currPacket; 
	//private final Runnable _missileTickThread;
	
	public MazewarServerBroadcast()
	{
		//alanwu: nothing to do here
		/*_missileTickThread = new Runnable() {
			private void run() {
				MazewarServerBroadcast.this.missileTick();
			}
		}*/
	}
	
	public void run() {
		// TODO Auto-generated method stub
		
		_missileTickThread = new Thread(
				new Runnable() {
					public void run() {
						Packet _missilePacket;
						
						_missilePacket = new Packet(null, ClientEvent.missileTick);
						
						try {
				            while (true) {
				                _broadCastPacket(_missilePacket);
				                //System.out.print("sending tick " + missileTickPacket.GetClientEvent().GetEventCode());
				                Thread.sleep(200);
				            }
				        }
				        catch (Exception e) {
				            // shouldn't happen
				        }
					}
				});
			
			
		
		while (true)
		{
			_currPacket = MazewarServer._eventQ.poll();
			_broadCastPacket(_currPacket);
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
	
	public void startMissileTickThread() {
		_missileTickThread.start();
	}
	
	private void _broadCastPacket(Packet _currPacket)
	{
		if (_currPacket!=null)
		{
			for (ObjectOutputStream stm : MazewarServer._listOutputs)
			{
				try {
					//System.out.println("broadcasting to: " + _currPacket.GetName());
					stm.writeObject(_currPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
