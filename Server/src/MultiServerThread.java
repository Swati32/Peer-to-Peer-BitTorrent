import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class MultiServerThread extends Thread {

	
	private String CONFIG = "";
    private Map<String, Integer> ports = null;
    
    public List<Integer> chunkList = new ArrayList<Integer>();
	public ArrayList<Integer> ConnectedPorts = new ArrayList<Integer>();
	private Socket socket = null;
    public int totalChunks = 0;
	public int block;
	public int MaxBlock;

	//Constructor
	public MultiServerThread(Socket socket, List<Integer> Chunklist, int totalChunks, int MaxBlock, int block,String confg, Map<String, Integer> ports) {
		this.CONFIG = confg;
		this.ports = ports;
		this.socket = socket;
		this.chunkList = Chunklist;
		this.totalChunks = totalChunks;
		this.block = block;
		this.MaxBlock = MaxBlock-1;
	} //end constructor

	//run Method
	public void run() {
		int id = 0;
		int portnum = 0;
		int[][] neigh;
		String type = null;
		DataInputStream in;
		DataOutputStream out;
		
		try {
			//Handshake
			// Receive Message Type 
			in = new DataInputStream(socket.getInputStream());
			type = in.readUTF();
			// Receive client ID 
			in = new DataInputStream(socket.getInputStream());
			id = in.readInt();
			// Receive client Port Number
			in = new DataInputStream(socket.getInputStream());
			portnum = in.readInt();
			System.out.println("CONNECTED TO  " + id + " " + portnum);
		} catch (IOException e) {
			
		}
		
        //Send Neighbors to the connecting client
		neigh = getNeighbors(id, portnum,socket);
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

		//Send chunks to Client
		if (!(type.equals("NEIGHBORS"))) {
			if (block >= MaxBlock) {
				block = 0;
			} else {
				block++;
			}
			
			Server.increment(block);
			
			//Send Total chunks that file is split into
			try {

				out = new DataOutputStream(socket.getOutputStream());
				out.writeInt(totalChunks);
			} catch (IOException e) {

			}
			
			
			//Send number of chunks that will be sent to client from server 
			try {

				out = new DataOutputStream(socket.getOutputStream());
				out.writeInt((int) chunkList.size());
				System.out.println(chunkList.size());
			} catch (IOException e) {
				System.out.println("Unable to communicate with Client");
			}

			
			//Send chunks ID to client from server 
			
			for (int i = 0; i < chunkList.size(); i++) {
				try {
					out = new DataOutputStream(socket.getOutputStream());
					out.writeInt((int) chunkList.get(i));
				} catch (IOException e) {
					System.out.println("Unable to communicate with Client");  
				}
			}


			//Send chunks to client from server 
			
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			OutputStream os = null;
			Socket sock = socket;

			try {
				
				for (int i = 0; i < chunkList.size(); i++) {
					int file = ((int) chunkList.get(i));
					String filename = Integer.toString(file);
					File outputFile = new File(filename);
					byte[] mybytearray = new byte[(int) outputFile.length()];
					fis = new FileInputStream(outputFile);
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
				//Close the connections
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
	}//end run

	
	// bootstrap logic
	
	public int[][] bootStrap(Socket sock ,int id,int portid) {
		
        System.out.println("New Client has arrived !!!!");
		DataOutputStream out;
		String [][] portarr = null;
		ReadWriteFile r = new ReadWriteFile();
		try {
			portarr = r.read(CONFIG);
     	} catch (IOException e) {
			e.printStackTrace();
		}
	
		int nport1 = 0;
		int nport2 = 0;
		
		int size = portarr.length;
		//Get the first and last Peer from list
		nport1 = Integer.parseInt(portarr[size-2][0]);
		nport2 = Integer.parseInt(portarr[1][0]);
		
		//Reassign Neighbors
		
		int temp1 = id;
		int temp2 = portid;
		
		portarr[1][2]= Integer.toString(temp1);
		portarr[size-2][3]= Integer.toString(temp1);
		portarr[size-1][0] = Integer.toString(temp1);
		portarr[size-1][1] = Integer.toString(temp2);
		portarr[size-1][2] = Integer.toString(nport1);
		portarr[size-1][3]= Integer.toString(nport2);
		
		//Update Config File
		r.write(portarr, CONFIG);
		
		  int [][] neigh = {{0,0},{0,0}};
	      neigh [0][0]=  Integer.parseInt(portarr[1][0]);
	      neigh [0][1]=  Integer.parseInt(portarr[1][1]);
	      neigh [1][0]=  Integer.parseInt(portarr[size-2][0]);
	      neigh [1][1]=  Integer.parseInt(portarr[size-2][1]);
	    
	      String [][] newArray = null;
	      try {
	    	  ReadWriteFile w = new ReadWriteFile();
			newArray = w.read(CONFIG);
		} catch (IOException e1) {
		
			e1.printStackTrace();
		}
	      
        int nport = neigh[0][1];
        
        //Update Neighbors
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
	      
	} //end bootstrap
		
		
	// Utility function to get Nieghbors from config file
	
	public int[][] getNeighbors(int id, int portid,Socket socket) {
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
		
	} //end get Neighbors

} //end main class


