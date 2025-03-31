import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import swiftbot.ImageSize;
import swiftbot.SwiftBotAPI;

public class ImageProcessing {
public static void main (String args[]) throws Exception
{
    SwiftBotAPI swiftbot = new SwiftBotAPI();
    
    System.out.println("Capturing an image...");
    
    BufferedImage img = swiftbot.takeStill(ImageSize.SQUARE_48x48);
    ImageIO.write(img, "jpg", new File("TestImage.jpg"));

    System.out.println("Finished running \n\n");
    
    System.exit(1);

}
}
