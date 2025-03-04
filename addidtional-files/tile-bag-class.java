package pij.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the bag of tiles in the SkraBBKle game.
 */
public class TileBag {
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
