import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Sequencer {
	private static long _current_count;
	private static int portNum = 4444;
    private static String hostName = "localhost";
    private static ServerSocket _serverSocket;
//    private static Vector<ObjectInputStream> _peerInputStreamList = new Vector<ObjectInputStream>();
//	private static Vector<ObjectOutputStream> _peerOutputStreamList = new Vector<ObjectOutputStream>();
//	
	public Sequencer()
	{
		_current_count = -1;
	}
	
	public static synchronized long Add(){
		Sequencer._current_count += 1;
		return Sequencer._current_count;
	}
	
	public static synchronized long Get(){
		return Sequencer._current_count + 1;
	}
	
	public static void main(String[] args) throws IOException 
    {
		_serverSocket = null;
        
        // handle connection requests
//        Thread thread = new Thread(){
//		    public void run(){
		    	try {
					_serverSocket = new ServerSocket(portNum);
					while (true)
			    	{
			    		System.out.println("Server waiting...");
	                	Socket serverSocket = _serverSocket.accept();
	                	System.out.println("Server accepted!");
	                	ObjectOutputStream _out = new ObjectOutputStream (serverSocket.getOutputStream());
						ObjectInputStream _in = new ObjectInputStream(serverSocket.getInputStream());
	                	
					    new SequencerReceive(_in, _out).start();
//						Sequencer._peerOutputStreamList.add(_out);
//						Sequencer._peerInputStreamList.add(_in);
			    	}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//		    }
//	    };
//	    thread.start();
    	    
    }
}
