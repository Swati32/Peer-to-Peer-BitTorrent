import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server {
	
	final static int MY_PORT = 8000;
	final static String FILE_TO_SEND = "C:/Users/Swati/leet/P2P/Server/CN.pdf";
	final static String CONFIG = "C:/Users/Swati/leet/P2P/Server/bin/config.txt";
	static ServerSocket servsock = null;
	static boolean listening = true;
	static String[][] portarr;
	static int Num_Neighbors = 0;
	static int block = 0;

	public static void main(String args[]) throws IOException {
		
		// Initialize Network
		
		Map<String,Integer> ports = new HashMap<String,Integer>();
		ReadWriteFile r = new ReadWriteFile();
		portarr = r.read(CONFIG);
		Num_Neighbors = portarr.length-2;
		for (int i = 1; i < portarr.length; i++) {
			ports.put(portarr[i][0], Integer.parseInt(portarr[i][1]));
		}
		
     
		//Split target File
		
		FileSplit f = new FileSplit();
		ArrayList<Integer> ChunkList = f.splitFile(new File(FILE_TO_SEND));
		
		// Arrange chunks into blocks
		
		ArrayList<List> ChunkBlocks = new ArrayList<List>();
		int Interval = ChunkList.size() / (Num_Neighbors);
		int extra = ChunkList.size() % (Num_Neighbors);
		int start = 1;
		
		//Reset if number of chunks < number of peers
		if(Interval == 0)                              
		{
			Interval = 1;
			extra = 0;
		}
		
		for (int index = 0; index <Num_Neighbors ; index++) {
			int end = (start + Interval);
			if (start == 1) {end = end + extra;}
			List<Integer> temp_list = new ArrayList<Integer>();
			
			if(start>ChunkList.size())    
			{
				start = 1;
				end = start + Interval;
			}
			
			for(int i=0;i<(end-start);i++)
			{
				temp_list.add(i,i+start);
			}
			
			if (start == 1) start = start + extra;
			ChunkBlocks.add(index,temp_list);
			start = end;
		    
		}
		
		// Start Server and listen

		final int SOCKET_PORT = Integer.parseInt(portarr[0][1]);

		try {
			servsock = new ServerSocket(SOCKET_PORT, 10);
			while (listening) {
				new MultiServerThread(servsock.accept(),
						               ChunkBlocks.get(block), 
						               ChunkList.size(),
						               ChunkBlocks.size(), 
						               block,
						               CONFIG,
						               ports).start();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				if (servsock != null)
					servsock.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	public static void increment(int newSize) {
		block = newSize;
	}

}

