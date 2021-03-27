import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * Eck Exercise 11.5
 * 
 * The sample program PhoneDirectoryFileDemo.java, from Subsection 11.3.2,
 * stores name/number pairs for a simple phone book in a text file in the user's
 * home directory. Modify that program so that it uses an XML format for the
 * data. The only significant changes that you will have to make are to the
 * parts of the program that read and write the data file. Use the DOM to read
 * the data, as discussed in Subsection 11.5.2. You can use the XML format
 * illustrated in the following sample phone directory file:
 * 
 * <?xml version="1.0"?> <phone_directory>
 * <entry name='barney' number='890-1203'/>
 * <entry name='fred' number='555-9923'/> </phone_directory>
 * 
 * @author jd07
 *
 */
public class PhoneDirectoryXML {

	// Name of file to store data
	private static String DATA_FILE_NAME = "phone_book_demo.xml";


	public static void main(String[] args) {
		String name, number;  // Name and number of an entry in the directory
		// (used at various places in the program).

		TreeMap<String,String>  phoneBook;   // Phone directory data structure.
		// Entries are name/number pairs.

		phoneBook = new TreeMap<String,String>();


		/* Create a dataFile variable of type File to represent the
		 * data file that is stored in the user's home directory.
		 */

		File userHomeDirectory = new File( System.getProperty("user.home") );
		File dataFile = new File( userHomeDirectory, DATA_FILE_NAME );


		/* If the data file already exists, then the data in the file is
		 * read and is used to initialize the phone directory.  The format
		 * of the file must be as follows:  Each line of the file represents
		 * one directory entry, with the name and the number for that entry
		 * separated by the character '%'.  If a file exists but does not
		 * have this format, then the program terminates; this is done to
		 * avoid overwriting a file that is being used for another purpose.
		 */

		if ( ! dataFile.exists() ) {
			System.out.println("No phone book data file found.  A new one");
			System.out.println("will be created, if you add any entries.");
			System.out.println("File name:  " + dataFile.getAbsolutePath());
		}
		else {
			/**
			 * Read XML file here
			 */
			System.out.println("Reading phone book data...");
			
	        Document xmldoc;
			try {
				DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            xmldoc = docReader.parse(dataFile);
			} catch (Exception e) {
				System.out.println("Error creating xmldoc object from file: " + e);
				System.out.println("This program cannot continue.");
				System.exit(1);
				return; // allows for possibility of xmldoc being uninitialized
			}
			try {
				// Check XML is for this program, and correct version
	            Element rootElement = xmldoc.getDocumentElement();
	            if ( ! rootElement.getNodeName().equals("phonedirectoryxml") )
	                throw new Exception("File is not a PhoneDirectoryXML file.");
	            String version = rootElement.getAttribute("version");
	            
	            // Don't need this try..catch
	            // rename Exception to NumberFormatException place as additional
	            // catch in the outer try..catch loop
	            try {
	                double versionNumber = Double.parseDouble(version);
	                System.out.println("Reading version number " + versionNumber);
	                if (versionNumber > 1.0)
	                    throw new Exception("File requires a newer version of PhoneDirectoryXML.");
	            }
	            catch (Exception e) {
	            	System.out.println("Error reading XML: ");
	            	return;
	            }

	            // Start reading name-number pairs
	            // <entry name='barney' number='890-1203'/>
	            NodeList nodes = rootElement.getChildNodes();
	            for (int i = 0; i < nodes.getLength(); i++) {
	                if (nodes.item(i) instanceof Element) {
	                    Element element = (Element)nodes.item(i);
	                    if (element.getTagName().equals("entry")) {
	                        name = element.getAttribute("name");
	                        number = element.getAttribute("number");
	                        phoneBook.put(name, number);
	                    } // end if element tag entry
	                } // end if node Element
	            } // end for: node.length (entries)
	            System.out.println("Found " + phoneBook.size() + " entries.");
			}
			catch (Exception e) {
				System.out.println("Error reading data in phone book data file.");
				System.out.println("File name:  " + dataFile.getAbsolutePath());
				System.out.println("This program cannot continue.");
				System.exit(1);
			}
		} // end if read phone book


		/* Read commands from the user and carry them out, until the
		 * user gives the "Exit from program" command.
		 */
		// Leave as is.	
		Scanner in = new Scanner( System.in );
		boolean changed = false;  // Have any changes been made to the directory?

		mainLoop: while (true) {
			System.out.println("\nSelect the action that you want to perform:");
			System.out.println("   1.  Look up a phone number.");
			System.out.println("   2.  Add or change a phone number.");
			System.out.println("   3.  Remove an entry from your phone directory.");
			System.out.println("   4.  List the entire phone directory.");
			System.out.println("   5.  Exit from the program.");
			System.out.println("Enter action number (1-5):  ");
			int command;
			if ( in.hasNextInt() ) {
				command = in.nextInt();
				in.nextLine();
			}
			else {
				System.out.println("\nILLEGAL RESPONSE.  YOU MUST ENTER A NUMBER.");
				in.nextLine();
				continue;
			}
			switch(command) {
			case 1:
				System.out.print("\nEnter the name whose number you want to look up: ");
				name = in.nextLine().trim().toLowerCase();
				number = phoneBook.get(name);
				if (number == null)
					System.out.println("\nSORRY, NO NUMBER FOUND FOR " + name);
				else
					System.out.println("\nNUMBER FOR " + name + ":  " + number);
				break;
			case 2:
				System.out.print("\nEnter the name: ");
				name = in.nextLine().trim().toLowerCase();
				if (name.length() == 0)
					System.out.println("\nNAME CANNOT BE BLANK.");
				else if (name.indexOf('%') >= 0)
					System.out.println("\nNAME CANNOT CONTAIN THE CHARACTER \"%\".");
				else { 
					System.out.print("Enter phone number: ");
					number = in.nextLine().trim();
					if (number.length() == 0)
						System.out.println("\nPHONE NUMBER CANNOT BE BLANK.");
					else {
						phoneBook.put(name,number);
						changed = true;
					}
				}
				break;
			case 3:
				System.out.print("\nEnter the name whose entry you want to remove: ");
				name = in.nextLine().trim().toLowerCase();
				number = phoneBook.get(name);
				if (number == null)
					System.out.println("\nSORRY, THERE IS NO ENTRY FOR " + name);
				else {
					phoneBook.remove(name);
					changed = true;
					System.out.println("\nDIRECTORY ENTRY REMOVED FOR " + name);
				}
				break;
			case 4:
				System.out.println("\nLIST OF ENTRIES IN YOUR PHONE BOOK:\n");
				for ( Map.Entry<String,String> entry : phoneBook.entrySet() )
					System.out.println("   " + entry.getKey() + ": " + entry.getValue() );
				break;
			case 5:
				System.out.println("\nExiting program.");
				break mainLoop;
			default:
				System.out.println("\nILLEGAL ACTION NUMBER.");
			}
		}


		/* Before ending the program, write the current contents of the
		 * phone directory, but only if some changes have been made to
		 * the directory.
		 */

		if (changed) {
						
			System.out.println("Saving phone directory changes to file " + 
					dataFile.getAbsolutePath() + " ...");
			PrintWriter out;
			try {
				out = new PrintWriter( new FileWriter(dataFile) );
			}
			catch (IOException e) {
				System.out.println("ERROR: Can't open data file for output.");
				return;
			}
			
			// Write XML here
			// <?xml version="1.0"?> <phone_directory>
			// <entry name='barney' number='890-1203'/>
			// <entry name='fred' number='555-9923'/> </phone_directory>
			out.println("<?xml version=\"1.0\"?>");
			out.println("<phonedirectoryxml version=\"1.0\">");
			for ( Map.Entry<String,String> entry : phoneBook.entrySet() )
				out.println("<entry name='" + entry.getKey() + "' number='" + entry.getValue() + "'/>" );
			out.println("</phonedirectoryxml>");
			out.flush();
			out.close();
			if (out.checkError())
				System.out.println("ERROR: Some error occurred while writing data file.");
			else
				System.out.println("Done.");
		} // end if changed

	} // end main

} // end Class PhoneDirectoryXML

