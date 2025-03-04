package pij.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    //==========================================================================
    // MODEL CLASSES
    //==========================================================================
    
    /**
     * Enum representing the direction of a move on the board.
     */
    public enum Direction {
        /**
         * Represents a move going from top to bottom
         */
        DOWN,
        
        /**
         * Represents a move going from left to right
         */
        RIGHT
    }
    
    /**
     * Enum representing the type of premium square on the board.
     */
    public enum PremiumType {
        /**
         * Standard square without premium
         */
        NONE,
        
        /**
         * Letter premium square
         */
        LETTER,
        
        /**
         * Word premium square
         */
        WORD
    }
    
    /**
     * Represents a position on the board with row and column.
     */
    public static class Position {
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
    
    /**
     * Represents a tile in the SkraBBKle game with a letter and a value.
     */
    public static class Tile {
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
    
    /**
     * Represents a square on the SkraBBKle board.
     */
    public static class Square {
        private PremiumType premiumType;
        private int premiumValue;
        private Tile tile;
        private boolean isOccupied;
        
        /**
         * Creates a standard square without premium.
         */
        public Square() {
            this.premiumType = PremiumType.NONE;
            this.premiumValue = 1;
            this.tile = null;
            this.isOccupied = false;
        }
        
        /**
         * Creates a premium square with the specified type and value.
         * 
         * @param premiumType Type of premium
         * @param premiumValue Value of premium
         */
        public Square(PremiumType premiumType, int premiumValue) {
            this.premiumType = premiumType;
            this.premiumValue = premiumValue;
            this.tile = null;
            this.isOccupied = false;
        }
        
        /**
         * Get the premium type of this square.
         * 
         * @return Premium type
         */
        public PremiumType getPremiumType() {
            return premiumType;
        }
        
        /**
         * Get the premium value of this square.
         * 
         * @return Premium value
         */
        public int getPremiumValue() {
            return premiumValue;
        }
        
        /**
         * Get the tile on this square.
         * 
         * @return Tile or null if the square is empty
         */
        public Tile getTile() {
            return tile;
        }
        
        /**
         * Check if this square is occupied by a tile.
         * 
         * @return true if the square is occupied, false otherwise
         */
        public boolean isOccupied() {
            return isOccupied;
        }
        
        /**
         * Place a tile on this square.
         * 
         * @param tile Tile to place
         * @return true if the tile was placed successfully, false otherwise
         */
        public boolean placeTile(Tile tile) {
            if (!isOccupied) {
                this.tile = tile;
                this.isOccupied = true;
                return true;
            }
            return false;
        }
        
        /**
         * Returns a string representation of this square.
         * 
         * @return String representation based on the square state
         */
        @Override
        public String toString() {
            if (isOccupied && tile != null) {
                return tile.getLetter() + String.valueOf(tile.getValue());
            } else {
                return switch (premiumType) {
                    case NONE -> " . ";
                    case LETTER -> {
                        if (premiumValue >= 0 && premiumValue < 10) {
                            yield "(" + premiumValue + ")";
                        } else {
                            yield "(" + premiumValue;
                        }
                    }
                    case WORD -> {
                        if (premiumValue >= 0 && premiumValue < 10) {
                            yield "{" + premiumValue + "}";
                        } else {
                            yield "{" + premiumValue;
                        }
                    }
                };
            }
        }
        
        /**
         * Creates a copy of this square.
         * 
         * @return A new square with the same properties
         */
        public Square copy() {
            Square copy = new Square(premiumType, premiumValue);
            if (isOccupied && tile != null) {
                copy.placeTile(tile.copy());
            }
            return copy;
        }
    }
    
    /**
     * Represents the SkraBBKle game board.
     */
    public static class Board {
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
    
    /**
     * Represents a player's tile rack in the SkraBBKle game.
     */
    public static class Rack {
        private final List<Tile> tiles;
        private final int maxSize;
        
        /**
         * Creates a new rack with the specified maximum size.
         * 
         * @param maxSize Maximum number of tiles the rack can hold
         */
        public Rack(int maxSize) {
            this.tiles = new ArrayList<>();
            this.maxSize = maxSize;
        }
        
        /**
         * Get the tiles on the rack.
         * 
         * @return List of tiles
         */
        public List<Tile> getTiles() {
            return new ArrayList<>(tiles);
        }
        
        /**
         * Get the number of tiles on the rack.
         * 
         * @return Number of tiles
         */
        public int getSize() {
            return tiles.size();
        }
        
        /**
         * Check if the rack is empty.
         * 
         * @return true if the rack is empty, false otherwise
         */
        public boolean isEmpty() {
            return tiles.isEmpty();
        }
        
        /**
         * Check if the rack is full.
         * 
         * @return true if the rack is full, false otherwise
         */
        public boolean isFull() {
            return tiles.size() >= maxSize;
        }
        
        /**
         * Add a tile to the rack.
         * 
         * @param tile Tile to add
         * @return true if the tile was added successfully, false otherwise
         */
        public boolean addTile(Tile tile) {
            if (tiles.size() < maxSize) {
                tiles.add(tile);
                return true;
            }
            return false;
        }
        
        /**
         * Remove a tile from the rack.
         * 
         * @param index Index of the tile to remove
         * @return Removed tile or null if the index is invalid
         */
        public Tile removeTile(int index) {
            if (index >= 0 && index < tiles.size()) {
                return tiles.remove(index);
            }
            return null;
        }
        
        /**
         * Remove a specific tile from the rack.
         * 
         * @param tile Tile to remove
         * @return true if the tile was removed successfully, false otherwise
         */
        public boolean removeTile(Tile tile) {
            return tiles.remove(tile);
        }
        
        /**
         * Get the sum of values of all tiles on the rack.
         * 
         * @return Sum of tile values
         */
        public int getTotalValue() {
            int total = 0;
            for (Tile tile : tiles) {
                total += tile.getValue();
            }
            return total;
        }
        
        /**
         * Check if the rack has tiles to form the specified word.
         * Upper case letters in the word represent standard tiles,
         * lower case letters represent wildcards.
         * 
         * @param word Word to check
         * @return true if the rack has the necessary tiles, false otherwise
         */
        public boolean canFormWord(String word) {
            // Create a copy of the tiles to work with
            List<Tile> tempTiles = new ArrayList<>(tiles);
            
            for (char c : word.toCharArray()) {
                boolean found = false;
                
                // Check if the character is uppercase (standard tile)
                if (Character.isUpperCase(c)) {
                    // Look for a matching standard tile
                    for (int i = 0; i < tempTiles.size(); i++) {
                        Tile tile = tempTiles.get(i);
                        if (!tile.isWildcard() && tile.getLetter() == c) {
                            tempTiles.remove(i);
                            found = true;
                            break;
                        }
                    }
                    
                    // If no standard tile found, try to use a wildcard
                    if (!found) {
                        for (int i = 0; i < tempTiles.size(); i++) {
                            Tile tile = tempTiles.get(i);
                            if (tile.isWildcard() && !tile.isWildcardAssigned()) {
                                tempTiles.remove(i);
                                found = true;
                                break;
                            }
                        }
                    }
                } 
                // Check if the character is lowercase (wildcard)
                else if (Character.isLowerCase(c)) {
                    // Look for a wildcard tile
                    for (int i = 0; i < tempTiles.size(); i++) {
                        Tile tile = tempTiles.get(i);
                        if (tile.isWildcard() && !tile.isWildcardAssigned()) {
                            tempTiles.remove(i);
                            found = true;
                            break;
                        }
                    }
                }
                
                if (!found) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Get the tiles needed to form the specified word.
         * Upper case letters in the word represent standard tiles,
         * lower case letters represent wildcards.
         * 
         * @param word Word to form
         * @return List of tiles or null if the word cannot be formed
         */
        public List<Tile> getTilesForWord(String word) {
            if (!canFormWord(word)) {
                return null;
            }
            
            List<Tile> wordTiles = new ArrayList<>();
            List<Tile> tempTiles = new ArrayList<>(tiles);
            
            for (char c : word.toCharArray()) {
                // Check if the character is uppercase (standard tile)
                if (Character.isUpperCase(c)) {
                    boolean found = false;
                    
                    // Look for a matching standard tile
                    for (int i = 0; i < tempTiles.size(); i++) {
                        Tile tile = tempTiles.get(i);
                        if (!tile.isWildcard() && tile.getLetter() == c) {
                            wordTiles.add(tile);
                            tempTiles.remove(i);
                            found = true;
                            break;
                        }
                    }
                    
                    // If no standard tile found, try to use a wildcard
                    if (!found) {
                        for (int i = 0; i < tempTiles.size(); i++) {
                            Tile tile = tempTiles.get(i);
                            if (tile.isWildcard() && !tile.isWildcardAssigned()) {
                                Tile wildcardTile = new Tile(tile.getValue());
                                wildcardTile.assignWildcard(Character.toLowerCase(c));
                                wordTiles.add(wildcardTile);
                                tempTiles.remove(i);
                                break;
                            }
                        }
                    }
                } 
                // Check if the character is lowercase (wildcard)
                else if (Character.isLowerCase(c)) {
                    // Look for a wildcard tile
                    for (int i = 0; i < tempTiles.size(); i++) {
                        Tile tile = tempTiles.get(i);
                        if (tile.isWildcard() && !tile.isWildcardAssigned()) {
                            Tile wildcardTile = new Tile(tile.getValue());
                            wildcardTile.assignWildcard(c);
                            wordTiles.add(wildcardTile);
                            tempTiles.remove(i);
                            break;
                        }
                    }
                }
            }
            
            return wordTiles;
        }
        
        /**
         * Remove the tiles needed to form the specified word from the rack.
         * Upper case letters in the word represent standard tiles,
         * lower case letters represent wildcards.
         * 
         * @param word Word to form
         * @return List of removed tiles or null if the word cannot be formed
         */
        public List<Tile> removeWordTiles(String word) {
            List<Tile> wordTiles = getTilesForWord(word);
            
            if (wordTiles != null) {
                for (Tile tile : wordTiles) {
                    // Find and remove the corresponding tile from the rack
                    if (tile.isWildcard() && tile.isWildcardAssigned()) {
                        // Find an unassigned wildcard in the rack
                        for (int i = 0; i < tiles.size(); i++) {
                            if (tiles.get(i).isWildcard() && !tiles.get(i).isWildcardAssigned()) {
                                tiles.remove(i);
                                break;
                            }
                        }
                    } else {
                        // Find a matching standard tile
                        for (int i = 0; i < tiles.size(); i++) {
                            if (tiles.get(i).getLetter() == tile.getLetter() && 
                                tiles.get(i).getValue() == tile.getValue() &&
                                !tiles.get(i).isWildcard()) {
                                tiles.remove(i);
                                break;
                            }
                        }
                    }
                }
            }
            
            return wordTiles;
        }
        
        /**
         * Returns a string representation of the rack.
         * 
         * @return String representation of the tiles on the rack
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            
            for (int i = 0; i < tiles.size(); i++) {
                builder.append(tiles.get(i).toString());
                if (i < tiles.size() - 1) {
                    builder.append(", ");
                }
            }
            
            return builder.toString();
        }
    }
    
    /**
     * Represents the bag of tiles in the SkraBBKle game.
     */
    public static class TileBag {
        private final List<Tile> tiles;
        
        /**
         * Creates a new tile bag with the initial set of tiles for SkraBBKle.
         */
        public TileBag() {
            this.tiles = new ArrayList<>();
            initializeDefaultTiles();
        }
        
        /**
         * Initializes the tile bag with the default distribution of tiles.
         */
        private void initializeDefaultTiles() {
            // Add letter tiles according to the SkraBBKle specifications
            addTiles('A', 1, 8);
            addTiles('B', 3, 2);
            addTiles('C', 3, 2);
            addTiles('D', 2, 4);
            addTiles('E', 1, 10);
            addTiles('F', 4, 3);
            addTiles('G', 2, 4);
            addTiles('H', 4, 3);
            addTiles('I', 1, 8);
            addTiles('J', 9, 1);
            addTiles('K', 6, 1);
            addTiles('L', 1, 4);
            addTiles('M', 3, 2);
            addTiles('N', 1, 7);
            addTiles('O', 1, 7);
            addTiles('P', 3, 2);
            addTiles('Q', 12, 1);
            addTiles('R', 1, 6);
            addTiles('S', 1, 4);
            addTiles('T', 1, 6);
            addTiles('U', 1, 5);
            addTiles('V', 4, 2);
            addTiles('W', 4, 1);
            addTiles('X', 9, 1);
            addTiles('Y', 5, 2);
            addTiles('Z', 11, 1);
            
            // Add wildcards (value 5)
            for (int i = 0; i < 2; i++) {
                tiles.add(new Tile(5));
            }
            
            // Shuffle the tiles
            shuffle();
        }
        
        /**
         * Adds tiles with the specified letter, value, and quantity to the bag.
         * 
         * @param letter Letter on the tile
         * @param value Value of the tile
         * @param quantity Number of tiles to add
         */
        private void addTiles(char letter, int value, int quantity) {
            for (int i = 0; i < quantity; i++) {
                tiles.add(new Tile(letter, value));
            }
        }
        
        /**
         * Shuffles the tiles in the bag.
         */
        public void shuffle() {
            Collections.shuffle(tiles);
        }
        
        /**
         * Get the number of tiles remaining in the bag.
         * 
         * @return Number of tiles
         */
        public int getSize() {
            return tiles.size();
        }
        
        /**
         * Check if the bag is empty.
         * 
         * @return true if the bag is empty, false otherwise
         */
        public boolean isEmpty() {
            return tiles.isEmpty();
        }
        
        /**
         * Draw a tile from the bag.
         * 
         * @return Tile or null if the bag is empty
         */
        public Tile drawTile() {
            if (!tiles.isEmpty()) {
                return tiles.remove(0);
            }
            return null;
        }
        
        /**
         * Draw multiple tiles from the bag.
         * 
         * @param count Number of tiles to draw
         * @return List of drawn tiles (may be smaller than count if bag doesn't have enough)
         */
        public List<Tile> drawTiles(int count) {
            List<Tile> drawnTiles = new ArrayList<>();
            
            for (int i = 0; i < count && !tiles.isEmpty(); i++) {
                drawnTiles.add(drawTile());
            }
            
            return drawnTiles;
        }
        
        /**
         * Returns a string representation of the tile bag.
         * 
         * @return String representation of the tiles in the bag
         */
        @Override
        public String toString() {
            return "Tile bag: " + tiles.size() + " tiles remaining";
        }
    }
    
    /**
     * Represents a move in the SkraBBKle game.
     */
    public static class Move {
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
    
    //==========================================================================
    // PLAYER CLASSES
    //==========================================================================
    
    /**
     * Abstract class representing a player in the SkraBBKle game.
     */
    public static abstract class Player {
        private final String name;
        private int score;
        private final Rack rack;
        private boolean hasPassedLastTurn;
        
        /**
         * Creates a player with the specified name.
         * 
         * @param name Player's name
         */
        public Player(String name) {
            this.name = name;
            this.score = 0;
            this.rack = new Rack(7);
            this.hasPassedLastTurn = false;
        }
        
        /**
         * Get the player's name.
         * 
         * @return Player's name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the player's score.
         * 
         * @return Player's score
         */
        public int getScore() {
            return score;
        }
        
        /**
         * Get the player's tile rack.
         * 
         * @return Player's tile rack
         */
        public Rack getRack() {
            return rack;
        }
        
        /**
         * Check if the player passed their last turn.
         * 
         * @return true if the player passed their last turn, false otherwise
         */
        public boolean hasPassedLastTurn() {
            return hasPassedLastTurn;
        }
        
        /**
         * Set whether the player passed their last turn.
         * 
         * @param passed true if the player passed their last turn, false otherwise
         */
        public void setPassedLastTurn(boolean passed) {
            this.hasPassedLastTurn = passed;
        }
        
        /**
         * Add points to the player's score.
         * 
         * @param points Points to add
         */
        public void addScore(int points) {
            this.score += points;
        }
        
        /**
         * Fill the player's rack from the tile bag.
         * 
         * @param tileBag Tile bag to draw from
         */
        public void fillRack(TileBag tileBag) {
            while (!rack.isFull() && !tileBag.isEmpty()) {
                Tile tile = tileBag.drawTile();
                if (tile != null) {
                    rack.addTile(tile);
                }
            }
        }
        
        /**
         * Make a move on the board.
         * 
         * @param board Current board state
         * @param tileBag Tile bag
         * @return Move made by the player
         */
        public abstract Move makeMove(Board board, TileBag tileBag);
        
        /**
         * Apply a final score penalty for unused tiles.
         */
        public void applyUnusedTilesPenalty() {
            score -= rack.getTotalValue();
        }
        
        /**
         * Check if the player has emptied their rack.
         * 
         * @return true if the player's rack is empty, false otherwise
         */
        public boolean hasEmptyRack() {
            return rack.isEmpty();
        }
    }
    
    /**
     * Class representing a human player in the SkraBBKle game.
     */
    public static class HumanPlayer extends Player {
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
            
            // Check if the move is valid for the board
            if (!move.isPass() && !board.isValidMove(move.getPosition(), move.getDirection(), move.getWord())) {
                System.out.println("The board does not permit word " + move.getWord() + " at position " + move.getPosition() + ". Please try again.");
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
    
    /**
     * Class representing a computer player in the SkraBBKle game.
     */
    public static class ComputerPlayer extends Player {
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
    
    //==========================================================================
    // UTILITY CLASSES
    //==========================================================================
    
    /**
     * Utility class for loading files.
     */
    public static class FileLoader {
        
        /**
         * Load a file and return its contents as a list of lines.
         * 
         * @param filePath Path to the file
         * @return List of lines in the file
         * @throws IOException If the file cannot be read
         */
        public static List<String> loadFile(String filePath) throws IOException {
            List<String> lines = new ArrayList<>();
            
            try {
                Path path = Paths.get(filePath);
                lines = Files.readAllLines(path);
            } catch (IOException e) {
                // Try to load from resources
                try (InputStream inputStream = FileLoader.class.getClassLoader()
                        .getResourceAsStream(filePath)) {
                    if (inputStream == null) {
                        throw new IOException("File not found: " + filePath);
                    }
                    
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    }
                } catch (IOException ex) {
                    throw new IOException("Failed to load file: " + filePath, ex);
                }
            }
            
            return lines;
        }
        
        /**
         * Check if a file exists and is readable.
         * 
         * @param filePath Path to the file
         * @return true if the file exists and is readable, false otherwise
         */
        public static boolean fileExists(String filePath) {
            if (Files.exists(Paths.get(filePath)) && Files.isReadable(Paths.get(filePath))) {
                return true;
            }
            
            // Check in resources
            try (InputStream inputStream = FileLoader.class.getClassLoader()
                    .getResourceAsStream(filePath)) {
                return inputStream != null;
            } catch (IOException e) {
                return false;
            }
        }
    }
    
    /**
     * Utility class for loading board configurations from files.
     */
    public static class BoardLoader {
        
        // Regular expressions for parsing the board tokens
        private static final String STANDARD_PATTERN = "\\.";
        private static final String LETTER_PREMIUM_PATTERN = "\\(([-]?\\d{1,2})\\)";
        private static final String WORD_PREMIUM_PATTERN = "\\{([-]?\\d{1,2})\\}";
        
        /**
         * Load a board from a file.
         * 
         * @param filePath Path to the board file
         * @return Loaded board or null if the file is invalid
         */
        public static Board loadBoard(String filePath) {
            try {
                List<String> lines = FileLoader.loadFile(filePath);
                
                if (lines.isEmpty()) {
                    return null;
                }
                
                // Parse the board size from the first line
                int size;
                try {
                    size = Integer.parseInt(lines.get(0).trim());
                    
                    // Check if the size is valid
                    if (size < 11 || size > 26) {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
                
                // Create a new board
                Board board = new Board(size);
                
                // Parse the board squares from the remaining lines
                for (int row = 0; row < size && row + 1 < lines.size(); row++) {
                    String line = lines.get(row + 1);
                    
                    // Check if the line has the correct length (no spaces allowed)
                    if (!isValidBoardLine(line, size)) {
                        return null;
                    }
                    
                    // Parse the tokens in the line
                    int col = 0;
                    int pos = 0;
                    
                    while (col < size && pos < line.length()) {
                        Square square = parseSquare(line, pos);
                        
                        if (square != null) {
                            board.setSquare(new Position(row + 1, col), square);
                            
                            // Move to the next token
                            if (line.charAt(pos) == '.') {
                                // Standard square is 1 character
                                pos += 1;
                            } else if (line.charAt(pos) == '(') {
                                // Letter premium square is 3-4 characters
                                String token = extractToken(line, pos, LETTER_PREMIUM_PATTERN);
                                pos += token.length();
                            } else if (line.charAt(pos) == '{') {
                                // Word premium square is 3-4 characters
                                String token = extractToken(line, pos, WORD_PREMIUM_PATTERN);
                                pos += token.length();
                            } else {
                                // Invalid token
                                return null;
                            }
                            
                            col++;
                        } else {
                            // Invalid token
                            return null;
                        }
                    }
                    
                    // Check if we parsed all columns
                    if (col != size) {
                        return null;
                    }
                }
                
                // Check if we parsed all rows
                if (lines.size() - 1 != size) {
                    return null;
                }
                
                return board;
            } catch (IOException e) {
                return null;
            }
        }
        
        /**
         * Check if a board line is valid.
         * 
         * @param line Line to check
         * @param size Expected number of tokens in the line
         * @return true if the line is valid, false otherwise
         */
        private static boolean isValidBoardLine(String line, int size) {
            if (line == null || line.isEmpty()) {
                return false;
            }
            
            // Count the number of tokens in the line
            int count = 0;
            int pos = 0;
            
            while (pos < line.length()) {
                if (line.charAt(pos) == '.') {
                    // Standard square is 1 character
                    pos += 1;
                    count++;
                } else if (line.charAt(pos) == '(') {
                    // Letter premium square is 3-4 characters
                    String token = extractToken(line, pos, LETTER_PREMIUM_PATTERN);
                    if (token.isEmpty()) {
                        return false;
                    }
                    pos += token.length();
                    count++;
                } else if (line.charAt(pos) == '{') {
                    // Word premium square is 3-4 characters
                    String token = extractToken(line, pos, WORD_PREMIUM_PATTERN);
                    if (token.isEmpty()) {
                        return false;
                    }
                    pos += token.length();
                    count++;
                } else {
                    // Invalid token
                    return false;
                }
            }
            
            return count == size;
        }
        
        /**
         * Extract a token from a line using a regex pattern.
         * 
         * @param line Line to extract from
         * @param pos Starting position in the line
         * @param pattern Regex pattern to match
         * @return Extracted token or empty string if no match
         */
        private static String extractToken(String line, int pos, String pattern) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(line.substring(pos));
            
            if (m.find() && m.start() == 0) {
                return m.group();
            }
            
            return "";
        }
        
        /**
         * Parse a square token from a line.
         * 
         * @param line Line to parse from
         * @param pos Position in the line
         * @return Parsed square or null if the token is invalid
         */
        private static Square parseSquare(String line, int pos) {
            if (pos >= line.length()) {
                return null;
            }
            
            char firstChar = line.charAt(pos);
            
            if (firstChar == '.') {
                // Standard square
                return new Square();
            } else if (firstChar == '(') {
                // Letter premium square
                String token = extractToken(line, pos, LETTER_PREMIUM_PATTERN);
                
                if (!token.isEmpty()) {
                    try {
                        // Extract the premium value from the token
                        int value = Integer.parseInt(token.substring(1, token.length() - 1));
                        
                        // Check if the value is valid
                        if (value >= -9 && value <= 99) {
                            return new Square(PremiumType.LETTER, value);
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else if (firstChar == '{') {
                // Word premium square
                String token = extractToken(line, pos, WORD_PREMIUM_PATTERN);
                
                if (!token.isEmpty()) {
                    try {
                        // Extract the premium value from the token
                        int value = Integer.parseInt(token.substring(1, token.length() - 1));
                        
                        // Check if the value is valid
                        if (value >= -9 && value <= 99) {
                            return new Square(PremiumType.WORD, value);
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Creates the default SkraBBKle board.
         * 
         * @return Default board
         */
        public static Board createDefaultBoard() {
            // Create a board of size 16 (as per the specification)
            Board board = new Board(16);
            
            // Define the board layout as specified
            String[] boardLayout = {
                ".{12}..(2)...{3}...(2)..{8}",
                "..{2}...(3)...(3)...{2}.",
                "...{2}...(2).(2)...{2}..",
                ".(2)..{2}...(2)...{2}..(2)",
                ".....{2}.....{0}....",
                "..(3)...(3)...(3)...(3).",
                "...(2)...(2).(2)...(2)..",
                "(3){-3}..(2)...{2}...(-4)..{-2}",
                "...(2)...(2).(2)...(2)..",
                "..(3)...(3)...(3)...(3).",
                ".....{2}.....{2}....",
                ".(2)..{-1}...(0)...{-1}..(2)",
                "...{2}...(2).(9)...{2}..",
                "..{2}...(3)...(3)...{2}.",
                ".{9}..(2)...{3}...(2)..{16}",
                "........(3)......."
            };
            
            // Parse each line and set the squares on the board
            for (int row = 0; row < 16; row++) {
                String line = boardLayout[row];
                
                int col = 0;
                int pos = 0;
                
                while (col < 16 && pos < line.length()) {
                    Square square = parseSquare(line, pos);
                    
                    if (square != null) {
                        board.setSquare(new Position(row + 1, col), square);
                        
                        // Move to the next token
                        if (line.charAt(pos) == '.') {
                            // Standard square is 1 character
                            pos += 1;
                        } else if (line.charAt(pos) == '(') {
                            // Letter premium square is 3-4 characters
                            String token = extractToken(line, pos, LETTER_PREMIUM_PATTERN);
                            pos += token.length();
                        } else if (line.charAt(pos) == '{') {
                            // Word premium square is 3-4 characters
                            String token = extractToken(line, pos, WORD_PREMIUM_PATTERN);
                            pos += token.length();
                        }
                        
                        col++;
                    } else {
                        break;
                    }
                }
            }
            
            return board;
        }
    }
    
    /**
     * Utility class for validating words against a dictionary.
     */
    public static class Dictionary {
        private final Set<String> words;
        
        /**
         * Creates a dictionary from a word list file.
         * 
         * @param filePath Path to the word list file
         * @throws IOException If the file cannot be read
         */
        public Dictionary(String filePath) throws IOException {
            words = new HashSet<>();
            loadWordList(filePath);
        }
        
        /**
         * Load a word list from a file.
         * 
         * @param filePath Path to the word list file
         * @throws IOException If the file cannot be read
         */
        private void loadWordList(String filePath) throws IOException {
            List<String> lines = FileLoader.loadFile(filePath);
            
            for (String line : lines) {
                // Trim and convert to uppercase
                String word = line.trim().toUpperCase();
                
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        }
        
        /**
         * Check if a word is in the dictionary.
         * 
         * @param word Word to check
         * @return true if the word is in the dictionary, false otherwise
         */
        public boolean isValidWord(String word) {
            if (word == null || word.isEmpty()) {
                return false;
            }
            
            // Convert to uppercase for case-insensitive comparison
            return words.contains(word.toUpperCase());
        }
        
        /**
         * Get the number of words in the dictionary.
         * 
         * @return Number of words
         */
        public int getSize() {
            return words.size();
        }
    }
    
    /**
     * Utility class for validating user input.
     */
    public static class InputValidator {
        
        /**
         * Validate a move string in the format "word,square".
         * 
         * @param moveString Move string to validate
         * @return true if the move string is valid, false otherwise
         */
        public static boolean isValidMoveFormat(String moveString) {
            if (moveString == null) {
                return false;
            }
            
            // Check for pass move
            if (moveString.equals(",")) {
                return true;
            }
            
            // Split the move string into word and position
            String[] parts = moveString.split(",");
            if (parts.length != 2) {
                return false;
            }
            
            String word = parts[0];
            String positionStr = parts[1];
            
            // Check if the word is valid (non-empty)
            if (word.isEmpty()) {
                return false;
            }
            
            // Check if the position string is valid (column letter and row number, or vice versa)
            if (!isValidPositionFormat(positionStr)) {
                return false;
            }
            
            return true;
        }
        
        /**
         * Validate a position string (e.g., "a1", "b2", "3c", "4d").
         * 
         * @param positionStr Position string to validate
         * @return true if the position string is valid, false otherwise
         */
        public static boolean isValidPositionFormat(String positionStr) {
            if (positionStr == null || positionStr.isEmpty() || positionStr.length() < 2) {
                return false;
            }
            
            // Check if the position string starts with a letter (a-z) or a digit
            char firstChar = positionStr.charAt(0);
            
            if (Character.isLetter(firstChar)) {
                // Format: column letter, row number (e.g., "a1", "b2")
                // Check if all characters after the first are digits
                for (int i = 1; i < positionStr.length(); i++) {
                    if (!Character.isDigit(positionStr.charAt(i))) {
                        return false;
                    }
                }
                
                // Check if the letter is valid (a-z)
                if (Character.toLowerCase(firstChar) < 'a' || Character.toLowerCase(firstChar) > 'z') {
                    return false;
                }
            } else if (Character.isDigit(firstChar)) {
                // Format: row number, column letter (e.g., "1a", "2b")
                // Check if the last character is a letter
                char lastChar = positionStr.charAt(positionStr.length() - 1);
                if (!Character.isLetter(lastChar)) {
                    return false;
                }
                
                // Check if all characters before the last are digits
                for (int i = 0; i < positionStr.length() - 1; i++) {
                    if (!Character.isDigit(positionStr.charAt(i))) {
                        return false;
                    }
                }
                
                // Check if the letter is valid (a-z)
                if (Character.toLowerCase(lastChar) < 'a' || Character.toLowerCase(lastChar) > 'z') {
                    return false;
                }
            } else {
                return false;
            }
            
            return true;
        }
        
        /**
         * Validate a choice input (e.g., "l", "d", "o", "c").
         * 
         * @param input Input to validate
         * @param validChoices Array of valid choices
         * @return true if the input is valid, false otherwise
         */
        public static boolean isValidChoice(String input, String[] validChoices) {
            if (input == null || input.isEmpty() || validChoices == null || validChoices.length == 0) {
                return false;
            }
            
            // Convert to lowercase for case-insensitive comparison
            String lowerInput = input.toLowerCase();
            
            for (String choice : validChoices) {
                if (lowerInput.equals(choice.toLowerCase())) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    //==========================================================================
    // GAME CLASSES
    //==========================================================================
    
    /**
     * Enum representing the current state of the SkraBBKle game.
     */
    public enum GameState {
        /**
         * Game is in progress
         */
        IN_PROGRESS,
        
        /**
         * Game has ended with a human player win
         */
        HUMAN_WIN,
        
        /**
         * Game has ended with a computer player win
         */
        COMPUTER_WIN,
        
        /**
         * Game has ended in a draw
         */
        DRAW
    }
    
    /**
     * Factory class for creating game instances.
     */
    public static class GameFactory {
        
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
    
    /**
     * Main class representing the SkraBBKle game.
     */
    public static class Game {
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
                    if (square.getPremiumType() == PremiumType.LETTER) {
                        tileScore *= square.getPremiumValue();
                    }
                    
                    wordScore += tileScore;
                    
                    // Apply word premium
                    if (square.getPremiumType() == PremiumType.WORD) {
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
}
