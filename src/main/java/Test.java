import swiftbot.*;

public class Test {
	static SwiftBotAPI swiftBot;
	public static void main(String[] args) {
		swiftBot = new SwiftBotAPI();

		 swiftBot.enableButton(Button.A, () -> {
		        System.out.println("Button A Pressed");
		 });
	
	
	}

}
