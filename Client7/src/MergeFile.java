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
public class MergeFile {
	private static String FILE_NAME = "Final.pdf";

    public void merge(int num) {
        int size = num;
        File ofile = new File(FILE_NAME);

        FileOutputStream fos;

        FileInputStream fis;

        byte[] fileBytes;

        int bytesRead = 0;

        List<File> list = new ArrayList<File>();
       
        for(int i=1; i<=size ; i++)
        {
        list.add(new File(Integer.toString(i)));
        }
        
        try {

            fos = new FileOutputStream(ofile,true);

            for (File file : list) {

                fis = new FileInputStream(file);

                fileBytes = new byte[(int) file.length()];

                bytesRead = fis.read(fileBytes, 0,(int)  file.length());

                assert(bytesRead == fileBytes.length);

                assert(bytesRead == (int) file.length());

                fos.write(fileBytes);

                fos.flush();

                fileBytes = null;

                fis.close();

                fis = null;

            }

            fos.close();

            fos = null;

        }catch (Exception exception){

            exception.printStackTrace();

        }

    }

}
