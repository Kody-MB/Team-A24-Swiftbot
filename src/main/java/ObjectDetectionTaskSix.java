import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import swiftbot.SwiftBotAPI;
import swiftbot.ImageSize;
import swiftbot.Button;

public class ObjectDetectionTaskSix {
    // Add class variables to track metrics
    private static String mode = "";
    private static LocalDateTime startTime;
    private static LocalDateTime endTime;
    private static int objectEncounters = 0;
    private static String imagesPath = "";
    private static String logFilePath = "";
    private static boolean isRunning = true; // Flag to control behavior loops
    
    public void start() {
        // Record start time
        startTime = LocalDateTime.now();
        
        // Create a new instance of the SwiftBot API to control the robot
        SwiftBotAPI swiftbot = new SwiftBotAPI();
        
        // Set up the images directory
        try {
            File directory = new File("SwiftBot_Images");
            if (!directory.exists()) {
                directory.mkdir();
            }
            imagesPath = directory.getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Error creating images directory: " + e.getMessage());
            imagesPath = System.getProperty("user.dir");
        }
        
        // Display instructions for the user
        System.out.println("-------------Welcome to Object Detection. Please scan a QR code to select a mode-------------");
        System.out.println("Options: 'Curious SwiftBot', 'Scaredy SwiftBot', or 'Dubious SwiftBot' ");
        
        // Variables to store the selected mode and whether it's valid
        boolean validMode = false;
        
        // Counter for the number of scan attempts
        int scanAttempts = 0;
        // Maximum number of scan attempts before giving up
        final int MAX_SCAN_ATTEMPTS = 25;
        
        // Keep scanning until the user provides a valid mode or reach max attempts
        while (!validMode && scanAttempts < MAX_SCAN_ATTEMPTS) {
            try {
                // Increment scan attempt counter
                scanAttempts++;
                System.out.println("Scan attempt " + scanAttempts + " of " + MAX_SCAN_ATTEMPTS);
                
                // Take a picture to scan for QR codes
                BufferedImage img = swiftbot.getQRImage();
                
                // Try to read the QR code from the image
                String decodedText = swiftbot.decodeQRImage(img);
                
                // Check if we successfully read a QR code
                if (!decodedText.isEmpty()) {

                    // Check if the QR code contains one of our three allowed modes
                    if (decodedText.equals("Curious SwiftBot") || 
                        decodedText.equals("Scaredy SwiftBot") || 
                        decodedText.equals("Dubious SwiftBot")) {
                        
                        // If it's a valid mode, save it and exit the scanning loop
                        mode = decodedText;
                        validMode = true;
                        System.out.println("-------------Mode selected: " + mode + "-------------");
                    } else {
                        // If the QR code doesn't match any valid mode, ask the user to try again
                        System.out.println("-------------Invalid mode detected. Please use one of the specified modes.-------------");
                        System.out.println("-------------Options: 'Curious SwiftBot', 'Scaredy SwiftBot', or 'Dubious SwiftBot'-------------");
                    }
                } else {
                    // If no QR code was found at all, let the user know
                    System.out.println("-------------No QR code detected. Please try again.-------------");
                }
                
                // Wait for 0.5 second before scanning again to avoid overwhelming the system
                Thread.sleep(500);
                
            } catch (Exception e) {
                // If anything goes wrong, show the error message
                System.out.println("-------------Error occurred: " + e.getMessage() + "-------------");
                e.printStackTrace();
            }
        }
        
        // Check if we got a valid mode or exceeded max attempts
        if (validMode) {
            // Once we have a valid mode, tell the user which mode is active
            
            // Set up X button to stop the behavior (not the program)
            swiftbot.enableButton(Button.X, () -> {
                System.out.println("X button pressed. Stopping behavior.");
                isRunning = false;
                endTime = LocalDateTime.now();
                System.out.println("Exiting " + mode.toLowerCase() + " behaviour");
                
                // Disable the X button temporarily to prevent multiple triggers
                swiftbot.disableButton(Button.X);
                
                // Stop any movement
                try {
                    swiftbot.stopMove();
                    swiftbot.disableUnderlights();
                } catch (Exception e) {
                    System.out.println("Error stopping movement: " + e.getMessage());
                }
                
                // The behavior will exit its loop due to isRunning being false
            });
            
            // Run different code depending on which mode was selected
            if (mode.equals("Curious SwiftBot")) {
                // Code for when the robot is in Curious mode
                System.out.println("-------------Curious mode activated: Exploring surroundings.-------------");
                // Run the curious behavior
                try {
                    curiousBehavior(swiftbot);
                } catch (Exception e) {
                    System.out.println("-------------Error in curious behavior: " + e.getMessage() + "-------------");
                    e.printStackTrace();
                }
            } else if (mode.equals("Scaredy SwiftBot")) {
                // Code for when the robot is in Scaredy mode
                System.out.println("-------------Scaredy mode activated: Being cautious.-------------");
                // Run the scaredy behavior
                try {
                    scaredyBehavior(swiftbot);
                } catch (Exception e) {
                    System.out.println("-------------Error in scaredy behavior: " + e.getMessage() + "-------------");
                    e.printStackTrace();
                }
            } else if (mode.equals("Dubious SwiftBot")) {
                // Code for when the robot is in Dubious mode
                System.out.println("-------------Dubious mode activated: Questioning everything.-------------");
                
                // Randomly choose between curious and scaredy behavior
                boolean chooseScaredyMode = Math.random() < 0.5;
                
                if (chooseScaredyMode) {
                    System.out.println("-------------Dubious SwiftBot has decided to be Scaredy today.-------------");
                    try {
                        scaredyBehavior(swiftbot);
                    } catch (Exception e) {
                        System.out.println("-------------Error in scaredy behavior: " + e.getMessage() + "-------------");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("-------------Dubious SwiftBot has decided to be Curious today.-------------");
                    try {
                        curiousBehavior(swiftbot);
                    } catch (Exception e) {
                        System.out.println("-------------Error in curious behavior: " + e.getMessage() + "-------------");
                        e.printStackTrace();
                    }
                }
            }
            
            // After behavior has ended (either naturally or due to X button)
            // Create the log file but don't show its path yet
            createLogFile();
            
            // Ask user if they want to view the log
            System.out.println("Press Y on the SwiftBot to view execution log.");
            System.out.println("Press X on the SwiftBot if you do not wish to view the execution log.");
            
            // Configure buttons for log viewing options
            configureLogViewButtons(swiftbot);
            
        } else {
            // If max attempts reached without finding a valid mode
            System.out.println("-------------Maximum scan attempts (" + MAX_SCAN_ATTEMPTS + ") reached without detecting a valid mode.-------------");
            System.out.println("-------------Please restart the program to try again.-------------");
            
            // Exit the program
            System.exit(0);
        }
    }
    
    // Method to configure buttons for log viewing
    private static void configureLogViewButtons(SwiftBotAPI swiftbot) {
        // Set up Y button to view log
        swiftbot.enableButton(Button.Y, () -> {
            System.out.println("-------------Y button pressed. Displaying execution log-------------");
            displayExecutionLog();
            
            // After viewing log, configure exit options
            System.out.println("Press X on the SwiftBot to exit program.");
            
            // Enable X button to exit completely after viewing log
            swiftbot.enableButton(Button.X, () -> {
                System.out.println("-------------X button pressed. Exiting program.-------------");
                System.out.println("Images saved at: " + imagesPath);
                System.exit(0);
            });
        });
        
        // Update X button functionality to show only images path without log file path
        swiftbot.enableButton(Button.X, () -> {
            System.out.println("X button pressed. Exiting program without viewing log.");
            System.out.println("Images saved at: " + imagesPath);
            System.exit(0);
        });
        
        // Keep the program running until a button is pressed
        while(true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Method to create the log file
    private static void createLogFile() {
        try {
            // Create log directory if it doesn't exist
            File logDir = new File("SwiftBot_Logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            // Create a timestamp for the filename
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            
            // Create the log file
            File logFile = new File(logDir, "swiftbot_log_" + timestamp + ".txt");
            logFilePath = logFile.getAbsolutePath();
            
            // Write to the log file
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                writer.println("SwiftBot Execution Log");
                writer.println("=====================");
                writer.println("Mode: " + mode);
                
                // Calculate duration
                Duration duration = Duration.between(startTime, endTime);
                long seconds = duration.getSeconds();
                writer.println("Duration: " + seconds + " seconds");
                
                writer.println("Object encounters: " + objectEncounters);
                writer.println("Images saved at: " + imagesPath);
                writer.println("Log file saved at: " + logFilePath);
            }
            
            // Don't output the log file path here, it will be shown only if user selects Y later
            
        } catch (IOException e) {
            System.out.println("-------------Error creating log file: " + e.getMessage() + "-------------");
            e.printStackTrace();
        }
    }
    
    // Method to display the execution log
    private static void displayExecutionLog() {
        try {
            // Calculate duration
            Duration duration = Duration.between(startTime, endTime);
            long seconds = duration.getSeconds();
            
            System.out.println("=====================");
            System.out.println("SwiftBot Execution Log");
            System.out.println("=====================");
            System.out.println("Mode: " + mode);
            System.out.println("Duration: " + seconds + " seconds");
            System.out.println("Object encounters: " + objectEncounters);
            System.out.println("Images saved at: " + imagesPath);
            System.out.println("Log file saved at: " + logFilePath);
            System.out.println("=====================");
            
        } catch (Exception e) {
            System.out.println("-------------Error displaying log: " + e.getMessage() + "-------------");
            e.printStackTrace();
        }
    }
    
    // Implementation of the curious behavior
    private static void curiousBehavior(SwiftBotAPI swiftbot) throws IOException, InterruptedException {
        // Green color for underlights
        int[] greenColor = {0, 255, 0};
        
        // Required gap (buffer zone) in cm
        final double REQUIRED_GAP = 30.0;
        
        // Tolerance for considering the object at the required gap (in cm)
        final double DISTANCE_TOLERANCE = 3.0;
        
        // Duration to wait after a movement (in milliseconds)
        final long WAIT_AFTER_MOVEMENT = 5000;
        
        // Flag to track if an object has been detected
        boolean objectDetected = false;
        
        // Variables to store the last distance and time
        double lastDistance = 0.0;
        long lastMovementTime = System.currentTimeMillis();
        
        // Turn directions for exploration
        int[] turnDirections = {20, -20, 30, -30, 40, -40};
        int currentTurnIndex = 0;
        
        System.out.println("-------------Beginning curious exploration...-------------");
        
        // Reset isRunning flag to ensure behavior starts in running state
        isRunning = true;
        
        while (isRunning) {
            try {
                // Use ultrasound to measure distance to the nearest object
                double currentDistance = swiftbot.useUltrasound();
                System.out.println("Current distance: " + currentDistance + " cm");
                
                // Check if an object is within detectable range (excluding failed readings around 1219)
                if (currentDistance < 1000) {
                    // Object detected
                    if (!objectDetected) {
                        // Increment object encounters counter if this is a new detection
                        objectEncounters++;
                        objectDetected = true;
                    }
                    System.out.println("-------------Object detected at " + currentDistance + " cm-------------");
                    
                    // Turn on green underlights
                    swiftbot.fillUnderlights(greenColor);
                    
                    // Check if the object is at the required gap (within tolerance)
                    if (Math.abs(currentDistance - REQUIRED_GAP) <= DISTANCE_TOLERANCE) {
                        // Object is at the required gap - blink underlights and remain stationary
                        System.out.println("-------------Object at required gap. Remaining stationary.-------------");
                        
                        // Blink the underlights
                        for (int i = 0; i < 5 && isRunning; i++) {
                            swiftbot.disableUnderlights();
                            Thread.sleep(250);
                            if (!isRunning) break; // Check if X was pressed during sleep
                            swiftbot.fillUnderlights(greenColor);
                            Thread.sleep(250);
                            if (!isRunning) break; // Check if X was pressed during sleep
                        }
                        
                        // Stop moving
                        swiftbot.stopMove();
                        
                    } else if (currentDistance < REQUIRED_GAP) {
                        // Object is too close - move backward
                        System.out.println("-------------Object too close. Moving backward.-------------");
                        swiftbot.move(-50, -50, 500);  // Move backward for 0.5 seconds
                        
                    } else {
                        // Object is too far - move forward
                        System.out.println("-------------Object too far. Moving forward.-------------");
                        swiftbot.move(32, 40, 500);  // Move forward for 0.5 seconds
                    }
                    
                    // Check if behavior was stopped during movement
                    if (!isRunning) break;
                    
                    // Check if we've reached the required gap (stop condition)
                    if (Math.abs(currentDistance - REQUIRED_GAP) <= DISTANCE_TOLERANCE) {
                        System.out.println("-------------Required gap achieved. Taking image.-------------");
                        
                        // Take an image of the object using takeStill method
                        BufferedImage objectImage = swiftbot.takeStill(ImageSize.SQUARE_720x720);
                        
                        // Save the image with a timestamp
                        saveImage(objectImage);
                        
                        // Turn off underlights
                        swiftbot.disableUnderlights();
                        
                        // Update last distance and time
                        lastDistance = currentDistance;
                        lastMovementTime = System.currentTimeMillis();
                        
                        // Wait for 5 seconds to see if the object moves, checking for X button press
                        System.out.println("-------------Waiting to see if object moves...-------------");
                        long waitStartTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - waitStartTime < WAIT_AFTER_MOVEMENT && isRunning) {
                            Thread.sleep(100);
                        }
                    }
                } else {
                    // No object detected or object is too far
                    if (objectDetected) {
                        System.out.println("-------------Object no longer detected.-------------");
                        objectDetected = false;
                    }
                    
                    // Check if we've been waiting for 5 seconds with no movement
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastMovementTime > WAIT_AFTER_MOVEMENT) {
                        System.out.println("-------------No movement detected for 5 seconds. Changing direction.-------------");
                        
                        // Wait for a second, checking for X button press
                        long waitStartTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - waitStartTime < 1000 && isRunning) {
                            Thread.sleep(100);
                        }
                        
                        // Check if behavior was stopped during wait
                        if (!isRunning) break;
                        
                        // Start moving in a different direction
                        int turnValue = turnDirections[currentTurnIndex];
                        currentTurnIndex = (currentTurnIndex + 1) % turnDirections.length;
                        
                        System.out.println("-------------Moving in a new direction...-------------");
                        if (turnValue > 0) {
                            swiftbot.move(32, 40 - turnValue, 1000);
                        } else {
                            swiftbot.move(32 + Math.abs(turnValue), 40, 1000);
                        }
                        
                        // Update last movement time
                        lastMovementTime = System.currentTimeMillis();
                    }
                }
                
                // Small delay to prevent overwhelming the sensors
                Thread.sleep(100);
                
            } catch (Exception e) {
                System.out.println("-------------Error during curious behavior: " + e.getMessage() + "-------------");
                e.printStackTrace();
                // Short delay to recover from errors
                Thread.sleep(1000);
            }
        }
        
        // Make sure all movements are stopped when exiting
        swiftbot.stopMove();
        swiftbot.disableUnderlights();
    }
    
