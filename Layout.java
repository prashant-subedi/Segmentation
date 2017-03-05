import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Layout {
	private BufferedImage original_image, image;
	private ArrayList blobs;

	ArrayList<BlobFinder.Blob> blobList;

	// Get The Image
	public Layout(BufferedImage image) {
		if (image==null){
			System.exit(1);
		}
		//Save Original Image for Later Use
		this.original_image = image;

		//For Some Reason The Blob Finder doesn't seem to detect edges
		//So I am adding white padding..for this
		//Create an image with size greater than original by padding 0f 1 pixel
		/*this.image=new BufferedImage(image.getWidth()+2,image.getHeight()+2,BufferedImage.TYPE_3BYTE_BGR);
		Graphics graphics=this.image.getGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		//Draw the original image in the new Image
		graphics.drawImage(image,1,1,null);
		//So we have a padded image
		 */

		this.image=copyImage(this.original_image);
		// Copy the image;

		//this.image.getSubimage(1, 1, image.getWidth()-1,image.getHeight()-1)=copyImage(image);

	}

	private BufferedImage copyImage(BufferedImage src_image) {
		return new BufferedImage(src_image.getColorModel(),
				src_image.copyData(null),
				src_image.isAlphaPremultiplied(), null);
	}

	public void detectBlobs() {
		// If global BufferedImage is not set, return
		if (image == null) {
			System.out.println("The Image is not set");
			return;
		}
		// Get source image
		BufferedImage srcImage = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		srcImage.getGraphics().drawImage(image, 0, 0, null);
		// Get image width and height
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		// Get raw image data
		Raster raster = srcImage.getData();
		DataBuffer buffer = raster.getDataBuffer();

		int type = buffer.getDataType();
		if (type != DataBuffer.TYPE_BYTE) {
			System.err.println("Wrong image data type");
		}
		if (buffer.getNumBanks() != 1) {
			System.err.println("Wrong image data format");
		}

		DataBufferByte byteBuffer = (DataBufferByte) buffer;
		byte[] srcData = byteBuffer.getData(0);

		// Sanity check image
		if (width * height * 3 != srcData.length) {
			System.err
			.println("Unexpected image data size. Should be RGB image");
		}

		// Output Image info
		/*System.out.printf(
				"Loaded image: width: %d, height: %d, num bytes: %d\n", width,
				height, srcData.length);
		*/
		// Create Monochrome version - using basic threshold technique
		byte[] monoData = new byte[width * height];
		int srcPtr = 0;
		int monoPtr = 0;

		while (srcPtr < srcData.length) {
			int val = ((srcData[srcPtr] & 0xFF) + (srcData[srcPtr + 1] & 0xFF) + (srcData[srcPtr + 2] & 0xFF)) / 3;
			monoData[monoPtr] = (val > 128) ? (byte) 0xFF : 0;

			srcPtr += 3;
			monoPtr += 1;
		}

		byte[] dstData = new byte[srcData.length];

		// Create Blob Finder
		BlobFinder finder = new BlobFinder(width, height);

		ArrayList<BlobFinder.Blob> blobList = new ArrayList();
		finder.detectBlobs(monoData, dstData, 0, -1, (byte) 0, blobList);
		this.blobList = blobList;
		//for(BlobFinder.Blob blob:blobList) System.out.println(blob);

	}

	public void markBlobs() {
		// Not Used
		// A Handy Method that I wrote to dispaly the Blobs in the Image
		for (BlobFinder.Blob blob : blobList) {
			int x = blob.xMin, y = blob.yMin;
			while (x <= blob.xMax) {
				image.setRGB(x, blob.yMin, 0x00000000);
				image.setRGB(x, blob.yMax, 0x00000000);
				x += 1;
			}
			while (y <= blob.yMax) {
				image.setRGB(blob.xMin, y, 0x00000000);
				image.setRGB(blob.xMax, y, 0x00000000);
				y += 1;
			}
		}

	}

	public void makeBinary(int threshold) {
		int height = image.getHeight();
		int width = image.getWidth();
		for (int i = 0; i < height; i++) {

			for (int j = 0; j < width; j++) {

				Color c = new Color(image.getRGB(j, i));
				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);
				int sum = red + green + blue;
				Color newColor;
				if (sum > threshold) {
					newColor = new Color(255, 255, 255);

				} else {
					newColor = new Color(0,0, 0, 0);

				}

				image.setRGB(j, i, newColor.getRGB());
			}
		}
	}

	public void smearHorizontallySelectively(int run_length) {
		int height = image.getHeight();
		int width = image.getWidth();
		for (int i = 0; i < height; i++) {
			int distance;
			Color cPrev = new Color(0xFFFFFFFF);
			for (int j = 0; j < width; j++) {
				Color c = new Color(image.getRGB(j, i));

				if (c.getRed() != 0 && cPrev.getRed() == 0) {
					distance = 1;
					while (distance <= run_length && j + distance < width) {
						Color c1 = new Color(image.getRGB(j + distance, i));
						if (c1.getRed() == 0) {
							// TODO improve efficiency by avoiding repeation
							image.setRGB(j, i, 0x00000000);
							cPrev = new Color(0x00000000);
							break;
						}
						cPrev = c;
						distance += 1;
					}
				} else {
					cPrev = c;
				}
			}
		}
	}
	public void smearVerticallySelectively(int run_length) {
		int height = image.getHeight();
		int width = image.getWidth();
		for (int i = 0; i < width; i++) {
			int distance;
			Color cPrev = new Color(0xFFFFFFFF);
			for (int j = 0; j < height; j++) {
				Color c = new Color(image.getRGB(i, j));

				if (c.getRed() != 0 && cPrev.getRed() == 0) {
					distance = 1;
					while (distance <= run_length && j + distance < height) {
						Color c1 = new Color(image.getRGB(i , j+ distance));
						if (c1.getRed() == 0) {
							// TODO improve efficiency by avoiding repeation
							image.setRGB(i, j, 0x00000000);
							cPrev = new Color(0x00000000);
							break;
						}
						cPrev = c;
						distance += 1;
					}
				} else {
					cPrev = c;
				}
			}
		}
	}

	public void removeBlobs(int blobHeight) {
		ArrayList<BlobFinder.Blob> toRemoveList=new ArrayList();
		for (BlobFinder.Blob blob : blobList) {
			if (blob.yMax - blob.yMin > blobHeight) {
				int x = blob.xMin, y;
				while (x <= blob.xMax) {
					y = blob.yMin;
					while (y <= blob.yMax) {
						image.setRGB(x, y, 0xFFFFFFFF);
						y += 1;
					}
					x += 1;
				}
				toRemoveList.add(blob);
			}
		}
		for(BlobFinder.Blob blob:toRemoveList)	blobList.remove(blob);
	}

	public void showImage(String fileName) {
		try {
			File xy_output = new File(fileName);
			ImageIO.write(image, "jpeg", xy_output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void markWhiteSpace() {
		for(BlobFinder.Blob blob : blobList) {
			int x = blob.xMin, y;
			while (x <= blob.xMax) {
				y = blob.yMin;
				while (y <=blob.yMax) {
					image.setRGB(x, y, 0x00000000);
					y += 1;
				}
				x += 1;
			}
		}
	

	}
	public void smearHorizontally(double fraction){
		for(BlobFinder.Blob blob:blobList){
			int x=blob.xMax,y;
			int xMax = blob.xMax+(int)Math.ceil((blob.yMax-blob.yMin)*fraction);
			if(xMax>=image.getWidth()-1){
				continue;
			}
			while(x<=xMax){
				y=blob.yMin;
				while(y<=blob.yMax){
					image.setRGB(x, y, 0x00000000);
					y+=1;
				}
				x+=1;
			}
		}
	}
	public void smearVertically(double fraction){
		for(BlobFinder.Blob blob:blobList){
			int y = blob.yMin, x;
			int yMax=blob.yMin-(int)Math.ceil((blob.yMax-blob.yMin)*fraction);
			if(yMax <= 0){
				continue;
			}
			
			while (y>=yMax) {
				x = blob.xMin;
				while (x <= blob.xMax) {
					image.setRGB(x, y, 0x00000000);
					x += 1;
				}
				y -=1;
			}
		}
	}
	public ArrayList<BufferedImage> getSegments(){
		ArrayList<BufferedImage> segments=new ArrayList();
		for(BlobFinder.Blob blob:blobList){
			segments.add(original_image.getSubimage(blob.xMin, blob.yMin, blob.xMax-blob.xMin+1, blob.yMax-blob.yMin+1));
		}
		return segments;
	}   
	public void keepEdgesOnly(){
		int height=image.getHeight();
		int width=image.getWidth();
		for(int x=0;x<width;x++){
			boolean b = true;
			for(int y=0;y<height;y++){
				if (x == 0 || y == 0 || x == image.getWidth() - 1 || y == image.getHeight() - 1) {

				}
				else{
					b = new Color(image.getRGB(x - 1, y - 1)).getRed() == 255;
					b = new Color(image.getRGB(x - 1, y)).getRed() == 255;
					b = new Color(image.getRGB(x - 1, y + 1)).getRed() == 255;
					b = new Color(image.getRGB(x, y - 1)).getRed() == 255;
					b = new Color(image.getRGB(x, y)).getRed() == 255;
					b = new Color(image.getRGB(x, y + 1)).getRed() == 255;
					b = new Color(image.getRGB(x + 1, y - 1)).getRed() == 255;
					b = new Color(image.getRGB(x + 1, y)).getRed() == 255;
					b = new Color(image.getRGB(x + 1, y + 1)).getRed() == 255;
				}
				if(b==false){
					image.setRGB(x, y, 0xFFFFFFFF);
				}
			}
		}
	}
	public void removeImages(){
		int offset=5;
		ArrayList<BlobFinder.Blob> toRemoveList=new ArrayList();
		for(BlobFinder.Blob blob:blobList){
			if(blob.mass==1){
				continue;
			}
			System.out.print(blob);
			int black_count=0;
			int height=blob.yMax-blob.yMin+1;
			int width=blob.xMax-blob.xMin+1;
			float[] coorelation=new float[width];
			for(int i=0;i<width;i++){
				boolean b=false;
				if(i<width-offset+1){
					b=true;
				}
				for(int j=0;j<height;j++){
					if(b==true){
						if(image.getRGB(i,j)!=image.getRGB(i+offset,j)) {coorelation[i]-=1;}
					}
					if(new Color(image.getRGB(i, j)).getRed()==255){
						black_count+=1;
					}
				}
				if(b==true){
					coorelation[i]=	2*coorelation[i]/width+1;
					//System.out.println(coorelation[i]);
				}
			}
			float avg_corr = 0;
			for(float cor:coorelation){
				avg_corr+=cor;
			}
			avg_corr/=(width-offset);
			System.out.println();
			
			if((float)blob.mass/(height*width)>.75){//||avg_corr>0.9){
				int x = blob.xMin, y;
				while (x <= blob.xMax) {
					y = blob.yMin;
					while (y <= blob.yMax) {
						image.setRGB(x, y, 0xFFFFFFFF);
						y += 1;
					}
					x += 1;
				}
				toRemoveList.add(blob);
			}
		}
		for(BlobFinder.Blob blob:toRemoveList){
			blobList.remove(blob);
		}
			
		
		
	}

}
