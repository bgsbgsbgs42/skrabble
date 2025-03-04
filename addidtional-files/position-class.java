package pij.model;

/**
 * Represents a position on the board with row and column.
 */
public class Position {
    private final int row;
    private final int column;
    
    /**
     * Creates a position with the specified row and column.
     * 
     * @param row Row number (1-based)
     * @param column Column number (0-based, where 0 is 'a', 1 is 'b', etc.)
     */
    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    /**
     * Get the row of this position.
     * 
     * @return Row number (1-based)
     */
    public int getRow() {
        return row;
    }
    
    /**
     * Get the column of this position.
     * 
     * @return Column number (0-based, where 0 is 'a', 1 is 'b', etc.)
     */
    public int getColumn() {
        return column;
    }
    
    /**
     * Get the next position in the specified direction.
     * 
     * @param direction Direction to move
     * @return New position
     */
    public Position next(Direction direction) {
        return switch (direction) {
            case DOWN -> new Position(row + 1, column);
            case RIGHT -> new Position(row, column + 1);
        };
    }
    
    /**
     * Parses a position string (e.g., "a1", "b2") and returns a Position object.
     * 
     * @param positionStr Position in string format
     * @param boardSize Size of the board
     * @return Position object or null if the position is invalid
     */
    public static Position parse(String positionStr, int boardSize) {
        if (positionStr == null || positionStr.isEmpty()) {
            return null;
        }
        
        // If first character is a letter (a-z), the direction is DOWN
        if (Character.isLetter(positionStr.charAt(0))) {
            if (positionStr.length() < 2) {
                return null;
            }
            
            char colChar = positionStr.charAt(0);
            
            // Convert column character to 0-based index
            int col = Character.toLowerCase(colChar) - 'a';
            
            try {
                int row = Integer.parseInt(positionStr.substring(1)) - 1;
                
                // Check if position is valid
                if (col >= 0 && col < boardSize && row >= 0 && row < boardSize) {
                    return new Position(row + 1, col);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        } 
        // If first character is a digit, the direction is RIGHT
        else if (Character.isDigit(positionStr.charAt(0))) {
            if (positionStr.length() < 2) {
                return null;
            }
            
            char colChar = positionStr.charAt(positionStr.length() - 1);
            
            // Convert column character to 0-based index
            int col = Character.toLowerCase(colChar) - 'a';
            
            try {
                int row = Integer.parseInt(positionStr.substring(0, positionStr.length() - 1)) - 1;
                
                // Check if position is valid
                if (col >= 0 && col < boardSize && row >= 0 && row < boardSize) {
                    return new Position(row + 1, col);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Parses a position and direction from a string.
     * 
     * @param positionStr Position in string format
     * @param boardSize Size of the board
     * @return Array with Position and Direction, or null if invalid
     */
    public static Object[] parseWithDirection(String positionStr, int boardSize) {
        Position position = parse(positionStr, boardSize);
        if (position == null) {
            return null;
        }
        
        Direction direction;
        
        // If first character is a letter (a-z), the direction is DOWN
        if (Character.isLetter(positionStr.charAt(0))) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.RIGHT;
        }
        
        return new Object[] { position, direction };
    }
    
    /**
     * Creates a string representation of this position.
     * 
     * @return String like "a1", "b2", etc.
     */
    @Override
    public String toString() {
        char colChar = (char) ('a' + column);
        return colChar + String.valueOf(row);
    }
    
    /**
     * Checks if two positions are equal.
     * 
     * @param obj Object to compare with
     * @return true if the positions are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Position other = (Position) obj;
        return row == other.row && column == other.column;
    }
    
    /**
     * Generates a hash code for this position.
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return 31 * row + column;
    }
}
