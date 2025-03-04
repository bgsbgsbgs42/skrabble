package pij.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pij.model.Board;
import pij.model.Direction;
import pij.model.Move;
import pij.model.Position;
import pij.model.Tile;
import pij.model.TileBag;
import pij.util.Dictionary;

/**
 * Class representing a computer player in the SkraBBKle game.
 */
public class ComputerPlayer extends Player {
    private final Dictionary dictionary;
    
    /**
     * Creates a computer player with the specified dictionary.
     * 
     * @param dictionary Dictionary for validating words
     */
    public ComputerPlayer(Dictionary dictionary) {
        super("Computer player");
        this.dictionary = dictionary;
    }
    
    @Override
    public Move makeMove(Board board, TileBag tileBag) {
        // If the board is empty, play a word through the center square
        if (board.isEmpty()) {
            return findMoveForEmptyBoard(board);
        }
        
        // Try to find a move for each number of tiles (7 to 1)
        for (int numTiles = 7; numTiles > 0; numTiles--) {
            // Generate all possible combinations of tiles
            List<List<Tile>> combinations = generateTileCombinations(getRack().getTiles(), numTiles);
            
            // Try each combination on the board
            for (List<Tile> combination : combinations) {
                // Generate all possible words from the combination
                Set<String> words = generateWords(combination);
                
                // Try each word on the board
                for (String word : words) {
                    // Try each position and direction
                    for (int row = 0; row < board.getSize(); row++) {
                        for (int col = 0; col < board.getSize(); col++) {
                            Position position = new Position(row + 1, col);
                            
                            // Try in the RIGHT direction
                            Move rightMove = tryMove(word, position, Direction.RIGHT, board);
                            if (rightMove != null) {
                                // Remove the tiles used in the move from the rack
                                getRack().removeWordTiles(rightMove.getWord());
                                setPassedLastTurn(false);
                                return rightMove;
                            }
                            
                            // Try in the DOWN direction
                            Move downMove = tryMove(word, position, Direction.DOWN, board);
                            if (downMove != null) {
                                // Remove the tiles used in the move from the rack
                                getRack().removeWordTiles(downMove.getWord());
                                setPassedLastTurn(false);
                                return downMove;
                            }
                        }
                    }
                }
            }
        }
        
        // If no move was found, pass the turn
        setPassedLastTurn(true);
        return new Move();
    }
    
    /**
     * Find a move for an empty board (first move of the game).
     * 
     * @param board Current board state
     * @return Move to make or null if no valid move is found
     */
    private Move findMoveForEmptyBoard(Board board) {
        // For the first move, try to use as many tiles as possible
        for (int numTiles = 7; numTiles > 1; numTiles--) {  // At least 2 tiles for first move
            // Generate all possible combinations of tiles
            List<List<Tile>> combinations = generateTileCombinations(getRack().getTiles(), numTiles);
            
            // Try each combination
            for (List<Tile> combination : combinations) {
                // Generate all possible words from the combination
                Set<String> words = generateWords(combination);
                
                // Try each word
                for (String word : words) {
                    // Check if the word is valid in the dictionary
                    if (dictionary.isValidWord(word)) {
                        // Place the word through the center square
                        Position center = board.getCenterSquare();
                        
                        // Try in the RIGHT direction
                        Position rightStart = findStartPosition(center, word, Direction.RIGHT);
                        if (rightStart != null) {
                            List<Tile> tiles = getRack().getTilesForWord(word);
                            if (tiles != null) {
                                // Remove the tiles used in the move from the rack
                                getRack().removeWordTiles(word);
                                setPassedLastTurn(false);
                                return new Move(word, rightStart, Direction.RIGHT, tiles);
                            }
                        }
                        
                        // Try in the DOWN direction
                        Position downStart = findStartPosition(center, word, Direction.DOWN);
                        if (downStart != null) {
                            List<Tile> tiles = getRack().getTilesForWord(word);
                            if (tiles != null) {
                                // Remove the tiles used in the move from the rack
                                getRack().removeWordTiles(word);
                                setPassedLastTurn(false);
                                return new Move(word, downStart, Direction.DOWN, tiles);
                            }
                        }
                    }
                }
            }
        }
        
        // If no move was found, pass the turn
        setPassedLastTurn(true);
        return new Move();
    }
    
