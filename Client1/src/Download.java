import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Download extends Thread{
	    //Initializations
		private int MY_PORT;
		private int MY_ID;
		private String Path =  "";
		private int TOTALCHUNKS;
		private int NP1 = 0;
		private int NP2 = 0;
		private List<Integer> Chunk_List = null;
		private int FILE_SIZE = 1024 * 100;
		private Upload Client_server = null;
		
		int bytesRead = 0;
		int current = 0;
		Socket sock = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		DataInputStream in = null;
		
		//Constructor
		public Download(Upload upload ,int port,int id, String path,int total_chunk,int np1,int np2, List<Integer> chunklist, int file_size){
			MY_PORT = port;
			MY_ID = id ;
			Path =  path;
			TOTALCHUNKS = total_chunk ;
			NP1 = np1;
			NP2 = np2;
			Chunk_List = chunklist;
			FILE_SIZE = file_size;
			Client_server = upload;
		} //end Constructor
		
		//Utility function to update Neighbors
		public void updateNeighbors(int np1,int np2){
		    	//NP1 = np1;
		    	//NP2 = np2;
		}
		public void run() {
			int NPORT = NP1;
			int neigh = 0;
     		while(Chunk_List.size() < TOTALCHUNKS) {
     			
				NPORT = (neigh==0)? NP2:NP1; //Flip Neighbor 
				neigh = 1- neigh;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				int chunks_to_recieve = 0;
				try {
					//Connect to Neighbor
					sock = new Socket("localhost", NPORT);
					if(sock != null){
					//Specify Request Type
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("DOWNLOAD CHUNKS");
					
					//Specify Chunklist Size
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(Chunk_List.size());
					
					//Send chunk list
					for (int n = 0; n < Chunk_List.size(); n++) {
						try {
							out = new DataOutputStream(sock.getOutputStream());
							out.writeInt(Chunk_List.get(n));
						} catch (IOException e) {
							System.err.println("Io exception found in Download ");
						}
					}
					
					//Receive chunklist from Neighbor
					List receivedList = new ArrayList();
					try {

						in = new DataInputStream(sock.getInputStream());
						chunks_to_recieve = in.readInt();
						for (int b = 0; b < chunks_to_recieve; b++) {
							in = new DataInputStream(sock.getInputStream());
							int temp = in.readInt();				
							receivedList.add(b,Integer.toString(temp));
							Chunk_List.add(temp);
						}
					} catch (IOException e) {
						System.err.println("IO exception found in DOWNLOAD ");
					}
					//Receive chunks from Neighbor
					for (int k = 0; k < chunks_to_recieve; k++) {
							byte[] byte_array = new byte[FILE_SIZE];
							InputStream is = sock.getInputStream();
							fos = new FileOutputStream(Path+(String)receivedList.get(k));
							bos = new BufferedOutputStream(fos);
							bytesRead = is.read(byte_array, 0, byte_array.length);
							current = bytesRead;
							do {
								bytesRead = is.read(byte_array, current,
										(byte_array.length - current));
								if (bytesRead >= 0)
									current += bytesRead;
							} while (bytesRead > 0);
							bos.write(byte_array, 0, current);
							bos.flush();
					}
					
					//Update Client Server 
					Client_server.updateChunkList(Chunk_List);
				 }
				
				} catch(java.net.ConnectException je){
					System.out.println("Unable to reach Port "+ NPORT);
				}catch (FileNotFoundException e) {
					System.err.println("File not found");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				} catch (ArrayIndexOutOfBoundsException aa) {
					aa.printStackTrace();
				} finally {
					try {
						if (fos != null)
							fos.close();
						if (bos != null)
							bos.close();
						if (sock != null)
							sock.close();
					} catch (FileNotFoundException e) {
						System.err.println("File not found");
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				}
			}
     		
			System.out.println(MY_PORT+" All chunks received successfully.  " );
			System.out.println("Merging...");
			MergeFile m = new MergeFile();
			m.merge(TOTALCHUNKS,Path);
			System.out.println(MY_PORT+ " Merge Complete.");
	} // end run
}// end Download



