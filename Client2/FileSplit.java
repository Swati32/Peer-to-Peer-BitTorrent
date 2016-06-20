

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
class FileSplit {
    public ArrayList splitFile(File f) throws IOException {
        int partCounter = 1;
        int sizeOfFiles = 1024 * 100;// 100KB
        byte[] buffer = new byte[sizeOfFiles];
        ArrayList ChunkList= new ArrayList<String>(); 
        
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            String name = f.getName();
            Long size =f.length();
            System.out.println("File Name : "+ name) ;
            System.out.println("File Size : "+ size) ;
            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                File newFile = new File(Integer.toString((partCounter++)));
                FileOutputStream out = new FileOutputStream(newFile) ;
                out.write(buffer, 0, tmp);
                ChunkList.add(partCounter);
            }
            System.out.println("Number of Chunks : "+ (partCounter-1)) ;  
        }
		return ChunkList;
    }

   
}