/** Eck Exercise 11.1
 * Based on Eck's DirectoryList but prints out all files in 
 * directory and all subdirectory.
 * This program lists the files in a directory and subdirectories
 * specified by the user.  The user is asked to type in a 
 * directory name. If the name entered by the user is not a 
 * directory, a message is printed and the program ends.
 
 * @author jd07
 *
 */
import java.io.File;
import java.util.Scanner;

public class DirectoryListRecursive {

	public static void main(String[] args) {

		String rootDirectoryName;  // Root directory name entered by the user.
        File rootDirectory;        // File object referring to the directory.
        Scanner scanner;       // For reading a line of input from the user.

        scanner = new Scanner(System.in);  // scanner reads from standard input.

        System.out.print("Enter a directory name: ");
        rootDirectoryName = scanner.nextLine().trim();
        rootDirectory = new File(rootDirectoryName);

        if (rootDirectory.isDirectory() == false) {
            if (rootDirectory.exists() == false)
                System.out.println("There is no such directory!");
            else
                System.out.println("That file is not a directory.");
        }
        else {
        	recursiveFileList(rootDirectory);
//            files = rootDirectory.list();
//            System.out.println("Files in directory \"" + directory + "\":");
//            for (int i = 0; i < files.length; i++)
//                System.out.println("   " + files[i]);
        }

	} // end main

	/**
	 * Recursively search through subdirectories in dir
	 * and print file names.
	 * @param dir
	 */
	public static void recursiveFileList(File dir) {
		String[] list = dir.list();
		for (String filename : list) {
			File file = new File(dir,filename);
			if (file.isDirectory() == true)  
				recursiveFileList(file);
			else
				System.out.println(file.getAbsolutePath());
		}
		
	} // end recursiveFileList
}
