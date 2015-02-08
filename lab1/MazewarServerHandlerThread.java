import java.net.*;
import java.io.*;

public class MazewarServerHandlerThread extends Thread {
	private Socket socket = null;
	private Packet currPacket = new Packet();

	public MazewarServerHandlerThread(Socket socket) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}

	public void run() {

		boolean gotByePacket = false;
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			Packet packetFromClient;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			

			while (( packetFromClient = (Packet) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				//Packet packetToClient = new Packet();
				//packetToClient.type = Packet.ECHO_REPLY;
				
				/* process message */
				/* just echo in this example */
				/*if(packetFromClient.type == Packet.ECHO_REQUEST) {
					packetToClient.message = packetFromClient.message;
					System.out.println("From Client: " + packetFromClient.message);

					// send reply back to client
					toClient.writeObject(packetToClient);
					
					// wait for next packet
					continue;
				}*/
				System.out.println("From Client: " + packetFromClient.GetClientEvent().GetEventCode());
				toClient.writeObject(packetFromClient);

				// wait for next packet
				continue;
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				/*if (packetFromClient.type == Packet.ECHO_NULL || packetFromClient.type == Packet.ECHO_BYE) {
					gotByePacket = true;
					packetToClient = new Packet();
					packetToClient.type = Packet.ECHO_BYE;
					packetToClient.message = "Bye!";
					toClient.writeObject(packetToClient);
					break;
				}*/
			}
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}

	//abstract private receivePacket()
}
