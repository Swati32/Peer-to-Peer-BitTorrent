

import java.io.*;


public class ReadWriteFile {
	public static int CONFIG_COL = 4;
	public static int CONFIG_ROWS = 0;
	
	public String[][] read(String filename) throws IOException {
		String line = null;
		String[][] ports = null;
		
		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();     //Skip header
			CONFIG_ROWS = countLines(filename); 
			ports = new String[CONFIG_ROWS-1][CONFIG_COL];
			int row= 0;
			while ((line = bufferedReader.readLine()) != null) {
				
				String[] elems = line.split("\\s+");
				for (int col = 0; col < CONFIG_COL; col++) {
					ports[row][col] = elems[col];
				}
				row++;
			}
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + filename + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + filename + "'");
		}
		
		return ports;
	}

	public void write(String[][] arr, String filename) {

		try {
			File file = new File(filename);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println("C PN Neigh1 Neigh2  ");
			for (int i = 0; i < arr.length; i++) {
				for (int j = 0; j < CONFIG_COL; j++) {
					printWriter.write(arr[i][j] + " ");
				}
				printWriter.println();
			}
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int countLines(String filename) throws IOException {
		LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
		while ((reader.readLine()) != null) {}
		int cnt = reader.getLineNumber(); 
		reader.close();
		return cnt;
	}
}
