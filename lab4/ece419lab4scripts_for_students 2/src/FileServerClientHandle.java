import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class FileServerClientHandle implements Runnable{
	ObjectInputStream in_stream;
	ObjectOutputStream out_stream;
	List<String> words;
	Thread t;
	
	public FileServerClientHandle(ObjectInputStream inStream, ObjectOutputStream outStream, List<String> ListWords)
	{
		in_stream = inStream;
		out_stream = outStream;
		words = ListWords;
	}

	@Override
	public void run() {
		String data;
		try {
			while (( data = (String) in_stream.readObject()) != null) {
				System.out.println("From Client: " + data);
				
				// data - ID - partition size
				String[] task = data.split("-");
				int id = Integer.parseInt(task[1]);
				int size = Integer.parseInt(task[2]);
				ArrayList<String> partition = null;
				
				try {
					partition = new ArrayList<String>( words.subList(id * size, (id + 1) * size));	
				} catch(IndexOutOfBoundsException e){
					partition = new ArrayList<String>( words.subList(id * size, words.size()));
				}
				
				System.out.println("Sending partition to client!");
				out_stream.writeObject(partition);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Client disconnected!");
		} catch (ClassNotFoundException e) {
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
