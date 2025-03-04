package pij.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading files.
 */
public class FileLoader {
    
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
