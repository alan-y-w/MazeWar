import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


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
		
		private Socket _clientSocket;
		private ObjectInputStream _inputStream;	
		private ObjectOutputStream _outputStream;
		private Thread _t;
        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name) {
                super(name);
                assert(name != null);
                this._clientSocket = null;
                this._inputStream = null;
                this._outputStream = null;
        }
        

        /**
         * Fill in here??
         * TODO: Add code to connect to the server, send packet and disconnect
         */
        public void ConnectToServer(String hostName, int portNumber)
        {
        	 try {
        			 	this._clientSocket = new Socket(hostName, portNumber);
        			 	this._outputStream = new ObjectOutputStream(this._clientSocket.getOutputStream());
        	         	this._inputStream = new ObjectInputStream(this._clientSocket.getInputStream());
        	         	
        	        } catch (UnknownHostException e) {
        	            System.err.println("Don't know about host " + hostName);
        	            System.exit(1);
        	        } catch (IOException e) {
        	            System.err.println("Couldn't get I/O for the connection to " +
        	                hostName);
        	            System.exit(1);
        	        } 
        }
        
        public void SendPacket(Packet packet)
        {
        	try {
        		this._outputStream.writeObject(packet);
        	} catch (IOException e) {
        		System.err.println("Cannot send packet to server");
        		e.printStackTrace();
        	}
        }
        
        public void run()
        {	
        	Packet packet = null;
        	//alanwu: TODO: listen to the server packets and pass moves to 
        	// itself as well as other Remote clients
        	while (true)
        	{
        		try {
        			packet = (Packet) this._inputStream.readObject();
        		} catch (ClassNotFoundException cn) {
                    cn.printStackTrace();
                } catch (IOException e) {
					e.printStackTrace();
				}
        		
        		// enQ
        		
        		// implement it to the right client
        		if (packet != null)
        		{
        			Client _client = (Client) Client.DictOfClients.get(packet.GetName());
        			switch (packet.GetEvent())
        			{	
        			case FORWARD:
        				_client.forward();
        				break;
        			case BACKWARD:
        				_client.backup();
        				break;
        			case TURNLEFT:
        				_client.turnLeft();
        				break;
        			case TURNRIGHT:
        				_client.turnRight();
        				break;
        			case FIRE:
        				_client.fire();
        				break;
					default:
						break;
        				
        			}
        		}
        		
        		
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
