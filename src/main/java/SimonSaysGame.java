import swiftbot.*;
import java.util.*;
import java.util.concurrent.*;
public class SimonSaysGame {
	static SwiftBotAPI swiftBot;
	
	  static int[] red = {255, 0, 0};
	    static int[] green = {0, 255, 0};
	    static int[] blue = {0, 0, 255};
	    static int[] yellow = {255, 255, 0};
	    static int[] empty = {0, 0, 0};

	    static int[][] colours = {red, blue, green, yellow, empty};
	public static void main(String[] args) {
		swiftBot = new SwiftBotAPI();
		int selection = 0;
		Scanner console = new Scanner(System.in);

		Button buttons[] = { Button.A, Button.B, Button.X, Button.Y };
		
		
		int[] pattern = new int [1000] ;
		boolean on = true;
		int highScore = -1;
		
		while(on){
			System.out.println("\nWelcome to Simon Says!");
			System.out.println("\nPlease select 1 to start a new game");
			System.out.println("\nPlease select 2 to read the rules");
			System.out.println("\nDANCE OFF select 3");
			System.out.println("\nPlease select 4 to exit the game");
			selection = console.nextInt();
			
		
			
			switch (selection) {
			case 1: 
				try {
					for(int i = 0; i< pattern.length; i++) {
						 int randomColour = (int)(Math.random()*4);
						 pattern[i] = randomColour;
					}
					boolean correct = true;
					int currentScore = 1;
					System.out.println("\nSimon Says \"welcome to the GAME\"");
					System.out.println("\nSimon Says \"Get ready!\"");
					System.out.println("3");
					TimeUnit.SECONDS.sleep(1);
					System.out.println("2");
					TimeUnit.SECONDS.sleep(1);
					System.out.println("1");
					TimeUnit.SECONDS.sleep(1);
					while(correct) {
						if( miniGame(pattern,currentScore)){
							currentScore++;
							System.out.println("Well done your current score is now: " + (currentScore - 1));
							dance();
							TimeUnit.SECONDS.sleep(2);
						}
						else {
							correct = false;
							 for(int i = 0; i < 5; i++) {
									swiftBot.fillUnderlights(red);
									TimeUnit.SECONDS.sleep(2);
									swiftBot.fillUnderlights(empty);
								}
							System.out.println("Oh no that was wrong!");
							TimeUnit.SECONDS.sleep(2);
							System.out.println("Your final score is: " + (currentScore - 1));
							TimeUnit.SECONDS.sleep(1);
							if((currentScore -1 ) > highScore) {
								highScore = (currentScore - 1);
								System.out.println("You achieved a new high score!");
								TimeUnit.SECONDS.sleep(1);
								System.out.println("Your new high score is: " + highScore);
								dance();
								TimeUnit.SECONDS.sleep(3);
							}
						}
						}
					
					
			}
				catch(InterruptedException e) {
					
				}
				
				
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
							swiftBot.fillUnderlights(yellow); // Fill the underlights
							TimeUnit.SECONDS.sleep(3);
						    swiftBot.fillUnderlights(empty);
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
				    TimeUnit.SECONDS.sleep(3);
				    System.out.println("If the robot flashes red like this");
				    TimeUnit.SECONDS.sleep(1);
				    for(int i = 0; i < 5; i++) {
						swiftBot.fillUnderlights(red);
						TimeUnit.SECONDS.sleep(2);
						swiftBot.fillUnderlights(empty);
					}
				    System.out.println("You entered the wrong colour in the pattern");
				    TimeUnit.SECONDS.sleep(3);
				    System.out.println("if the robot dances like this");
				    TimeUnit.SECONDS.sleep(1);
				    dance();
				    System.out.println("the WHOLE pattern is correct and you move to the next round");
				    TimeUnit.SECONDS.sleep(2);
				    System.out.println("Lets run a quick practice!");
				    TimeUnit.SECONDS.sleep(3);
				    System.out.println("A random colour will appear, press the correct corresponding button");
				    TimeUnit.SECONDS.sleep(3);
				    int randomColour = (int)(Math.random()*4);
				    swiftBot.fillUnderlights(colours[randomColour]);
				    TimeUnit.SECONDS.sleep(3);
				    swiftBot.fillUnderlights(empty);
				    boolean correct = false;
				    while(!correct) {
				    	 if (ButtonCheck(randomColour)) {
						    	dance();
						    	TimeUnit.SECONDS.sleep(2);
						    	System.out.println("Good Job!");
						    	TimeUnit.SECONDS.sleep(2);
						    	System.out.println("Lets do it with two Colours!");
						    	TimeUnit.SECONDS.sleep(3);
						    	   randomColour = (int) (Math.random() * 4);
							        pattern[0] = randomColour;
							        randomColour = (int) (Math.random() * 4);
							        pattern[1] = randomColour;     
						    	correct = miniGame(pattern,2);
						    	while(!correct) {
						    		 for(int i = 0; i < 5; i++) {
											swiftBot.fillUnderlights(red);
											TimeUnit.SECONDS.sleep(2);
											swiftBot.fillUnderlights(empty);
										}
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
				    		 for(int i = 0; i < 5; i++) {
									swiftBot.fillUnderlights(red);
									TimeUnit.SECONDS.sleep(2);
									swiftBot.fillUnderlights(empty);
								}
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
				  
				   pattern = null;
				    
				    
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
			break;
			case 3:
				dance();
				break;
			
			case 4:
				System.out.println("Come back to play Simon Says Another Time!");
				on = false;
				System.exit(0);
				break;
			}
			
		}
	
	}
	
	public static void dance () {

			
					for(int i = 0; i< 10; i++) {
						int randomColour = (int)(Math.random()*4);
						swiftBot.fillUnderlights(colours[randomColour]); 
						swiftBot.move(50, -50, 200);
						swiftBot.move(-50, 50, 200);
					}
					swiftBot.fillUnderlights(empty);
					
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
	            TimeUnit.MILLISECONDS.sleep(250);
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