    /**
     * Find the starting position for placing a word through the center square.
     * 
     * @param center Center square position
     * @param word Word to place
     * @param direction Direction to place the word
     * @return Starting position or null if the word cannot be placed
     */
    private Position findStartPosition(Position center, String word, Direction direction) {
        // Calculate the offset from the center square to the starting position
        for (int i = 0; i < word.length(); i++) {
            Position start;
            
            if (direction == Direction.RIGHT) {
                // Move i positions to the left
                start = new Position(center.getRow(), center.getColumn() - i);
            } else {
                // Move i positions up
                start = new Position(center.getRow() - i, center.getColumn());
            }
            
            // Check if the word would fit on the board
            if (isValidStartPosition(start, word, direction)) {
                return start;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a starting position is valid for placing a word.
     * 
     * @param start Starting position
     * @param word Word to place
     * @param direction Direction to place the word
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidStartPosition(Position start, String word, Direction direction) {
        // Check if the starting position is on the board
        if (start.getRow() < 1 || start.getRow() > 16 || 
            start.getColumn() < 0 || start.getColumn() >= 16) {
            return false;
        }
        
        // Check if the word would fit on the board
        Position end;
        
        if (direction == Direction.RIGHT) {
            end = new Position(start.getRow(), start.getColumn() + word.length() - 1);
        } else {
            end = new Position(start.getRow() + word.length() - 1, start.getColumn());
        }
        
        return end.getRow() >= 1 && end.getRow() <= 16 && 
               end.getColumn() >= 0 && end.getColumn() < 16;
    }
    
    /**
     * Try to place a word on the board at the specified position and direction.
     * 
     * @param word Word to place
     * @param position Starting position
     * @param direction Direction to place the word
     * @param board Current board state
     * @return Move to make or null if the move is invalid
     */
    private Move tryMove(String word, Position position, Direction direction, Board board) {
        // Check if the word is valid in the dictionary
        if (!dictionary.isValidWord(word)) {
            return null;
        }
        
        // Check if the move is valid on the board
        if (!board.isValidMove(position, direction, word)) {
            return null;
        }
        
        // Get the tiles needed for the word
        List<Tile> tiles = getRack().getTilesForWord(word);
        
        if (tiles != null) {
            return new Move(word, position, direction, tiles);
        }
        
        return null;
    }
    
    /**
     * Generate all possible combinations of n tiles from a list of tiles.
     * 
     * @param tiles List of tiles
     * @param n Number of tiles in each combination
     * @return List of combinations
     */
    private List<List<Tile>> generateTileCombinations(List<Tile> tiles, int n) {
        List<List<Tile>> combinations = new ArrayList<>();
        generateTileCombinationsHelper(tiles, n, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    /**
     * Helper method for generating tile combinations.
     * 
     * @param tiles List of tiles
     * @param n Number of tiles in each combination
     * @param start Starting index
     * @param current Current combination
     * @param result List to store all combinations
     */
    private void generateTileCombinationsHelper(List<Tile> tiles, int n, int start, 
                                               List<Tile> current, List<List<Tile>> result) {
        if (current.size() == n) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < tiles.size(); i++) {
            current.add(tiles.get(i));
            generateTileCombinationsHelper(tiles, n, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Generate all possible words from a list of tiles.
     * 
     * @param tiles List of tiles
     * @return Set of words
     */
    private Set<String> generateWords(List<Tile> tiles) {
        Set<String> words = new HashSet<>();
        
        // Generate all permutations of the tiles
        List<List<Tile>> permutations = new ArrayList<>();
        generatePermutations(tiles, new ArrayList<>(), permutations, new boolean[tiles.size()]);
        
        // Convert each permutation to a word
        for (List<Tile> permutation : permutations) {
            StringBuilder word = new StringBuilder();
            for (Tile tile : permutation) {
                word.append(tile.getLetter());
            }
            
            // Add the word to the set
            words.add(word.toString());
        }
        
        return words;
    }
    
    /**
     * Generate all permutations of a list of tiles.
     * 
     * @param tiles List of tiles
     * @param current Current permutation
     * @param result List to store all permutations
     * @param used Array to track used tiles
     */
    private void generatePermutations(List<Tile> tiles, List<Tile> current, 
                                     List<List<Tile>> result, boolean[] used) {
        if (current.size() == tiles.size()) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = 0; i < tiles.size(); i++) {
            if (!used[i]) {
                used[i] = true;
                current.add(tiles.get(i));
                generatePermutations(tiles, current, result, used);
                current.remove(current.size() - 1);
                used[i] = false;
            }
        }
    }
}
