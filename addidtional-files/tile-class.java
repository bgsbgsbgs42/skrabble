package pij.model;

/**
 * Represents a tile in the SkraBBKle game with a letter and a value.
 */
public class Tile {
    private char letter;
    private final int value;
    private final boolean isWildcard;
    private boolean wildcardAssigned;
    
    /**
     * Creates a regular tile with the specified letter and value.
     * 
     * @param letter The letter on the tile
     * @param value The numerical value of the tile
     */
    public Tile(char letter, int value) {
        this.letter = Character.toUpperCase(letter);
        this.value = value;
        this.isWildcard = false;
        this.wildcardAssigned = false;
    }
    
    /**
     * Creates a wildcard tile with the specified value.
     * 
     * @param value The numerical value of the wildcard tile
     */
    public Tile(int value) {
        this.letter = '_';
        this.value = value;
        this.isWildcard = true;
        this.wildcardAssigned = false;
    }
    
    /**
     * Get the letter on this tile.
     * 
     * @return The letter
     */
    public char getLetter() {
        return letter;
    }
    
    /**
     * Get the numerical value of this tile.
     * 
     * @return The value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Check if this tile is a wildcard.
     * 
     * @return true if this is a wildcard tile, false otherwise
     */
    public boolean isWildcard() {
        return isWildcard;
    }
    
    /**
     * Check if this wildcard tile has been assigned a letter.
     * 
     * @return true if the wildcard has been assigned, false otherwise
     */
    public boolean isWildcardAssigned() {
        return wildcardAssigned;
    }
    
    /**
     * Assign a letter to this wildcard tile.
     * 
     * @param letter The letter to assign
     * @return true if the assignment was successful, false otherwise
     */
    public boolean assignWildcard(char letter) {
        if (isWildcard && !wildcardAssigned) {
            this.letter = Character.toLowerCase(letter);
            this.wildcardAssigned = true;
            return true;
        }
        return false;
    }
    
    /**
     * Creates a copy of this tile.
     * 
     * @return A new tile with the same properties
     */
    public Tile copy() {
        if (isWildcard) {
            Tile copy = new Tile(value);
            if (wildcardAssigned) {
                copy.assignWildcard(letter);
            }
            return copy;
        } else {
            return new Tile(letter, value);
        }
    }
    
    /**
     * Returns a string representation of this tile.
     * 
     * @return String in the format "[X1]" for a regular tile with letter X and value 1,
     *         or "[_5]" for an unassigned wildcard with value 5,
     *         or "[a5]" for a wildcard assigned to 'a' with value 5.
     */
    @Override
    public String toString() {
        return "[" + letter + value + "]";
    }
    
    /**
     * Checks if two tiles are equal.
     * 
     * @param obj Object to compare with
     * @return true if the tiles are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Tile other = (Tile) obj;
        return letter == other.letter && 
               value == other.value && 
               isWildcard == other.isWildcard && 
               wildcardAssigned == other.wildcardAssigned;
    }
    
    /**
     * Generates a hash code for this tile.
     * 
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int result = letter;
        result = 31 * result + value;
        result = 31 * result + (isWildcard ? 1 : 0);
        result = 31 * result + (wildcardAssigned ? 1 : 0);
        return result;
    }
}
