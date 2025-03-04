package pij.player;

import java.util.Scanner;

import pij.model.Board;
import pij.model.Move;
import pij.model.TileBag;
import pij.util.InputValidator;

/**
 * Class representing a human player in the SkraBBKle game.
 */
public class HumanPlayer extends Player {
    private final Scanner scanner;
    
    /**
     * Creates a human player.
     * 
     * @param scanner Scanner for reading input
     */
    public HumanPlayer(Scanner scanner) {
        super("Human player");
        this.scanner = scanner;
    }
    
    @Override
    public Move makeMove(Board board, TileBag tileBag) {
        // Display the player's tiles
        System.out.println("It's your turn! Your tiles:");
        System.out.println(getRack().toString());
        
        // Prompt for move input
        System.out.println("Please enter your move in the format: \"word,square\" (without the quotes)");
        System.out.println("For example, for suitable tile rack and board configuration, a downward move");
        System.out.println("could be \"HI,f4\" and a rightward move could be \"HI,4f\".");
        System.out.println();
        System.out.println("In the word, upper-case letters are standard tiles");
        System.out.println("and lower-case letters are wildcards.");
        System.out.println("Entering \",\" passes the turn.");
        
        // Read and validate the move
        String moveString = scanner.nextLine().trim();
        
        // Check if the move format is valid
        while (!InputValidator.isValidMoveFormat(moveString)) {
            System.out.println("Illegal move format");
            System.out.println("Please enter your move in the format: \"word,square\" (without the quotes)");
            System.out.println("For example, for suitable tile rack and board configuration, a downward move");
            System.out.println("could be \"HI,f4\" and a rightward move could be \"HI,4f\".");
            System.out.println();
            System.out.println("In the word, upper-case letters are standard tiles");
            System.out.println("and lower-case letters are wildcards.");
            System.out.println("Entering \",\" passes the turn.");
            
            moveString = scanner.nextLine().trim();
        }
        
        // Parse the move
        Move move = Move.parse(moveString, board.getSize(), getRack());
        
        // Check if the move is valid
        if (move == null) {
            System.out.println("With tiles " + getRack().toString() + " you cannot play word " + moveString + "!");
            // Recursively try again
            return makeMove(board, tileBag);
        }
        
        // If the move is a pass, set the flag
        if (move.isPass()) {
            setPassedLastTurn(true);
        } else {
            setPassedLastTurn(false);
            
            // Remove the tiles used in the move from the rack
            getRack().removeWordTiles(move.getWord());
        }
        
        return move;
    }
}
