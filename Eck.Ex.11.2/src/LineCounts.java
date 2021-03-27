import java.io.*;

/** Eck Exercise 11.2
 * Command line program that counts the number
 * of lines in the text files entered by the user
 * at the command line:
 * java LineCounts file1.txt file2.txt file3.txt
 * @author jd07
 *
 */
public class LineCounts {

	public static void main(String[] args) {

//		String fileName = "C:\\Users\\jd07\\eclipse-workspace-eck\\EckCh11Examples\\src\\DirectoryList.java";
		String fileName;

		System.out.println(args.length + " inputs.");

		int lineCount; // integer to keep line counts
//		BufferedReader in;  // BufferedReader for reading from FileReader.

		for (int i = 0; i < args.length; i++) {
			lineCount = 0;
			fileName = args[i];
			
			try (BufferedReader in = new BufferedReader( new FileReader( fileName) ) ) {
				String line = in.readLine();
				while ( line != null ) {
					line = in.readLine();
					lineCount++;
				}
				System.out.println("Found " + lineCount + " lines in " + fileName);
			}	
			catch (FileNotFoundException e) {
					System.out.println("Could not find " + fileName);
					System.out.println(e);
			} 
			catch (IOException e) {
				System.out.println("Problem reading " + fileName);
				System.out.println(e);
			}

			

//			Alternative: requires "BufferedReader in;" uncommented.
//			try {
//				in = new BufferedReader( new FileReader( fileName) );
//				try {
//					String line = in.readLine();
//					while ( line != null ) {
//						line = in.readLine();
//						lineCount++;
//					}
//					System.out.println("Found " + lineCount + " lines in " + fileName);
//				} catch (IOException e) {
//					System.out.println("Problem reading " + fileName);
//					System.out.println(e);
//				}
//
//			}
//			catch (FileNotFoundException e) {
//				System.out.println("Could not find " + fileName);
//				System.out.println(e);
//			}
		} // end for args
		System.out.println("---Done---");

	} // end main

}
