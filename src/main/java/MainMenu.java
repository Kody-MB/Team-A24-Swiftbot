import java.util.Scanner;
public class MainMenu {

	public static void main(String[] args) {
		
		try (Scanner console = new Scanner(System.in)) {
			boolean programOn = true;
			System.out.println("----------Welcome to the Group A-24 swiftBot program!----------");
			
			while(programOn) {
				System.out.println("please select one of the options to run one of our tasks!");
				
				System.out.println(
						"1 = Navigation by Sifat       \t|\t 2 = ZigZag by Robert\n" +
						"3 = Search for Light by Marks    \t|\t 4 = Dance by Kody\n" +
						"5 = Master Mind by Canberk   \t|\t 6 = Draw Shape by Fawaz\n" +
						"7 = Detect Object by Hamza \t|\t 8 = Traffic Light by Ahsen\n" +
						"0 = Exit\n");
System.out.print("Enter a number: ");
				
				String input = console.nextLine();
				
				switch(input) {
				case "0":
					System.out.println("Come Back Soon!");
					programOn = false;
					break;
				case "1":
					try {
						new NavigationTask().start();
					} catch (InterruptedException e) {
					
					}
					break;
				case "2": 
					try {
						new ZigZag().start();
					} catch (InterruptedException e) {
					
					}
					break;
				case "3":
					new SwiftBotLightSeek().start();
					break;
				case "4":
					new DanceTaskTwo().start();
					break;
				case "5":
					new MasterMind().startGame();
					break;
				case "6":
					break;
				case "7":
					new ObjectDetectionTaskSix().start();
					break;
				case "8":
					break;
				default:
					System.out.println("These are not valid lables try again");
				break;
				}
				
			}
		}
	
	}

}
