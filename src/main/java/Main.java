
import swiftbot.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Main {
    static SwiftBotAPI API;
    static long startTime;
    static Map<String, Integer> trafficLightLog = new ConcurrentHashMap<>();
    static int totalLightsDetected = 0;
    static boolean running = true;
    static final boolean DEBUG = true;
    static final int DETECTION_DISTANCE = 30; // Detection distance in cm
    static final int RESPONSE_DISTANCE = 20;  // Response distance in cm
    static final int DEFAULT_SPEED = 40;      // Default movement speed
    static final int CONTINUOUS_MOVE_TIME = 500; // Time for continuous movement (ms)
    static boolean programOn = true;
    public void start(SwiftBotAPI bot) {
        try {
            API = bot; // Initialize SwiftBot API
        } catch (Exception e) {
            System.out.println("\nI2C disabled! Run: sudo raspi-config nonint do_i2c 0");
            System.exit(5);
        }

        System.out.println("Starting SwiftBot Traffic Navigation...");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|..####...######...####...#####...######..######..#####.....##...|");
        System.out.println("|.##........##....##..##..##..##....##....##......##..##....##...|");
        System.out.println("|..####.....##....######..#####.....##....####....##..##....##...|");
        System.out.println("|.....##....##....##..##..##..##....##....##......##..##.........|");
        System.out.println("|..####.....##....##..##..##..##....##....######..#####.....##...|");
        System.out.println("|................................................................|");
        System.out.println("+----------------------------------------------------------------+");
        startTime = System.currentTimeMillis();
        
        while (programOn){
        	 System.out.println("Press 'A' on the SwiftBot to start navigation");
             System.out.println("Press 'X' on the SwiftBot to exit and save log");
             int option = ButtonCheck();
             if(option == 0) {
            	 startNavigation();
             }
             else if (option == 2) {
            	 saveLogAndExit();
            	 programOn = false;
             }
             else {
            	 System.out.println("These are not valid options try again");
             }
        }
        // Assign button actions
      programOn = true;
      API.disableAllButtons();
    }

    public static void startNavigation() {
    	API.enableButton(Button.X, Main::saveLogAndExit);	  // Ends Navigation
        System.out.println("Navigation started. SwiftBot moving at default speed...");
        API.fillUnderlights(new int[]{255, 255, 0}); // Set lights to yellow
        
        while (running) {
            // Default forward movement with a time value > 0
            API.move(DEFAULT_SPEED, DEFAULT_SPEED, CONTINUOUS_MOVE_TIME);
            
            // Check distance to objects
            double distance = API.useUltrasound();
            
            // Take image for colour detection
            BufferedImage image = API.takeStill(ImageSize.SQUARE_720x720);
            if (image == null) continue; // Skip if no image is captured
            
            // Only process colours when within detection range
            if (distance <= DETECTION_DISTANCE) {
                String detectedColor = detectColor(image);
                
                if (detectedColor != null) {
                    // Enum the total lights detected
                		totalLightsDetected++;
                    trafficLightLog.put(detectedColor, trafficLightLog.getOrDefault(detectedColor, 0) + 1);
                    
                    // Only respond to colours when within response range
                    if (distance <= RESPONSE_DISTANCE) {
                        handleTrafficLight(detectedColor);
                    }
                }
            }
            
            // Brief pause to avoid overloading the system
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Navigation interrupted: " + e.getMessage());
            }
        }
    }

    public static String detectColor(BufferedImage image) {
        int red = 0, green = 0, blue = 0;
        int centerX = image.getWidth() / 3;	// Divide image by 1/3 to Focus on Center
        int centerY = image.getHeight() / 3;
        int width = image.getWidth() / 3;	
        int height = image.getHeight() / 3;
        int totalPixels = width * height;

        if (totalPixels == 0) return null; // Avoid divide-by-zero

        // Process only the central region to improve accuracy
        for (int y = centerY; y < centerY + height; y++) {
            for (int x = centerX; x < centerX + width; x++) {
                int rgb = image.getRGB(x, y);
                red += (rgb >> 16) & 0xFF;	// Loop through all pixels and Bitshift
                green += (rgb >> 8) & 0xFF;
                blue += rgb & 0xFF;
            }
        }

        red /= totalPixels;
        green /= totalPixels;
        blue /= totalPixels;

        // Ignore very dark images (camera failure or night conditions)
        if (red + green + blue < 100) return null;

        // Reference Colours for Euclidean Distance
        int[] idealRed = {255, 0, 0};
        int[] idealGreen = {0, 200, 0};
        int[] idealYellow = {220, 220, 0};
        int[] idealBlue = {0, 0, 255};

        // Calculate Euclidean distances (Compares different shades or Red, Green, Blue and Yellow)
        double distRed = colourDistance(red, green, blue, idealRed);
        double distGreen = colourDistance(red, green, blue, idealGreen);
        double distYellow = colourDistance(red, green, blue, idealYellow);
        double distBlue = colourDistance(red, green, blue, idealBlue);

        // Find the closest match
        double minDistance = Math.min(Math.min(distRed, distGreen), Math.min(distBlue, distYellow)); // Mathematical Equation to calc the colour distances

        if (DEBUG) {
            System.out.println("Colour RGB: (" + red + "," + green + "," + blue + ")");
            System.out.println("Distances - Red: " + distRed + ", Green: " + distGreen + 
                               ", Yellow: " + distYellow + ", Blue: " + distBlue);
        }

        if (minDistance == distRed) return "RED";
        if (minDistance == distGreen) return "GREEN";
        if (minDistance == distYellow) return "YELLOW";
        if (minDistance == distBlue) return "BLUE";

        return null;
    }

    // Function to calculate Euclidean distance
    private static double colourDistance(int r, int g, int b, int[] target) {
        return Math.sqrt(Math.pow(r - target[0], 2) +
                Math.pow(g - target[1], 2) +
                Math.pow(b - target[2], 2));
    }

    public static void handleTrafficLight(String colour) {
        try {
            switch (colour) {
                case "RED":
                    System.out.println("Detected RED light. Stopping for 1 second.");
                    API.fillUnderlights(new int[]{255, 0, 0});
                    API.move(0, 0, 1000); // Stop for 1 second
                    break;

                case "GREEN":
                    System.out.println("Detected GREEN light. Passing quickly then stopping.");
                    API.fillUnderlights(new int[]{0, 255, 0});
                    API.move(DEFAULT_SPEED * 2, DEFAULT_SPEED * 2, 2000); // Pass quickly
                    API.move(0, 0, 1000); // Stop for 1 second
                    API.fillUnderlights(new int[]{255, 255, 0}); // Set lights to yellow
                    break;

                case "BLUE":
                    System.out.println("Detected BLUE light. Stopping, blinking, and turning left.");
                    API.move(0, 0, 1000); // Stop for 1 second
                    
                    // Blink blue lights
                    for (int i = 0; i < 3; i++) {
                        API.fillUnderlights(new int[]{0, 0, 255});
                        try { Thread.sleep(200); } catch (InterruptedException e) { }
                        API.fillUnderlights(new int[]{0, 0, 0});
                        try { Thread.sleep(200); } catch (InterruptedException e) { }
                    }
                    
                    // Turn left 90 degrees
                    API.move(-DEFAULT_SPEED, DEFAULT_SPEED, 800); // Turn left (adjust time as needed to achieve 90°)
                    
                    // Lower speed and retrace path briefly
                    API.move(DEFAULT_SPEED/2, DEFAULT_SPEED/2, 1000);
                    break;

                case "YELLOW":
                    System.out.println("Detected YELLOW light. Doing a 360° turn.");
                    API.move(DEFAULT_SPEED, -DEFAULT_SPEED, 3600); // 360° turn (adjust time as needed)
                    API.fillUnderlights(new int[]{255, 0, 0}); // Set lights to red
                    break;

                default:
                    System.out.println("No valid traffic light detected.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error handling traffic light: " + e.getMessage());
        }
    }

    public static void saveLogAndExit() {
        System.out.println("\nButton X Pressed! Stopping SwiftBot...");
        running = false; // Stop movement loop

        API.move(0, 0, 1); // Stop SwiftBot immediately with valid time
        long executionTime = System.currentTimeMillis() - startTime;

        // Find the most encountered traffic light colour
        String mostEncounteredColour = trafficLightLog.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // Print summary
        System.out.println("\n=== SwiftBot Traffic Light Report ===");
        System.out.println("Total Lights Detected: " + totalLightsDetected);
        for (Map.Entry<String, Integer> entry : trafficLightLog.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Most Encountered Traffic Light Colour: " + mostEncounteredColour);
        System.out.println("Execution Time: " + (executionTime / 1000) + " seconds");

        // Save log
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("swiftbot_log.txt", true))) { 
            writer.write("\n=== SwiftBot Traffic Light Report ===\n");
            writer.write("Total Lights Detected: " + totalLightsDetected + "\n");
            for (Map.Entry<String, Integer> entry : trafficLightLog.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            writer.write("Most Encountered Traffic Light Colour: " + mostEncounteredColour + "\n");
            writer.write("Execution Time: " + executionTime / 1000 + " seconds\n");
            System.out.println("Log saved successfully. Exiting...");
        } catch (IOException e) {
            System.out.println("Error writing log file: " + e.getMessage());
        }
        programOn = false;
    }
    
    public static int ButtonCheck() {
 	   int option = 0;
 	    
 	    final int[] buttonPress = { -1 }; 
 	    Button buttons[] = { Button.A, Button.B, Button.X, Button.Y };
 	    CountDownLatch latch = new CountDownLatch(1); // Latch to wait for a button press

 	    // Configure button actions
 	    API.enableButton(Button.A, () -> {
 	        buttonPress[0] = 0;
 	        API.disableButton(Button.A);
 	        latch.countDown(); // Release the latch when Button A is pressed
 	    });
 	    API.enableButton(Button.B, () -> {
 	        buttonPress[0] = 1;
 	        API.disableButton(Button.B);
 	        latch.countDown(); // Release the latch when Button B is pressed
 	    });
 	    API.enableButton(Button.X, () -> {
 	        buttonPress[0] = 2;
 	        API.disableButton(Button.X);
 	        latch.countDown(); // Release the latch when Button X is pressed
 	    });
 	    API.enableButton(Button.Y, () -> {
 	        buttonPress[0] = 3;
 	        API.disableButton(Button.Y);
 	        latch.countDown(); // Release the latch when Button Y is pressed
 	    });

 	    try {
 	        latch.await(); // Wait for a button press
 	    } catch (InterruptedException e) {
 	        Thread.currentThread().interrupt(); // Restore interrupt status
 	        return 0;
 	    }

 	    // Check if the button pressed matches the expected value
 	    if (buttonPress[0] == 2) {
 	        option = 2;
 	    }
 	    else if (buttonPress[0] == 3){
 	    	  option = 3;
 	    } 
 	    else if (buttonPress[0] == 1){
 	    	  option = 1;
 	    }
 	    else {
 	    	option = 0;
 	    }
 	    API.disableAllButtons();

 	    return option;
 	}
}