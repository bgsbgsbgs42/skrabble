package pij.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for validating words against a dictionary.
 */
public class Dictionary {
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
