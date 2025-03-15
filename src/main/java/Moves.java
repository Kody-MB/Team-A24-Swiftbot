import swiftbot.SwiftBotAPI;

public class Moves {
	String hexNum;
	int octNum;
	int decNum;
	String binNum;
	int wheelSpeed;
	static SwiftBotAPI swiftBot;
	int [] underlight = {0,0,0};
	int red;
	int blue;
	int green;
	
	
	public Moves (String hexNum, SwiftBotAPI bot ) {
	this.hexNum = hexNum;
	this.decNum = hexToDec(hexNum);
	this.octNum = hexToOctal();
	this.binNum = hexToBin();
	this.wheelSpeed = calcWheelSpeed();
	this.underlight = calcRGB();
	swiftBot = bot;

	}
	
	public static int hexToDec(String hexNum) { //hexadecimal to decimal converter
		char symbol; //the current digit
		int hexNum2Dec = 0;
		int lengthOfHexNum = hexNum.length()-1; //index starts at 0 so we remove one index
		for(int i = 0; i < hexNum.length();i++) {
			symbol = hexNum.charAt(i); // takes the first digit
			if(Character.toString(symbol).matches("[0-9]")){ //if the char is between 0 to 9 
				hexNum2Dec = hexNum2Dec+ Integer.parseInt(Character.toString(symbol))*(int)Math.pow(16,lengthOfHexNum);
				 //multiplying digit by 16 to the power of its place value e.g(7f: 7*16^1 = 112)
				lengthOfHexNum--; //moving down in place value 
			}
			else
			{
				switch(Character.toUpperCase(symbol)) { //the same but making the letter its respective decimal value  
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
	
	public  int hexToOctal() {//hexadecimal to octal converter 
	    int octNum = 0;
	    int tempDecNum = decNum; //stores the temporary decimal number 
	    int placeValue = 1; //keeps track of positional value in octal system

	    while (tempDecNum > 0) {
	        octNum += (tempDecNum % 8) * placeValue; //extract last octal digit and add to result
	        tempDecNum /= 8; // reduce decimal number by dividing by 8
	        placeValue *= 10; //move to the next place value 
	    }

	    return octNum;
	}
	
	public  String hexToBin () {//hexadecimal to binary converter 
	    StringBuilder Binary = new StringBuilder (); //used to store binary digits
	    int  tempDecNum = this.decNum;
	    String binNum ="";
	    if (this.decNum!=0) {
	    	 while (tempDecNum > 0){
			    	Binary.append(tempDecNum%2); //adding the remainder to the binary value
			    	tempDecNum/=2;
			    }
	    	 binNum= Binary.reverse().toString(); /*reversing the string to make the place values correct
	    	 e.g (16 = 00001 reverse = 10000)*/
	    }
	    else {
	    	binNum = "0";
	    }
	    return binNum;
	   
}
	
	public  int calcWheelSpeed () {//calculating wheel speed
		int wheelSpeed = 0;
		if(this.octNum >100) {
			wheelSpeed = 100;
		}
		else if (this.octNum > 50) {
			wheelSpeed = this.octNum;
		}
		else {
			wheelSpeed = this.octNum + 50;
		}
		return wheelSpeed;
	}
	public int[] calcRGB () {//assigning red green blue under lights;
		int [] colour = new int [3];
		red = this.decNum;
		green = this.decNum%80*3;
		if(red > green) {
			blue = red;
		}
		else {
			blue = green;
		}
		return colour;
		}
	
	public  void peformMovements () {//performing movements with given parameters 
		swiftBot.fillUnderlights(underlight);
		for(int i = 0; i < this.binNum.length();i++) {
			
			if(this.binNum.charAt(i) == '0') {
				swiftBot.move(100, 0, 4000);
			}
			else {
				if(this.hexNum.length()>1) {//if the length of the hexadecimal is 2 the length of movement is 1 second if its 1 its half a second 
					swiftBot.move(wheelSpeed, wheelSpeed, 1000);
				}
				else {
					swiftBot.move(wheelSpeed, wheelSpeed, 500);
				}
			}
		}
	}

	public String getHexNum() {
		return hexNum;
	}

	public int getOctNum() {
		return octNum;
	}

	public int getDecNum() {
		return decNum;
	}

	public String getBinNum() {
		return binNum;
	}

	public int getWheelSpeed() {
		return wheelSpeed;
	}

	public int getRed() {
		return red;
	}

	public int getBlue() {
		return blue;
	}

	public int getGreen() {
		return green;
	}



}

