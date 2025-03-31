import java.awt.image.BufferedImage;

import swiftbot.SwiftBotAPI;

public class QR {
	public static void main (String[]args) {
	SwiftBotAPI API = new SwiftBotAPI();
	
	for(int i=0;i<25;++i)
	{
	try
	{
			BufferedImage img = API.getQRImage();
			String decodedText = API.decodeQRImage(img);
			if(!decodedText.isEmpty())
			{
				System.out.println(decodedText);
			}
			else
			{
				System.out.println("+++NO QR CODE!");
			}
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	System.exit(1);

}}
