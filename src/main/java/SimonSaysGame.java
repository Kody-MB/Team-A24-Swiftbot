import swiftbot.*;
import java.util.*;
import java.util.concurrent.*;
public class SimonSaysGame {
	static SwiftBotAPI swiftBot;
	
	  static int[] red = {255, 0, 0};
	    static int[] green = {0, 255, 0};
	    static int[] blue = {0, 0, 255};
	    static int[] white = {255, 255, 255};
	    static int[] empty = {0, 0, 0};

	    static int[][] colours = {red, blue, green, white, empty};
	public static void main(String[] args) {
		swiftBot = new SwiftBotAPI();
		int selection = 0;
		Scanner console = new Scanner(System.in);

		Button buttons[] = { Button.A, Button.B, Button.X, Button.Y };
		
		
		int[] pattern = new int [1000] ;
		boolean on = true;
		
		while(on){
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
					TimeUnit.SECONDS.sleep(2);
					System.out.println("Each Button corresponds to a colour");
					TimeUnit.SECONDS.sleep(2);
					System.out.println("Please look at your robot and take note of the colour and its button");
					TimeUnit.SECONDS.sleep(3);
					
					Thread fillUnderlightsThread = new Thread(() -> { 
				        try {
				        	swiftBot.fillUnderlights(red); // Fill the underlights
							TimeUnit.SECONDS.sleep(3);
							swiftBot.fillUnderlights(blue); // Fill the underlights
							TimeUnit.SECONDS.sleep(3);
							swiftBot.fillUnderlights(green); // Fill the underlights
							TimeUnit.SECONDS.sleep(3);
							swiftBot.fillUnderlights(white); // Fill the underlights
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    });

				    Thread buttonLightThread = new Thread(() -> {
				    	try {
				    		 swiftBot.setButtonLight(Button.A, true); // Set the button light
							TimeUnit.SECONDS.sleep(3);
							swiftBot.setButtonLight(Button.A, false);
				    		 swiftBot.setButtonLight(Button.B, true); // Set the button light
							TimeUnit.SECONDS.sleep(3);
							swiftBot.setButtonLight(Button.B,false);
				    		 swiftBot.setButtonLight(Button.X, true); // Set the button light
							TimeUnit.SECONDS.sleep(3);
							swiftBot.setButtonLight(Button.X, false);
				    		 swiftBot.setButtonLight(Button.Y, true); // Set the button light
							TimeUnit.SECONDS.sleep(3);
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
				    TimeUnit.SECONDS.sleep(3);
				    swiftBot.fillUnderlights(empty);
				    boolean correct = false;
				    while(!correct) {
				    	 if (ButtonCheck(randomColour)) {
						    	dance();
						    	System.out.println("Good Job!");
						    	System.out.println("Lets do it with two Colours!");
						    	   randomColour = (int) (Math.random() * 4);
							        pattern[0] = randomColour;
							        randomColour = (int) (Math.random() * 4);
							        pattern[1] = randomColour;     
						    	correct = miniGame(pattern,2);
						    	while(!correct) {
						    		System.out.println("Come on you got this");
						    		System.out.println("Lets try again");
						    		  randomColour = (int) (Math.random() * 4);
								        pattern[0] = randomColour;
								        randomColour = (int) (Math.random() * 4);
								        pattern[1] = randomColour;
							    	correct = miniGame(pattern,2);
						    	}
						    
						    						    }
				    	 else {
				    		 System.out.println("That wasn't right lets try again");
				    		 randomColour = (int)(Math.random()*4);
				    		 swiftBot.fillUnderlights(colours[randomColour]);
							    TimeUnit.SECONDS.sleep(3);
							    swiftBot.fillUnderlights(empty);
							    ButtonCheck(randomColour);
				    	 }
				
				    }
				    
				    
				   System.out.println("Now that you understand the rules, go and get a new high score!");
				   dance();
				    
				    
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
			break;
			}
			
		}
	
	}
	
	public static void dance () {
		for(int i = 0; i < 5; i++) {
			swiftBot.move(50, -50, 200);
			swiftBot.move(-50, 50, 200);
		}
	}
	
	public static boolean ButtonCheck(int randomColour) {
	    boolean check = false;
	    
	    final int[] buttonPress = { -1 }; 
	    Button buttons[] = { Button.A, Button.B, Button.X, Button.Y };
	    CountDownLatch latch = new CountDownLatch(1); // Latch to wait for a button press

	    // Configure button actions
	    swiftBot.enableButton(Button.A, () -> {
	        buttonPress[0] = 0;
	        swiftBot.disableButton(Button.A);
	        latch.countDown(); // Release the latch when Button A is pressed
	    });
	    swiftBot.enableButton(Button.B, () -> {
	        buttonPress[0] = 1;
	        swiftBot.disableButton(Button.B);
	        latch.countDown(); // Release the latch when Button B is pressed
	    });
	    swiftBot.enableButton(Button.X, () -> {
	        buttonPress[0] = 2;
	        swiftBot.disableButton(Button.X);
	        latch.countDown(); // Release the latch when Button X is pressed
	    });
	    swiftBot.enableButton(Button.Y, () -> {
	        buttonPress[0] = 3;
	        swiftBot.disableButton(Button.Y);
	        latch.countDown(); // Release the latch when Button Y is pressed
	    });

	    try {
	        latch.await(); // Wait for a button press
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt(); // Restore interrupt status
	        System.err.println("Interrupted while waiting for button press.");
	        return false; // Return false if interrupted
	    }

	    // Check if the button pressed matches the expected value
	    if (buttonPress[0] == randomColour) {
	        check = true;
	    }
	    swiftBot.disableAllButtons();

	    return check;
	}
	
	 public static boolean miniGame(int[] pattern, int scoreLength) {
		 boolean result = true;
		 try {
			
	        for (int i = 0; i < scoreLength; i++) {
	            swiftBot.fillUnderlights(colours[pattern[i]]);
	            TimeUnit.SECONDS.sleep(2);
	            swiftBot.fillUnderlights(empty);
	        }
	        for( int i= 0; i <scoreLength; i++) {
	        	
	        	if (!ButtonCheck(pattern[i])) {
	        		result = false;
	        		break;
	        	}
	        }
	       
		 } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
		 return result;
	        
	    }
	
}
