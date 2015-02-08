import java.net.*;
import java.io.*;
import java.util;

public class MazewarServer {

    //enqueue - multithreaded since
    //many clients
//dequeue and broadcast

    int portNum = 4555;
    String hostName = "localhost"
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        /*try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }*/

        serverSocket = new ServerSocket(hostName, portNum);
        while (listening) {
        	new MazewarServerHandlerThread(serverSocket.accept()).start();
        }

        serverSocket.close();
    }
}
