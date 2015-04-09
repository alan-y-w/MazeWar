import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;


public class JobTrackerAssigner implements Runnable {
	private Thread t;
	ZkConnector zkc;
	List<String> list;
	String task;
	String assignPath;
	Watcher watcherWorkerRoot;
	
	public JobTrackerAssigner(String task, String assignPath, ZkConnector zkc, Watcher watcherWorkerRoot)
	{
		this.task = task;
		this.zkc = zkc;
		this.assignPath = assignPath;
		this.watcherWorkerRoot = watcherWorkerRoot;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true)
		{
			list = zkc.getChildren(assignPath, watcherWorkerRoot);
    		
			if (list!=null)
			{
				for (String s : list)
				{
					//zkc.update(assignPath + "/" + list.get(0), task.getBytes());
					
					// check and see if worker available
					String fullAssignPath = assignPath + "/" + s;
					byte[] data = null;
					try {
						data = zkc.read(fullAssignPath);
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (data== null)
					{
						// available worker found, assign job
						//task = password + "-" + partitionID + "-" +partitionSize;
						System.out.println(">>> re-assign task: " + task + "to " + fullAssignPath);
						try {
							zkc.update(fullAssignPath, task.getBytes());
						} catch (KeeperException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// worker finish -> job tracker clear assign
						// allow enough time for the worker to up the watch again
						// avoid the case when the assign is cleared to null, while the user's watch is down
						// another task is assigned - in this case the worker will miss the task being assigned.
						// take a 1 second break for the worker to up the watch again.
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					}
				}
			}
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
