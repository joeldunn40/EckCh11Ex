
/** Eck Exercise 11.6
The sample program Checkers.java from Subsection 7.5.3
lets two players play checkers. It would be nice if, 
in the middle of a game, the state of the game could be 
saved to a file. Later, the file could be read back into 
the file to restore the game and allow the players to 
continue. Add the ability to save and load files to the 
checkers program. Design a simple text-based format for the files. 
It's a little tricky to completely restore the state of a game. 
The program has a variable board of type CheckersData that 
stores the current contents of the board, and it has a variable 
currentPlayer of type int that indicates whether Red or Black is 
currently moving. This data must be stored in the file when a file 
is saved. When a file is read into the program, you should read the 
data into two local variables newBoard of type CheckersData 
and newCurrentPlayer of type int. Once you have successfully read 
all the data from the file, you can use the following code to set 
up the program state correctly. This code assumes that you have 
introduced two new variables saveButton and loadButton of type Button 
to represent the "Save Game" and "Load Game" buttons:

board = newBoard;  // Set up game with data read from file.
currentPlayer = newCurrentPlayer;
legalMoves = board.getLegalMoves(currentPlayer);
selectedRow = -1;
gameInProgress = true;
newGameButton.setDisable(true);
loadButton.setDisable(true);
saveButton.setDisable(false);
resignButton.setDisable(false);
if (currentPlayer == CheckersData.RED)
   message.setText("Game loaded -- it's RED's move.");
else
   message.setText("Game loaded -- it's BLACK's move.");
drawBoard();

 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This panel lets two users play checkers against each other. Red always starts
 * the game. If a player can jump an opponent's piece, then the player must
 * jump. When a player can make no more moves, the game ends.
 */
