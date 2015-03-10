//package notyetdistributed.lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/*
Copyright (C) 2004 Geoffrey Alan Washburn
    
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
    
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
    
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/

/**
 * An abstract class for {@link Client}s in a {@link Maze} that local to the 
 * computer the game is running upon. You may choose to implement some of 
 * your code for communicating with other implementations by overriding 
 * methods in {@link Client} here to intercept upcalls by {@link GUIClient} and 
 * {@link RobotClient} and generate the appropriate network events.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: LocalClient.java 343 2004-01-24 03:43:45Z geoffw $
 */


public abstract class LocalClient extends Client implements Runnable{	
		// socket to let other clients to connect to
		private ServerSocket _serverSocket;
		private Thread _t;
		// list of sockets for this game instance to connect to as clients
		// for broadcast
		private Vector<ObjectInputStream> _peerInputStreamList = new Vector<ObjectInputStream>();
		private Vector<ObjectOutputStream> _peerOutputStreamList = new Vector<ObjectOutputStream>();
		private static LinkedBlockingQueue<String> _peerNames = new LinkedBlockingQueue<String>(); 
		protected static PriorityQueue<Packet> _eventQ = new PriorityQueue<Packet>(10, new PacketComparator());
		private static Maze maze;
		private static ObjectInputStream _seqInStream;
		private static ObjectOutputStream _seqOutStream;
		private static int _seqPortNum = 4444;
	    private static String _seqHostName = "localhost";
	    public static long _curSeqNumber = -1;
	    
		/** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name, Maze maze) {
                super(name);
                assert(name != null);
                LocalClient.maze = maze;
                
                // connect to the sequencer service
                // temp code
                // TODO: make this distributed
                try {
					Socket seqSocket = new Socket(_seqHostName, _seqPortNum);
					LocalClient._seqOutStream = new ObjectOutputStream (seqSocket.getOutputStream());
					LocalClient._seqInStream = new ObjectInputStream(seqSocket.getInputStream());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Can't connect to Sequencer Service!");
				}
        }
        
        public static Packet GetSequenceNumber(Packet packet)
        {
        	Packet retPacket = null;
        	try {
        		synchronized(LocalClient.class)
        		{
				LocalClient._seqOutStream.writeObject(packet);
				retPacket = (Packet) LocalClient._seqInStream.readObject();
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return retPacket;
        }
        
        // start a thread to handle incoming peer requests
        public void ConnectToPeer(final String [] hostnames, final int[] ports)
        {
        	Thread thread = new Thread(){
        	    public void run(){
        	    	int count = 0;
                	for (int port : ports) {
                        try {
                        	_serverSocket = new ServerSocket(port);
                        	while (true)
                        	{
                        		// loop to listen to incoming new client requests
                        		// this thread will stay in this loop
//                        		System.out.println("Server waiting...");
                        		ObjectOutputStream _out ;
                        		ObjectInputStream _in ;
                        		synchronized(this)
                        		{
        	                	Socket serverSocket = _serverSocket.accept();
//        	                	System.out.println("Server accepted!");
        	                	 _out = new ObjectOutputStream (serverSocket.getOutputStream());
								 _in = new ObjectInputStream(serverSocket.getInputStream());
        	                	
        	                	_peerOutputStreamList.add(_out);
        	    				_peerInputStreamList.add(_in);
                        		}
        	    				
        	    				// start a new thread to handle new peer
        	    				new ClientReceive(_in, _out, LocalClient._eventQ, this.getName()).start();
                        	}

                        } catch (IOException ex) {
                        	// the port is taken on this host, connect to it as client
                        	Socket clientSocket;
							try {
								ObjectOutputStream _out ;
                        		ObjectInputStream _in ;
								synchronized(this)
                        		{
								clientSocket = new Socket(hostnames[count], ports[count]);
								 _out = new ObjectOutputStream (clientSocket.getOutputStream());
								 _in = new ObjectInputStream(clientSocket.getInputStream());
								_peerOutputStreamList.add(_out);
								_peerInputStreamList.add(_in);
	                        	
	                        	System.out.println("Connected as client! port: " +  ports[count]);
                        		}
	                        	// start a new thread to handle new peer
	                        	new ClientReceive(_in, _out, LocalClient._eventQ, this.getName()).start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							count++;
                        	//continue; // try next port
                        }
                    }
        	    }
        	};
        	thread.setName(this.getName());
        	thread.start();
        }
        
        public void run() {
			// poll the event queue and implements actions to player
        	// special cases:
        	// if it's an init packet, add it to the name queue
        	// TODO: if it's a quit packet, remove the client
        	Packet packet = null;
        	
        	while (true)
        	{
    			synchronized(this){
    				if (_eventQ.size() == 0)// the Q is empty, sleep and wait for more requests
	        		{
	        			try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        		}
    				else if (_eventQ.peek().seqNumber == _curSeqNumber)
	        		{
	        			_curSeqNumber++;
						packet = _eventQ.poll();

		        		if (packet != null) {
		        			int eventCode = packet.GetClientEvent().GetEventCode();
		        			if (eventCode != 5)
		        			{
								// implement it to the right client
			        			if (Client.DictOfClients.containsKey(packet.GetName()))
			        			{
			        				ImplementPacket(packet, eventCode);
			        			}
		        			}
							else
							{
								// init packet doesn't need to be ordered
								_curSeqNumber -- ;
								synchronized(this)
								{
								String name = packet.GetName();
								
								// if not already added as a client
								if (!name.equals(this.getName()))
								{
									System.out.println("creating remote client: " + name);
			        				LocalClient.maze.addClient(new RemoteClient(name));
								}
								else 
								{
									// create the gui client
									System.out.println("creating GUI client: " + this.getName());
									LocalClient.maze.addClient((GUIClient)this);
								}
								
								// reset the RandGen to be consistent
								LocalClient.maze.ResetRandGen();
								}
		        				// set position & orientation
		        				// set score
		        				//break;
							}
						
		        		}
	        		}
	        		else
	        		{
	        			System.out.println("Seq Number Wrong!!!");
	        			System.out.println("Q head: "+_eventQ.peek().seqNumber);
	        			System.out.println("Current Seq Num: "+  _curSeqNumber);
	        			//break;
	        		}
        		}
        	}
		}
        
        private void ImplementPacket(Packet packet, int EventCode)
        {
        	Set<String> _clientNames = DictOfClients.keySet();
        	
			Client _client = (Client) Client.DictOfClients.get(packet.GetName());
			switch (EventCode){	
			case 0:
				_client.forward();
				break;
			case 1:
				_client.backup();
				break;
			case 2:
				_client.turnLeft();
				break;
			case 3:
				_client.turnRight();
				break;
			case 4:
				_client.fire();
				break;
			case 6: // missile
				for(String _clientName: _clientNames) {
					Client.DictOfClients.get(_clientName).missileTick();
					//System.out.print("Client got missile tick");
				}
				break;
			default:
				break;
				
        	}
        }
        
        public void start ()
        {
           if (_t == null)
           {
              _t = new Thread (this, "ServerListener");
              _t.start ();
           }
        }
}

