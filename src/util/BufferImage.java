package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;

import sun.misc.BASE64Encoder;

public class BufferImage {
	
	static File imageFile =  new File("image1.jpg");
	static String image_string = null;
	
	public static String URLtoImage(String Url)  {
	try 
	{
		String Img_URL = Url;
		 URL url = new URL(Img_URL);
		
		 FileUtils.copyURLToFile(url, imageFile);
		 
		BufferedImage img = null;
	     img = ImageIO.read(imageFile); // eventually C:\\ImageTest\\pic2.jpg
	     image_string = encodeToString(img, "jpg");
	    //System.out.println(image_string);
	    
	     if(imageFile.delete()){
				System.out.println(imageFile.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
	    
	} 
	catch (IOException e) 
	{
	    e.printStackTrace();
	}
	return image_string;
	}
	
	public static String encodeToString(BufferedImage image, String type) {
		String base64String = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
		ImageIO.write(image, type, bos);
		byte[] imageBytes = bos.toByteArray();
		BASE64Encoder encoder = new BASE64Encoder();
		base64String = encoder.encode(imageBytes);
		bos.close();
		} catch (IOException e) {
		e.printStackTrace();
		}
		return base64String;
		}
}
