import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
	
	final static int SERVER_PORT = 8000;
	final static String SERVER = "127.0.0.1";
	final static int FILE_SIZE = 1024 * 100;
	
	//Initializations
	private int MY_PORT ;
	private int MY_ID = 1;
	private int TOTALCHUNKS;
	private int NP1 = 0;
	private int NP2 = 0;
	private List<Integer> CHUNKLIST = new ArrayList<Integer>();
	private String FILE_PATH = "C:/Users/Swati/leet/P2P/Client1/Client";

	Socket sock = null;
	int bytesRead=0;
	int current = 0;
	FileOutputStream fos = null;
	ObjectOutputStream out =null;
	BufferedOutputStream bos = null;
	
	//Client Construtor
    public Client(int port, int id){
    	MY_PORT = port;
    	MY_ID = id;
    	
    	File directory = new File(FILE_PATH.concat(Integer.toString(id)));
    	FILE_PATH = FILE_PATH+Integer.toString(id)+"/";
    	if (! directory.exists()){
            directory.mkdir();
        }
    	downloadFromServer();
		connect_to_peers();
    } //end Client Construtor

	//Main
	public static void main(String args[]) throws IOException {
		Client client1 = new Client(8001,1);
		Client client2 = new Client(8002,2);
		Client client3 = new Client(8003,3);
		Client client4 = new Client(8004,4);
		Client client5 = new Client(8005,5);
		Client client6 = new Client(8006,6);
	
	}//end main
    
	//Utility function to Start Client Server and Client Download thread
	public void connect_to_peers() {
		try {
			Upload upload = new Upload(MY_PORT, FILE_PATH, CHUNKLIST);
			upload.start();
			Thread.sleep(6000);
			Download download = new Download(upload,MY_PORT, MY_ID, FILE_PATH, TOTALCHUNKS, NP1, NP2, CHUNKLIST,FILE_SIZE);
			download.start();
			upload.updateClient(download);
		} catch (InterruptedException e) {
		}
	} //end connect_to_peers
	
	
	//Utility function to download files from Server
	public void downloadFromServer() {
		int chunks_to_recieve = 0;
		DataInputStream in;
		DataOutputStream out;
		
		try {
			int[][] neigh = new int[2][2];
			try {
				sock = new Socket("localhost", SERVER_PORT);
				System.out.println("CONNECTED TO SERVER ");
				try {
					//Send Request Type
					out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("DOWNLOAD");
					//Send ID
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(MY_ID);
					//Send PORT
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(MY_PORT);
                    //Send Neighbors
					for (int i = 0; i < 2; i++) {
						for (int j = 0; j < 2; j++) {
							try {
								in = new DataInputStream(sock.getInputStream());
								neigh[i][j] = in.readInt();
							} catch (IOException e) {
								System.out.println("Error Reading Neighbors from Server ");
							}
							NP1 = neigh[0][1];
							NP2 = neigh[1][1];
							
						}
						
					}
				  } catch (IOException e) {
					System.out.println("Error Reading Neighbors from Server ");
				}

			} catch (IOException e) {
				System.out.println("Unable to connect to Server ");
			}

			//Receive Total Chunks from Server
			in = new DataInputStream(sock.getInputStream());
			TOTALCHUNKS = in.readInt();

			//Receive chunklist from Server
			in = new DataInputStream(sock.getInputStream());
			chunks_to_recieve = in.readInt();
			for (int i = 0; i < chunks_to_recieve; i++) {
				try {
					in = new DataInputStream(sock.getInputStream());
					int temp = in.readInt();
					CHUNKLIST.add(i, temp);
				} catch (IOException e) {
					System.out.println("Unable to read from Server");
				}

			}

			//Receive Chunks from Server
			for (int i = 0; i < chunks_to_recieve; i++) {
				byte[] myarray = new byte[FILE_SIZE];
				InputStream is = sock.getInputStream();
				fos = new FileOutputStream(FILE_PATH + CHUNKLIST.get(i));
				bos = new BufferedOutputStream(fos);
				bytesRead = is.read(myarray, 0, myarray.length);
				current = bytesRead;

				do {
					bytesRead = is.read(myarray, current,
							(myarray.length - current));
					if (bytesRead >= 0)
						current += bytesRead;
				} while (bytesRead > 0);

				bos.write(myarray, 0, current);
				bos.flush();
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				if (fos != null)
					fos.close();
				if (bos != null)
					bos.close();
				if (sock != null)
					sock.close();
			} catch (FileNotFoundException e) {
				System.err.println("File not found in main");
			} catch (IOException ioException) {
				System.err.println("io exception in main");
			}
		} //end try-catch

	} //end downloadFromServer
}//end Client
