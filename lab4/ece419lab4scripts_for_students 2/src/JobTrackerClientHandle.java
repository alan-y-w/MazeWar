import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;


public class JobTrackerClientHandle implements Runnable{
	ObjectInputStream in_stream;
	ObjectOutputStream out_stream;
	BlockingQueue<String> Q;
	Thread t;
	
	public JobTrackerClientHandle(ObjectInputStream inStream, ObjectOutputStream outStream, BlockingQueue queue)
	{
		in_stream = inStream;
		out_stream = outStream;
		Q = queue;
	}

	@Override
	public void run() {
		String data;
		try {
			//while (( data = (String) in_stream.readObject()) != null) {
				data = (String) in_stream.readObject();
				System.out.println("From Client: " + data);
				Q.put(data);
			//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
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
