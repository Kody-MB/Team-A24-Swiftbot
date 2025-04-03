import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class NavigationTask {
	static SwiftBotAPI swiftBot;
	static String decodedMessage = ""; 
	static Queue<String> commandQueue = new LinkedList<>(); // Store the commands
	static long programStartTime; // Track program start time

	public void start(SwiftBotAPI bot)  throws InterruptedException {
		try {
			swiftBot = bot;
			programStartTime = System.currentTimeMillis(); // Initialize program start time 	
		} catch (Exception e) {
			System.out.println("\nI2C disabled!");
			System.out.println("Run the following command:");
			System.out.println("sudo raspi-config nonint do_i2c 0\n");
			System.exit(5);
		}

		Scanner reader = new Scanner(System.in);
		boolean programOn = true;
		while (programOn) {
            System.out.println("\nWould you like to start? (yes/no)");
            String ans = reader.next();

            switch (ans.toLowerCase()) {
                case "yes":
                    commandQueue.clear(); // Reset command queue for new scan
                    decodedMessage = scanQRCode(5000);
                    if (!decodedMessage.isEmpty() && validateCommands(decodedMessage)) {
                        processDecodedMessage(decodedMessage);
                    }
                    break;

                case "no":
                    programOn = false;
                    break;

                default:
                    System.out.println("ERROR: Please enter a valid answer.");
                    break;
            }
        }
		programOn = true;
    }

	public static String scanQRCode(long timeout) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeout;
		String scannedMessage = "";

		while (System.currentTimeMillis() < endTime && scannedMessage.isEmpty()) {
			BufferedImage img = swiftBot.getQRImage();
			scannedMessage = swiftBot.decodeQRImage(img);

			if (scannedMessage.isEmpty()) {
				System.out.println("No QR Code found. Adjust the distance or try another.");
			} else {
				System.out.println("SUCCESS: QR code found");
				System.out.println("Decoded message: " + scannedMessage);
			}
		}

		if (scannedMessage.isEmpty()) {
			System.out.println("No QR code detected within the time limit.");
		}

		System.out.println("Time elapsed: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
		Thread.sleep(5000);
		return scannedMessage;
	}

	public static void createLogFile(String originalCommands) {  // Accepts original QR commands
	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	    String timestamp = LocalDateTime.now().format(dtf);
	    String fileName = System.getProperty("user.home") + File.separator + "SwiftBotLog_" + timestamp + ".txt";

	    try (FileWriter writer = new FileWriter(fileName)) {
	        writer.write("SwiftBot Command Log\n");
	        writer.write("====================\n");
	        writer.write("Commands Received:\n");

	        // Write only the original QR code commands (not traced commands)
	        String[] commands = originalCommands.split(";");
	        for (String command : commands) {
	            writer.write(command + "\n");
	        }

	        // Calculate and log total execution time
	        double totalTime = (System.currentTimeMillis() - programStartTime) / 1000.0;
	        writer.write(String.format("\nTotal Execution Time: %.1f seconds\n", totalTime));

	        // Log the exact time the log file was written
	        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
	        String currentTime = LocalDateTime.now().format(timeFormat);
	        writer.write("Log Written At: " + currentTime + "\n");

	        System.out.println("Log file created at: " + fileName);
	    } catch (IOException e) {
	        System.out.println("Error writing log file: " + e.getMessage());
	    }
	}
    public static boolean validateCommands(String decodedMessage) {
        String[] commands = decodedMessage.split(";");

        for (String command : commands) {
            String[] parts = command.split(",");

            if (parts[0].equals("W")) {
                if (parts.length != 1) {
                    System.out.println("Error: 'W' command should not have additional parameters.");
                    return false;
                }
            } else if (parts[0].equals("T")) {
                if (parts.length != 2) {
                    System.out.println("Error: 'T' command must have exactly one integer value.");
                    return false;
                }
                try {
                    int traceCount = Integer.parseInt(parts[1]);
                    if (traceCount < 1) {
                        System.out.println("Error: Trace count must be a positive number.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: Trace count must be an integer.");
                    return false;
                }
            } else {
                if (parts.length != 3) {
                    System.out.println("Error: Each movement command must have three parts (Direction, Speed, Duration).\n");
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void processDecodedMessage(String message) {
        commandQueue.clear(); // Clear previous commands
        String[] commands = message.split(";");

        int commandLimit = Math.min(commands.length, 10);
        System.out.println("Processing up to " + commandLimit + " commands (Max 10 allowed per session).");

        for (int i = 0; i < commandLimit; i++) {
            String command = commands[i];
            String[] parts = command.split(",");

            if (parts[0].equals("W")) {
                System.out.println("Creating a LOG file.");
                createLogFile(message);  // ✅ Pass only the original QR code commands
            } else if (parts[0].equals("T")) {
                int traceCount = Integer.parseInt(parts[1]);
                traceCommands(traceCount);
            } else if (parts.length == 3) {
                commandQueue.add(command);
                executeCommand(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } else {
                System.out.println("Invalid command format: " + command);
            }
        }
    }

    public static void executeCommand(String direction, int speed, int duration) {
        switch (direction) {
            case "R":
                System.out.println("Moving Right with speed " + speed + " for " + duration + " seconds.");
                swiftBot.move(speed, 0, duration * 1000);
                break;

            case "L":
                System.out.println("Moving Left with speed " + speed + " for " + duration + " seconds.");
                swiftBot.move(0, speed, duration * 1000);
                break;

            case "B":
                System.out.println("Moving Back with speed " + speed + " for " + duration + " seconds.");
                swiftBot.move(-speed, -speed, duration * 1000);
                break;

            case "F":
                System.out.println("Moving Forward with speed " + speed + " for " + duration + " seconds.");
                swiftBot.move(speed, speed, duration * 1000);
                break;

            default:
                System.out.println("Unknown command: " + direction);
                break;
        }
    }



    public static void traceCommands(int traceCount) {
        if (traceCount > commandQueue.size()) {
            System.out.println("Error: Trace count exceeds the number of executed movements in this session.");
            return;
        }

        System.out.println("Retracing last " + traceCount + " movement commands...");
        LinkedList<String> recentCommands = new LinkedList<>(commandQueue);
        for (int i = recentCommands.size() - traceCount; i < recentCommands.size(); i++) {
            String command = recentCommands.get(i);
            System.out.println("Executing trace: " + command);
            String[] parts = command.split(",");
            executeCommand(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }


	
		
	}
