package pij.model;

import java.util.List;

/**
 * Represents a move in the SkraBBKle game.
 */
public class Move {
    private final String word;
    private final Position position;
    private final Direction direction;
    private final List<Tile> tiles;
    private final boolean isPass;
    
    /**
     * Creates a move to place tiles on the board.
     * 
     * @param word Word formed by the move
     * @param position Starting position of the move
     * @param direction Direction of the move
     * @param tiles Tiles used in the move
     */
    public Move(String word, Position position, Direction direction, List<Tile> tiles) {
        this.word = word;
        this.position = position;
        this.direction = direction;
        this.tiles = tiles;
        this.isPass = false;
    }
    
    /**
     * Creates a pass move.
     */
    public Move() {
        this.word = "";
        this.position = null;
        this.direction = null;
        this.tiles = null;
        this.isPass = true;
    }
    
    /**
     * Get the word formed by this move.
     * 
     * @return Word
     */
    public String getWord() {
        return word;
    }
    
    /**
     * Get the starting position of this move.
     * 
     * @return Position
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Get the direction of this move.
     * 
     * @return Direction
     */
    public Direction getDirection() {
        return direction;
    }
    
    /**
     * Get the tiles used in this move.
     * 
     * @return List of tiles
     */
    public List<Tile> getTiles() {
        return tiles;
    }
    
    /**
     * Check if this is a pass move.
     * 
     * @return true if this is a pass move, false otherwise
     */
    public boolean isPass() {
        return isPass;
    }
    
    /**
     * Get the number of tiles used in this move.
     * 
     * @return Number of tiles
     */
    public int getNumberOfTiles() {
        return isPass ? 0 : tiles.size();
    }
    
    /**
     * Parse a move string in the format "word,square".
     * 
     * @param moveString Move string
     * @param boardSize Size of the board
     * @param rack Player's tile rack
     * @return Move object or null if the move is invalid
     */
    public static Move parse(String moveString, int boardSize, Rack rack) {
        // Check for pass move
        if (moveString.equals(",")) {
            return new Move();
        }
        
        // Split the move string into word and position
        String[] parts = moveString.split(",");
        if (parts.length != 2) {
            return null;
        }
        
        String word = parts[0];
        String positionStr = parts[1];
        
        // Check if the word is valid
        if (word.isEmpty()) {
            return null;
        }
        
        // Parse position and direction
        Object[] posAndDir = Position.parseWithDirection(positionStr, boardSize);
        if (posAndDir == null) {
            return null;
        }
        
        Position position = (Position) posAndDir[0];
        Direction direction = (Direction) posAndDir[1];
        
        // Check if the rack has the tiles for the word
        List<Tile> tiles = rack.getTilesForWord(word);
        if (tiles == null) {
            return null;
        }
        
        return new Move(word, position, direction, tiles);
    }
    
    /**
     * Returns a string representation of this move.
     * 
     * @return String representation of the move
     */
    @Override
    public String toString() {
        if (isPass) {
            return "Pass";
        } else {
            return "Word: " + word + " at position " + position.toString();
        }
    }
}
