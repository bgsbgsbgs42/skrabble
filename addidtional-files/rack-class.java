package pij.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player's tile rack in the SkraBBKle game.
 */
public class Rack {
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