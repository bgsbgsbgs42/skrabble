package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import pij.model.Tile;

/**
 * Tests for the Tile class.
 */
public class TileTest {
    
    @Test
    public void testRegularTileCreation() {
        Tile tile = new Tile('A', 1);
        assertEquals('A', tile.getLetter());
        assertEquals(1, tile.getValue());
        assertFalse(tile.isWildcard());
        assertFalse(tile.isWildcardAssigned());
    }
    
    @Test
    public void testWildcardTileCreation() {
        Tile tile = new Tile(5);
        assertEquals('_', tile.getLetter());
        assertEquals(5, tile.getValue());
        assertTrue(tile.isWildcard());
        assertFalse(tile.isWildcardAssigned());
    }
    
    @Test
    public void testWildcardAssignment() {
        Tile tile = new Tile(5);
        assertTrue(tile.assignWildcard('a'));
        assertEquals('a', tile.getLetter());
        assertTrue(tile.isWildcardAssigned());
    }
    
    @Test
    public void testCannotAssignWildcardTwice() {
        Tile tile = new Tile(5);
        assertTrue(tile.assignWildcard('a'));
        assertFalse(tile.assignWildcard('b'));
        assertEquals('a', tile.getLetter());
    }
    
    @Test
    public void testCannotAssignToRegularTile() {
        Tile tile = new Tile('A', 1);
        assertFalse(tile.assignWildcard('b'));
        assertEquals('A', tile.getLetter());
    }
    
    @Test
    public void testCopyRegularTile() {
        Tile original = new Tile('A', 1);
        Tile copy = original.copy();
        
        assertEquals(original.getLetter(), copy.getLetter());
        assertEquals(original.getValue(), copy.getValue());
        assertEquals(original.isWildcard(), copy.isWildcard());
        assertEquals(original.isWildcardAssigned(), copy.isWildcardAssigned());
        
        // Verify they are distinct objects
        assertNotSame(original, copy);
    }
    
    @Test
    public void testCopyWildcardTile() {
        Tile original = new Tile(5);
        original.assignWildcard('a');
        
        Tile copy = original.copy();
        
        assertEquals(original.getLetter(), copy.getLetter());
        assertEquals(original.getValue(), copy.getValue());
        assertEquals(original.isWildcard(), copy.isWildcard());
        assertEquals(original.isWildcardAssigned(), copy.isWildcardAssigned());
        
        // Verify they are distinct objects
        assertNotSame(original, copy);
    }
}
