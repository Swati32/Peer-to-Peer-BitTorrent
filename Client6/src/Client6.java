import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client6 {
	int MY_ID = 6;
	ServerSocket sSocket;
	Socket connection = null;
	ObjectOutputStream out;
	DataInputStream in;
	public final static int MY_PORT = 8006;

	public final static int SERVER_PORT = 8000;

	public static List CHUNKLIST = new ArrayList();
	public static int TOTALCHUNKS;
	public final static String FILE_TO_SEND = "";
	public final static String SERVER = "127.0.0.1";
	public final static String FILE_TO_RECEIVE = "";
	public final static int FILE_SIZE = 1024 * 100;
	public static int NEIGH1 = 0;
	public static int NEIGH2 = 0;
	public static int NP1 = 0;
	public static int NP2 = 0;
	Socket requestSocket;

	public void Client() {
	}

	FileInputStream fis = null;
	BufferedInputStream bis = null;
	OutputStream os = null;
	ServerSocket servsock = null;
	static Socket sock = null;
	int bytesRead;
	int current = 0;
	FileOutputStream fos = null;
	BufferedOutputStream bos = null;

	public void downloadFromServer() {
		int num = 0;
		DataInputStream in;
		DataOutputStream out;
		try {
			int[][] neigh = { { 0, 0 }, { 0, 0 } };
			try {
				sock = new Socket("localhost", SERVER_PORT);
				System.out.println("CONNECTED TO SERVER ");
				System.out.println(); 
				
				try {
					// create write stream to send information
					out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("DOWNLOAD");
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(MY_ID);
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(MY_PORT);

					for (int i = 0; i < 2; i++) {
						for (int j = 0; j < 2; j++) {
							try {
								// create write stream to send information
								
								in = new DataInputStream(sock.getInputStream());

								neigh[i][j] = in.readInt();
								

							} catch (IOException e) {

							}
							NP1 = neigh[0][1];
							NP2 = neigh[1][1];
							
						}
						
					}
					System.out.println("Got my neighbor 1 "+ neigh[0][0]+" "+neigh[0][1]);
					System.out.println("Got my neighbor 2 "+ neigh[1][0]+" "+neigh[1][1]);
				} catch (IOException e) {
					
				}

			} catch (IOException e) {
			}

			in = new DataInputStream(sock.getInputStream());
			TOTALCHUNKS = in.readInt();
			System.out.println();
			System.out.println("DOWNLOADING CHUNKS.....");

			in = new DataInputStream(sock.getInputStream());
			num = in.readInt();

			for (int i = 0; i < num; i++) {
				try {
					// create write stream to send information
					in = new DataInputStream(sock.getInputStream());
					int temp = in.readInt();
					CHUNKLIST.add(i, temp);
				} catch (IOException e) {
					// Bail out
				}

			}

			// receive file
			for (int i = 0; i < num; i++) {
				byte[] myarray = new byte[FILE_SIZE];
				InputStream is = sock.getInputStream();
				fos = new FileOutputStream(FILE_TO_RECEIVE + CHUNKLIST.get(i));
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
				System.out.println();
				System.out.println("S Chunk " + CHUNKLIST.get(i)
						+ " downloaded (" + current + " bytes)"
						+ " from Server");
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
		}

	}

	public void run() {
		try {
			(new Thread(new upload())).start();
			Thread.sleep(5000);
			(new Thread(new download())).start();
		} catch (InterruptedException e) {
		}
	}

	public static void main(String args[]) throws IOException {
		Client6 client = new Client6();
		client.downloadFromServer();
		
		client.run();

	}

	// UPLOAD

	class MultiUploadServerThread implements Runnable {
		private Socket socket = null;
		private List tempchunklist = new ArrayList();

		public MultiUploadServerThread(Socket socket, List tempCHUNKLIST) {

			this.socket = socket;
			this.tempchunklist = tempCHUNKLIST;
		}

		public void run() {
			
			try {
				
				DataOutputStream out;
				DataInputStream in;
				in = new DataInputStream(socket.getInputStream());
				String temp = in.readUTF();
				//System.out.println("I got download request I think " + temp);
				if (temp.equals("Updated Neighbors")) {
					in = new DataInputStream(socket.getInputStream());
					int temp1 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int temp2 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int temp3 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int temp4 = in.readInt();
					NP1 = temp1;
					NP2 = temp3;
					int np1 = temp2;
					int np2 = temp4;
					System.out.println("new neighbors are: "+"Neighbor 1: "+np1+" "+NP1);
					System.out.println("new neighbors are: "+"Neighbor 2: "+np2+" "+NP2);
				}				else {
					System.out.println();
					System.out.println("Got download request from neighbor");	
					
					FileInputStream fis = null;
					BufferedInputStream bis = null;
					OutputStream os = null;
					ServerSocket servsock = null;
					Socket sock = socket;
					int num = 0;
					
					List NeighborCHUNKLIST = new ArrayList();
                   
					try {

						// send file

						in = new DataInputStream(socket.getInputStream());
						num = in.readInt();
						//System.out.println("UUU AFTER 1 "+ CHUNKLIST.size());
						//System.out.println("UUU AFTER 1 "+ tempchunklist.size());
						//System.out.println("UUU AFTER 1 "+ newtemplist.size());
						System.out.println();
						System.out.println("Receiving chunk list from neighbor ");
						
						
						for (int j = 0; j < num; j++) {
							try {
								// create write stream to send information
								in = new DataInputStream(socket.getInputStream());
								int temp3 = in.readInt();
								//System.out.println("UUU AFTER 2 "+ CHUNKLIST.size());
								NeighborCHUNKLIST.add(j, temp3);

							//	System.out.println(NeighborCHUNKLIST.get(j));
							} catch (IOException e) {
								socket.close();
							}

						}
						System.out.println();
						for (int i = 0; i < CHUNKLIST.size(); i++) {
							if(i==0){System.out.print("Received neighbor's list [");}
							System.out.print(" "+ CHUNKLIST.get(i));
							if(i==CHUNKLIST.size()-1){System.out.print("]\n");}
						}
						
						List newtemplist=new ArrayList();
						for(int i=0;i<tempchunklist.size();i++){
							newtemplist.add(i,tempchunklist.get(i));
						}

						
						

						newtemplist.removeAll(NeighborCHUNKLIST);
						num = newtemplist.size();
						System.out.println();
                       System.out.println("Comparing neighbor's list to my chunk list.. ");
						
						System.out.println();
						
						for (int i = 0; i < newtemplist.size(); i++) {
							if(i==0){System.out.print("Chunks to be sent to neighbor [");}
							System.out.print(" "+ newtemplist.get(i));
							if(i==newtemplist.size()-1){System.out.print("]\n");}
						}

						
						System.out.println();
                      	System.out.println("Sending Chunks to neighbor:  ");
                      	
						out = new DataOutputStream(socket.getOutputStream());
						out.writeInt(num);

						for (int a = 0; a < newtemplist.size(); a++)

						{
							
							out = new DataOutputStream(socket.getOutputStream());
							out = new DataOutputStream(socket.getOutputStream());
							String s = newtemplist.get(a).toString();
							int si = Integer.parseInt(s);
							out.writeInt(si);
							
						}
						

						for (int i = 0; i < newtemplist.size(); i++) {
							String temp4 = (newtemplist.get(i)).toString();
							File myFile = new File(temp4);
							byte[] myarray = new byte[(int) myFile.length()];
							fis = new FileInputStream(myFile);
							bis = new BufferedInputStream(fis);
							bis.read(myarray, 0, myarray.length);
							os = sock.getOutputStream();
							os.write(myarray, 0, myarray.length);
							os.flush();
							System.out.println("U Chunk " + newtemplist.get(i)
									+ " uploaded (" + current + " bytes)");
						
						}
						
					} catch (FileNotFoundException e) {
						in.close();
				
						bis.close();
						os.close();
						socket.close();
						System.err.println("File not found in upload 1 ");
						
					} catch (IOException ioException) {
						in.close();
						bis.close();
						os.close();
						socket.close();
						System.err.println("IO exception found in upload 2 ");
						
					}  finally {
						try {
							if (bis != null)
								bis.close();
							if (os != null)
								os.close();
							if (socket != null)
								socket.close();
						} catch (IOException ioException) {
							
							bis.close();
							os.close();
							socket.close();
							System.err.println("IO exception found in upload 3 ");
							
						}
					}
				}
			} catch (IOException e) {
				
			}
		}
	}

	public class upload implements Runnable {

		public void run() {
			DataOutputStream out;
			try {
				servsock = new ServerSocket(MY_PORT, 10);

				while (true) {

					MultiUploadServerThread D1 = new MultiUploadServerThread(
							servsock.accept(), CHUNKLIST);
					Thread t1 = new Thread(D1);
					t1.start();

	
				}
			} catch (IOException e) {
			}
		}

	} // upload end

	public class download implements Runnable {


		@Override
		public void run() {

			int NPORT = NP1;
			System.out.println();
			for (int i = 0; i < CHUNKLIST.size(); i++) {
				if(i==0){System.out.print("Sending my chunk list to neighbor [");}
				System.out.print(" "+ CHUNKLIST.get(i));
				if(i==CHUNKLIST.size()-1){System.out.print("]\n");}
			}
			
			for (int i = 0; CHUNKLIST.size() < TOTALCHUNKS; i++) {
			
				if(i%2==0){
					NPORT = NP2;
				}
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
				
				
				
				
				int num = 0;
				try {

					sock = new Socket("localhost", NPORT);
			
					DataOutputStream out;
					out = new DataOutputStream(sock.getOutputStream());
					out.writeUTF("DOWNLOAD CHUNKS");
					
					out = new DataOutputStream(sock.getOutputStream());
					out.writeInt(CHUNKLIST.size());
					List receivedList = new ArrayList();
					
	            
	 

					for (int n = 0; n < CHUNKLIST.size(); n++) {
						try {

							out = new DataOutputStream(sock.getOutputStream());
							String s = CHUNKLIST.get(n).toString();
							int si = Integer.parseInt(s);
							out.writeInt(si);
					
						} catch (IOException e) {
							System.err.println("Io exception found in Download ");
						}
					}
					
					try {

						in = new DataInputStream(sock.getInputStream());
						num = in.readInt();
	
						for (int b = 0; b < num; b++) {
							in = new DataInputStream(sock.getInputStream());
							int temp = in.readInt();
		
							String t = Integer.toString(temp);
							receivedList.add(b, t);
						}
					} catch (IOException e) {
						System.err.println("IO exception found in DOWNLOAD ");
					}
					

					int index = CHUNKLIST.size() - 1;
					if (index < 0) {
						index = 0;
					}
					for (int k = 0; k < num; k++) {
						byte[] myarray = new byte[FILE_SIZE];
						InputStream is = sock.getInputStream();
						String temp = receivedList.get(k).toString();
						CHUNKLIST.add(index, Integer.parseInt(temp));
						fos = new FileOutputStream((String) receivedList.get(k));
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
					System.out.println("D Chunk " + receivedList.get(k)
								+ " downloaded (" + current + " bytes)");
					}
						
					} catch (FileNotFoundException e) {
					System.err.println("File not found");
				} catch (IOException ioException) {
					ioException.printStackTrace();
				} catch (ArrayIndexOutOfBoundsException aa) {
					
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
			System.out.println("All chunks received successfully.  " );
			MergeFile m = new MergeFile();
			m.merge(TOTALCHUNKS);
			

		}


	}

	
}
