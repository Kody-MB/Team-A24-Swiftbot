import swiftbot.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class SimonSaysGame {
	static SwiftBotAPI swiftBot;
	public static void main(String[] args) {
		swiftBot = new SwiftBotAPI();
		int selection = 0;
		Scanner console = new Scanner(System.in);

		Button buttons[] = { Button.A, Button.B, Button.X, Button.Y };
		int[] red = {255, 0, 0};
		int[] green = {0, 255, 0};
		int[] blue = {0, 0, 255};
		int[] white = {255, 255, 255};
		int[] empty = {0, 0, 0};
		
		int [] colours [] = {red, green, blue, white, empty};
		
		System.out.println("Welcome to Simon Says!");
		System.out.println("Please select 1 to start a new game");
		System.out.println("Please select 2 to read the rules");
		System.out.println("Please select 3 to exit the game");
		selection = console.nextInt();
		

		
		switch (selection) {
		case 1: 
		break;
		
		case 2:
			try {
		


				System.out.println("The rules of the game are simple");
				TimeUnit.SECONDS.sleep(3);
				System.out.println("Each Button corresponds to a colour");
				TimeUnit.SECONDS.sleep(3);
				System.out.println("Please look at your robot and take note of the colour and its button");
				TimeUnit.SECONDS.sleep(3);
				
				Thread fillUnderlightsThread = new Thread(() -> { 
			        try {
			        	swiftBot.fillUnderlights(red); // Fill the underlights
						TimeUnit.SECONDS.sleep(5);
						swiftBot.fillUnderlights(green); // Fill the underlights
						TimeUnit.SECONDS.sleep(5);
						swiftBot.fillUnderlights(blue); // Fill the underlights
						TimeUnit.SECONDS.sleep(5);
						swiftBot.fillUnderlights(white); // Fill the underlights
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });

			    Thread buttonLightThread = new Thread(() -> {
			    	try {
			    		 swiftBot.setButtonLight(Button.A, true); // Set the button light
						TimeUnit.SECONDS.sleep(5);
						swiftBot.setButtonLight(Button.A, false);
			    		 swiftBot.setButtonLight(Button.B, true); // Set the button light
						TimeUnit.SECONDS.sleep(5);
						swiftBot.setButtonLight(Button.B,false);
			    		 swiftBot.setButtonLight(Button.X, true); // Set the button light
						TimeUnit.SECONDS.sleep(5);
						swiftBot.setButtonLight(Button.X, false);
			    		 swiftBot.setButtonLight(Button.Y, true); // Set the button light
						TimeUnit.SECONDS.sleep(5);
						swiftBot.setButtonLight(Button.Y, false);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    });
			    fillUnderlightsThread.start(); // Start the thread for underlights
			    buttonLightThread.start();     // Start the thread for button light
			    // Optionally wait for both threads to complete before moving on
			    fillUnderlightsThread.join();
			    buttonLightThread.join();
			    
			    System.out.println("Lets run a quick practice!");
			    System.out.println("A random colour will appear, press the correct corresponding button");
			    int randomColour = (int)(Math.random()*4);
			    
			    swiftBot.fillUnderlights(colours[randomColour]);
			    TimeUnit.SECONDS.sleep(5);
			    System.out.println("Work?");
			    
			    
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		break;
		}
		
	}
	
	public static void dance () {
		for(int i = 0; i < 5; i++) {
			swiftBot.move(50, -50, 200);
			swiftBot.move(-50, 50, 200);
			
		}
	}

}
