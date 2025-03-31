

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import swiftbot.Button;
import swiftbot.ImageSize;
import swiftbot.SwiftBotAPI;

public class SwiftBotLightSeek {
    private static SwiftBotAPI API; // Normal API reference
    private static boolean journeyActive = false;
    private static ArrayList<JourneyEvent> journeyLog;
    private static final int OBSTACLE_DISTANCE_CHECKER = 50; // this checks how much distance it must check up to between object and robot
    private static final int SWIFT_SPEED = 50; // 50 is a slow speed I have created for the robot to travel at
    private static final int TURNING_TIME = 500; // Time in ms for how long it takes for robot to turn
    private static final int FORWARD_TIME = 800; // it travels forward at 800 ms 
    private static final int[] RUN_COLOUR = {0, 255, 0}; // When robot is moving correctly it will have a green led underlight
    private static final int[] OBSTACLE_COLOUR = {255, 0, 0}; // If obstacle is detected it will show red light
    private static final int[] INACTIVE_COLOUR = {0, 0, 255}; // If the robot hasn't been activated or isn't stationary then it will have blue underlights
    private static final int OBSTACLE_TIMER = 10000; // this is the 10 second timer for if a obstacle is in the way
    
    public void start() {
        
        API = new SwiftBotAPI(); 
        journeyLog = new ArrayList<>();
        
        setupButtons(); // this is to set up the buttons actions
        //lights indicating the state of robot
        API.fillUnderlights(INACTIVE_COLOUR);
        
        System.out.println("Swift Bot Light Seeker has been started"); 
        System.out.println("Press Button A for the swiftbot to search for light");
        System.out.println("Press Button X at any time if you wish to stop the journey");
        
        // Keep program running and it will make sure to look out for if the buttons are pressed
        while (true) {
            try {
                Thread.sleep(100); //doesnt execute anything for 100 milliseconds (reduces cpu usage)
            } catch (InterruptedException e) {
                e.printStackTrace(); //this is used to identify any interruptions during the sleep 
            }
        }
    }
    
    private static void setupButtons() {
        // Button A - Starts the journey
        API.enableButton(Button.A, () -> {
            System.out.println("You have pressed button A");
            if (!journeyActive) { //checks if the journey is already active or not
                startJourney(); //starts the journey if it hasnt been started yet
            }
        });
        
        // Button X - Stop the journey and offer information display
        API.enableButton(Button.X, () -> {
            System.out.println("You have pressed button X");
            if (journeyActive) {    //checks if the journey is already active or not
                stopJourney();      //stops the journey if it hasnt been started yet
                offerInformationDisplay(); //asks if user wants information about journey
            } else {
                // If journey has already been stopped and user presses X it wont display the information 
                System.out.println("We won't display your journey information in that case.");
                API.disableButton(Button.X); // button x is disabled after pressing it as no journey is active
                API.disableButton(Button.Y); //button y is also disabled as it will not be needed anymore
            }
        });
        
     // Button B - shows that its not a valid input
        API.enableButton(Button.B, () -> {
            System.out.println("Invalid Input, please press a valid button");
        });

        
       
    }
    
    private static void startJourney() {
        journeyActive = true; // this makes the status of journey acitve 
        journeyLog = new ArrayList<>(); // Reset journey log so that it can be fresh, with nothing else inside it
        
        // this logs the beginning of the journey
        logEvent("Journey Started");
        System.out.println("Your Swift Bot light seeking adventure has begun, remmember if you wish to stop just press X.");
        
        // lights are set to green as this is the active colour
        API.fillUnderlights(RUN_COLOUR);
        
        // Start of the journey thread
        Thread journeyThread = new Thread(() -> {
            try {
                executeJourney(); //this makes the 'executeJourney' handle all of the journey logic
            } catch (Exception e) { //this catches any exceptions that might arise
                e.printStackTrace(); //this shows the exceptions in order to potentially help debug
                logEvent("Journey Error: " + e.getMessage()); //this logs the error message, into the journey log
            }
        });
        journeyThread.start(); //this will begin the journey thread meaning it will begin the journey logic running in the background
    }
    
