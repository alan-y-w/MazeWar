//package notyetdistributed.lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


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
		//private Vector<ObjectInputStream> _peerInputStreamList = new Vector<ObjectInputStream>();
		private Vector<ObjectOutputStream> _peerOutputStreamList = new Vector<ObjectOutputStream>();
		private static LinkedBlockingQueue<String> _peerNames = new LinkedBlockingQueue<String>(); 
		public static Hashtable<Integer, ObjectOutputStream> DictOfOutStreams = new Hashtable<Integer, ObjectOutputStream>();
		public static Hashtable<Integer, ObjectInputStream> DictOfInStreams = new Hashtable<Integer, ObjectInputStream>();
		public static Hashtable<Integer, Socket> DictOfSockets = new Hashtable<Integer, Socket>();
		protected static PriorityBlockingQueue<Packet> _eventQ = new PriorityBlockingQueue<Packet>(10, new PacketComparator());
		private static Maze maze;
		private static ObjectInputStream _seqInStream;
		private static ObjectOutputStream _seqOutStream;
		private static Socket seqSocket;
		private static int _seqPortNum = 4444;
	    private static String _seqHostName = "128.100.13.61";
	    public static long _curSeqNumber = -1;
	    public static DirectedPoint InitPoint;
	    public static int InitScore;
	    public int myPortNum = 0;
	    public static boolean GuiReadyFlag = false;
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
                	LocalClient.seqSocket = new Socket(_seqHostName, _seqPortNum);
					LocalClient._seqOutStream = new ObjectOutputStream (seqSocket.getOutputStream());
					LocalClient._seqInStream = new ObjectInputStream(seqSocket.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Can't connect to Sequencer Service!");
				}
        }
        
        public void SendPacket(Packet packet)
        {
        	// get a sequence number
        	packet = LocalClient.GetSequenceNumber(packet);
    		
        	// put it on my own Q
        	synchronized(this)
        	{
	        	LocalClient._eventQ.offer(packet);
        	}
        	// broadcast it
        	BroadCastToPeers(packet);
        }
        
        public void BroadCastToPeers(Packet packet)
        {
        	ObjectOutputStream outRemove = null;
        	synchronized(this)
        	{
	        	for (ObjectOutputStream out : _peerOutputStreamList)
	        	{
	        		try {
						out.writeObject(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("Cannot broadcast");
						outRemove = out;
					}
	        	}
	        	
	        	if (outRemove != null)
	        	{
	        		_peerOutputStreamList.remove(outRemove);
	        	}
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
        	    	int i;
                	for ( i = 0; i < ports.length; i++) {
                        try {
                        	Socket clientSocket;
							
							ObjectOutputStream _out ;
                    		ObjectInputStream _in ;

							clientSocket = new Socket(hostnames[i], ports[i]);
							System.out.println("-->Connected as client! port: " +  ports[i]);
							 _out = new ObjectOutputStream (clientSocket.getOutputStream());
							 _in = new ObjectInputStream(clientSocket.getInputStream());
							_peerOutputStreamList.add(_out);
							
							DictOfOutStreams.put(ports[i], _out);
                    		DictOfInStreams.put(ports[i], _in);
                    		DictOfSockets.put(ports[i], clientSocket);
                    		
                        	// start a new thread to handle new peer
							InitHandShake(_out);
                        	new ClientReceive(_in).start();

                        } catch (IOException ex) {
                        	
							break;
							
                        }
                        ObjectOutputStream _out ;
                    }
                	
                	if (i < ports.length)
            		{
                		try {
                			_serverSocket = new ServerSocket(ports[i]);
	                	
	                		// loop to listen to incoming new client requests
	                		// this thread will stay in this loop
	                		System.out.println("Server waiting on port" + ports[i]);
	                		ObjectOutputStream _out ;
	                		ObjectInputStream _in ;
	                		Socket serverSocket ;
	                		while (i < ports.length)
	                		{
	                			serverSocket = _serverSocket.accept();
	    	                	System.out.println("Server accepted! port: " + ports[i]);
	    	                	 _out = new ObjectOutputStream (serverSocket.getOutputStream());
								 _in = new ObjectInputStream(serverSocket.getInputStream());
	    	                	 _peerOutputStreamList.add(_out);
		                		DictOfOutStreams.put(ports[i], _out);
		                		DictOfInStreams.put(ports[i], _in);
		                		DictOfSockets.put(ports[i], serverSocket);
		                		myPortNum = ports[i];
			    				// start a new thread to handle new peer
		                		InitHandShake(_out);
			    				new ClientReceive(_in).start();
			    				
			    				// if I'm the first peer starting the game, I become the one 
		                    	// sends missile ticks
		                    	if (ports[i] == 4555)
		                    	{
		                    		MissleTickStart();
		                    	}
		                    	
			    				i ++;
	                		}
	                		
                    	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
                		
                	}
        	    }
        	};
        	thread.setName(this.getName());
        	thread.start();
        }
        
        public void run() {
        	Packet packet = null;
        	
        	while (true)
        	{
        		// lock the Q access
    			synchronized(this){
    				if (_eventQ.size() == 0)// the Q is empty, sleep and wait for more requests
	        		{
    					continue;
	        		}
    				
    				else if ( (_eventQ.peek() != null) && (_eventQ.peek().GetClientEvent().GetEventCode() == 5))
    				{
    					packet = _eventQ.poll();
    					// init packet doesn't need to be ordered
						// therefore do not increment the _curSeqNumber
						String name = packet.GetName();
						
						// if not already added as a client
						if (!name.equals(this.getName()))
						{
							System.out.println("creating remote client: " + name);
							LocalClient.maze.addClientToPoint(new RemoteClient(name), packet.point, packet.score);
						}
						else 
						{
							// create the gui client
							if (!LocalClient.maze.isClientAdded(this))
							{
								System.out.println("creating GUI client: " + this.getName());
								LocalClient.maze.addClientToPoint((GUIClient)this, LocalClient.InitPoint, 0);
								LocalClient.GuiReadyFlag = true;
							}
						}
						
						// reset the RandGen to be consistent
						LocalClient.maze.ResetRandGen();
    				}
    				// "==" condition check ensures _curSeqNumber goes up without gaps
    				else if ( (_eventQ.peek() != null) && (_eventQ.peek().seqNumber == _curSeqNumber))
	        		{
    					
//    					System.out.println("Curent Seq Num: "+_curSeqNumber);
//	        			System.out.println("Packet Seq Num: " +_eventQ.peek().seqNumber);
	        			
						packet = _eventQ.poll();
		        		if (packet != null) {
		        			int eventCode = packet.GetClientEvent().GetEventCode();
		        			if (eventCode != 5)
		        			{
		        				_curSeqNumber++;
								// implement it to the right client
			        			if (Client.DictOfClients.containsKey(packet.GetName()))
			        			{
			        				ImplementPacket(packet, eventCode);
			        			}
		        			}
							else
							{
								
							}
						
		        		}
	        		}
	        		else
	        		{
	        			System.out.println("Seq Number out of order!!!");
	        			System.out.println("->Packet Seq Num: " +_eventQ.peek().seqNumber);
	        			System.out.println("->Current Seq Num: "+  _curSeqNumber);
	        			try {
	        				// earlier pacekts not yet arrived, wait a bit
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//break;
	        		}
        		} // end of synchronized block
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

		private void InitHandShake(ObjectOutputStream _outStream)
		{
			// send my own name
			// run this only when initializing
//			System.out.println("Send init packet to Server: "+ this.getName());
			try {
		    		Packet myPacket = LocalClient.GetSequenceNumber(new Packet(this.getName(), ClientEvent.init));
//		    		System.out.println("Obtain seq number: "+ myPacket.seqNumber);
		    		// generate the init location
		    		if (LocalClient.maze.isClientAdded(this))
		    		{
		    			myPacket.point = (DirectedPoint) this.getPoint();
		    		}
		    		else
		    		{
			    		myPacket.point = LocalClient.maze.getInitClientPoint(this);
			    		InitPoint = myPacket.point;
		    		}
		    		
		    		myPacket.score = Client.scoreTable.getScore(this);
		    		
		    		_outStream.writeObject(myPacket);
		    		// put myself on the Q
		    		synchronized(this)
		    		{
		    			LocalClient._eventQ.offer(myPacket);
		    			// take the largest init packet's sequence number as my initial sequence number
			    		if ((myPacket.GetClientEvent().GetEventCode() == 5) && (LocalClient._curSeqNumber == -1))
			    		{
			    			LocalClient._curSeqNumber = myPacket.seqNumber;
			    		}
		    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
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
				_client.maze.shootMissile();
				break;
			case 7: // quit
				synchronized(this)
				{
					_peerOutputStreamList.remove(Client.DictOfClients.get(packet.GetName()));
					_client.maze.removeClient(Client.DictOfClients.get(packet.GetName()));
					Client.DictOfClients.remove(packet.GetName());
//					int portNum = packet.score;
//					try {
//						//DictOfOutStreams.get(portNum).close();
//						//DictOfInStreams.get(portNum).close();
//						//DictOfSockets.get(portNum).close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						//e.printStackTrace();
//					}
				}
				
//				for(String _clientName: _clientNames) {
//					Client.DictOfClients.get(_clientName).missileTick();
//					System.out.print("Client got missile tick");
//				}
				break;
			default:
				break;
				
			}
		}

		private void MissleTickStart()
		{
			Thread thread = new Thread()
			{
				public void run(){
					try {
						Thread.sleep(2000);
			            while (true) {
		            		SendPacket(new Packet(this.getName(), ClientEvent.missileTick));
			            	//System.out.print("sending tick " + missileTickPacket.GetClientEvent().GetEventCode());
			                Thread.sleep(200);
			            }
			        }
			        catch (Exception e) {
			            // shouldn't happen
			        }
				}
			};
			thread.start();
			thread.setName(this.getName());
		}
		
		public void ClientExit()
		{
			try {
				DictOfSockets.get(myPortNum).close();
				
				LocalClient._seqOutStream.close(); 
				LocalClient._seqInStream.close();
				LocalClient.seqSocket.close();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
}

