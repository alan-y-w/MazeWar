package notyetdistributed.lab1;

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
    
    // alanwu: keep track of client names for multiplayer game initialization
    protected static ArrayList<String> _listNames =  (new ArrayList());
    
    public static void main(String[] args) throws IOException 
    {
        ServerSocket serverSocket = null;
        int num_players = 0;
        int player_count = 0;
        
        if (args.length ==  0)
        {
        	System.out.println("ERROR: player number not entered");
        	return;
        }
        else
        {
        	num_players = Integer.parseInt(args[0]);
	        
	        System.out.println("Number of player: " + num_players);
        }
        
        serverSocket = new ServerSocket(portNum);
        
        new MazewarServerBroadcast().start();
        
        while (player_count < num_players) {
        	Socket new_socket = serverSocket.accept();
        	
        	ObjectInputStream in_stream = new ObjectInputStream(new_socket.getInputStream());
        	ObjectOutputStream out_stream = new ObjectOutputStream(new_socket.getOutputStream());
        	
        	_listInputs.add(in_stream);
        	_listOutputs.add(out_stream);
        	new MazewarServerReceive(in_stream).start();
        	player_count += 1;
        }

        // alanwu: TODO: when there is a key input, send client names to all client
        // let client to send an init packet
        while (true)
        {
	        try {
	        	// sleep this thread to let other thread run and get init packet
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if (_listNames.size() == num_players)
	        {
	        	_broadCastNames();
	        	break;
	        }
        }
        //serverSocket.close();
    }
    
    private static void _broadCastNames()
	{
		for (ObjectOutputStream stm : MazewarServer._listOutputs)
		{
			try {
				System.out.println("broadcasting Names to clients");
				stm.writeObject(_listNames);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
}
