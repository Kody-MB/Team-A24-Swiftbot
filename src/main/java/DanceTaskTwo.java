import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Scanner;
import swiftbot.ImageSize;
import swiftbot.SwiftBotAPI;
import java.util.concurrent.*;
import java.io.*;
public class DanceTaskTwo {

	static SwiftBotAPI swiftBot;
	static ArrayList <String> invalidHexes = new ArrayList<String>();
	static ArrayList<Moves> currentHexNums = new ArrayList<Moves>(); //stores the current list of inputed numbers 

	public static void main(String[] args) {
	BufferedImage ScannedImage;
	String ScannedString = null;
	 File moveLog = new File("TaskTwoDance_Move_Log_File.txt"); //creates new file for completed moves
     FileWriter moveLogFileWriter = null;
	boolean programOn = true;
	Scanner console = new Scanner(System.in);
	
	try {
	 moveLogFileWriter = new FileWriter(moveLog);
	} catch (IOException e) {
		e.printStackTrace();
		System.out.println("Log move file does not exist");
	}
	
	
	swiftBot = new SwiftBotAPI();
	System.out.println("-------------Welcome-------------");
	while(programOn){
		
		try {
			System.out.println("Please scan a QR code");
			int scanCount =0;
			ScannedImage = swiftBot.getQRImage();
			ScannedString = swiftBot.decodeQRImage(ScannedImage);
			boolean earlyExit = false;
			
			while(ScannedString.isEmpty() && !earlyExit) { // checks for a valid string or if the user has exited early
				
			System.out.println("no QR code detected please try again");
			scanCount = scanCount + 1;
			System.out.println("Scan count: " + scanCount);
			TimeUnit.SECONDS.sleep(2);
			ScannedImage = swiftBot.getQRImage();
			ScannedString = swiftBot.decodeQRImage(ScannedImage);
			if (scanCount % 5 ==0) { //checks for QR code 5 times before asking if the user is still trying
				System.out.println("Are you still trying to scan?");
				System.out.println("type y/Y for yes or n/N for No");
				String stillHereCheck = console.nextLine().toUpperCase();
				while(!stillHereCheck.equals("Y") && !stillHereCheck.equals("N")){
					System.out.println("type y/Y for yes or n/N for No");
					stillHereCheck = console.nextLine().toUpperCase();
				}
				if(stillHereCheck.equals("Y")) {
					System.out.println("try a different QR code or move the camera"); //suggests options if user is still trying
					TimeUnit.SECONDS.sleep(2);
					ScannedImage = swiftBot.getQRImage();
					ScannedString = swiftBot.decodeQRImage(ScannedImage);
				}
				if(stillHereCheck.equals("N")) {
					earlyExit = true;
				}
			}
			
			}
			
			if(earlyExit) {
				programOn = false; //switches program off
				break;
			}
			
			System.out.println("The following String Has Been Scanned - " + ScannedString); //shows user what string has been scanned 
			TimeUnit.SECONDS.sleep(2);
			
			if(!ScannedString.isEmpty()) {
			validHexes(ScannedString);
			
			if(currentHexNums.isEmpty()) { //checks if there were valid hexadecimal numbers 
				System.out.println("no valid hexadecimal numbers");
				System.out.println("only hexadecimal numbers are accepted moves");
				TimeUnit.SECONDS.sleep(2);
			}
			else {
				System.out.println();
				System.out.println("The following symbols have been removed for not being valid Hexadecimal Numbers or because we have exceeded the limit of 5");
				
				for(int i =0; i< invalidHexes.size();i++) {
					System.out.print(invalidHexes.get(i) + ':');
				} //shows the omitted symbols
				
				TimeUnit.SECONDS.sleep(2);
				System.out.println();
				System.out.println("The following moves will be done");
				
				for(int i =0; i< currentHexNums.size();i++) {
					System.out.print(currentHexNums.get(i).getHexNum() + ":");
				} //shows the valid move set
				
				System.out.println();
				
				for(int i = 0; i < currentHexNums.size();i++) {
					System.out.println("Hexadecimal Number "+ currentHexNums.get(i).getHexNum()+
							", Octal Number " + currentHexNums.get(i).getOctNum() + 
							", Decimal Number " + currentHexNums.get(i).getDecNum()+
							", Binary Number " + currentHexNums.get(i).getBinNum()+
							", Wheel Speed = " + currentHexNums.get(i).getWheelSpeed()+
							", LED Colour (Red " + currentHexNums.get(i).getRed() 
							+  ", Green " + currentHexNums.get(i).getGreen() 
							+ ", Blue " + currentHexNums.get(i).getBlue() + ")");
							TimeUnit.SECONDS.sleep(1);} // outputs the list of moves and their respective parameters 
				
				System.out.println("Give me some space and lets get ready to move!");
				System.out.println("Starting in 3");
				TimeUnit.SECONDS.sleep(1);
				System.out.println("2");
				TimeUnit.SECONDS.sleep(1);
				System.out.println("1");
				TimeUnit.SECONDS.sleep(1);
				
				try {
					for(int i = 0; i < currentHexNums.size();i++) {
						moveLogFileWriter.write("\n" + currentHexNums.get(i).getHexNum());
					} //logs the move set into the move log file 
					
				}
				

				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Log move file does not exist");
				}
				for(int i = 0; i < currentHexNums.size();i++) {
					currentHexNums.get(i).peformMovements();
				}// runs the moves one by one 
				System.out.println("If you would like scan a new set of hexdecimal moves please type Y/y");
				System.out.println("If you would like to log the moves and quit the progam please type N/n");
				//prompts user to input a new QR Code or to exit the program
				String input = console.nextLine().toUpperCase();
				
				while(!input.equals("N") && !input.equals("Y") ) {
					System.out.println("Please input n/N or y/Y");
					input = console.nextLine().toUpperCase();
				}
				if(input.equals("N")) {
					programOn = false;
				} //switches the program off exiting the while loop 
			}
	}
	
		
	
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		} 
	}
	try {
		System.out.println("Find the move log file here! --> " + moveLog.getAbsolutePath()); //outputs the file path for move log
		moveLogFileWriter.close();
		System.out.println("Come make me dance again soon!"); 
		
	}
	catch (IOException e) {
		e.printStackTrace();
		System.out.println("Log move file does not exist");
	}

	
	
	}
	

	public static void validHexes(String ScannedString) {
		invalidHexes.clear();
		currentHexNums.clear(); //readies program for new inputs 
	    int validHexCount = 0;
	    String hexNum = "";

	    for (int i = 0; i < ScannedString.length(); i++) {
	        char currentChar = ScannedString.charAt(i); //checks each part of the string

	        if (currentChar == ':') { //checks if its the end of hexadecimal number
	            if (!hexNum.isEmpty()) { //process previous hex segment
	                if (hexNum.matches("[0-9a-fA-F]{1,2}")) { //checks if its a valid 2 digit hex number 
	                    if (validHexCount < 5) {// checks if the list of current inputs is less than 5
	                        currentHexNums.add(new Moves(hexNum, swiftBot));// creates new hex object
	                        validHexCount++;
	                    } else {
	                        invalidHexes.add(hexNum); //adds if exceeds 5
	                    }
	                } else {
	                    invalidHexes.add(hexNum); //adds if not valid hex or more than 2 digits
	                }
	            }
	            hexNum = ""; //reset for next segment
	     
	        } else {
	            hexNum += currentChar;
	        }
	    }

	    //handles the last digit if it does not end in a ':'
	    if (!hexNum.isEmpty()) {
	        if (hexNum.matches("[0-9a-fA-F]{1,2}")) {
	            if (validHexCount < 5) {
	            	 currentHexNums.add(new Moves(hexNum, swiftBot));
	            } else {
	                invalidHexes.add(hexNum);
	            }
	        } else {
	            invalidHexes.add(hexNum);
	        }
	    }
	}
		
}