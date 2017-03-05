import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO; 
public class start {
	public static void main(String[] s){
		
		
		File input = new File("/home/prashant/Desktop/five.png");
		try{
			Layout layout=new Layout(ImageIO.read(input));
			layout.makeBinary(150);

			layout.smearHorizontallySelectively(1);
			layout.smearVerticallySelectively(1);
			layout.detectBlobs();
			layout.showImage("/home/prashant/Desktop/final12.jpeg");
			layout.removeBlobs(100);
			layout.removeImages();
			layout.markBlobs();
			layout.showImage("/home/prashant/Desktop/final12.jpeg");

			/*layout.detectBlobs();
			
				
			//Here I am removing Images using a fixed height, but a better way would be to use SVM or Neural Networks with data!!!.:D
			layout.removeBlobs(100);
			//layout.keepEdgesOnly();
			//I need to remove images to do anything done.. So I am removing the image forcefully.
			//layout.markBlobs();
			layout.markWhiteSpace();
			layout.showImage("/home/prashant/Desktop/final123.jpeg");
			//Argument should be faction of size of font
			//This part ASSUMES NO IMAGES
			

			layout.smearHorizontally(1);
			
			layout.smearVertically(0.4);
			
			layout.detectBlobs();
			layout.markWhiteSpace();
			layout.showImage("/home/prashant/Desktop/finalxy.jpeg");
			ArrayList<BufferedImage> segmentList=layout.getSegments();
			//layout.showBlob();
			int i=0;
			for(BufferedImage image:segmentList){
				try {
					File xy_output = new File("/home/prashant/Desktop/Converted/"+i+".jpeg");
					ImageIO.write(image, "jpeg", xy_output);
					i+=1;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			*/
			System.out.print("Hello World");
		}catch(IOException e){
			
			e.printStackTrace();
		}
		
	}
	
}
