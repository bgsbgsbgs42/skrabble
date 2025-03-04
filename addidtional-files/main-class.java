package pij.main;

import pij.game.Game;
import pij.game.GameFactory;

/**
 * Main class to run the SkraBBKle game.
 */
public class Main {
    /**
     * Entry point of the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Display welcome message
        displayWelcomeMessage();
        
        // Create and start the game
        Game game = GameFactory.createGame();
        game.start();
    }

    /**
     * Display welcome message for the game.
     */
    private static void displayWelcomeMessage() {
        System.out.println("============                   ============");
        System.out.println("============ S k r a B B K l e ============");
        System.out.println("============                   ============");
        System.out.println();
    }
}
