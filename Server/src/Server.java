import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server {
	ServerSocket sSocket;
	Socket connection = null;
	public static ServerSocket servsock = null;
	public static boolean listening = true;
	public static String[][] portarr;
	public final static int MY_PORT = 8000;
	/**
	 * 
	 */
	public final static String FILE_TO_SEND = "C:/Users/hp/workspace4/Server/CN.pdf";
	public final static String CONFIG = "C:/Users/hp/workspace4/Server/bin/config.txt";

	public static int block = 0;

	public static void main(String args[]) throws IOException {
		Map ports = new HashMap();

		// Read Configuration file

		ReadWriteFile r = new ReadWriteFile();
		portarr = r.read(CONFIG);
		for (int i = 1; i < portarr.length - 1; i++) {
			//System.out.println(portarr[i][1]);
			int port = Integer.parseInt(portarr[i][1]);
			ports.put(portarr[i][0], port);
			

		}
		

		FileSplit f = new FileSplit();
		ArrayList<String> ChunkList = f.splitFile(new File(FILE_TO_SEND));
		ArrayList<List> ChunkBlocks = new ArrayList<List>();

		System.out.println("Size of chunk list after splitting: "+ ChunkList.size());

		int Interval = ChunkList.size() / (portarr.length - 2);
		
		int Interval1 = Interval;
		int extra = ChunkList.size() % (portarr.length - 2);
		if(Interval==0)
		{
			Interval=1;
			extra=0;
		}
		int start=1;
		int index = 0;
		for (int j = 1; index <(portarr.length - 2); j++) {
			
			int end = (start + Interval);
			if (start == 1) {end = end + extra;}
			//ChunkBlocks.add(index,ChunkList.subList(start, end));
			List Chunkist = new ArrayList();
			System.out.println("start-end:"+start+"-"+end);
			
			if(start>ChunkList.size())
			{
				start=1;
				end = start+Interval;
			}
			
			for(int i=0;i<(end-start);i++)
			{
				
				Chunkist.add(i,i+start);
			}
			if (start == 1) {start=start+extra;}
			ChunkBlocks.add(index,Chunkist);
			start=end;
			for (int i=0 ;i<Chunkist.size();i++) {
				
				System.out.println("Elements in chunk "+index+ " "+Chunkist.get(i));
			}
		    index++;
		}
		
		

		final int SOCKET_PORT = Integer.parseInt(portarr[0][1]);

		try {
			servsock = new ServerSocket(SOCKET_PORT, 10);
			while (listening) {
				System.out.println("Waiting!!!");
				new MultiServerThread1(servsock.accept(),
						ChunkBlocks.get(block), ports, ChunkList.size(),
						ChunkBlocks.size(), block).start();
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

//THREAD

class MultiServerThread1 extends Thread {

	private Socket socket = null;
	public List chunkList = new ArrayList();
	public ArrayList ConnectedPorts = new ArrayList();

public static int[][] bootStrap(Socket sock ,int id,int portid) {
	// bootstrap logic
	
	//call readwritefile
   System.out.println("New Client has arrived !!!!");
	DataInputStream in;
	DataOutputStream out;
	List newList = new ArrayList();
	String [][] portarr = null;
	ReadWriteFile r = new ReadWriteFile();
	try {
		portarr = r.read(CONFIG);
		
		/*for(int i=0;i<portarr.length;i++)
		{
			for(int j=0;j<4;j++)
			{
	       System.out.print(portarr[i][j]+" ");
			}
			System.out.println();
		}*/
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	int port =0;
	int nport1 =0;
	int nport2 =0;
	
	int size = portarr.length;
	
		 nport1 = Integer.parseInt(portarr[size-2][0]);
	     nport2 = Integer.parseInt(portarr[1][0]);
	
	
	
	
	int temp1 = id;
	int temp2 = portid;
	
	portarr[1][2]= Integer.toString(temp1);
	portarr[size-2][3]= Integer.toString(temp1);
	portarr[size-1][0] = Integer.toString(temp1);
	portarr[size-1][1] = Integer.toString(temp2);
	portarr[size-1][2] = Integer.toString(nport1);
	portarr[size-1][3]= Integer.toString(nport2);
			
	
	/*for(int i=0;i<portarr.length;i++)
	{
		for(int j=0;j<4;j++)
		{
       System.out.print(portarr[i][j]+" ");
		}
		System.out.println();
	}*/
	
	r.write(portarr, CONFIG);
	
	  int [][] neigh = {{0,0},{0,0}};
      neigh [0][0]=  Integer.parseInt(portarr[1][0]);
      neigh [0][1]= Integer.parseInt(portarr[1][1]);
      neigh [1][0]=  Integer.parseInt(portarr[size-2][0]);
      neigh [1][1]=  Integer.parseInt(portarr[size-2][1]);
    
    
     
      String [][] newArray = null;
      try {
    	  ReadWriteFile w = new ReadWriteFile();
		newArray = w.read(CONFIG);
	} catch (IOException e1) {
	
		e1.printStackTrace();
	}
      
      
     /* for(int i=0;i<portarr.length;i++)
		{
			for(int j=0;j<4;j++)
			{
	       System.out.print(portarr[i][j]+" ");
			}
			System.out.println();
		}*/
      
      
     int nport = neigh[0][1];
      
      
     
      
      
      for(int i=0;i<2;i++)
  	{
  		
        try {
			sock = new Socket("localhost", nport);
			System.out.println("Connecting...");
			out = new DataOutputStream(sock.getOutputStream());
			out.writeUTF("Updated Neighbors");
			out = new DataOutputStream(sock.getOutputStream());
			out.writeInt(Integer.parseInt(newArray[1][2]));
			out = new DataOutputStream(sock.getOutputStream());
			out.writeInt(Integer.parseInt(newArray[1][2]));
			
			out = new DataOutputStream(sock.getOutputStream());
			out.writeInt(Integer.parseInt(newArray[1][3]));
			out = new DataOutputStream(sock.getOutputStream());
			out.writeInt(Integer.parseInt(newArray[1][3]));
			
			
	  		
		} catch (UnknownHostException e) {
			} catch (IOException e) {
			
		}
       
        nport = neigh[1][1];
        
  	}
      
    return neigh;
      
}
	public MultiServerThread1(ServerSocket servsock, Object object, int portid,
		int portid2, int portid3) {
	// TODO Auto-generated constructor stub
}
	public final static String CONFIG = "C:/Users/hp/workspace4/Server/bin/config.txt";
    public static Map ports = new HashMap();
	public int totalChunks = 0;
	public static int block;
	public static int MaxBlock;

	public MultiServerThread1(Socket socket, List Chunklist, Map ports,
			int totalChunks, int MaxBlock, int block) {
		super("MultiServerThread1");
		this.socket = socket;
		this.chunkList = Chunklist;
		this.totalChunks = totalChunks;
		this.block = block;
		this.MaxBlock = MaxBlock-1;
		this.ports = ports;
	}

	public int[][] getNeigh(int id, int portid,Socket socket) {
		String[][] portarr = null;
		int j = 0;
		int index = 0;
		int[][] neigh = { { 0, 0 }, { 0, 0 } };
		ReadWriteFile r = new ReadWriteFile();
		try {
			portarr = r.read(CONFIG);
		} catch (IOException e) {
		}
		while (j < portarr.length - 1) {
			if (portarr[j][0].equals(Integer.toString(id))) {
				index = j;
				System.out.println("Found neighbors....");
				neigh[0][0] = Integer.parseInt(portarr[j][2]);
				neigh[0][1] = (int) ports.get(portarr[j][2]);
				neigh[1][0] = Integer.parseInt(portarr[j][3]);
				neigh[1][1] = (int) ports.get(portarr[j][3]);

				break;
			}
			j++;
		}
		if (index == 0) {
			neigh=bootStrap(socket ,id ,portid);
		}
		return neigh;
	}
	public void run() {
		int id = 0;
		int portnum = 0;
		int[][] neigh;
		String type = null;
		DataInputStream in;
		DataOutputStream out;
		try {
			in = new DataInputStream(socket.getInputStream());
			type = in.readUTF();
			// create write stream to send information
			in = new DataInputStream(socket.getInputStream());
			id = in.readInt();
			in = new DataInputStream(socket.getInputStream());
			portnum = in.readInt();
			System.out.println("CONNECTED TO  " + id + " " + portnum);
		} catch (IOException e) {
			
		}

		neigh = getNeigh(id, portnum,socket);
		System.out.println("Sending Neighbors...");
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				try {
					System.out.println(neigh[i][j]);
					out = new DataOutputStream(socket.getOutputStream());
					out.writeInt(neigh[i][j]);
				} catch (IOException e) {

				}
			}
		}

		if (!(type.equals("NEIGHBORS"))) {
			if (block >= MaxBlock) {
				block = 0;
			} else {
				block++;
			}
			Server ns = new Server();
			ns.increment(block);

			System.out.println("TOTAL CHUNKS: "+totalChunks);
			try {

				out = new DataOutputStream(socket.getOutputStream());
				out.writeInt(totalChunks);
				System.out.println(totalChunks);
			} catch (IOException e) {

			}
			
			System.out.println("CHUNK SIZE is: ");

			try {

				out = new DataOutputStream(socket.getOutputStream());
				out.writeInt((int) chunkList.size());
				System.out.println(chunkList.size());
			} catch (IOException e) {

			}
			System.out.println("SENDING CHUNKS ");

			// code for sending the list
			for (int i = 0; i < chunkList.size(); i++) {
				System.out.println(chunkList.get(i));
			}

			for (int i = 0; i < chunkList.size(); i++) {
				try {

					out = new DataOutputStream(socket.getOutputStream());
					out.writeInt((int) chunkList.get(i));
				} catch (IOException e) {

				}
			}

			FileInputStream fis = null;
			BufferedInputStream bis = null;
			OutputStream os = null;
			ServerSocket servsock = null;
			Socket sock = socket;

			try {
				
				for (int i = 0; i < chunkList.size(); i++) {
					int file = ((int) chunkList.get(i));
					String filename = Integer.toString(file);
					File myFile = new File(filename);
					byte[] mybytearray = new byte[(int) myFile.length()];
					fis = new FileInputStream(myFile);
					bis = new BufferedInputStream(fis);
					bis.read(mybytearray, 0, mybytearray.length);
					os = sock.getOutputStream();
					System.out.println("Sending chunk " + chunkList.get(i) + " ("+ mybytearray.length + " bytes)");
					os.write(mybytearray, 0, mybytearray.length);
					os.flush();
					System.out.println("Done.");
				}
			} catch (FileNotFoundException e) {
				System.err.println("File not found");
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} finally {
				try {
					if (bis != null)
						bis.close();
					if (os != null)
						os.close();
					if (sock != null)
						sock.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
	}
}