public class CheckersWithSave extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	// ---------------------------------------------------------------------

	CheckersBoard board; // A canvas on which a checker board is drawn,
	// defined by a static nested subclass. Much of
	// the game logic is defined in this class.

	private Button newGameButton; // Button for starting a new game.

	private Button resignButton; // Button that a player can use to end
	// the game by resigning.
	
	private Button saveButton; // User can save game state to file
	private Button loadButton; // User can load game state from previously saved file.

	private Label message; // Label for displaying messages to the user.
	
	private File editFile; // File user can use to save game state to.
	private Stage window; // Stage used to 'own' file dialog boxes

	/**
	 * The constructor creates the Board (which in turn creates and manages the
	 * buttons and message label), adds all the components, and sets the bounds of
	 * the components. A null layout is used. (This is the only thing that is done
	 * in the main Checkers class.)
	 */
	public void start(Stage stage) {

		/* Create the label that will show messages. */

		window = stage;
		
		message = new Label("Click \"New Game\" to begin.");
		message.setTextFill(Color.rgb(100, 255, 100)); // Light green.
		message.setFont(Font.font(null, FontWeight.BOLD, 18));

		/*
		 * Create the buttons and the board. The buttons MUST be created first, since
		 * they are used in the CheckerBoard constructor!
		 */

		newGameButton = new Button("New Game");
		resignButton = new Button("Resign");
		saveButton = new Button("Save Game");
		loadButton = new Button("Load Game");
		
		board = new CheckersBoard(); // a subclass of Canvas, defined below
		board.drawBoard(); // draws the content of the checkerboard

		/*
		 * Set up ActionEvent handlers for the buttons and a MousePressed handler for
		 * the board. The handlers call instance methods in the board object.
		 */

		newGameButton.setOnAction(e -> board.doNewGame());
		resignButton.setOnAction(e -> board.doResign());
		saveButton.setOnAction(e -> board.doSaveGame());
		loadButton.setOnAction(e -> board.doLoadGame());
		board.setOnMousePressed(e -> board.mousePressed(e));

		/* Set the location of each child by calling its relocate() method */

		board.relocate(20, 20);
		newGameButton.relocate(370, 100);
		resignButton.relocate(370, 220);
		saveButton.relocate(370, 140);
		loadButton.relocate(370, 180);
		message.relocate(20, 370);

		/*
		 * Set the sizes of the buttons. For this to have an effect, make the butons
		 * "unmanaged." If they are managed, the Pane will set their sizes.
		 */

		resignButton.setManaged(false);
		resignButton.resize(100, 30);
		newGameButton.setManaged(false);
		newGameButton.resize(100, 30);
		saveButton.setManaged(false);
		saveButton.resize(100, 30);
		loadButton.setManaged(false);
		loadButton.resize(100, 30);

		/*
		 * Create the Pane and give it a preferred size. If the preferred size were not
		 * set, the unmanaged buttons would not be included in the Pane's computed
		 * preferred size.
		 */

		Pane root = new Pane();

		root.setPrefWidth(500);
		root.setPrefHeight(420);

		/* Add the child nodes to the Pane and set up the rest of the GUI */

		root.getChildren().addAll(board, newGameButton, resignButton, saveButton, loadButton, message);
		root.setStyle("-fx-background-color: darkgreen; " + "-fx-border-color: darkred; -fx-border-width:3");
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Checkers!");
		stage.show();

	} // end start()

	// -------------------- Nested Classes -------------------------------

	/**
	 * A CheckersMove object represents a move in the game of Checkers. It holds the
	 * row and column of the piece that is to be moved and the row and column of the
	 * square to which it is to be moved. (This class makes no guarantee that the
	 * move is legal.)
	 */
	private static class CheckersMove {
		int fromRow, fromCol; // Position of piece to be moved.
		int toRow, toCol; // Square it is to move to.

		CheckersMove(int r1, int c1, int r2, int c2) {
			// Constructor. Just set the values of the instance variables.
			fromRow = r1;
			fromCol = c1;
			toRow = r2;
			toCol = c2;
		}

		boolean isJump() {
			// Test whether this move is a jump. It is assumed that
			// the move is legal. In a jump, the piece moves two
			// rows. (In a regular move, it only moves one row.)
			return (fromRow - toRow == 2 || fromRow - toRow == -2);
		}
	} // end class CheckersMove.

	/**
	 * This canvas displays a 320-by-320 checkerboard pattern with a 2-pixel dark
	 * red border. The canvas will be exactly 324-by-324 pixels. This class contains
	 * methods that are called in response to a mouse click on the canvas and in
	 * response to clicks on the New Game and Resign buttons. Note that the "New
	 * Game" and "Resign" buttons must be created before the Board constructor is
	 * called, since the constructor references the buttons (in the call to
	 * doNewGame()).
	 */
	private class CheckersBoard extends Canvas {

		CheckersData board; // The data for the checkers board is kept here.
		// This board is also responsible for generating
		// lists of legal moves.

		boolean gameInProgress; // Is a game currently in progress?

		/* The next three variables are valid only when the game is in progress. */

		int currentPlayer; // Whose turn is it now? The possible values
		// are CheckersData.RED and CheckersData.BLACK.

		int selectedRow, selectedCol; // If the current player has selected a piece to
		// move, these give the row and column
		// containing that piece. If no piece is
		// yet selected, then selectedRow is -1.

		CheckersMove[] legalMoves; // An array containing the legal moves for the
		// current player.

		/**
		 * Constructor. Creates a CheckersData to represent the contents of the
		 * checkerboard, and calls doNewGame to start the first game.
		 */
		CheckersBoard() {
			super(324, 324); // canvas is 324-by-324 pixels
			board = new CheckersData();
			doNewGame();
		}

		void doLoadGame() {
			/*
			board = newBoard;  // Set up game with data read from file.
			currentPlayer = newCurrentPlayer;
			legalMoves = board.getLegalMoves(currentPlayer);
			selectedRow = -1;
			gameInProgress = true;
			newGameButton.setDisable(true);
			loadButton.setDisable(true);
			saveButton.setDisable(false);
			resignButton.setDisable(false);
			if (currentPlayer == CheckersData.RED)
			   message.setText("Game loaded -- it's RED's move.");
			else
			   message.setText("Game loaded -- it's BLACK's move.");
			drawBoard();
			*/
			/* Do we need new CheckersData constructor to set newBoard pieces?
			   No, board.board[][] is visible in this class. 
			   Eck's solution does create setPieceAt method in CheckersData class
			   then newBoard is instance of CheckersData class.
			*/
			
			// GET FILE
	        FileChooser fileDialog = new FileChooser();
	        
	        // This allows user to keep overwriting a file previously saved easily
	        if (editFile == null) {
	            // No file is being edited.  Set file name in dialog to "filename.txt"
	            // and set the directory in the dialog to the user's home directory.
	            fileDialog.setInitialFileName("filename.xml");
	            fileDialog.setInitialDirectory( new File( System.getProperty("user.home")));
	        }
	        else {
	            // Get the file name and directory for the dialog from
	            // the file that is currently being edited.
	            fileDialog.setInitialFileName(editFile.getName());
	            fileDialog.setInitialDirectory(editFile.getParentFile());
	        }
	        
	        fileDialog.setTitle("Select File to be loaded");
	        // By setting an owner for showSaveDialog, further interaction can be blocked.
	        // Need to set a global Stage variable "window".
	        File selectedFile = fileDialog.showOpenDialog(window);
	        if ( selectedFile == null )
	            return;  // User did not select a file.
	        // Note: User has selected a file AND if the file exists has
	        //    confirmed that it is OK to erase the exiting file.

			// OPEN XML
	        Document xmldoc;
			try {
				DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            xmldoc = docReader.parse(selectedFile);
			} catch (Exception e) {
				System.out.println("Error creating xmldoc object from file: " + e);
				System.out.println("This program cannot continue.");
				System.exit(1);
				return; // allows for possibility of xmldoc being uninitialized
			}
			System.out.println("Opened XML");
			// PARSE XML
			try {
				// Check XML is for this program, and correct version
				Element rootElement = xmldoc.getDocumentElement();
				if ( ! rootElement.getNodeName().equals("checkerwithsave") )
					throw new Exception("File is not a checkerwithsave file.");
				String version = rootElement.getAttribute("version");
				double versionNumber = Double.parseDouble(version);
				System.out.println("Reading version number " + versionNumber);
				if (versionNumber > 1.0)
					throw new NumberFormatException("File requires a newer version of CheckersWithSave.");

				NodeList nodes = rootElement.getChildNodes();
				System.out.println("Found " + nodes.getLength() + " nodes.");

				// Variables to store game state variables from XML file.
				int newCurrentPlayer = 0;
                int[][] newBoard = new int[8][8];
                for (int i = 0; i < 8; i++)
                	for (int j = 0; j < 8; j++)
                		newBoard[i][j]=0;

                // Check that complete game state variable read before resetting game.
                boolean successReadPlayer = false;
                boolean successReadPieces = false;
                
				for (int i = 0; i < nodes.getLength(); i++) {
					if (nodes.item(i) instanceof Element) {
						System.out.println("is element");
						Element element = (Element)nodes.item(i);
						if (element.getTagName().equals("player")) {
							newCurrentPlayer = Integer.parseInt(element.getAttribute("currentplayer"));
							System.out.println("Current player = " + newCurrentPlayer);
							successReadPlayer = true;
						} else if (element.getTagName().equals("pieces")) {
	                        NodeList pieceNodes = element.getChildNodes();
	                        int row, col, value;
	                        for (int j = 0; j < pieceNodes.getLength(); j++) {
	                            if (pieceNodes.item(j) instanceof Element) {
	                                Element pieceElement = (Element)pieceNodes.item(j);
	                                if (pieceElement.getTagName().equals("location")) {
	                                	row = Integer.parseInt(pieceElement.getAttribute("row"));
	                                	col = Integer.parseInt(pieceElement.getAttribute("col"));
	                                	value = Integer.parseInt(pieceElement.getAttribute("value"));
	        							System.out.println("Piece " + value + " at " + row + ", " + col + ".");
	        							newBoard[row][col] = value;
	        							successReadPieces = true;
	                                }
	                            }
	                        } // end for: pieces:locations
						}// end if: player, pieces
					} // end if node Element
				} // end for: node.length
								
				if (successReadPlayer && successReadPieces) {
					// Now set up board
					board.board = newBoard;  // Set up game with data read from file.
					currentPlayer = newCurrentPlayer;
					legalMoves = board.getLegalMoves(currentPlayer);
					selectedRow = -1;
					gameInProgress = true;
					newGameButton.setDisable(true);
					loadButton.setDisable(true); // Do we really want this?
					saveButton.setDisable(false);
					resignButton.setDisable(false);
					if (currentPlayer == CheckersData.RED)
						message.setText("Game loaded -- it's RED's move.");
					else
						message.setText("Game loaded -- it's BLACK's move.");
					drawBoard();
				} else {
					throw new Exception("Could not succesfully read current player or piece locations.");
				}
				
				
			}
			catch (NumberFormatException e) {
				System.out.println("Error reading XML: " + e);
				return;
			}
			catch (Exception e) {
				System.out.println("Error reading data in file.");
				System.out.println("File name:  " + selectedFile.getAbsolutePath());
				System.out.println("Error:  " + e);
				System.out.println("This program cannot continue.");
				System.exit(1);
			}

			
			
		} // end doLoadGame

		void doSaveGame() {
			// Call file chooser
			// Check can open
			// Copied from SimplePaintWithXML
	        FileChooser fileDialog = new FileChooser();
	        
	        // This allows user to keep overwriting a file previously saved easily
	        if (editFile == null) {
	            // No file is being edited.  Set file name in dialog to "filename.txt"
	            // and set the directory in the dialog to the user's home directory.
	            fileDialog.setInitialFileName("filename.xml");
	            fileDialog.setInitialDirectory( new File( System.getProperty("user.home")));
	        }
	        else {
	            // Get the file name and directory for the dialog from
	            // the file that is currently being edited.
	            fileDialog.setInitialFileName(editFile.getName());
	            fileDialog.setInitialDirectory(editFile.getParentFile());
	        }
	        
	        fileDialog.setTitle("Select File to be Saved");
	        // By setting an owner for showSaveDialog, further interaction can be blocked.
	        // Need to set a global Stage variable "window".
	        File selectedFile = fileDialog.showSaveDialog(window);
	        if ( selectedFile == null )
	            return;  // User did not select a file.
	        // Note: User has selected a file AND if the file exists has
	        //    confirmed that it is OK to erase the exiting file.
	        
	        // Now create print stream
	        PrintWriter out; 
	        try {
	            FileWriter stream = new FileWriter(selectedFile); 
	            out = new PrintWriter( stream );
	        }
	        catch (Exception e) {
	            Alert errorAlert = new Alert(Alert.AlertType.ERROR,
	                    "Sorry, but an error occurred\nwhile trying to open the file\nfor writing.");
	            errorAlert.showAndWait();
	            return;
	        }

			/* 
			 * Get game state:
			 * gameInProgress - not needed? Global
			 * currentPlayer - CheckerBoard attribute 
			 * board = CheckersData object
			 * board.pieceAt() gets piece value at location [8][8]
			 * Could save all locations in data file (64)
			 * Or just save squares with pieces on them
			 */
            out.println("<?xml version=\"1.0\"?>");
			out.println("<checkerwithsave version=\"1.0\">");
			out.println("<player currentplayer='" + currentPlayer + "'/>");
			out.println("<pieces>");
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					int val = board.pieceAt(i, j);
					if (val > 0) {
//						System.out.println("board["+i+"]["+j+"] = "+val);
						out.println("<location row='" + i + "' col='" + j + "' value='" + val + "'/>");
					}
				}
			}
			out.println("</pieces>");
			out.println("</checkerwithsave>");
			out.flush();
			out.close();
		} // end doSaveGame

		/**
		 * Start a new game. This method is called when the Board is first created and
		 * when the "New Game" button is clicked. Event handling is set up in the
		 * start() method in the main class.
		 */
		void doNewGame() {
			if (gameInProgress == true) {
				// This should not be possible, but it doesn't hurt to check.
				message.setText("Finish the current game first!");
				return;
			}
			board.setUpGame(); // Set up the pieces.
			currentPlayer = CheckersData.RED; // RED moves first.
			legalMoves = board.getLegalMoves(CheckersData.RED); // Get RED's legal moves.
			selectedRow = -1; // RED has not yet selected a piece to move.
			message.setText("Red:  Make your move.");
			gameInProgress = true;
			newGameButton.setDisable(true);
			resignButton.setDisable(false);
			drawBoard();
		}

		/**
		 * Current player resigns. Game ends. Opponent wins. This method is called when
		 * the user clicks the "Resign" button. Event handling is set up in the start()
		 * method in the main class.
		 */
		void doResign() {
			if (gameInProgress == false) { // Should be impossible.
				message.setText("There is no game in progress!");
				return;
			}
			if (currentPlayer == CheckersData.RED)
				gameOver("RED resigns.  BLACK wins.");
			else
				gameOver("BLACK resigns.  RED wins.");
		}

		/**
		 * The game ends. The parameter, str, is displayed as a message to the user. The
		 * states of the buttons are adjusted so players can start a new game. This
		 * method is called when the game ends at any point in this class.
		 */
		void gameOver(String str) {
			message.setText(str);
			newGameButton.setDisable(false);
			resignButton.setDisable(true);
			gameInProgress = false;
		}

		/**
		 * This is called by mousePressed() when a player clicks on the square in the
		 * specified row and col. It has already been checked that a game is, in fact,
		 * in progress.
		 */
		void doClickSquare(int row, int col) {

			/*
			 * If the player clicked on one of the pieces that the player can move, mark
			 * this row and col as selected and return. (This might change a previous
			 * selection.) Reset the message, in case it was previously displaying an error
			 * message.
			 */

			for (int i = 0; i < legalMoves.length; i++)
				if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
					selectedRow = row;
					selectedCol = col;
					if (currentPlayer == CheckersData.RED)
						message.setText("RED:  Make your move.");
					else
						message.setText("BLACK:  Make your move.");
					drawBoard();
					return;
				}

			/*
			 * If no piece has been selected to be moved, the user must first select a
			 * piece. Show an error message and return.
			 */

			if (selectedRow < 0) {
				message.setText("Click the piece you want to move.");
				return;
			}

			/*
			 * If the user clicked on a square where the selected piece can be legally
			 * moved, then make the move and return.
			 */

			for (int i = 0; i < legalMoves.length; i++)
				if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
						&& legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
					doMakeMove(legalMoves[i]);
					return;
				}

			/*
			 * If we get to this point, there is a piece selected, and the square where the
			 * user just clicked is not one where that piece can be legally moved. Show an
			 * error message.
			 */

			message.setText("Click the square you want to move to.");

		} // end doClickSquare()

		/**
		 * This is called when the current player has chosen the specified move. Make
		 * the move, and then either end or continue the game appropriately.
		 */
		void doMakeMove(CheckersMove move) {

			board.makeMove(move);

			/*
			 * If the move was a jump, it's possible that the player has another jump. Check
			 * for legal jumps starting from the square that the player just moved to. If
			 * there are any, the player must jump. The same player continues moving.
			 */

			if (move.isJump()) {
				legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol);
				if (legalMoves != null) {
					if (currentPlayer == CheckersData.RED)
						message.setText("RED:  You must continue jumping.");
					else
						message.setText("BLACK:  You must continue jumping.");
					selectedRow = move.toRow; // Since only one piece can be moved, select it.
					selectedCol = move.toCol;
					drawBoard();
					return;
				}
			}

			/*
			 * The current player's turn is ended, so change to the other player. Get that
			 * player's legal moves. If the player has no legal moves, then the game ends.
			 */

			if (currentPlayer == CheckersData.RED) {
				currentPlayer = CheckersData.BLACK;
				legalMoves = board.getLegalMoves(currentPlayer);
				if (legalMoves == null)
					gameOver("BLACK has no moves.  RED wins.");
				else if (legalMoves[0].isJump())
					message.setText("BLACK:  Make your move.  You must jump.");
				else
					message.setText("BLACK:  Make your move.");
			} else {
				currentPlayer = CheckersData.RED;
				legalMoves = board.getLegalMoves(currentPlayer);
				if (legalMoves == null)
					gameOver("RED has no moves.  BLACK wins.");
				else if (legalMoves[0].isJump())
					message.setText("RED:  Make your move.  You must jump.");
				else
					message.setText("RED:  Make your move.");
			}

			/*
			 * Set selectedRow = -1 to record that the player has not yet selected a piece
			 * to move.
			 */

			selectedRow = -1;

			/*
			 * As a courtesy to the user, if all legal moves use the same piece, then select
			 * that piece automatically so the user won't have to click on it to select it.
			 */

			if (legalMoves != null) {
				boolean sameStartSquare = true;
				for (int i = 1; i < legalMoves.length; i++)
					if (legalMoves[i].fromRow != legalMoves[0].fromRow
							|| legalMoves[i].fromCol != legalMoves[0].fromCol) {
						sameStartSquare = false;
						break;
					}
				if (sameStartSquare) {
					selectedRow = legalMoves[0].fromRow;
					selectedCol = legalMoves[0].fromCol;
				}
			}

			/* Make sure the board is redrawn in its new state. */

			drawBoard();

		} // end doMakeMove();

		/**
		 * Draw a checkerboard pattern in gray and lightGray. Draw the checkers. If a
		 * game is in progress, highlight the legal moves.
		 */
		public void drawBoard() {

			GraphicsContext g = getGraphicsContext2D();
			g.setFont(Font.font(18));

			/* Draw a two-pixel black border around the edges of the canvas. */

			g.setStroke(Color.DARKRED);
			g.setLineWidth(2);
			g.strokeRect(1, 1, 322, 322);

			/* Draw the squares of the checkerboard and the checkers. */

			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (row % 2 == col % 2)
						g.setFill(Color.LIGHTGRAY);
					else
						g.setFill(Color.GRAY);
					g.fillRect(2 + col * 40, 2 + row * 40, 40, 40);
					switch (board.pieceAt(row, col)) {
					case CheckersData.RED:
						g.setFill(Color.RED);
						g.fillOval(8 + col * 40, 8 + row * 40, 28, 28);
						break;
					case CheckersData.BLACK:
						g.setFill(Color.BLACK);
						g.fillOval(8 + col * 40, 8 + row * 40, 28, 28);
						break;
					case CheckersData.RED_KING:
						g.setFill(Color.RED);
						g.fillOval(8 + col * 40, 8 + row * 40, 28, 28);
						g.setFill(Color.WHITE);
						g.fillText("K", 15 + col * 40, 29 + row * 40);
						break;
					case CheckersData.BLACK_KING:
						g.setFill(Color.BLACK);
						g.fillOval(8 + col * 40, 8 + row * 40, 28, 28);
						g.setFill(Color.WHITE);
						g.fillText("K", 15 + col * 40, 29 + row * 40);
						break;
					}
				}
			}

			/*
			 * If a game is in progress, highlight the legal moves. Note that legalMoves is
			 * never null while a game is in progress.
			 */

			if (gameInProgress) {
				/* First, draw a 4-pixel cyan border around the pieces that can be moved. */
				g.setStroke(Color.CYAN);
				g.setLineWidth(4);
				for (int i = 0; i < legalMoves.length; i++) {
					g.strokeRect(4 + legalMoves[i].fromCol * 40, 4 + legalMoves[i].fromRow * 40, 36, 36);
				}
				/*
				 * If a piece is selected for moving (i.e. if selectedRow >= 0), then draw a
				 * yellow border around that piece and draw green borders around each square
				 * that that piece can be moved to.
				 */
				if (selectedRow >= 0) {
					g.setStroke(Color.YELLOW);
					g.setLineWidth(4);
					g.strokeRect(4 + selectedCol * 40, 4 + selectedRow * 40, 36, 36);
					g.setStroke(Color.LIME);
					g.setLineWidth(4);
					for (int i = 0; i < legalMoves.length; i++) {
						if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
							g.strokeRect(4 + legalMoves[i].toCol * 40, 4 + legalMoves[i].toRow * 40, 36, 36);
						}
					}
				}
			}

		} // end drawBoard()

		/**
		 * Respond to a user click on the board. If no game is in progress, show an
		 * error message. Otherwise, find the row and column that the user clicked and
		 * call doClickSquare() to handle it.
		 */
		public void mousePressed(MouseEvent evt) {
			if (gameInProgress == false)
				message.setText("Click \"New Game\" to start a new game.");
			else {
				int col = (int) ((evt.getX() - 2) / 40);
				int row = (int) ((evt.getY() - 2) / 40);
				if (col >= 0 && col < 8 && row >= 0 && row < 8)
					doClickSquare(row, col);
			}
		}

	} // end class Board

	/**
	 * An object of this class holds data about a game of checkers. It knows what
	 * kind of piece is on each square of the checkerboard. Note that RED moves "up"
	 * the board (i.e. row number decreases) while BLACK moves "down" the board
	 * (i.e. row number increases). Methods are provided to return lists of
	 * available legal moves.
	 */
	private static class CheckersData {

		/*
		 * The following constants represent the possible contents of a square on the
		 * board. The constants RED and BLACK also represent players in the game.
		 */

		static final int EMPTY = 0, RED = 1, RED_KING = 2, BLACK = 3, BLACK_KING = 4;

		int[][] board; // board[r][c] is the contents of row r, column c.

		/**
		 * Constructor. Create the board and set it up for a new game.
		 */
		CheckersData() {
			board = new int[8][8];
			setUpGame();
		}

		/**
		 * Set up the board with checkers in position for the beginning of a game. Note
		 * that checkers can only be found in squares that satisfy row % 2 == col % 2.
		 * At the start of the game, all such squares in the first three rows contain
		 * black squares and all such squares in the last three rows contain red
		 * squares.
		 */
		void setUpGame() {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (row % 2 == col % 2) {
						if (row < 3)
							board[row][col] = BLACK;
						else if (row > 4)
							board[row][col] = RED;
						else
							board[row][col] = EMPTY;
					} else {
						board[row][col] = EMPTY;
					}
				}
			}
		} // end setUpGame()

		/**
		 * Return the contents of the square in the specified row and column.
		 */
		int pieceAt(int row, int col) {
			return board[row][col];
		}

		/**
		 * Make the specified move. It is assumed that move is non-null and that the
		 * move it represents is legal.
		 */
		void makeMove(CheckersMove move) {
			makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
		}

		/**
		 * Make the move from (fromRow,fromCol) to (toRow,toCol). It is assumed that
		 * this move is legal. If the move is a jump, the jumped piece is removed from
		 * the board. If a piece moves to the last row on the opponent's side of the
		 * board, the piece becomes a king.
		 */
		void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
			board[toRow][toCol] = board[fromRow][fromCol];
			board[fromRow][fromCol] = EMPTY;
			if (fromRow - toRow == 2 || fromRow - toRow == -2) {
				// The move is a jump. Remove the jumped piece from the board.
				int jumpRow = (fromRow + toRow) / 2; // Row of the jumped piece.
				int jumpCol = (fromCol + toCol) / 2; // Column of the jumped piece.
				board[jumpRow][jumpCol] = EMPTY;
			}
			if (toRow == 0 && board[toRow][toCol] == RED)
				board[toRow][toCol] = RED_KING;
			if (toRow == 7 && board[toRow][toCol] == BLACK)
				board[toRow][toCol] = BLACK_KING;
		}

		/**
		 * Return an array containing all the legal CheckersMoves for the specified
		 * player on the current board. If the player has no legal moves, null is
		 * returned. The value of player should be one of the constants RED or BLACK; if
		 * not, null is returned. If the returned value is non-null, it consists
		 * entirely of jump moves or entirely of regular moves, since if the player can
		 * jump, only jumps are legal moves.
		 */
		CheckersMove[] getLegalMoves(int player) {

			if (player != RED && player != BLACK)
				return null;

			int playerKing; // The constant representing a King belonging to player.
			if (player == RED)
				playerKing = RED_KING;
			else
				playerKing = BLACK_KING;

			ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>(); // Moves will be stored in this list.

			/*
			 * First, check for any possible jumps. Look at each square on the board. If
			 * that square contains one of the player's pieces, look at a possible jump in
			 * each of the four directions from that square. If there is a legal jump in
			 * that direction, put it in the moves ArrayList.
			 */

			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					if (board[row][col] == player || board[row][col] == playerKing) {
						if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2))
							moves.add(new CheckersMove(row, col, row + 2, col + 2));
						if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2))
							moves.add(new CheckersMove(row, col, row - 2, col + 2));
						if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2))
							moves.add(new CheckersMove(row, col, row + 2, col - 2));
						if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2))
							moves.add(new CheckersMove(row, col, row - 2, col - 2));
					}
				}
			}

			/*
			 * If any jump moves were found, then the user must jump, so we don't add any
			 * regular moves. However, if no jumps were found, check for any legal regular
			 * moves. Look at each square on the board. If that square contains one of the
			 * player's pieces, look at a possible move in each of the four directions from
			 * that square. If there is a legal move in that direction, put it in the moves
			 * ArrayList.
			 */

			if (moves.size() == 0) {
				for (int row = 0; row < 8; row++) {
					for (int col = 0; col < 8; col++) {
						if (board[row][col] == player || board[row][col] == playerKing) {
							if (canMove(player, row, col, row + 1, col + 1))
								moves.add(new CheckersMove(row, col, row + 1, col + 1));
							if (canMove(player, row, col, row - 1, col + 1))
								moves.add(new CheckersMove(row, col, row - 1, col + 1));
							if (canMove(player, row, col, row + 1, col - 1))
								moves.add(new CheckersMove(row, col, row + 1, col - 1));
							if (canMove(player, row, col, row - 1, col - 1))
								moves.add(new CheckersMove(row, col, row - 1, col - 1));
						}
					}
				}
			}

			/*
			 * If no legal moves have been found, return null. Otherwise, create an array
			 * just big enough to hold all the legal moves, copy the legal moves from the
			 * ArrayList into the array, and return the array.
			 */

			if (moves.size() == 0)
				return null;
			else {
				CheckersMove[] moveArray = new CheckersMove[moves.size()];
				for (int i = 0; i < moves.size(); i++)
					moveArray[i] = moves.get(i);
				return moveArray;
			}

		} // end getLegalMoves

		/**
		 * Return a list of the legal jumps that the specified player can make starting
		 * from the specified row and column. If no such jumps are possible, null is
		 * returned. The logic is similar to the logic of the getLegalMoves() method.
		 */
		CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
			if (player != RED && player != BLACK)
				return null;
			int playerKing; // The constant representing a King belonging to player.
			if (player == RED)
				playerKing = RED_KING;
			else
				playerKing = BLACK_KING;
			ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>(); // The legal jumps will be stored in this
			// list.
			if (board[row][col] == player || board[row][col] == playerKing) {
				if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2))
					moves.add(new CheckersMove(row, col, row + 2, col + 2));
				if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2))
					moves.add(new CheckersMove(row, col, row - 2, col + 2));
				if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2))
					moves.add(new CheckersMove(row, col, row + 2, col - 2));
				if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2))
					moves.add(new CheckersMove(row, col, row - 2, col - 2));
			}
			if (moves.size() == 0)
				return null;
			else {
				CheckersMove[] moveArray = new CheckersMove[moves.size()];
				for (int i = 0; i < moves.size(); i++)
					moveArray[i] = moves.get(i);
				return moveArray;
			}
		} // end getLegalJumpsFrom()

		/**
		 * This is called by the two previous methods to check whether the player can
		 * legally jump from (r1,c1) to (r3,c3). It is assumed that the player has a
		 * piece at (r1,c1), that (r3,c3) is a position that is 2 rows and 2 columns
		 * distant from (r1,c1) and that (r2,c2) is the square between (r1,c1) and
		 * (r3,c3).
		 */
		private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) {

			if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
				return false; // (r3,c3) is off the board.

			if (board[r3][c3] != EMPTY)
				return false; // (r3,c3) already contains a piece.

			if (player == RED) {
				if (board[r1][c1] == RED && r3 > r1)
					return false; // Regular red piece can only move up.
				if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
					return false; // There is no black piece to jump.
				return true; // The jump is legal.
			} else {
				if (board[r1][c1] == BLACK && r3 < r1)
					return false; // Regular black piece can only move downn.
				if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
					return false; // There is no red piece to jump.
				return true; // The jump is legal.
			}

		} // end canJump()

		/**
		 * This is called by the getLegalMoves() method to determine whether the player
		 * can legally move from (r1,c1) to (r2,c2). It is assumed that (r1,r2) contains
		 * one of the player's pieces and that (r2,c2) is a neighboring square.
		 */
		private boolean canMove(int player, int r1, int c1, int r2, int c2) {

			if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
				return false; // (r2,c2) is off the board.

			if (board[r2][c2] != EMPTY)
				return false; // (r2,c2) already contains a piece.

			if (player == RED) {
				if (board[r1][c1] == RED && r2 > r1)
					return false; // Regular red piece can only move down.
				return true; // The move is legal.
			} else {
				if (board[r1][c1] == BLACK && r2 < r1)
					return false; // Regular black piece can only move up.
				return true; // The move is legal.
			}

		} // end canMove()

	} // end class CheckersData

} // end class Checkers
