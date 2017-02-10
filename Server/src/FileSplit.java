
import java.io.*;
import java.util.*;


class FileSplit {
	
    //Splits a file into chunks and returns the chunk number in List	
    public ArrayList<Integer> splitFile(File file) throws IOException {
    	
        int partCounter = 1;
        int sizeOfFiles = 1024 * 100;      // 100KB
        byte[] buffer = new byte[sizeOfFiles];
        ArrayList<Integer> ChunkList= new ArrayList<Integer>(); 
        
        
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            String name = file.getName();
            Long size =file.length();
            System.out.println("File Name : "+ name) ;
            System.out.println("File Size : "+ size) ;
            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                File newFile = new File(Integer.toString((partCounter++)));
                FileOutputStream out = new FileOutputStream(newFile) ;
                out.write(buffer, 0, tmp);
                ChunkList.add(partCounter);
                out.close();
            }
            System.out.println("Number of Chunks : "+ (partCounter-1)) ;  
        }
	return ChunkList;
    }
}
