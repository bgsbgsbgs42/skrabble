package pij.util;

/**
 * Utility class for validating user input.
 */
public class InputValidator {
    
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
