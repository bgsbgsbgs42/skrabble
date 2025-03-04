package pij.game;

import java.io.IOException;
import java.util.Scanner;

import pij.model.Board;
import pij.player.ComputerPlayer;
import pij.player.HumanPlayer;
import pij.util.BoardLoader;
import pij.util.Dictionary;
import pij.util.FileLoader;
import pij.util.InputValidator;

/**
 * Factory class for creating game instances.
 */
public class GameFactory {
    
    private static final String DEFAULT_BOARD_PATH = "resources/defaultBoard.txt";
    private static final String WORD_LIST_PATH = "resources/wordlist.txt";
    
    /**
     * Create a new game based on user input.
     * 
     * @return Game instance
     */
    public static Game createGame() {
        Scanner scanner = new Scanner(System.in);
        
        // Prompt for board selection
        Board board = promptForBoard(scanner);
        
        // Prompt for game type (open or closed)
        boolean openGame = promptForGameType(scanner);
        
        // Create players
        HumanPlayer humanPlayer = new HumanPlayer(scanner);
        
        // Create dictionary
        Dictionary dictionary;
        try {
            dictionary = new Dictionary(WORD_LIST_PATH);
        } catch (IOException e) {
            System.out.println("Error loading word list: " + e.getMessage());
            System.out.println("Using empty dictionary instead.");
            
            try {
                dictionary = new Dictionary("");
            } catch (IOException ex) {
                dictionary = null;
            }
        }
        
        ComputerPlayer computerPlayer = new ComputerPlayer(dictionary);
        
        // Create game
        return new Game(board, humanPlayer, computerPlayer, openGame);
    }
    
    /**
     * Prompt the user to select a board.
     * 
     * @param scanner Scanner for reading input
     * @return Selected board
     */
    private static Board promptForBoard(Scanner scanner) {
        System.out.println("Would you like to _l_oad a board or use the _d_efault board?");
        System.out.print("Please enter your choice (l/d): ");
        
        String choice = scanner.nextLine().trim().toLowerCase();
        
        while (!InputValidator.isValidChoice(choice, new String[]{"l", "d"})) {
            System.out.println("Invalid choice. Please enter 'l' to load a board or 'd' to use the default board.");
            System.out.print("Please enter your choice (l/d): ");
            choice = scanner.nextLine().trim().toLowerCase();
        }
        
        if (choice.equals("d")) {
            // Load default board
            return BoardLoader.createDefaultBoard();
        } else {
            // Prompt for board file
            System.out.print("Please enter the file name of the board: ");
            String filePath = scanner.nextLine().trim();
            
            Board board = null;
            
            while (board == null) {
                if (!FileLoader.fileExists(filePath)) {
                    System.out.println("This is not a valid file. Please enter the file name of the board: ");
                    filePath = scanner.nextLine().trim();
                    continue;
                }
                
                board = BoardLoader.loadBoard(filePath);
                
                if (board == null) {
                    System.out.println("This is not a valid file. Please enter the file name of the board: ");
                    filePath = scanner.nextLine().trim();
                }
            }
            
            return board;
        }
    }
    
    /**
     * Prompt the user to select the game type (open or closed).
     * 
     * @param scanner Scanner for reading input
     * @return true for open game, false for closed game
     */
    private static boolean promptForGameType(Scanner scanner) {
        System.out.println("Would you like to play an _o_pen or a _c_losed game?");
        System.out.print("Please enter your choice (o/c): ");
        
        String choice = scanner.nextLine().trim().toLowerCase();
        
        while (!InputValidator.isValidChoice(choice, new String[]{"o", "c"})) {
            System.out.println("Invalid choice. Please enter 'o' for open game or 'c' for closed game.");
            System.out.print("Please enter your choice (o/c): ");
            choice = scanner.nextLine().trim().toLowerCase();
        }
        
        return choice.equals("o");
    }
}
