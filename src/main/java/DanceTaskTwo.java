import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Scanner;

import swiftbot.SwiftBotAPI;
import java.util.concurrent.*;
import java.io.*;
public class DanceTaskTwo {
	static SwiftBotAPI swiftBot;
	
	static ArrayList <String> InputtedHexes = new ArrayList<String>();
	static ArrayList <String> invalidHexes = new ArrayList<String>();
	static ArrayList <String> currentHexNums = new ArrayList<String>();
	static int red =0;
	static int green=0;
	static int blue=0;
	
	public static void main(String[] args) {
 	int counter = 0;
	BufferedImage ScannedImage;
	String ScannedString = null;
	String wtf;
	 File moveLog = new File("TaskTwoDance_Move_Log_File.txt");
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
			ScannedImage = swiftBot.getQRImage();
			ScannedString = swiftBot.decodeQRImage(ScannedImage);
			while(ScannedString.isEmpty()) {
			System.out.println("no QR code detected please try again");
			TimeUnit.SECONDS.sleep(2);
			ScannedImage = swiftBot.getQRImage();
			ScannedString = swiftBot.decodeQRImage(ScannedImage);
			}
			System.out.println("The following String Has Been Scanned - " + ScannedString);
			TimeUnit.SECONDS.sleep(2);
			if(!ScannedString.isEmpty()) {
			validHexes(ScannedString);
			System.out.println();
			System.out.println("The following symbols have been removed for not being valid Hexadecimal Numbers");
			for(int i =0; i< invalidHexes.size();i++) {
				System.out.print(invalidHexes.get(i) + ':');
			}
			TimeUnit.SECONDS.sleep(2);
			System.out.println();
			System.out.println("The following moves will be done");
			for(int i =0; i< currentHexNums.size();i++) {
				System.out.print(currentHexNums.get(i) + ':');
			}
			System.out.println();
			
			for(int i = 0; i < currentHexNums.size();i++) {
				calcRGB(currentHexNums.get(i));
				System.out.println("Hexadecimal Number "+ currentHexNums.get(i)+
						", Octal Number " + hexToOctal(currentHexNums.get(i)) + 
						", Decimal Number " + hexToDec(currentHexNums.get(i))+
						", Binary Number " + hexToBin(currentHexNums.get(i))+
						", Wheel Speed = " + calcWheelSpeed(currentHexNums.get(i))+
						", LED Colour (Red " + red +  ", Green " + green + ", Blue " + blue + ")");
						TimeUnit.SECONDS.sleep(1);}
			
			System.out.println("Give me some space and lets get ready to move!");
			System.out.println("Starting in 3");
			TimeUnit.SECONDS.sleep(1);
			System.out.println("2");
			TimeUnit.SECONDS.sleep(1);
			System.out.println("1");
			TimeUnit.SECONDS.sleep(1);
			for(int i = 0; i < currentHexNums.size();i++) {
				calcRGB(currentHexNums.get(i));
				peformMovements(currentHexNums.get(i));
			}
			System.out.println("If you would like scan a new set of hexdecimal moves please type Y/y");
			System.out.println("If you would like to log the moves and quit the progam please type N/n");
			String input = console.nextLine().toUpperCase();
			
			while(!input.equals("N") && !input.equals("Y") ) {
				System.out.println("Please input n/N or y/Y");
				input = console.nextLine().toUpperCase();
			}
			if(input.equals("N")) {
				programOn = false;
			}
			else {
				
			}
	}
	
		
	
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		} 
	}
	try {
		for(int i = 0; i < currentHexNums.size();i++) {
			moveLogFileWriter.write("\n" + currentHexNums.get(i));
		}
		System.out.println("Find the move log file here! --> " + moveLog.getAbsolutePath());
		moveLogFileWriter.close();
		System.out.println("Come make me dance again soon!");
		
	}
	

	catch (IOException e) {
		e.printStackTrace();
		System.out.println("Log move file does not exist");
	}
	
	
	}
	

	public static void validHexes(String ScannedString) {
	    // Reset lists for each scan
	    
	    int validHexCount = 0;
	    String hexNum = "";
	    boolean validSymbol = true;

	    for (int i = 0; i < ScannedString.length(); i++) {
	        char currentChar = ScannedString.charAt(i);

	        if (currentChar == ':') {
	            if (!hexNum.isEmpty()) { // Process previous hex segment
	                if (hexNum.matches("[0-9a-fA-F]{1,2}")) { // Valid hex (1 or more characters)
	                    if (validHexCount < 5) {
	                        currentHexNums.add(hexNum);
	                        validHexCount++;
	                    } else {
	                        invalidHexes.add(hexNum);
	                    }
	                } else {
	                    invalidHexes.add(hexNum);
	                }
	            }
	            hexNum = ""; // Reset for next segment
	            validSymbol = true;
	        } else {
	            hexNum += currentChar;
	            if (!Character.toString(currentChar).matches("[0-9a-fA-F]{1,2}")) {
	                validSymbol = false;
	               
	            }
	        }
	    }

	    // Handle the last segment if not processed
	    if (!hexNum.isEmpty()) {
	        if (hexNum.matches("[0-9a-fA-F]{1,2}")) { // Allow single-character hex
	            if (validHexCount < 5) {
	                currentHexNums.add(hexNum);
	            } else {
	                invalidHexes.add(hexNum);
	            }
	        } else {
	            invalidHexes.add(hexNum);
	        }
	    }
	}
	
	public static int hexToDec(String hexNum) {
		char symbol;
		int hexNum2Dec = 0;
		int lengthOfHexNum = hexNum.length()-1;
		for(int i = 0; i < hexNum.length();i++) {
			symbol = hexNum.charAt(i);
			if(Character.toString(symbol).matches("[0-9]")){
				hexNum2Dec = hexNum2Dec+ Integer.parseInt(Character.toString(symbol))*(int)Math.pow(16,lengthOfHexNum);
				lengthOfHexNum--;
			}
			else
			{
				switch(Character.toUpperCase(symbol)) {
				case 'A':
					hexNum2Dec = hexNum2Dec+ 10*(int)Math.pow(16,lengthOfHexNum);	
					break;
				case 'B':
					hexNum2Dec = hexNum2Dec+ 11*(int)Math.pow(16,lengthOfHexNum);	
					break;
				case 'C':
					hexNum2Dec = hexNum2Dec+ 12*(int)Math.pow(16,lengthOfHexNum);
					break;
				case 'D':
					hexNum2Dec = hexNum2Dec+ 13*(int)Math.pow(16,lengthOfHexNum);	
					break;
				case 'E':
					hexNum2Dec = hexNum2Dec+ 14*(int)Math.pow(16,lengthOfHexNum);	
					break;
				case 'F':
					hexNum2Dec = hexNum2Dec+ 15*(int)Math.pow(16,lengthOfHexNum);
					break;
				
				}
				lengthOfHexNum--;
				
			}
			
			
			
		}
		
		
		return hexNum2Dec;
	}
	public static int hexToOctal(String hexNumToDec) {
	    int octNum = 0;
	    int dec = hexToDec(hexNumToDec);
	    int placeValue = 1; // Keeps track of positional value in octal system

	    while (dec > 0) {
	        octNum += (dec % 8) * placeValue; // Extract last octal digit and add to result
	        dec /= 8; // Reduce decimal number by dividing by 8
	        placeValue *= 10; // Move to the next place value (1s, 10s, 100s, etc.)
	    }

	    return octNum;
	}
	
	public static String hexToBin (String hexNumToDec) {
		    int dec = hexToDec(hexNumToDec);
		    StringBuilder Binary = new StringBuilder ();
		    String binNum ="";
		    if (dec!=0) {
		    	 while (dec > 0){
				    	Binary.append(dec%2);
				    	dec/=2;
				    }
		    	 binNum= Binary.reverse().toString();
		    }
		    else {
		    	binNum = "0";
		    }
		    return binNum;
		   
	}
	
	public static int calcWheelSpeed (String hexNum) {
		int wheelSpeed = 0;
		int octNum = hexToOctal(hexNum);
		if(octNum >100) {
			wheelSpeed = 100;
		}
		else if (octNum > 50) {
			wheelSpeed = octNum;
		}
		else {
			wheelSpeed = octNum + 50;
		}
		return wheelSpeed;
	}
	
	public static void calcRGB (String hexNum) {
		int dec = hexToDec(hexNum);
		red = dec ;
		green = dec%80*3;
		if(red > green) {
			blue = red;
		}
		else {
			blue = green;
		}
		}
	
	public static void peformMovements (String hexNum) {
		int [] colour = {red, green, blue};
		int octNum = hexToOctal(hexNum);
		int decNum = hexToDec(hexNum);
		String binNum = hexToBin(hexNum);
		int wheelSpeed = calcWheelSpeed(hexNum);
		
		swiftBot.fillUnderlights(colour);
		for(int i = 0; i < binNum.length();i++) {
			if(binNum.charAt(i) == '0') {
				swiftBot.move(100, 0, 4000);
			}
			else {
				if(hexNum.length()>=1) {
					swiftBot.move(wheelSpeed, wheelSpeed, 1000);
				}
				else {
					swiftBot.move(wheelSpeed, wheelSpeed, 500);
				}
			}
		}
	}
		
}