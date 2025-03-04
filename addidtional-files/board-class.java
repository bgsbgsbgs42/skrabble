package pij.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the SkraBBKle game board.
 */
public class Board {
    private final int size;
    private final Square[][] squares;
    private Position centerSquare;
    
    /**
     * Creates a board with the specified size.
     * 
     * @param size Board size (between 11 and 26)
     */
    public Board(int size) {
        if (size < 11 || size > 26) {
            throw new IllegalArgumentException("Board size must be between 11 and 26");
        }
        
        this.size = size;
        this.squares = new Square[size][size];
        
        // Initialize all squares as standard
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                squares[row][col] = new Square();
            }
        }
        
        // Calculate center square
        calculateCenterSquare();
    }
    
    /**
     * Get the size of the board.
     * 
     * @return Board size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get the center square position.
     * 
     * @return Center square position
     */
    public Position getCenterSquare() {
        return centerSquare;
    }
    
    /**
     * Get the square at the specified position.
     * 
     * @param position Position on the board
     * @return Square at the position or null if position is invalid
     */
    public Square getSquare(Position position) {
        int row = position.getRow() - 1;
        int col = position.getColumn();
        
        if (isValidPosition(row, col)) {
            return squares[row][col];
        }
        
        return null;
    }
    
    /**
     * Set a square at the specified position.
     * 
     * @param position Position on the board
     * @param square Square to set
     * @return true if the square was set successfully, false otherwise
     */
    public boolean setSquare(Position position, Square square) {
        int row = position.getRow() - 1;
        int col = position.getColumn();
        
        if (isValidPosition(row, col)) {
            squares[row][col] = square;
            return true;
        }
        
        return false;
    }
    
    /**
     * Place a tile on the board at the specified position.
     * 
     * @param position Position on the board
     * @param tile Tile to place
     * @return true if the tile was placed successfully, false otherwise
     */
    public boolean placeTile(Position position, Tile tile) {
        Square square = getSquare(position);
        
        if (square != null && !square.isOccupied()) {
            return square.placeTile(tile);
        }
        
        return false;
    }
    
    /**
     * Check if a position is valid on the board.
     * 
     * @param row Row index (0-based)
     * @param col Column index (0-based)
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }
    
    /**
     * Calculate the center square position according to the rules.
     */
    private void calculateCenterSquare() {
        // If size is even, take the top-left of the four central squares
        if (size % 2 == 0) {
            int centerRow = size / 2 - 1;
            int centerCol = size / 2 - 1;
            centerSquare = new Position(centerRow + 1, centerCol);
        } else {
            // If size is odd, take the exact center
            int centerRow = size / 2;
            int centerCol = size / 2;
            centerSquare = new Position(centerRow + 1, centerCol);
        }
    }
    
    /**
     * Check if the board is empty (no tiles placed).
     * 
     * @return true if the board is empty, false otherwise
     */
    public boolean isEmpty() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (squares[row][col].isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check if the board has a tile at the specified position.
     * 
     * @param position Position to check
     * @return true if there is a tile at the position, false otherwise
     */
    public boolean hasTileAt(Position position) {
        Square square = getSquare(position);
        return square != null && square.isOccupied();
    }
    
    /**
     * Check if a move is valid on the board (connected to existing tiles or center).
     * 
     * @param position Starting position of the move
     * @param direction Direction of the move
     * @param word Word to play
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(Position position, Direction direction, String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        // First move must go through the center square
        if (isEmpty()) {
            boolean touchesCenter = false;
            Position current = position;
            
            for (int i = 0; i < word.length(); i++) {
                if (current.equals(centerSquare)) {
                    touchesCenter = true;
                    break;
                }
                current = current.next(direction);
            }
            
            if (!touchesCenter) {
                return false;
            }
        } else {
            // Subsequent moves must connect with existing tiles
            boolean connected = false;
            Position current = position;
            
            for (int i = 0; i < word.length(); i++) {
                // Check if this position has a tile
                if (hasTileAt(current)) {
                    connected = true;
                    break;
                }
                
                // Check adjacent positions (perpendicular to direction)
                List<Position> adjacentPositions = getAdjacentPositions(current, direction);
                for (Position adjacent : adjacentPositions) {
                    if (hasTileAt(adjacent)) {
                        connected = true;
                        break;
                    }
                }
                
                if (connected) {
                    break;
                }
                
                current = current.next(direction);
            }
            
            if (!connected) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get positions adjacent to the specified position in a direction perpendicular to the move.
     * 
     * @param position Position to check
     * @param direction Direction of the move
     * @return List of adjacent positions
     */
    private List<Position> getAdjacentPositions(Position position, Direction direction) {
        List<Position> adjacentPositions = new ArrayList<>();
        int row = position.getRow() - 1;
        int col = position.getColumn();
        
        if (direction == Direction.RIGHT) {
            // Check positions above and below
            if (isValidPosition(row - 1, col)) {
                adjacentPositions.add(new Position(row, col));
            }
            if (isValidPosition(row + 1, col)) {
                adjacentPositions.add(new Position(row + 2, col));
            }
        } else {
            // Check positions to the left and right
            if (isValidPosition(row, col - 1)) {
                adjacentPositions.add(new Position(row + 1, col - 1));
            }
            if (isValidPosition(row, col + 1)) {
                adjacentPositions.add(new Position(row + 1, col + 1));
            }
        }
        
        return adjacentPositions;
    }
    
    /**
     * Returns a string representation of the board.
     * 
     * @return String representation of the board
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        // Column headers
        builder.append("    ");
        for (int col = 0; col < size; col++) {
            char colChar = (char) ('a' + col);
            builder.append(colChar).append("  ");
        }
        builder.append("\n\n");
        
        // Board rows
        for (int row = 0; row < size; row++) {
            // Row number at the beginning
            builder.append(String.format("%2d  ", row + 1));
            
            // Squares in the row
            for (int col = 0; col < size; col++) {
                builder.append(squares[row][col].toString()).append(" ");
            }
            
            // Row number at the end
            builder.append(" ").append(String.format("%2d", row + 1)).append("\n");
        }
        
        builder.append("\n");
        
        // Column headers again
        builder.append("    ");
        for (int col = 0; col < size; col++) {
            char colChar = (char) ('a' + col);
            builder.append(colChar).append("  ");
        }
        builder.append("\n");
        
        return builder.toString();
    }
}
