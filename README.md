# Skrabble

This project implements a Scrabble-like board game called SkraBBKle, where a human player competes against a computer player. The game follows specific rules for tile placement, scoring, and game termination as described in the project specification. You will find in the additional files folder all the files necessary to run this game. Alternatively, there is a compiled-main file that also has all the functionality of the additonal files.

## How to Run

1. Compile the project:
```
javac -d bin src/pij/main/Main.java src/pij/model/*.java src/pij/player/*.java src/pij/game/*.java src/pij/util/*.java
```

2. Run the game:
```
java -cp bin pij.main.Main
```

## Project Structure

The project is organized into the following packages:

- `pij.main`: Contains the entry point of the application (`Main.java`)
- `pij.model`: Contains the core game model classes (Board, Tile, etc.)
- `pij.player`: Contains the player classes (HumanPlayer, ComputerPlayer)
- `pij.game`: Contains the game logic and state management
- `pij.util`: Contains utility classes for file loading and input validation
- `test`: Contains JUnit tests for the project classes

## Game Rules

The game follows the SkraBBKle rules as specified in the project requirements. Key points include:

- Board size of 16x16 with premium squares for letter and word bonuses
- Initial tile rack of 7 tiles for each player
- Human player starts first and places a word through the center square
- Players take turns adding to the board until the game ends
- Game ends when the tile bag is empty and one player has emptied their rack or when both players pass twice
- The player with the highest score wins

## Testing

The project includes JUnit 5 tests to validate the functionality of various components. Run the tests with:

```
java -cp bin:lib/junit-platform-console-standalone-1.8.1.jar org.junit.platform.console.ConsoleLauncher --scan-classpath --include-engine=junit-jupiter
```
