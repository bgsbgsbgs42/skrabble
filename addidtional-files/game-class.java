package pij.game;

import pij.model.Board;
import pij.model.Direction;
import pij.model.Move;
import pij.model.Position;
import pij.model.Square;
import pij.model.Tile;
import pij.model.TileBag;
import pij.player.ComputerPlayer;
import pij.player.HumanPlayer;
import pij.player.Player;

/**
 * Main class representing the SkraBBKle game.
 */
public class Game {
    private final Board board;
    private final HumanPlayer humanPlayer;
    private final ComputerPlayer computerPlayer;
    private final TileBag tileBag;
    private boolean isHumanTurn;
    private GameState gameState;
    private final boolean openGame;
    private int consecutivePasses;
    
    /**
     * Creates a new game with the specified board and players.
     * 
     * @param board Game board
     * @param humanPlayer Human player
     * @param computerPlayer Computer player
     * @param openGame Whether this is an open game (computer's tiles are visible)
     */
    public Game(Board board, HumanPlayer humanPlayer, ComputerPlayer computerPlayer, boolean openGame) {
        this.board = board;
        this.humanPlayer = humanPlayer;
        this.computerPlayer = computerPlayer;
        this.tileBag = new TileBag();
        this.isHumanTurn = true;  // Human player starts first
        this.gameState = GameState.IN_PROGRESS;
        this.openGame = openGame;
        this.consecutivePasses = 0;
    }
    
    /**
     * Start the game.
     */
    public void start() {
        // Deal initial tiles to both players
        humanPlayer.fillRack(tileBag);
        computerPlayer.fillRack(tileBag);
        
        // Main game loop
        while (gameState == GameState.IN_PROGRESS) {
            // Display the current state
            displayGameState();
            
            // Make a move
            if (isHumanTurn) {
                playHumanTurn();
            } else {
                playComputerTurn();
            }
            
            // Check if the game has ended
            checkGameEnd();
            
            // Switch turns
            isHumanTurn = !isHumanTurn;
        }
        
        // Display the final state and winner
        displayGameEnd();
    }
    
    /**
     * Display the current state of the game.
     */
    private void displayGameState() {
        // Display the board
        System.out.println(board.toString());
        
        // Display player scores
        System.out.println("Human player score:    " + humanPlayer.getScore());
        System.out.println("Computer player score: " + computerPlayer.getScore());
        System.out.println();
        
        // In open game, display computer's tiles
        if (openGame) {
            System.out.println("OPEN GAME: The computer's tiles:");
            System.out.println("OPEN GAME: " + computerPlayer.getRack().toString());
        }
    }
    
    /**
     * Play the human player's turn.
     */
    private void playHumanTurn() {
        // Display whose turn it is
        System.out.println("It's your turn!");
        
        // Ask for a move
        Move move = humanPlayer.makeMove(board, tileBag);
        
        // Process the move
        if (!move.isPass()) {
            // Place the tiles on the board
            int score = placeWordOnBoard(move, humanPlayer);
            
            // Update the player's score
            humanPlayer.addScore(score);
            
            // Display the move
            System.out.println("The move is:    " + move.toString());
            
            // Fill the player's rack
            humanPlayer.fillRack(tileBag);
            
            // Reset the consecutive passes counter
            consecutivePasses = 0;
        } else {
            System.out.println("You passed your turn.");
            consecutivePasses++;
        }
    }
    
    /**
     * Play the computer player's turn.
     */
    private void playComputerTurn() {
        // Display whose turn it is
        System.out.println("It's the computer's turn!");
        
        // Make a move
        Move move = computerPlayer.makeMove(board, tileBag);
        
        // Process the move
        if (!move.isPass()) {
            // Place the tiles on the board
            int score = placeWordOnBoard(move, computerPlayer);
            
            // Update the player's score
            computerPlayer.addScore(score);
            
            // Display the move
            System.out.println("The move is:    " + move.toString());
            
            // Fill the player's rack
            computerPlayer.fillRack(tileBag);
            
            // Reset the consecutive passes counter
            consecutivePasses = 0;
        } else {
            System.out.println("The computer passed its turn.");
            consecutivePasses++;
        }
    }
    
    /**
     * Place a word on the board and calculate the score.
     * 
     * @param move Move to make
     * @param player Player making the move
     * @return Score for the move
     */
    private int placeWordOnBoard(Move move, Player player) {
        if (move.isPass()) {
            return 0;
        }
        
        String word = move.getWord();
        Position position = move.getPosition();
        Direction direction = move.getDirection();
        
        int wordScore = 0;
        int wordMultiplier = 1;
        int tilesPlaced = 0;
        
        // Place the tiles on the board and calculate the score
        Position current = position;
        
        for (int i = 0; i < word.length(); i++) {
            // Skip occupied positions
            while (board.hasTileAt(current)) {
                current = current.next(direction);
            }
            
            // Place the tile
            Tile tile = move.getTiles().get(tilesPlaced);
            Square square = board.getSquare(current);
            
            if (square != null && !square.isOccupied()) {
                square.placeTile(tile);
                
                // Calculate score for this tile
                int tileScore = tile.getValue();
                
                // Apply letter premium
                if (square.getPremiumType() == pij.model.PremiumType.LETTER) {
                    tileScore *= square.getPremiumValue();
                }
                
                wordScore += tileScore;
                
                // Apply word premium
                if (square.getPremiumType() == pij.model.PremiumType.WORD) {
                    wordMultiplier *= square.getPremiumValue();
                }
                
                tilesPlaced++;
            }
            
            current = current.next(direction);
        }
        
        // Apply word multiplier
        wordScore *= wordMultiplier;
        
        // Add bonus for using all 7 tiles
        if (move.getNumberOfTiles() == 7) {
            wordScore += 75;
        }
        
        return wordScore;
    }
    
    /**
     * Check if the game has ended.
     */
    private void checkGameEnd() {
        // Game ends if the tile bag is empty and one player has an empty rack
        if (tileBag.isEmpty() && (humanPlayer.hasEmptyRack() || computerPlayer.hasEmptyRack())) {
            determineWinner();
        }
        
        // Game also ends if both players pass twice in a row
        if (consecutivePasses >= 4) {
            determineWinner();
        }
    }
    
    /**
     * Determine the winner of the game.
     */
    private void determineWinner() {
        // Apply penalty for unused tiles
        humanPlayer.applyUnusedTilesPenalty();
        computerPlayer.applyUnusedTilesPenalty();
        
        // Determine the winner
        int humanScore = humanPlayer.getScore();
        int computerScore = computerPlayer.getScore();
        
        if (humanScore > computerScore) {
            gameState = GameState.HUMAN_WIN;
        } else if (computerScore > humanScore) {
            gameState = GameState.COMPUTER_WIN;
        } else {
            gameState = GameState.DRAW;
        }
    }
    
    /**
     * Display the end of the game.
     */
    private void displayGameEnd() {
        System.out.println("Game Over!");
        System.out.println("The human player scored " + humanPlayer.getScore() + " points.");
        System.out.println("The computer player scored " + computerPlayer.getScore() + " points.");
        
        switch (gameState) {
            case HUMAN_WIN:
                System.out.println("The human player wins!");
                break;
            case COMPUTER_WIN:
                System.out.println("The computer player wins!");
                break;
            case DRAW:
                System.out.println("It's a draw!");
                break;
            default:
                break;
        }
    }
}
