/**
 * Eck Exercise 11.3
 * 
 * Notes on Eck's solution:
 * Eck's solution used subroutines which throw error detected by if (out.checkError()).
 * This could avoid several nested try..catch.
 * Uses str.equalsIgnoreCase().startsWith("GET");
 * Client accepts all commands as arguments only: INDEX, GET sent internally.
 * No overwrite option etc (though could be -f command line option).
 * Uses try..catch..finally to close connections.
 * 
 * For this exercise, you will write a network server program. The program is a
 * simple file server that makes a collection of files available for
 * transmission to clients. When the server starts up, it needs to know the name
 * of the directory that contains the collection of files. This information can
 * be provided as a command-line argument. You can assume that the directory
 * contains only regular files (that is, it does not contain any
 * sub-directories). You can also assume that all the files are text files.
 * 
 * When a client connects to the server, the server first reads a one-line
 * command from the client. The command can be the string "INDEX". In this case,
 * the server responds by sending a list of names of all the files that are
 * available on the server. Or the command can be of the form "GET <filename>",
 * where <filename> is a file name. The server checks whether the requested file
 * actually exists. If so, it first sends the word "OK" as a message to the
 * client. Then it sends the contents of the file and closes the connection.
 * Otherwise, it sends a line beginning with the word "ERROR" to the client and
 * closes the connection. (The error response can include an error message on
 * the rest of the line.)
 * 
 * Your program should use a subroutine to handle each request that the server
 * receives. It should not stop after handling one request; it should remain
 * open and continue to accept new requests. See the DirectoryList example in
 * Subsection 11.2.2 for help with the problem of getting the list of files in
 * the directory.
 * 
 * On command send single line indicating success or failure on initiating send
 * 0: Success (following lines will be file data or list)
 * 1: Error (following lines will be error messages)
 * @author jd07
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Date;
import java.util.Scanner;

public class FileServer {

	/**
	 * Port to listen on, if none is specified on the command line.
	 */
	static final int LISTENING_PORT = 1776;

	/**
	 * Handshake string. Each end of the connection sends this  string to the 
	 * other just after the connection is opened.  This is done to confirm that 
	 * the program on the other side of the connection is a CLChat program.
	 */
	static final String HANDSHAKE = "FileServerProgram";
	
	static File directory;        // File object referring to the directory.


	public static void main(String[] args) {

		String directoryName;   // The directory the file server has access to.

		ServerSocket listener;  // Listens for a connection request.
		Socket connection;      // For communication with the client.

		BufferedReader incoming;  // Stream for receiving data from client.
		PrintWriter outgoing;     // Stream for sending data to client.
		String messageOut;        // A message to be sent to the client.
		String messageIn;         // A message received from the client.

		/* First, get the directory path from the command line */
		if (args.length > 0) 
			directoryName = args[0];
		else {
			Scanner stdin = new Scanner(System.in);
			System.out.print("Enter directory name: ");
			directoryName = stdin.nextLine();
		}
		directory = new File(directoryName);

		// Check is a directory 
		if (directory.isDirectory() == false) {
			if (directory.exists() == false) {
				System.out.println("There is no such directory! Exiting.");
				return;
			} else {
				System.out.println("That file is not a directory. Exiting.");
				return;
			}
		}

		// At this point directory has been identified.

		/* Wait for a connection request.  When it arrives, close
       down the listener.  Create streams for communication
       and exchange the handshake. */

		try {
			listener = new ServerSocket(LISTENING_PORT);
			System.out.println("Listening on port " + listener.getLocalPort());
			while(true) {
				connection = listener.accept();

				System.out.println("Connection from " +  
						connection.getInetAddress().toString() );

				// listener.close(); // Close client socket instead. 
				incoming = new BufferedReader( 
						new InputStreamReader(connection.getInputStream()) );
				outgoing = new PrintWriter(connection.getOutputStream());
				outgoing.println(HANDSHAKE);  // Send handshake to client.
				outgoing.flush();
				messageIn = incoming.readLine();  // Receive handshake from client.
				if (! HANDSHAKE.equals(messageIn) ) {
					throw new Exception("Connected program is not" + HANDSHAKE + "!");
				}
				System.out.println("Connected.  Waiting for the command.");

				// Process Commands from Client
				incoming = new BufferedReader( 
						new InputStreamReader(connection.getInputStream()) );
				messageIn = incoming.readLine();  // Read line from client.
				if (messageIn.equals("INDEX")) {
					// Call the INDEX subroutine
					System.out.println("Received INDEX command, fetching directory list.");
					sendDirectoryList(connection);
				} else if ((messageIn.length() >= 3) && (messageIn.substring(0,3).equals("GET"))) {
					// Call the GET subroutine	
					System.out.println("Received GET command, fetching file contents.");
					String fileName = messageIn.substring(4); // all characters from "GET "		
					File fullPath = new File(directory,fileName);
					if ( (fullPath.exists() == true) && (fullPath.isDirectory() == false) ) {
						sendFileContents(connection,fullPath);
					} else {
						System.out.println("Filename provided is a directory not a file: " + fullPath);
						sendCommandError(connection,"1: Filename provided is a directory not a file: " + fullPath);						
					}
				} else {
					System.out.println("Do not recognise command: " + messageIn);
					sendCommandError(connection,"1: Do not recognise command, try again");					
				}

				// Close connection at this point: may want to do this subroutines?
//				connection.close();

			} // end while
		}
		catch (Exception e) {
			System.out.println("An error occurred while opening connection.");
			System.out.println(e.toString());
			return;
		}

	}  // end main()

	private static void sendCommandError(Socket client, String string) {
		try {
			PrintWriter outgoing;   // Stream for sending data.
			outgoing = new PrintWriter( client.getOutputStream() );
			outgoing.println( string );
			outgoing.flush();  // Make sure the data is actually sent!
			// client.close(); // Do not close 
		}
		catch (Exception e){
			System.out.println("Error: " + e);
		}
	} // end sendCommandError

	private static void sendFileContents(Socket client, File fullPath) {
//		String fileName = messageIn.substring(4); // all characters from "GET "		
//		File fullPath = new File(directory,fileName);
		try {
			PrintWriter outgoing;   // Stream for sending data.
			outgoing = new PrintWriter( client.getOutputStream() );

			// nested try..catch - not good practice?
//			try (BufferedReader in = new BufferedReader( new FileReader( fullPath ) ) ) {
			BufferedReader in = new BufferedReader( new FileReader( fullPath ) ); 
				String line = in.readLine();
				outgoing.println( "0: File contents of " + fullPath );
				while ( line != null ) {
					outgoing.println( line );
					line = in.readLine();
					outgoing.flush();  // Make sure the data is actually sent!
				}
//			}	
//			catch (FileNotFoundException e) {
//				System.out.println("Could not find " + fileName);
//				System.out.println(e);
//				outgoing.println( "Could not find " + fileName );
//			} 
//			catch (IOException e) {
//				System.out.println("Problem reading " + fileName);
//				System.out.println(e);
//				outgoing.println("Problem reading " + fileName);
//				outgoing.println("Error message:");
//				outgoing.println(e);
//			}

			outgoing.flush();  // Make sure the data is actually sent!
			client.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Could not find " + fullPath);
			System.out.println(e);
			// How could we catch AND send error messages to client?
			// e.g. if creating outgoing outputStream failed?
//			outgoing.println( "Could not find " + fileName );
		} 
		catch (IOException e) {
			System.out.println("Problem reading " + fullPath);
			System.out.println(e);
//			outgoing.println("Problem reading " + fileName);
//			outgoing.println("Error message:");
//			outgoing.println(e);
		}
		catch (Exception e){
			System.out.println("Error: " + e);
		}


	}

	private static void sendDirectoryList(Socket client) {

		try {
			PrintWriter outgoing;   // Stream for sending data.
			outgoing = new PrintWriter( client.getOutputStream() );
			outgoing.flush();  // Make sure the data is actually sent!
			String[] files = directory.list();
			System.out.println("Found " + files.length + " files.");
			outgoing.println("0: Found " + files.length + " files from " + directory.getName() + ":");
			for (int i = 0; i < files.length; i++) {
				System.out.println("   " + files[i]);
				outgoing.println("   " + files[i]);
				outgoing.flush();  // Make sure the data is actually sent!
			}
			outgoing.flush();  // Make sure the data is actually sent!
			client.close();
		}
		catch (Exception e){
			System.out.println("Error: " + e);
		}

	}

}