    // Implementation of the scaredy behaviour
    private static void scaredyBehavior(SwiftBotAPI swiftbot) throws IOException, InterruptedException {
        // Colours for underlights
        int[] blueColor = {0, 0, 255};    // Blue for wandering
        int[] redColor = {255, 0, 0};     // Red for fleeing
        
        // Distance threshold for detecting objects (in cm)
        final double DETECTION_THRESHOLD = 50.0;
        
        // Duration to wait after fleeing (in milliseconds)
        final long WAIT_AFTER_FLEEING = 5000;
        
        // Flag to track if an object has been detected
        boolean objectDetected = false;
        
        // Variable to store the last detection time
        long lastDetectionTime = System.currentTimeMillis();
        
        // Turn directions for exploration (small variations)
        int[] turnDirections = {10, -10, 15, -15, 20, -20};
        int currentTurnIndex = 0;
        
        System.out.println("-------------Beginning scaredy exploration...-------------");
        
        // Reset isRunning flag to ensure behavior starts in running state
        isRunning = true;
        
        // Set blue underlights for wandering mode
        swiftbot.fillUnderlights(blueColor);
        
        // Start moving forward slowly
        swiftbot.startMove(30, 30);
        
        while (isRunning) {
            try {
                // Use ultrasound to measure distance to the nearest object
                double currentDistance = swiftbot.useUltrasound();
                System.out.println("Current distance: " + currentDistance + " cm");
                
                // Check if an object is within the detection threshold (excluding failed readings around 1219)
                if (currentDistance < DETECTION_THRESHOLD && currentDistance < 1000) {
                    // Object detected - time to get scared!
                    System.out.println("-------------Object detected at " + currentDistance + " cm! Getting scared!-------------");
                    
                    // Increment object encounters counter if this is a new detection
                    if (!objectDetected) {
                        objectEncounters++;
                        objectDetected = true;
                    }
                    
                    // Stop current movement
                    swiftbot.stopMove();
                    
                    // Check if behavior was stopped
                    if (!isRunning) break;
                    
                    // Take an image of the scary object
                    System.out.println("-------------Taking image of scary object...-------------");
                    BufferedImage objectImage = swiftbot.takeStill(ImageSize.SQUARE_720x720);
                    
                    // Save the image with a timestamp
                    saveImage(objectImage);
                    
                    // Blink the underlights in panic
                    System.out.println("-------------Blinking underlights in panic!-------------");
                    for (int i = 0; i < 5 && isRunning; i++) {
                        swiftbot.disableUnderlights();
                        Thread.sleep(100);
                        if (!isRunning) break; // Check if X was pressed
                        swiftbot.fillUnderlights(redColor);
                        Thread.sleep(100);
                        if (!isRunning) break; // Check if X was pressed
                    }
                    
                    // Check if behavior was stopped during blinking
                    if (!isRunning) break;
                    
                    // Keep red underlights on while fleeing
                    swiftbot.fillUnderlights(redColor);
                    
                    // Back up and turn in the opposite direction
                    System.out.println("-------------Backing up and turning away!-------------");
                    
                    // Back up first
                    swiftbot.move(-70, -70, 1000);
                    
                    // Check if behavior was stopped
                    if (!isRunning) break;
                    
                    // Turn around (180 degrees)
                    swiftbot.move(-60, 60, 1000);
                    
                    // Check if behavior was stopped
                    if (!isRunning) break;
                    
                    // Move away for three seconds
                    System.out.println("-------------Running away for 3 seconds!-------------");
                    
                    // Break this up into smaller chunks to check for X button press
                    for (int i = 0; i < 6 && isRunning; i++) {
                        swiftbot.move(80, 80, 500);
                        if (!isRunning) break;
                    }
                    
                    // Check if behavior was stopped
                    if (!isRunning) break;
                    
                    // Stop after fleeing
                    swiftbot.stopMove();
                    
                    // Switch back to blue underlights for wandering
                    swiftbot.fillUnderlights(blueColor);
                    
                    // Update last detection time
                    lastDetectionTime = System.currentTimeMillis();
                    
                    // Wait a moment before continuing
                    Thread.sleep(500);
                    
                    // Resume wandering
                    swiftbot.startMove(30, 30);
                    
                    // Reset object detection flag
                    objectDetected = false;
                    
                } else {
                    // No object detected within threshold
                    
                    // Check if we've been wandering for 5 seconds without detecting anything
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastDetectionTime > WAIT_AFTER_FLEEING) {
                        System.out.println("-------------No object detected for 5 seconds. Changing direction.-------------");
                        
                        // Stop current movement
                        swiftbot.stopMove();
                        
                        // Wait for a second, checking for X button press
                        long waitStartTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - waitStartTime < 1000 && isRunning) {
                            Thread.sleep(100);
                        }
                        
                        // Check if behavior was stopped during wait
                        if (!isRunning) break;
                        
                        // Start moving in a slightly different direction
                        int turnValue = turnDirections[currentTurnIndex];
                        currentTurnIndex = (currentTurnIndex + 1) % turnDirections.length;
                        
                        System.out.println("-------------Moving in a new direction...-------------");
                        if (turnValue > 0) {
                            // Turn slightly right then go forward
                            swiftbot.move(30, 30 - turnValue, 500);
                        } else {
                            // Turn slightly left then go forward
                            swiftbot.move(30 + Math.abs(turnValue), 30, 500);
                        }
                        
                        // Check if behavior was stopped
                        if (!isRunning) break;
                        
                        // Resume wandering in the new direction
                        swiftbot.startMove(30, 30);
                        
                        // Update last detection time
                        lastDetectionTime = System.currentTimeMillis();
                    }
                }
                
                // Small delay to prevent overwhelming the sensors
                Thread.sleep(100);
                
            } catch (Exception e) {
                System.out.println("-------------Error during scaredy behavior: " + e.getMessage() + "-------------");
                e.printStackTrace();
                // Short delay to recover from errors
                Thread.sleep(1000);
                
                // Make sure we're still moving after an error if the behavior is still running
                if (isRunning) {
                    swiftbot.startMove(30, 30);
                }
            }
        }
        
        // Make sure all movements are stopped when exiting
        swiftbot.stopMove();
        swiftbot.disableUnderlights();
    }
    
    // Helper method to save an image with a timestamp
    private static void saveImage(BufferedImage image) {
        try {
            // Check if image is null
            if (image == null) {
                System.out.println("-------------Error: Captured image is null-------------");
                return;
            }
            
            // Create a timestamp for the filename
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = now.format(formatter);
            
            // Make sure the SwiftBot_Images directory exists
            File directory = new File("SwiftBot_Images");
            if (!directory.exists()) {
                directory.mkdir();
            }
            
            // Create a file with the timestamp in the name
            File outputFile = new File(directory, "object_image_" + timestamp + ".jpg");
            
            // Save the image as a JPEG
            ImageIO.write(image, "jpg", outputFile);
            
            System.out.println("Image saved as: " + outputFile.getAbsolutePath() );
        } catch (IOException e) {
            System.out.println("Error saving image: " + e.getMessage() );
            e.printStackTrace();
        }
    }
}