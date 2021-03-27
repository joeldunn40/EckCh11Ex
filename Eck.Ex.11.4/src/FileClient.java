
/** 
 * Eck Exercise 11.4
 * 
 * Client program to test FileServer
 * 
 * Write a client program for the server from Exercise 11.3. 
 * Design a user interface that will let the user do at least 
 * two things: (1) Get a list of files that are available on
 * the server and display the list on standard output; and (2) 
 * Get a copy of a specified file from the server and save it
 * to a local file (on the computer where the client is running).
 * 
 * @author jd07
 *
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {

	static final int LISTENING_PORT = 1776;

	/**
	 * Handshake string. Each end of the connection sends this string to the other
	 * just after the connection is opened. This is done to confirm that the program
	 * on the other side of the connection is a CLChat program.
	 */
	static final String HANDSHAKE = "FileServerProgram";

	public static void main(String[] args) {
		String hostName; // Name of the server computer to connect to.
		Socket connection; // A socket for communicating with server.
		BufferedReader incoming; // Stream for reading data from the server.
		PrintWriter outgoing; // Stream for sending data to the server.
		String messageIn; // Message received from server.
		String messageOut; // Message sent to server.
		Scanner userInput; // Wrapper for system.in: read lines of input from user.

		/* Get computer name from command line. */

		if (args.length > 0)
			hostName = args[0];
		else {
			Scanner stdin = new Scanner(System.in);
			System.out.print("Enter computer name or IP address: ");
			hostName = stdin.nextLine();
			stdin.close();
		}

		/* Make the connection, check handshake, send message, receive message. */
		try {
			System.out.println("Connecting to " + hostName + " on port " + LISTENING_PORT);
			connection = new Socket(hostName, LISTENING_PORT);
			incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			outgoing = new PrintWriter(connection.getOutputStream());
			outgoing.println(HANDSHAKE); // Send handshake to server.
			outgoing.flush();
			messageIn = incoming.readLine(); // Receive handshake from server.
			if (!messageIn.equals(HANDSHAKE)) {
				throw new IOException("Connected program is not" + HANDSHAKE + "!");
			}
			System.out.println("Connected.");
			System.out.println("Enter \"INDEX\" to get file list.");
			System.out.println("Enter \"GET <filename>\" to get file contents.");

			// Read command from client user
			userInput = new Scanner(System.in);
			messageOut = userInput.nextLine();
			userInput.close();

			// Send user's command to server.
			incoming = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			outgoing = new PrintWriter(connection.getOutputStream());
			outgoing.println(messageOut); // Send command to server.
			outgoing.flush();

			// Receive return message from server & print to client's screen.
			System.out.println("Server response:");
			messageIn = incoming.readLine();
			System.out.println(messageIn);
			
			if (messageIn.startsWith("0")) {
				System.out.println("SUCCESS!!");
				
				// Copy file for GET, everything else print out what is sent from server
				if ((messageOut.length() >= 3) && (messageOut.substring(0,3).equals("GET"))) {
					// Call the GET subroutine	
					String fileName = messageOut.substring(4); // all characters from "GET "		
					File file = new File(fileName); // name of file to create
					System.out.println("Attempting to copy " + fileName);
					// if file.exists(): overwrite? ignore?
					if (file.exists()) {
						// Ask user for permission to overwrite
						System.out.println("  " + fileName + " already exists. Exiting.");
						connection.close();
						return;
					}
					
					// File does not exist so can make copy
			        PrintWriter out; 
			        try {
			            FileWriter stream = new FileWriter(fileName); 
			            out = new PrintWriter( stream );
			        }
			        catch (Exception e) {
			            System.out.println("Sorry, but an error occurred\nwhile trying to open the file\nfor writing.\n" + e);
			            return;
			        }
			        
			        // Send text line by line
			        try {
						messageIn = incoming.readLine();
						while (messageIn != null) {
//							System.out.println(messageIn);
							out.println(messageIn);
							messageIn = incoming.readLine();
						}
			            out.flush();
			            out.close();
			            System.out.println("Finished writing file. Exiting.");
			            connection.close();
			            return;
			        } catch (Exception e) {
			        	System.out.println("Error writing to file " + e);
			        	return;
			        }

				} else { // INDEX command
					while (messageIn != null) {
						messageIn = incoming.readLine();
						System.out.println(messageIn);
					}
				} // end if GET
				
			} else {
				System.out.println("FAILURE!: closing connection.");
				connection.close();
				return;
			} // end if success/fail
			

		} catch (Exception e) {
			System.out.println("An error occurred while opening connection.");
			System.out.println(e.toString());
			return;
		}

	} // end main

}