    private static void executeJourney() throws InterruptedException, IOException {
        while (journeyActive) { //whilst jounrey is active it must remain working
            // in order to ensure the obstacle distance is correct is measures multiple times
            double obstacleDistance = getAverageObstacleDistance(3);
            logEvent("Average Distance : " + obstacleDistance + " cm"); // logs average distance of obstacle

            // this identifies if obstacle is detected in a specific range
            if (obstacleDistance > 0 && obstacleDistance < OBSTACLE_DISTANCE_CHECKER) {
                // once an obstacle is seen the bot instantly stops moving
                API.stopMove();
                logEvent("Obstacle seen at " + obstacleDistance + " cm");
                System.out.println("OBSTACLE SEEN at " + obstacleDistance + " cm");

                // red lights switch on to show obstacle
                API.fillUnderlights(OBSTACLE_COLOUR);
                
                //takes a picture of obstacle and saves it
                captureAndSaveObstacleImage();
                
                //10 second timer starts in order to see if it has been removed or not
                long startTime = System.currentTimeMillis(); //secures the current time in order to start the timer
                long remainingTime = OBSTACLE_TIMER; //makes sure it starts for 10 seconds 
                boolean obstacleRemoved = false; //this is here to see if the obsytacle is removed or not

                while (remainingTime > 0 && !obstacleRemoved && journeyActive) { // while time remains and if obstacle has not moved, the journey is deemed as active
                    // see if journey was stopped during the timer
                    if (!journeyActive) break; //if it was stopped then exit
                    
                    // Take various readings to confirm obstacles current status
                    double currentDistance = getAverageObstacleDistance(3);
                    
                    // Check to see if the obstacle has been removed
                    if (currentDistance > OBSTACLE_DISTANCE_CHECKER || currentDistance <= 0) {
                        // if Obstacle is gone, it can resume light-seeking 
                        logEvent("Obstacle removed. Continuing journey.");
                        System.out.println("OBSTACLE REMOVED. Continuing journey.");
                        obstacleRemoved = true;
                        // lights go back to green
                        API.fillUnderlights(RUN_COLOUR);
                        break;
                    }

                    // countdown timer is shown
                    remainingTime = OBSTACLE_TIMER - (System.currentTimeMillis() - startTime); //shows remaining time by subtracting
                    int secondsLeft = (int) (remainingTime / 1000) + 1; //converts milliseconds to seconds and add 1 to ensure its rounded up
                    System.out.println("OBSTACLE STILL SEEN. " + secondsLeft + " seconds remaining...");
                    
                    // lights flash to show obstacle
                    if (secondsLeft % 2 == 0) { //this is to show that its a even number of seconds remaining 
                        API.fillUnderlights(new int[]{255, 165, 0}); // Orange flash
                        Thread.sleep(500); //keeps orange light for 500 milliseconds
                        API.fillUnderlights(OBSTACLE_COLOUR); // Back to red
                        Thread.sleep(500); //keeps red light for 500 milliseconds
                    } else {
                        Thread.sleep(1000); //dont do anything for 1 second 
                    }
                }

                // After 10 seconds, check if object is still there
                if (!obstacleRemoved && journeyActive) { //shows that obstacle isnt removed and journney is still running
                    System.out.println("OBSTACLE STILL SEEN AFTER 10 SECONDS HAVE PASSED. Changing direction.");
                    logEvent("Obstacle still seen after timer. Changed direction.");
                    
                    // A little back up in order to turn
                    API.move(-SWIFT_SPEED, -SWIFT_SPEED, 800); //backs up
                    Thread.sleep(300); //pause
                    
                    // Take a new image to get another option where to go
                    BufferedImage image = captureImage();
                    if (image != null) { //image captured
                        int[][] pixelMatrix = convertImageToMatrix(image); //converted to pixels
                        
                        // Always choose either LEFT or RIGHT when there's an obstacle
                        // rather than using the second brightest which might be CENTER/FORWARD
                        Random rand = new Random();
                        String alternativeDirection = rand.nextBoolean() ? "LEFT" : "RIGHT";
                        
                        logEvent("Avoiding obstacle by turning: " + alternativeDirection);
                        System.out.println("Taking an alternative route: " + alternativeDirection);
                        
                        // lights turn back to green before moving
                        API.fillUnderlights(RUN_COLOUR);
                        
                        // Move in the different direction
                        moveBasedOnLight(alternativeDirection);
                    } else {
                        // If camera doesn't work, just pick a random direction
                        String randomDirection = new Random().nextBoolean() ? "LEFT" : "RIGHT";
                        logEvent("Camera error. moving in random direction: " + randomDirection);
                        System.out.println("Camera error. moving in random direction: " + randomDirection);
                        moveBasedOnLight(randomDirection);
                    }
                }
            } else if (journeyActive) {
                // no obstacles then continue moving depending on light intensity
                BufferedImage image = captureImage();
                if (image != null) {
                    int[][] pixelMatrix = convertImageToMatrix(image);
                    String direction = determineBrightestDirection(pixelMatrix);
                    logEvent("Brightest direction: " + direction);
                    moveBasedOnLight(direction);
                }
            }

            // Wait before next analysis
            Thread.sleep(300);
        }
    }
    
