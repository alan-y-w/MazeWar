import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MazewarServer {
    //enqueue - multithreaded since
    //many clients
	//dequeue and broadcast

    private static int portNum = 4555;
    private String hostName = "localhost";
    
    protected static ArrayList <ObjectInputStream> _listInputs = new ArrayList();
    protected static ArrayList <ObjectOutputStream> _listOutputs = new ArrayList();
    protected static Queue<Packet> _eventQ = new ConcurrentLinkedQueue(); 
    
    public static void main(String[] args) throws IOException 
    {
        ServerSocket serverSocket = null;
        boolean listening = true;

        serverSocket = new ServerSocket(portNum);
        
        new MazewarServerBroadcast().start();
        
        while (listening) {
        	Socket new_socket = serverSocket.accept();
        	
        	ObjectInputStream in_stream = new ObjectInputStream(new_socket.getInputStream());
        	ObjectOutputStream out_stream = new ObjectOutputStream(new_socket.getOutputStream());
        	
        	_listInputs.add(in_stream);
        	_listOutputs.add(out_stream);
        	new MazewarServerReceive(in_stream).start();
        }
        
        

        serverSocket.close();
    }
}
