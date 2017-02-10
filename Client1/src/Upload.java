import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


public class Upload extends Thread {
		private int MY_PORT=0;
		private String Path =  "";
		private List<Integer> Chunk_List = null;
		private Download client = null;
		
	    ServerSocket servsock =null;
	    
	    public Upload(int port,String path, List<Integer> chunklist){
	    	MY_PORT = port;
		    Path =  path;
		    Chunk_List = chunklist;
	    }
	    
	    public void updateChunkList(List<Integer> chunklist){
	    	Chunk_List = chunklist;
	    }
	    public void updateClient(Download download){
	    	client = download;
	    }
	    public void updateNeighbors(int np1,int np2){
	    	//client.updateNeighbors(np1, np2);
	    }
	    
		public void run() {
			try {
	            // Start Listening			
				servsock = new ServerSocket(MY_PORT,10);
				while (true) {
					MultiUploadServerThread D1 = new MultiUploadServerThread(this,servsock.accept(),Chunk_List,Path);
					Thread t1 = new Thread(D1);
					t1.start();
				}
			} catch (IOException e) {
				System.out.println("Unable to start Upload Process");
				System.out.println(e.getMessage());
			}finally{
				try {
					servsock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	} // upload end