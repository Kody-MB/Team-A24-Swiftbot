import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;
import swiftbot.*;
import java.util.regex.Pattern;

public class ZigZag {
    private static SwiftBotAPI swiftBot;
    private static SwiftBotController speedController;

    public void start () throws InterruptedException {
        try {
            swiftBot = new SwiftBotAPI();
        } catch (Exception e) {
            System.out.println("\nI2C disabled!");
            System.out.println("Run the following command:");
            System.out.println("sudo raspi-config nonint do_i2c 0\n");
            System.exit(5);
        }

        speedController = new SwiftBotController();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Choose input method:");
            System.out.println("1. Manual input");
            System.out.println("2. Scan QR code");
            System.out.print("Enter choice (1 or 2): ");
            String choice = scanner.nextLine();

            String input = "";
            if (choice.equals("1")) {
                System.out.print("Enter zigzag pattern (Length:Sections): ");
                input = scanner.nextLine();
            } else if (choice.equals("2")) {
                input = scanQRCode();
                if (input == null) {
                    System.out.println("Invalid QR code. Try again.");
                    continue;
                }
                System.out.println("Scanned input: " + input);
            } else {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            if (!isValidInput(input)) {
                System.out.println("Invalid input. Ensure format is length:sections (15 <= length <= 85, even sections <= 12). Try again.");
                continue;
            }

            String[] parts = input.split(":");
            int length = Integer.parseInt(parts[0]);
            int sections = Integer.parseInt(parts[1]);

            int speed = speedController.getRandomSpeed(); // Random speed between 10-100
            System.out.println("Speed set to: " + speed);

            double totalDistance = length * sections;
            double straightLineDistance = Math.sqrt(Math.pow((sections / 2) * length, 2) + Math.pow((sections / 2) * length, 2));
            double totalTime = calculateTime(length, speed) * sections;

            executeZigzag(length, sections, speed);
            logJourney(input, speed, totalDistance, totalTime, straightLineDistance);

            updateJourneyStats(input, straightLineDistance);

            System.out.println("Press 'Y' to continue or 'X' to exit.");
            String exitChoice = scanner.nextLine().toUpperCase();
            if (exitChoice.equals("X")) running = false;
        }

        displaySummary();
        scanner.close();
    }

    private static String scanQRCode() {
        System.out.println("Scan a QR code containing the zigzag parameters (format: length:sections):");
        BufferedImage img = swiftBot.getQRImage();
        String decodedMessage = swiftBot.decodeQRImage(img);

        if (isValidQRInput(decodedMessage)) {
            return decodedMessage;
        } else {
            return null;
        }
    }

    private static boolean isValidQRInput(String input) {
        // Validate format: "length:sections" with the specified constraints
        if (!Pattern.matches("\\d+:\\d+", input)) {
            return false;
        }

        String[] values = input.split(":");
        int length = Integer.parseInt(values[0]);
        int sections = Integer.parseInt(values[1]);

        return (length >= 15 && length <= 85) && (sections % 2 == 0 && sections <= 12);
    }

    private static boolean isValidInput(String input) {
        return isValidQRInput(input); // Reuse the same validation logic
    }

    private static double calculateTime(int length, int speed) {
        return length / (speed * 0.1); // Placeholder formula, should be calibrated
    }

    private static void executeZigzag(int length, int sections, int speed) throws InterruptedException {
        for (int i = 0; i < sections; i++) {
            swiftBot.fillUnderlights(i % 2 == 0 ? new int[]{0, 255, 0} : new int[]{0, 0, 255}); // Green or Blue
            swiftBot.move(speed, speed, (int) (calculateTime(length, speed) * 1000));
            Thread.sleep((long) (calculateTime(length, speed) * 1000));
            swiftBot.stopMove();
            Thread.sleep(1000);
            swiftBot.move(0,75,1000);
        }

        // Retrace path
        for (int i = sections - 1; i >= 0; i--) {
            swiftBot.fillUnderlights(i % 2 == 0 ? new int[]{0, 255, 0} : new int[]{0, 0, 255}); // Green or Blue
            swiftBot.move(speed, speed, (int) (calculateTime(length, speed) * 1000));
            Thread.sleep((long) (calculateTime(length, speed) * 1000));
            swiftBot.stopMove();
            Thread.sleep(1000);
            swiftBot.move(75,0,1000);
        }
        swiftBot.disableUnderlights();
    }

    private static void logJourney(String input, int speed, double totalDistance, double totalTime, double straightLineDistance) {
        try (FileWriter fw = new FileWriter(SwiftBotZigzag.LOG_FILE, true)) {
            fw.write(String.format("Journey: %s, Speed: %d, Total Distance: %.2f, Time: %.2f, Straight Line Distance: %.2f\n",
                    input, speed, totalDistance, totalTime, straightLineDistance));
        } catch (IOException e) {
            System.out.println("Error writing to log file.");
        }
    }

    private static void updateJourneyStats(String input, double straightLineDistance) {
        SwiftBotZigzag.journeyCount++;
        if (straightLineDistance > SwiftBotZigzag.longestDistance) {
            SwiftBotZigzag.longestDistance = straightLineDistance;
            SwiftBotZigzag.longestJourney = input;
        }
        if (straightLineDistance < SwiftBotZigzag.shortestDistance) {
            SwiftBotZigzag.shortestDistance = straightLineDistance;
            SwiftBotZigzag.shortestJourney = input;
        }
    }

    private static void displaySummary() {
        System.out.println("\nSummary of Journeys:");
        System.out.println("Total Zigzag Journeys: " + SwiftBotZigzag.journeyCount);
        System.out.println("Longest Journey: " + SwiftBotZigzag.longestJourney + " (" + SwiftBotZigzag.longestDistance + " cm)");
        System.out.println("Shortest Journey: " + SwiftBotZigzag.shortestJourney + " (" + SwiftBotZigzag.shortestDistance + " cm)");
        System.out.println("Log file saved at: " + SwiftBotZigzag.LOG_FILE);
    }

    private static class SwiftBotZigzag {
        private static final int MIN_LENGTH = 15;
        private static final int MAX_LENGTH = 85;
        private static final int MAX_SECTIONS = 12;
        private static int journeyCount = 0;
        private static String longestJourney = "";
        private static double longestDistance = 0;
        private static String shortestJourney = "";
        private static double shortestDistance = Double.MAX_VALUE;
        private static final String LOG_FILE = "swiftbot_log.txt";
    }

    private static class SwiftBotController {
        private static final int MIN_SPEED = 10;  // Adjust based on API limits
        private static final int MAX_SPEED = 100; // Adjust based on API limits
        private Random random;

        public SwiftBotController() {
            this.random = new Random();
        }

        public int getRandomSpeed() {
            return random.nextInt(MAX_SPEED - MIN_SPEED + 1) + MIN_SPEED;
        }
    }
}