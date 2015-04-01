import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class ClientDriver implements Runnable  {
	ZkConnector zkc;
    Watcher watcher;
    static String myPath = "/tracker";
    static String trackerIP = null;
    
    Socket _clientSocket = null; 
    ObjectOutputStream _outputStream = null; 
    ObjectInputStream _inputStream = null;
    
    private Thread t;
    
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException
    {
    	// connect to zookeeper
    	if (args.length != 1) {
            System.out.println("Usage: java -classpath lib/zookeeper-3.3.2.jar:lib/log4j-1.2.15.jar:. Test zkServer:clientPort");
            return;
        }
    	
    	// handle user requests
    	ClientDriver client = new ClientDriver(args[0]);
    	client.checkUpdate();
    	client.start();
    }
    
    private void processInput(String userInput)
    {
    	if (userInput.equalsIgnoreCase("status"))
    	{
    		// check status
    	}
    	else
    	{
    		// comm with job tracker
    		try {
				this._outputStream.writeObject(userInput);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public ClientDriver(String hosts) throws UnknownHostException, KeeperException, InterruptedException {
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
    
    private void checkUpdate() {
        Stat stat = zkc.exists(myPath, watcher);
        if (stat != null) {              // znode does exist; read data
        	
        	byte[] data = null;
			try {
				data = zkc.read(myPath);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	String tempIP = new String(data);
        	
        	// IP changes, reconnect
    		trackerIP = tempIP;
    		System.out.println("Connecting to tracker IP: "+ trackerIP);
    		
    		// connect to the server
    		this.connectToServer(trackerIP, JobTracker.portNum);
        } 
    }
    
    private void connectToServer(String hostName, int portNumber)
    {
    	try {
    		if (_clientSocket!= null)
    		{
    			_clientSocket.close();
    		}
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

    private void handleEvent(WatchedEvent event) {
        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(myPath)) {
            if (type == EventType.NodeDeleted) {
                System.out.println(myPath + " deleted! Let's go!");       
                checkUpdate(); // try to connect
            }
            if ((type == EventType.NodeCreated)) {
                System.out.println(myPath + " created!");       
                //try{ Thread.sleep(5000); } catch (Exception e) {}
                checkUpdate(); // re-enable the watch
            }
            if ((type == EventType.NodeDataChanged))
            {
            	// update data no need to reconnect
            }
        }
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true)
    	{
    		System.out.print(">> ");
    		
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		 
		    String userInput = null;
		    
		    try {
				userInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    System.out.println(userInput);
		    
		    this.processInput(userInput);
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
