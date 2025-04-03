import swiftbot.*;
import java.util.Scanner;

public class SwiftBotCalibration40Percent {
    private static SwiftBotAPI swiftBot;

    public static void main(String[] args) throws InterruptedException {
        try {
            swiftBot = new SwiftBotAPI(); // Initialize SwiftBot
        } catch (Exception e) {
            System.out.println("\nI2C disabled! Run the following command:");
            System.out.println("sudo raspi-config nonint do_i2c 0\n");
            System.exit(5);
        }

        System.out.println("SwiftBot Speed Calibration at 40%");
        System.out.println("This program to move the SwiftBot forward at 40% speed and then we callibrate it based on that");

        // Speed setting to test (40%)
        int speed = 40;

        // Define the time to move at 40% speed (in milliseconds)
        int moveTime = 2000; // 2 seconds

        // Move the SwiftBot forward at 40% speed
        System.out.println("\nMoving SwiftBot at 40% speed for " + (moveTime / 1000) + " seconds...");
        swiftBot.move(speed, speed, moveTime);

        // Wait for the movement to complete
        Thread.sleep(moveTime);

        // Measure the distance traveled (you will need to measure this manually)
        System.out.println("Measure the distance traveled (in cm) and enter it below:");
        Scanner scanner = new Scanner(System.in);
        double distance = scanner.nextDouble();

        // Calculate speed in cm/s
        double speedCmPerSecond = distance / (moveTime / 1000.0);
        System.out.println("Speed at 40%: " + speedCmPerSecond + " cm/s");

        // Log the results
        System.out.println("\nCalibration Results:");
        System.out.println("Speed Setting: " + speed + "%");
        System.out.println("Distance Traveled: " + distance + " cm");
        System.out.println("Calculated Speed: " + speedCmPerSecond + " cm/s");

        System.out.println("\nCalibration complete!");
    }
}
