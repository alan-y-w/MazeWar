import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
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
	String assignPath = "/assign";
	String workerPath = "/worker";
    ZkConnector zkc;
    Watcher watcherElection; // for election
    Watcher watcherWorkerRoot; // for detecting new worker
    Watcher watcherWorker; // for dead worker handle, result collection
    Watcher watcherAssign; // for keeping track of assignment
    
    List<String> workerList;
//    Watcher watcherTask;	// split a job into tasks
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

        // leader election
        jobtracker.checkpath(myIP.getBytes());
        
        // watch the worker list
        jobtracker.checkWorker();
        
        System.out.println("Waiting for EnQ");
        while (true) {
        	String data = queue.take();
        	System.out.println("DeQ: " + data);
        	
        	// split the job into tasks
        	
        	// assign task to the assign list
        	jobtracker.assignTask(data);
        	
        }
    }

    public JobTracker(String hosts) {
        zkc = new ZkConnector();
        try {
            zkc.connect(hosts);
        } catch(Exception e) {
            System.out.println("Zookeeper connect "+ e.getMessage());
        }
 
        watcherElection = new Watcher() { // Anonymous Watcher
                            @Override
                            public void process(WatchedEvent event) {
                            	handleEventElection(event);
                        
                            } };
                            
        watcherWorkerRoot = new Watcher() { // Anonymous Watcher
                                @Override
                                public void process(WatchedEvent event) {
                                	handleEventWorkerRoot(event);
                            
                                	      } };
                                	      
        watcherWorker = new Watcher() { // Anonymous Watcher
				          @Override
				          public void process(WatchedEvent event) {
				          	handleEventWorker(event);
				      
				          	      } };
                                	      
//        watcherAssign	= new Watcher() { // Anonymous Watcher
//					            @Override
//					            public void process(WatchedEvent event) {
//					            	handleEventAssign(event);
//					        
//					            	      } };
    }
    
    private void assignTask(String task) throws KeeperException, InterruptedException
    {
    	List<String> list = zkc.getChildren(assignPath, watcherWorkerRoot);
    	
    	if (list.size() != 0)
    	{
    		// set the first node 
    		// TODO: do this properly
    		zkc.update(assignPath + "/" + list.get(0), task.getBytes());
    	}
    	
    }
    
    private void checkpath(byte[] data) {
        Stat stat = zkc.exists(myPath, watcherElection);
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
    
    private void checkWorker()
    {
    	workerList = zkc.getChildren(workerPath, watcherWorkerRoot);
    	System.out.println("workers: " + workerList);
    	
    	// set individual watch
    	if (workerList!=null)
    	{
	    	for (String s: workerList)
	        {
	        	zkc.exists(workerPath + "/" + s, watcherWorker);
	        }
    	}
    }

    // Election
    private void handleEventElection(WatchedEvent event) {
    	
        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(myPath)) {
            if (type == EventType.NodeDeleted) {
                System.out.println(myPath + " deleted! Let's go!");
                checkpath(myIP.getBytes()); // try to become the boss
            }
            if ((type == EventType.NodeCreated) ) {
                System.out.println(myPath + " created!");            
                checkpath(myIP.getBytes()); // re-enable the watch
            }
        }
    }
    
    // Dead worker job re-assign
    private void handleEventWorkerRoot(WatchedEvent event) {
    	
        String path = event.getPath();
        EventType type = event.getType();
        if(path.equalsIgnoreCase(workerPath)) {
            if (type == EventType.NodeChildrenChanged) {
                System.out.println(workerPath + ": Children changed!");

                // get worker list, re-enable watch on root
                // add watch to individual children
                checkWorker();
            }
        }
    }
    
    private void handleEventWorker(WatchedEvent event) {
    	
        String path = event.getPath();
        String workerIndex = path.substring(7);
        EventType type = event.getType();
        
        if (type == EventType.NodeDataChanged) {
            System.out.println(path + "processing finished! now collecting data!");
            
            
            // remove assigned data
           try {
        	   	byte[] data = zkc.read(path);
        	   	String result = new String (data);
        	   	System.out.println("retrieve data: " + result);
        	    System.out.println("clear assignment!");
				zkc.update(assignPath + workerIndex, null);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // re-enable the watch
            zkc.exists(path, watcherWorker);
        }
            
        if (type == EventType.NodeDeleted)
        {
        	// re-enable the watch
        	System.out.println("Dead Worker!!" + path);
        	
        	// if there is task assigned to this worker, re-assign it to someone else
        	// /worker/worker-0, workerIndex = /worker-0
        	
        	String assignedTask = null;
        	System.out.println("Dead Worker index: " + workerIndex);
        	
        	try {
        		byte[] data = zkc.read(assignPath + workerIndex);
        		if (data != null)
        		{
        			assignedTask = new String(data);
        		}
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (assignedTask!= null)
        	{
        		System.out.println("dead worker has task: " + assignedTask);
        		
        		// TODO: reassgin the task
        	}
        	
        	// remove the old assign node of the dead worker
        	System.out.println("Dead Worker assign remove!: " + assignPath + workerIndex);
        	try {
				zkc.delete(assignPath + workerIndex);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	// no need to up the watch again
        	//zkc.exists(myPath, watcherElection);
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
