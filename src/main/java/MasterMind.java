import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class MasterMind {
	static SwiftBotAPI API;
    private final String[] COLORS = {"R", "G", "B", "Y", "O", "P"};  // Available colors
    private String[] secretCode;  // The secret code the player needs to guess
    private int maxAttempts = 6;  // Maximum number of attempts allowed
    private int playerScore = 0;  // Player's score
    private int botScore = 0;  // Bot's score
    private File logFile = new File("game_log.txt");  // Log file for the game
    private boolean programOn = true;

    // For handling button presses during the game
    private CountDownLatch buttonLatch;
    private String buttonPressed = "";
    private int numColorsSelected = 4;  // Default number of colors in the code
    private int attemptsSelected = 6;  // Default maximum number of attempts
    
    private void setupButtons(SwiftBotAPI bot) {
    	API = bot;
        // Set up button A and B for game mode selection
        API.enableButton(Button.A, () -> {
            buttonPressed = "A";
            if (buttonLatch != null && buttonLatch.getCount() > 0) {
                buttonLatch.countDown();  // Countdown to allow progression
                System.out.println("You have selected Default mode.");
            }
        });
        
        // Set up button B for customized mode selection
        API.enableButton(Button.B, () -> {
            buttonPressed = "B";
            if (buttonLatch != null && buttonLatch.getCount() > 0) {
                buttonLatch.countDown();
                System.out.println("You have selected Customized mode.");
            }
        });
        
        // Additional buttons (X and Y) for other interactions
        API.enableButton(Button.X, () -> {
            buttonPressed = "X";
            if (buttonLatch != null && buttonLatch.getCount() > 0) {
                buttonLatch.countDown();
            }
        });
        
        API.enableButton(Button.Y, () -> {
            buttonPressed = "Y";
            if (buttonLatch != null && buttonLatch.getCount() > 0) {
                buttonLatch.countDown();
            }
        });
    }
    
    // Waits for a button press and returns the button pressed
    private String waitForButtonPress() {
        buttonLatch = new CountDownLatch(1);
        buttonPressed = "";
        try {
            buttonLatch.await();  // Wait until a button is pressed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buttonPressed;
    }
    
    // Gets a numeric input for things like number of colors and attempts
    private int getNumericInput(String prompt, int min, int max) {
        System.out.println(prompt);
        System.out.println("Press A to decrease, B to increase, X to confirm");
        
        int value = min;  // Initialize value to the minimum
        System.out.println("Current value: " + value);
        
        while (true) {
            String button = waitForButtonPress();
            
            // Decrease the value if 'A' is pressed
            if (button.equals("A") && value > min) {
                value--;
                System.out.println("Current value: " + value);
            } 
            // Increase the value if 'B' is pressed
            else if (button.equals("B") && value < max) {
                value++;
                System.out.println("Current value: " + value);
            } 
            // Confirm the value with 'X' button
            else if (button.equals("X")) {
                return value;
            }
        }
    }

    // Starts the game loop where the user can select a mode and play
    public void startGame(SwiftBotAPI bot) {
    	setupButtons(bot);
        while (programOn) {
            System.out.println("Press 'A' for Default mode or 'B' for Customized mode: or Press 'X' to quit ");
            String mode = waitForButtonPress();
            
            // Choose game mode based on the button press
            if (mode.equals("A")) {
                playGame(4, maxAttempts);  // Default mode: 4 colors, 6 attempts
            } else if (mode.equals("B")) {
                numColorsSelected = getNumericInput("Enter number of colors:", 3, 6);
                attemptsSelected = getNumericInput("Enter max attempts:", 1, 10);
                playGame(numColorsSelected, attemptsSelected);  // Customized mode
            } else if (mode.equals("X")) {
            	programOn = false;
            }
            
            else {
                System.out.println("Invalid choice. Try again.");
                continue;
            }
            
            // Ask the player if they want to quit or play again
            System.out.println("Press 'X' to quit or 'Y' to play again:");
            String choice = waitForButtonPress();
            if (choice.equals("X")) programOn = false;
        }
        System.out.println("Game log saved at: " + logFile.getAbsolutePath());
        
        // Disable buttons when game is over
        API.disableButton(Button.A);
        API.disableButton(Button.B);
        API.disableButton(Button.X);
        API.disableButton(Button.Y);
        programOn = true;
    }

    // Main game logic for playing the game with given number of colors and attempts
    private void playGame(int numColors, int maxAttempts) {
        generateSecretCode(numColors);  // Generate a random secret code
        int attemptsLeft = maxAttempts;

        // Game loop for each attempt
        while (attemptsLeft > 0) {
            System.out.println("Show color cards one by one:");
            String[] playerGuess = new String[numColors];
            for (int i = 0; i < numColors; i++) {
                System.out.println("Showing color " + (i+1) + " of " + numColors);
                System.out.println("Press A to scan color card");
                
                // Wait for button press to scan color
                String button = waitForButtonPress();
                if (button.equals("A")) {
                    playerGuess[i] = scanColorCard();  // Scan the color card
                    System.out.println("Scanned color: " + playerGuess[i]);
                } else {
                    i--;  // Retry if wrong button pressed
                    System.out.println("Please press A to scan");
                }
            }
            
            // Get feedback for the guess and log the round
            String feedback = getFeedback(playerGuess);
            System.out.println("Feedback: " + feedback);
            logRound(playerGuess, feedback, maxAttempts - attemptsLeft + 1, attemptsLeft - 1);
            
            // Check if the guess is correct
            if (feedback.equals("+".repeat(numColors))) {
                System.out.println("Congratulations! You won!");
                playerScore++;
                return;
            }
            attemptsLeft--;
            
            // Ask to continue to the next guess
            System.out.println("Attempts left: " + attemptsLeft);
            System.out.println("Press A to continue to next guess");
            waitForButtonPress();
        }

        // If no attempts are left, the game is over
        System.out.println("Game over! The correct code was: " + String.join("", secretCode));
        botScore++;
    }

    // Generate a random secret code of a given number of colors
    private void generateSecretCode(int numColors) {
        List<String> colorList = new ArrayList<>(Arrays.asList(COLORS));
        Collections.shuffle(colorList);  // Shuffle the list of colors
        secretCode = colorList.subList(0, numColors).toArray(new String[0]);  // Select the first 'numColors' colors
    }

    // Scan the color card and return the identified color
    private String scanColorCard() {
        try {
            System.out.println("Taking image...");
            BufferedImage img = API.takeStill(ImageSize.SQUARE_1080x1080);
            
            // Save the scanned image for debugging purposes
            try {
                ImageIO.write(img, "jpg", new File("scanned_color.jpg"));
                System.out.println("Image saved to scanned_color.jpg");
            } catch (IOException e) {
                System.out.println("Failed to save image: " + e.getMessage());
            }
            
            // Process the image to identify the color
            return improvedIdentifyColor(img);
        } catch (Exception e) {
            System.out.println("Error scanning color: " + e.getMessage());
            e.printStackTrace();
            return "?";  // Return a default value in case of error
        }
    }

    // Improved color identification using the image's center region
    private String improvedIdentifyColor(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        // Focus on the center of the image for better color detection
        int centerX = width / 2;
        int centerY = height / 2;
        int sampleRadius = Math.min(width, height) / 4;
        
        // Accumulate RGB values from the center region
        long sumR = 0, sumG = 0, sumB = 0;
        int pixelCount = 0;
        
        // Loop through the pixels in the center region
        for (int x = centerX - sampleRadius; x < centerX + sampleRadius; x++) {
            for (int y = centerY - sampleRadius; y < centerY + sampleRadius; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixel = img.getRGB(x, y);
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    
                    // Ignore black or white pixels
                    if (!isBlackOrWhite(r, g, b)) {
                        sumR += r;
                        sumG += g;
                        sumB += b;
                        pixelCount++;
                    }
                }
            }
        }
        
        // If no valid pixels found, return a default color
        if (pixelCount == 0) {
            System.out.println("Warning: No valid pixels found in the image");
            return "?";
        }
        
        // Compute the average color values from the sampled region
        int avgR = (int) (sumR / pixelCount);
        int avgG = (int) (sumG / pixelCount);
        int avgB = (int) (sumB / pixelCount);
        
        // Identify the closest color by comparing RGB values
        System.out.println("Average RGB: (" + avgR + ", " + avgG + ", " + avgB + ")");
        return determineColorByDistance(avgR, avgG, avgB);
    }
    
    // Helper method to check if a pixel is too black or too white
    private boolean isBlackOrWhite(int r, int g, int b) {
        boolean tooBlack = r < 30 && g < 30 && b < 30;
        boolean tooWhite = r > 230 && g > 230 && b > 230;
        return tooBlack || tooWhite;
    }
    
    // Determine the closest color based on RGB distance
    private String determineColorByDistance(int r, int g, int b) {
        int[][] colorRGB = {
            {255, 0, 0},    // R - Red
            {0, 200, 0},    // G - Green
            {0, 0, 255},    // B - Blue
            {255, 255, 0},  // Y - Yellow
            {255, 165, 0},  // O - Orange
            {150, 0, 200}   // P - Purple
        };
        
        int minDistance = Integer.MAX_VALUE;
        int closestColorIndex = 0;
        
        // Calculate Euclidean distance between RGB values
        for (int i = 0; i < COLORS.length; i++) {
            int dr = r - colorRGB[i][0];
            int dg = g - colorRGB[i][1];
            int db = b - colorRGB[i][2];
            int distance = dr * dr + dg * dg + db * db;
            
            if (distance < minDistance) {
                minDistance = distance;
                closestColorIndex = i;
            }
        }
        
        // Fallback method based on dominant RGB channel
        String dominantChannel = colorDominance(r, g, b);
        System.out.println("Closest color: " + COLORS[closestColorIndex] + ", Dominant channel: " + dominantChannel);
        
        // If the color is too ambiguous, use the dominant channel for fallback
        if (minDistance > 25000) {
            System.out.println("Using dominant channel due to large distance: " + minDistance);
            return dominantChannel;
        }
        
        return COLORS[closestColorIndex];
    }
    
    // Determine color dominance based on the strongest RGB channel
    private String colorDominance(int r, int g, int b) {
        if (r > g && r > b) {
            if (g > b * 1.5) return "Y"; // Yellow - strong red and green
            if (g > b) return "O";       // Orange - strong red, moderate green
            return "R";                  // Red - strong red only
        } else if (g > r && g > b) {
            return "G";                  // Green - dominant green
        } else if (b > r && b > g) {
            if (r > g * 1.5) return "P"; // Purple - strong blue and red
            return "B";                  // Blue - dominant blue
        }
        
        // Default fallback based on channel dominance
        if (r > 100 && g > 100) return "Y"; // Yellow-ish
        if (r > 100 && b > 100) return "P"; // Purple-ish
        if (g > 100 && b > 100) return "B"; // Teal-ish (closest to blue)
        if (r > g && r > b) return "R";     // Red-ish
        if (g > r && g > b) return "G";     // Green-ish
        return "B";                         // Blue-ish
    }

    // Provide feedback on the player's guess (correct, misplaced, or wrong)
    private String getFeedback(String[] guess) {
        StringBuilder feedback = new StringBuilder();
        boolean[] matched = new boolean[guess.length];
        boolean[] used = new boolean[guess.length];
        
        // First check for exact matches (correct position and color)
        for (int i = 0; i < guess.length; i++) {
            if (guess[i].equals(secretCode[i])) {
                feedback.append("+");
                matched[i] = true;
                used[i] = true;
            }
        }
        
        // Then check for misplaced colors (correct color but wrong position)
        for (int i = 0; i < guess.length; i++) {
            if (!matched[i]) {
                for (int j = 0; j < secretCode.length; j++) {
                    if (!used[j] && guess[i].equals(secretCode[j])) {
                        feedback.append("-");
                        used[j] = true;
                        break;
                    }
                }
            }
        }
        return feedback.toString();
    }

    // Log each round of the game (guess, feedback, and remaining attempts)
    private void logRound(String[] guess, String feedback, int round, int attemptsLeft) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write("Round: " + round + " | Code: " + String.join("", secretCode) + 
                         " | Guess: " + String.join("", guess) + " | Feedback: " + feedback + 
                         " | Attempts Left: " + attemptsLeft + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main entry point to start the game
    public void start(SwiftBotAPI bot) {
    	API = bot;
        new MasterMind().startGame(bot);
    }
}