    private static void captureAndSaveObstacleImage() {
        try {
            BufferedImage image = API.takeStill(ImageSize.SQUARE_720x720); //still image, 720x720
            if (image != null) {
                // Create a directory for obstacle images if it doesn't exist
                File directory = new File("obstacle_images");
                if (!directory.exists()) {
                    directory.mkdir();
                }
                
                // Generate a unique filename depending on timestamp
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                File outputFile = new File("obstacle_images/obstacle_" + timestamp + ".jpg");
                
                // Saves the image
                ImageIO.write(image, "jpg", outputFile);
                logEvent("Obstacle image saved as: " + outputFile.getName());
                System.out.println("Obstacle image saved as: " + outputFile.getName());
            }
        } catch (Exception e) { //error or exception
            logEvent("Error saving obstacle image: " + e.getMessage());
            System.out.println("Error saving obstacle image: " + e.getMessage());
        }
    }
    
    // Get average of multiple distance readings for reliability
    private static double getAverageObstacleDistance(int numReadings) {
        double sum = 0; //this is the variable to store the addition of all valid analysis
        int validReadings = 0;  //this counts all valid analysis and readings
        
        for (int i = 0; i < numReadings; i++) { //looped in order to get many different results
            try {
                double reading = API.useUltrasound(); // distance analysis from ultrasound
                if (reading > 0) {  //if more than 0 add to amount
                    sum += reading;
                    validReadings++;
                }
                Thread.sleep(50); // small pause between readings
            } catch (Exception e) {
                logEvent("Ultrasound Error: " + e.getMessage());
            }
        }
        
        return (validReadings > 0) ? (sum / validReadings) : 0; //if there is at least 1 legit reading give the average otherwise give back 0
    }
    
    private static BufferedImage captureImage() {
        try {
            return API.takeStill(ImageSize.SQUARE_720x720);
        } catch (Exception e) {
            logEvent("Camera Error: " + e.getMessage());
            return null;
        }
    }
    
    private static int[][] convertImageToMatrix(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int[][] matrix = new int[width][height]; //creates 2d array in order to store the grayscale intensity of pixels
        for (int x = 0; x < width; x++) { //loop in each pixel inside the image
            for (int y = 0; y < height; y++) {
                int pixel = image.getRGB(x, y); //gets rgb values with coordinates
                int r = (pixel >> 16) & 0xFF; //shifts to get red
                int g = (pixel >> 8) & 0xFF; //shifts to get green
                int b = pixel & 0xFF; //takes blue out
                matrix[x][y] = (r + g + b) / 3; // converts to grayscale intensity
            }
        }
        return matrix; //gives back the grey intensities
    }
    
    private static String determineBrightestDirection(int[][] matrix) { //sees which has biggest brightness
        int width = matrix.length;
        int third = width / 3;
        int leftSum = 0, centerSum = 0, rightSum = 0;

        for (int x = 0; x < width; x++) { //loops through pixels
            for (int y = 0; y < matrix[0].length; y++) {
                if (x < third) leftSum += matrix[x][y];
                else if (x < 2 * third) centerSum += matrix[x][y];
                else rightSum += matrix[x][y];
            }
        }

        System.out.println("Brightness Levels -> Left: " + leftSum + ", Center: " + centerSum + ", Right: " + rightSum);

        int maxBrightness = Math.max(leftSum, Math.max(centerSum, rightSum));

        // moves forward if at least 90%
        if (centerSum >= 0.9 * maxBrightness) return "FORWARD";
        if (leftSum == rightSum && leftSum >= 0.9 * maxBrightness) return new Random().nextBoolean() ? "LEFT" : "RIGHT";
        if (leftSum == maxBrightness) return "LEFT";
        if (rightSum == maxBrightness) return "RIGHT";

        return "FORWARD"; // Default case as long as they all are equal
    }


