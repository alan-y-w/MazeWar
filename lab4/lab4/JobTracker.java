import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class JobTracker implements Runnable {
	
	String myPath = "/tracker";
    ZkConnector zkc;
    Watcher watcher;
    static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(16);
    
    public static int portNum = 4555;
	static String myIP;
    
	private Thread t;
	
    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
      
        if (args.length != 1) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. Test zkServer:clientPort");
            return;
        }

        JobTracker jobtracker = new JobTracker(args[0]);   
        JobTracker.myIP = Inet4Address.getLocalHost().getHostAddress();

        jobtracker.checkpath(myIP.getBytes());
        // do stuff
        
        System.out.println("Waiting for EnQ");
        while (true) {
        	String data = queue.take();
        	System.out.println("DeQ: " + data);
        }
    }

    public JobTracker(String hosts) {
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }
 
        watcher = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                                handleEvent(event);
                        
                            } };
    }
    
    private void checkpath(byte[] data) {
        Stat stat = zkc.exists(myPath, watcher);
        if (stat == null) {              // znode doesn't exist; let's try creating it
            System.out.println("Creating " + myPath);
            Code ret = zkc.create(
                        myPath,         // Path of znode
                        data,           // Data needed.
                        CreateMode.EPHEMERAL  // Znode type, set to EPHEMERAL.
                        );
            if (ret == Code.OK) System.out.println("***the boss!***");
            
            // now this is the primary, run as server
            this.start();
        }
    }

    private void handleEvent(WatchedEvent event) {
    	
        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(myPath)) {
            if (type == EventType.NodeDeleted) {
                System.out.println(myPath + " deleted! Let's go!");
                checkpath(myIP.getBytes()); // try to become the boss
            }
            if ((type == EventType.NodeCreated) ) {
                System.out.println(myPath + " created/changed!");     
                
                // alanwu: update local copy of the state data
                try {
					byte[] data =  zkc.read(myPath);
					myIP = new String(data);
					
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                //try{ Thread.sleep(5000); } catch (Exception e) {}
                checkpath(myIP.getBytes()); // re-enable the watch
            }
            
            if (type == EventType.NodeDataChanged)
            {
            	// re-enable the watch
            	zkc.exists(myPath, watcher);
            }
            
        }
    }

	@Override
	// handles client connections
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(portNum);
			System.out.println("Server at " + portNum);
			while (true) {
	        	Socket new_socket = serverSocket.accept();
	        	System.out.println("Connection with client made!");
	        	ObjectInputStream in_stream = new ObjectInputStream(new_socket.getInputStream());
	        	ObjectOutputStream out_stream = new ObjectOutputStream(new_socket.getOutputStream());

	        	new JobTrackerClientHandle(in_stream, out_stream, JobTracker.queue).start();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start ()
	{
		if (t == null)
		{
			t = new Thread (this);
			t.start ();
		}
	}
}