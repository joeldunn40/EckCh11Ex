/** 
 * Eck Exercise 11.3
 * 
 * Client program to test FileServer
 * 
 * Adpated from DateClient:
 * This program opens a connection to a computer specified
 * as the first command-line argument.  If no command-line
 * argument is given, it prompts the user for a computer
 * to connect to.  The connection is made to
 * the port specified by LISTENING_PORT.  The program reads one
 * line of text from the connection and then closes the
 * connection.  It displays the text that it reads on
 * standard output.  This program is meant to be used with
 * the server program, FileServer, which sends the file list
 * or contents from the location the server has been told to 
 * use.

 * @author jd07
 *
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {

	static final int LISTENING_PORT = 1776;

	/**
	 * Handshake string. Each end of the connection sends this  string to the 
	 * other just after the connection is opened.  This is done to confirm that 
	 * the program on the other side of the connection is a CLChat program.
	 */
	static final String HANDSHAKE = "FileServerProgram";
	
	public static void main(String[] args) {
        String hostName;         // Name of the server computer to connect to.
        Socket connection;       // A socket for communicating with server.
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
        	connection = new Socket(hostName,LISTENING_PORT);
        	incoming = new BufferedReader(
        			new InputStreamReader(connection.getInputStream()) );
        	outgoing = new PrintWriter(connection.getOutputStream());
        	outgoing.println(HANDSHAKE);  // Send handshake to server.
        	outgoing.flush();
        	messageIn = incoming.readLine();  // Receive handshake from server.
        	if (! messageIn.equals(HANDSHAKE) ) {
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
            incoming = new BufferedReader(
            		new InputStreamReader(connection.getInputStream()) );
            outgoing = new PrintWriter(connection.getOutputStream());
            outgoing.println(messageOut);  // Send command to server.
            outgoing.flush();

            // Receive return message from server & print to client's screen.
            System.out.println("Server response:");
            messageIn = incoming.readLine(); 
            while (messageIn != null) {
            	System.out.println(messageIn);
            	messageIn = incoming.readLine(); 
            }
        	
        }
        catch (Exception e) {
        	System.out.println("An error occurred while opening connection.");
        	System.out.println(e.toString());
        	return;
        }

	} // end main

}