    // second brightest direction (if the 2 brightest match)
    private static String getSecondBrightestDirection(int[][] matrix) {
        int width = matrix.length;
        int third = width / 3;
        int leftSum = 0, centerSum = 0, rightSum = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                if (x < third) leftSum += matrix[x][y];
                else if (x < 2 * third) centerSum += matrix[x][y];
                else rightSum += matrix[x][y];
            }
        }

        // If the highest columns are equal, choose one randomly
        if (leftSum == rightSum && leftSum > centerSum) {
            return new Random().nextBoolean() ? "LEFT" : "RIGHT";
        }
        if (centerSum > leftSum && centerSum > rightSum) return "CENTER";
        return leftSum > rightSum ? "LEFT" : "RIGHT";
    }
    
    private static void moveBasedOnLight(String direction) {
        System.out.println("Executing move: " + direction);
        System.out.println("Speed: " + SWIFT_SPEED + " units"); // Display speed

        try {
            // First move 30 degrees based on the direction with highest light intensity
            switch (direction) {
                case "LEFT":
                    System.out.println("Turning 30 degrees left");
                    logEvent("Turning 30 degrees left");
                    // To turn left, left wheel goes backward, right wheel goes forward
                    API.move(-SWIFT_SPEED, SWIFT_SPEED, TURNING_TIME);
                    //  pause after turning
                    Thread.sleep(200);
                    // After turning, move forward
                    System.out.println("Moving forward");
                    logEvent("Moving forward");
                    API.move(SWIFT_SPEED, SWIFT_SPEED, FORWARD_TIME);
                    break;
                case "RIGHT":
                    System.out.println("Turning 30 degrees right");
                    logEvent("Turning 30 degrees right");
                    // To turn right, left wheel goes forward, right wheel goes backward
                    API.move(SWIFT_SPEED, -SWIFT_SPEED, TURNING_TIME);
                    // Short pause after turning
                    Thread.sleep(200);
                    // After turning, move forward
                    System.out.println("Moving forward");
                    logEvent("Moving forward");
                    API.move(SWIFT_SPEED, SWIFT_SPEED, FORWARD_TIME);
                    break;
                case "CENTER":
                case "FORWARD":
                    //  move forward without turning
                    System.out.println("Moving forward");
                    logEvent("Moving forward");
                    API.move(SWIFT_SPEED, SWIFT_SPEED, FORWARD_TIME);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logEvent("Movement error: " + e.getMessage());
        }
    }
    
    private static void stopJourney() {
        journeyActive = false;
        API.stopMove();
        API.fillUnderlights(INACTIVE_COLOUR);

        
        logEvent("Journey Stopped");
        System.out.println("Journey stopped!");

        
        saveJourneyLogToFile();
    }

    private static void saveJourneyLogToFile() {
        if (journeyLog.isEmpty()) {
            System.out.println("No journey data to save.");
            return;
        }
        
        // Create logs directory (if there isn't one)
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        
        //  filename with timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        File logFile = new File(logDir, "journey_log_" + timestamp + ".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            for (JourneyEvent event : journeyLog) {
                writer.write(event.getTimestamp() + " - " + event.getMessage());
                writer.newLine();
            }
            System.out.println("Journey log saved to " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error saving journey log: " + e.getMessage());
        }
    }

    
    private static void offerInformationDisplay() {
        System.out.println("Would you like to see information and metrics about the journey?");
        System.out.println("Press Y to view journey information");
        System.out.println("Press X to skip viewing journey information");
        
        // Y button is disabled before enabled
        API.disableButton(Button.Y);
        
        // Y button set up to show information
        API.enableButton(Button.Y, () -> {
            System.out.println("Button Y pressed");
            displayJourneyInformation();
            API.disableButton(Button.Y);
            API.disableButton(Button.X);
        });
    }
    
    private static void displayJourneyInformation() {
        if (journeyLog.isEmpty()) {
            System.out.println("No journey data recorded.");
            return;
        }
        
        System.out.println("Journey log details:");
        for (JourneyEvent event : journeyLog) {
            System.out.println(event.getTimestamp() + " - " + event.getMessage());
        }
    }
    
    private static void logEvent(String message) {
        journeyLog.add(new JourneyEvent(System.currentTimeMillis(), message));
    }
    
    static class JourneyEvent {
        private long timestamp;
        private String message;
        
        public JourneyEvent(long timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }
        
        public String getTimestamp() {
            return new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(timestamp));
        }
        
        public String getMessage() {
            return message;
        }
    }
}