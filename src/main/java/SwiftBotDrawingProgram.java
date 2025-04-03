import swiftbot.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Shape {
    String type; // Type of shape (Square or Triangle)
    List<Integer> sides; // Side lengths of the shape
    List<Double> angles; // Angles of the triangle (if applicable)
    long timeTaken; // Time taken to draw the shape
	
    public Shape(String type, List<Integer> sides) {
        this.type = type;
        this.sides = sides;
        this.angles = new ArrayList<>();
    }

    public void setAngles(List<Double> angles) {
        this.angles = angles;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    // Calculate the area of the shape
    public double calculateArea() {
        if (type.equals("S")) {
            return sides.get(0) * sides.get(0); // Area of square
        } else if (type.equals("T")) {
            // Using Heron's formula for triangle area
            double s = (sides.get(0) + sides.get(1) + sides.get(2)) / 2.0;
            return Math.sqrt(s * (s - sides.get(0)) * (s - sides.get(1)) * (s - sides.get(2)));
        }
        return 0;
    }

    @Override
    public String toString() {
        if (type.equals("S")) {
            return "Square: " + sides.get(0) + " (time: " + timeTaken + " ms)";
        } else if (type.equals("T")) {
            return "Triangle: " + sides.get(0) + ", " + sides.get(1) + ", " + sides.get(2) +
                   " (angles: " + angles.get(0) + ", " + angles.get(1) + ", " + angles.get(2) +
                   "; time: " + timeTaken + " ms)";
        }
        return "";
    }
}

public class SwiftBotDrawingProgram {
	static boolean scan = true;
    private static List<Shape> drawnShapes = new ArrayList<>(); // Stores all drawn shapes
    private static SwiftBotAPI swiftBot; // SwiftBot API instance
    private static final double SPEED_CM_PER_SECOND = 21.5; // Calibrated speed at 40%
    private static volatile boolean running = true; // Flag to control the main loop

    public void start (SwiftBotAPI bot) throws InterruptedException {
        try {
            swiftBot = bot; // Initialize SwiftBot
        } catch (Exception e) {
            System.out.println("\nI2C disabled! Run the following command:");
            System.out.println("sudo raspi-config nonint do_i2c 0\n");
            System.exit(5);
        }
        System.out.println("\n*****************************************************************");
		System.out.println("*****************************************************************");
		System.out.println("");
        System.out.println("SwiftBot Drawing Program");
        System.out.println("*****************************************************************");
		System.out.println("*****************************************************************\n");
        System.out.println("Scan a QR code or press 'X' on the SwiftBot to terminate.");

        // Add a shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            swiftBot.disableUnderlights();
            swiftBot.disableAllButtons();
            swiftBot.move(0, 0, 0); // Stop the motors
        }));
        
        swiftBot.enableButton(Button.X, () -> {
            System.out.println("X button pressed. Terminating program...");
            running = false; 
            scan = false; // Stop the main loop
            logResults(); // Log the results before exiting
            
        });

        // Enable the 'X' button to terminate the program
        
        // Main loop to process QR codes
        while (running) {
            System.out.println("Scanning for QR code...");

         

            // Scan for QR code
            String qrContent = scanQRCode();
            if (qrContent == null || qrContent.isEmpty()) {
                System.out.println("No QR code detected. Please try again.");
                continue;
            }

            // Parse the QR code content
            List<Shape> shapes = parseQRCode(qrContent);
            if (shapes.isEmpty()) {
                System.out.println("Invalid QR code content. It should follow the format S-()&T-()-()-(). Please try again.");
                continue;
            }

            // Process each shape
            for (Shape shape : shapes) {
                if (validateShape(shape)) {
                    drawShape(shape);
                    drawnShapes.add(shape);
                } else {
                    System.out.println("Invalid shape: " + shape);
                }
            }
        }
        running = true;

        System.out.println("Program terminated.");
        swiftBot.disableAllButtons();
    }

    // Scan for a QR code and return its content
    private static String scanQRCode() {

        while (scan == true) {
            try {
                BufferedImage img = swiftBot.getQRImage();
                String decodedText = swiftBot.decodeQRImage(img);
                if (!decodedText.isEmpty()) {
                    System.out.println("QR Code Detected: " + decodedText);
                    return decodedText;
                } else {
                    System.out.println("No QR code detected. Retrying in 10 seconds...");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000); // Wait for 10 seconds before retrying
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        scan = true;
        return null;
    }

    // Parse the QR code content into Shape objects
    private static List<Shape> parseQRCode(String qrContent) {
        List<Shape> shapes = new ArrayList<>();
        String[] shapeCommands = qrContent.split("&");

        for (String command : shapeCommands) {
            String[] parts = command.split("-");
            if (parts.length < 2) {
                continue; // Invalid command
            }

            String type = parts[0].toUpperCase();
            if (type.equals("S") && parts.length == 2) {
                // Square
                int side = Integer.parseInt(parts[1]);
                shapes.add(new Shape("S", List.of(side)));
            } else if (type.equals("T") && parts.length == 4) {
                // Triangle
                int side1 = Integer.parseInt(parts[1]);
                int side2 = Integer.parseInt(parts[2]);
                int side3 = Integer.parseInt(parts[3]);
                shapes.add(new Shape("T", List.of(side1, side2, side3)));
            }
        }

        return shapes;
    }

    // Validate the shape's side lengths
    private static boolean validateShape(Shape shape) {
        if (shape.type.equals("S")) {
            int side = shape.sides.get(0);
            return side >= 15 && side <= 85;
        } else if (shape.type.equals("T")) {
            int a = shape.sides.get(0);
            int b = shape.sides.get(1);
            int c = shape.sides.get(2);
            return (a + b > c) && (a + c > b) && (b + c > a) &&
                   a >= 15 && a <= 85 &&
                   b >= 15 && b <= 85 &&
                   c >= 15 && c <= 85;
        }
        return false;
    }

    // Draw the shape using SwiftBot
    private static void drawShape(Shape shape) throws InterruptedException {
        System.out.println("Drawing " + shape.type + " with sides: " + shape.sides);

        if (shape.type.equals("T")) {
            // Calculate angles for triangle
            List<Double> angles = calculateTriangleAngles(shape.sides);
            shape.setAngles(angles);
        }

        // Simulate drawing time
        long timeTaken = calculateDrawingTime(shape);
        shape.setTimeTaken(timeTaken);

        // Move SwiftBot to draw the shape
        if (shape.type.equals("S")) {
            drawSquare(shape.sides.get(0));
        } else if (shape.type.equals("T")) {
            drawTriangle(shape.sides);
        }

        System.out.println("Shape drawn: " + shape);
        System.out.println("SwiftBot blinking green lights to indicate completion.");
        swiftBot.fillUnderlights(new int[] { 0, 255, 0 }); // Blink green underlights
        Thread.sleep(1000);
        swiftBot.disableUnderlights();

        // Move 15 cm backwards for the next shape
        int moveTime = (int) ((15 / SPEED_CM_PER_SECOND) * 1000); // Time to move 15 cm
        swiftBot.move(-40, -40, moveTime);
    }

    // Calculate the angles of a triangle using the Law of Cosines
    private static List<Double> calculateTriangleAngles(List<Integer> sides) {
        int a = sides.get(0);
        int b = sides.get(1);
        int c = sides.get(2);

        double angleA = Math.toDegrees(Math.acos((b * b + c * c - a * a) / (2.0 * b * c)));
        double angleB = Math.toDegrees(Math.acos((a * a + c * c - b * b) / (2.0 * a * c)));
        double angleC = 180 - angleA - angleB;

        return List.of(angleA, angleB, angleC);
    }

    // Calculate the time required to draw the shape based on distance and speed
    private static long calculateDrawingTime(Shape shape) {
        int distance = shape.sides.stream().mapToInt(Integer::intValue).sum();
        return (long) ((distance / SPEED_CM_PER_SECOND) * 1000); // Time in milliseconds
    }

    // Draw a square using SwiftBot
    private static void drawSquare(int sideLength) throws InterruptedException {
        int speed = 40; // 40% speed
        int moveTime = (int) ((sideLength / SPEED_CM_PER_SECOND) * 1000); // Time in milliseconds

        for (int i = 0; i < 4; i++) {
            swiftBot.move(speed, speed, moveTime); // Move forward
            swiftBot.move(speed, -speed, 500); // Turn 90 degrees
        }
    }

    // Draw a triangle using SwiftBot
    private static void drawTriangle(List<Integer> sides) throws InterruptedException {
        int speed = 40; // 40% speed

        for (int i = 0; i < 3; i++) {
            int moveTime = (int) ((sides.get(i) / SPEED_CM_PER_SECOND) * 1000); // Time in milliseconds
            swiftBot.move(speed, speed, moveTime); // Move forward
            swiftBot.move(speed, -speed, 667); // Turn 120 degrees
        }
    }

    // Log the results to a text file
    private static void logResults() {
        String filePath = "swiftbot_log.txt"; // File path for the log file

        try (FileWriter writer = new FileWriter(filePath)) {
            // Log all drawn shapes
            writer.write("Drawn Shapes:\n");
            for (Shape shape : drawnShapes) {
                writer.write(shape.toString() + "\n");
            }

            // Log largest shape by area
            Shape largestShape = drawnShapes.stream()
                    .max((s1, s2) -> Double.compare(s1.calculateArea(), s2.calculateArea()))
                    .orElse(null);
            if (largestShape != null) {
                writer.write("\nLargest Shape: " + largestShape + "\n");
            }

            // Log most frequently drawn shape
            long squareCount = drawnShapes.stream().filter(s -> s.type.equals("S")).count();
            long triangleCount = drawnShapes.stream().filter(s -> s.type.equals("T")).count();
            if (squareCount > triangleCount) {
                writer.write("Most Frequent Shape: Square (" + squareCount + " times)\n");
            } else if (triangleCount > squareCount) {
                writer.write("Most Frequent Shape: Triangle (" + triangleCount + " times)\n");
            } else {
                writer.write("Most Frequent Shape: None (equal count)\n");
            }

            // Log average time
            double averageTime = drawnShapes.stream().mapToLong(s -> s.timeTaken).average().orElse(0);
            writer.write("Average Time: " + averageTime + " ms\n");

            System.out.println("Log file saved at: " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }
}