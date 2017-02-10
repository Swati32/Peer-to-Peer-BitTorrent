import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class MultiUploadServerThread implements Runnable {
		private Socket socket = null;
		private String FILE_PATH = null;
		private Upload Parent = null;
		private List tempchunklist = new ArrayList();
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		OutputStream os = null;
		Socket sock = socket;


		//Constructor
		public MultiUploadServerThread(Upload parent,Socket socket, List<Integer> tempCHUNKLIST,String FILE_PATH) {
			this.Parent = parent;
			this.socket = socket;
			this.tempchunklist = tempCHUNKLIST;		
			this.FILE_PATH = FILE_PATH;
		}

		//Run Method
		public void run() {
			try {
				DataOutputStream out;
				DataInputStream in;
				in = new DataInputStream(socket.getInputStream());
				String RequestType = in.readUTF();
				if (RequestType.equals("Updated Neighbors")) {
					
					in = new DataInputStream(socket.getInputStream());
					int ID1 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int PORT1 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int ID2 = in.readInt();
					in = new DataInputStream(socket.getInputStream());
					int PORT2 = in.readInt();
				    Parent.updateNeighbors(PORT1, PORT2);
					
				} else {
					List<Integer> NeighborCHUNKLIST = new ArrayList<Integer>();
					try {
						in = new DataInputStream(socket.getInputStream());
						int chunks_with_neighbor = in.readInt();
						for (int j = 0; j < chunks_with_neighbor; j++) {
							try {
								in = new DataInputStream(socket.getInputStream());
								NeighborCHUNKLIST.add(j, in.readInt());
							} catch (IOException e) {
								socket.close();
							}
						}
						
						
						// Comparing neighbor list with current chunklist and storing result in templist
						
						List templist=new ArrayList();
						for(int i=0;i<tempchunklist.size();i++){
							templist.add(tempchunklist.get(i));
						}
						templist.removeAll(NeighborCHUNKLIST);
						int chunks_to_send = templist.size();
						
						// Send chunklist to be sent to Neighbor
						out = new DataOutputStream(socket.getOutputStream());
						out.writeInt(chunks_to_send);

						for (int a = 0; a < templist.size(); a++)
						{
							out = new DataOutputStream(socket.getOutputStream());
							String s = templist.get(a).toString();
							out.writeInt(Integer.parseInt(s));
						}
                        
						//Send Chunks to be sent to Neighbor
						for (int i = 0; i < templist.size(); i++) {
							String outputFile = FILE_PATH.concat(templist.get(i).toString());
							File out_file = new File(outputFile);
							byte[] byte_array = new byte[(int) out_file.length()];
							fis = new FileInputStream(out_file);
							bis = new BufferedInputStream(fis);
							bis.read(byte_array, 0, byte_array.length);
							os = socket.getOutputStream();
							os.write(byte_array, 0, byte_array.length);
							os.flush();
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
					}//end try-catch-finally
				} // end- if-else Request type
			} catch (IOException e) {
				System.err.println("IO exception found in upload 4 ");
          }
		}
	}


