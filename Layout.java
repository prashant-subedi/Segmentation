import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
		if (image == null) {
			System.exit(1);
		}
		// Save Original Image for Later Use
		this.original_image = image;

		// For Some Reason The Blob Finder doesn't seem to detect edges
		// So I am adding white padding..for this
		// Create an image with size greater than original by padding 0f 1 pixel
		/*
		 * this.image=new
		 * BufferedImage(image.getWidth()+2,image.getHeight()+2,BufferedImage
		 * .TYPE_3BYTE_BGR); Graphics graphics=this.image.getGraphics();
		 * graphics.setColor(Color.white); graphics.fillRect(0, 0,
		 * image.getWidth(), image.getHeight()); //Draw the original image in
		 * the new Image graphics.drawImage(image,1,1,null); //So we have a
		 * padded image
		 */

		this.image = copyImage(this.original_image);
		// Copy the image;

		// this.image.getSubimage(1, 1,
		// image.getWidth()-1,image.getHeight()-1)=copyImage(image);

	}

	private BufferedImage copyImage(BufferedImage src_image) {
		return new BufferedImage(src_image.getColorModel(),
				src_image.copyData(null), src_image.isAlphaPremultiplied(),
				null);
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
		/*
		 * System.out.printf(
		 * "Loaded image: width: %d, height: %d, num bytes: %d\n", width,
		 * height, srcData.length);
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
		// Count Pixel
		for (BlobFinder.Blob blob : blobList)
			countPixel(blob);

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
					newColor = new Color(0, 0, 0, 0);

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
						Color c1 = new Color(image.getRGB(i, j + distance));
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

	public void removeBlobs(double fraction) {
		int blobHeight=(int)(fraction*image.getHeight());
		ArrayList<BlobFinder.Blob> toRemoveList = new ArrayList();
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
				continue;
			}
			
		}
		for (BlobFinder.Blob blob : toRemoveList)
			blobList.remove(blob);
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
		for (BlobFinder.Blob blob : blobList) {
			int x = blob.xMin, y;
			while (x <= blob.xMax) {
				y = blob.yMin;
				while (y <= blob.yMax) {
					image.setRGB(x, y, 0x00000000);
					y += 1;
				}
				x += 1;
			}
		}

	}

	public void smearHorizontally(double fraction) {
		for (BlobFinder.Blob blob : blobList) {
			int x = blob.xMax, y;
			int xMax = blob.xMax
					+ (int) Math.ceil((blob.yMax - blob.yMin) * fraction);
			if (xMax >= image.getWidth() - 1) {
				continue;
			}
			while (x <= xMax) {
				y = blob.yMin;
				while (y <= blob.yMax) {
					image.setRGB(x, y, 0x00000000);
					y += 1;
				}
				x += 1;
			}
		}
	}

	public void smearVertically(double fraction) {
		for (BlobFinder.Blob blob : blobList) {
			int y = blob.yMin, x;
			int yMax = blob.yMin
					- (int) Math.ceil((blob.yMax - blob.yMin) * fraction);
			if (yMax <= 0) {
				continue;
			}

			while (y >= yMax) {
				x = blob.xMin;
				while (x <= blob.xMax) {
					image.setRGB(x, y, 0x00000000);
					x += 1;
				}
				y -= 1;
			}
		}
	}

	public ArrayList<BufferedImage> getSegments() {
		ArrayList<BufferedImage> segments = new ArrayList();
		for (BlobFinder.Blob blob : blobList) {
			segments.add(original_image.getSubimage(blob.xMin, blob.yMin,
					blob.xMax - blob.xMin + 1, blob.yMax - blob.yMin + 1));
		}
		return segments;
	}

	public void keepEdgesOnly() {
		int height = image.getHeight();
		int width = image.getWidth();
		for (int x = 0; x < width; x++) {
			boolean b = true;
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x == image.getWidth() - 1
						|| y == image.getHeight() - 1) {

				} else {
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
				if (b == false) {
					image.setRGB(x, y, 0xFFFFFFFF);
				}
			}
		}
	}

	public void removeImages() {
		int offset = 5;
		ArrayList<BlobFinder.Blob> toRemoveList = new ArrayList();
		for (BlobFinder.Blob blob : blobList) {
			if (blob.mass == 1) {
				continue;
			}
			int black_count = 0;
			int height = blob.yMax - blob.yMin + 1;
			int width = blob.xMax - blob.xMin + 1;
			float avg_corr = 0;
			if ((float) blob.mass / (height * width) > .75) {
				int x = blob.xMin, y;
				while (x <= blob.xMax) {
					y = blob.yMin;
					while (y <= blob.yMax) {
						image.setRGB(x, y, 0xFFFFFFFF);
						y += 1;
					}
					x += 1;
				}
				//toRemoveList.add(blob);
				continue;
			}
			if(isText(blob)==false){
				toRemoveList.add(blob);
			}
			
		}
		for (BlobFinder.Blob blob : toRemoveList) {
			blobList.remove(blob);
		}

	}

	public void countPixel(BlobFinder.Blob blob) {
		BufferedImage takeimage = image.getSubimage(blob.xMin, blob.yMin,
				blob.xMax - blob.xMin+1, blob.yMax - blob.yMin+1);
		int bCount = 0;
		int maxBcount = 0;
		int wCount = 0;
		int maxWcount = 0;
		int mWidth = takeimage.getWidth();
		int mHeight = takeimage.getHeight();
		int diagonal_length = mWidth > mHeight ? mHeight : mWidth;
		for (int i = 0; i < diagonal_length; i++) {
			int mpixel = takeimage.getRGB(i, i);
			Color mgetcolor = new Color(mpixel);
			if (mgetcolor.getRed() == 0) { // blackpixel encounterd
				bCount += 1;
				if (bCount > maxBcount) {
					maxBcount = bCount;
				}
				wCount = 0;
			} else {
				wCount += 1;
				if (wCount > maxWcount) {
					maxWcount = wCount;
					bCount = 0;
				}
			}

		}
		blob.white_diagonal = maxWcount;
		blob.black_diagonal = maxBcount;
	} // CountPixel() ends here.

	public boolean isText(BlobFinder.Blob blob) {
		BufferedImage takeimage = image.getSubimage(blob.xMin, blob.yMin,
				blob.xMax - blob.xMin, blob.yMax - blob.yMin);
		System.out.println("The new Formed arrays are:\n");
		if (blob.black_diagonal > (blob.xMax - blob.xMin) / 10) {
			return false;
		}
		return true;
	}

	public void projection() {
		BlobFinder.Blob bestBlob = null;
		for (BlobFinder.Blob blob : blobList) {
			if (bestBlob == null)
				bestBlob = blob;
			else {
				if (bestBlob.white_diagonal < blob.white_diagonal) {
					if(blob.mass>10){continue;}
					bestBlob = blob;
				}
			}
		}
		
		BufferedImage mini_image = original_image.getSubimage(bestBlob.xMin,
				bestBlob.yMin, bestBlob.xMax - bestBlob.xMin+1, bestBlob.yMax
						- bestBlob.yMin+1);
				
		try {
			File xy_output = new File("A.jpeg");
			ImageIO.write(mini_image, "jpeg", xy_output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int miniwidth = mini_image.getWidth();
		int miniheight = mini_image.getHeight();
		System.out.println(miniwidth);
		int[] horizontalprojection = new int[mini_image.getWidth()];
		int[] verticalprojection = new int[mini_image.getHeight()];
		// vertical projection starts here.
		for (int x = 0; x < mini_image.getWidth(); x++) { // vertical projection
															// i.e. you count
			// pixel with respect to x axis.
			int tempcount = 0;
			for (int y = 0; y < mini_image.getHeight(); y++) {
				int pixel = mini_image.getRGB(x, y);
				Color temp = new Color(pixel);
				if (temp.getRed() == 0) { // black pixel encountered.
					tempcount += 1;
				}
			}
			horizontalprojection[x] = tempcount;
		}

		// vertical projection ends.
		// horizontal projection count starts here

		// if there are values less than 5 then we assume that it is written
		// horizontally
		// if its not then vertically.

		for (int x = 0; x < miniheight; x++) { // horizontal projection i.e. you
			// count pixel with respect to x
			// axis.
			int tempcount = 0;
			for (int y = 0; y < miniwidth; y++) {
				int getp = mini_image.getRGB(y, x);
				Color tempcolor = new Color(getp);
				if (tempcolor.getRed() == 0) { // black pixel encountered.
					tempcount += 1;
				}

			}
			verticalprojection[x] = tempcount;

		}
		// checking if the transition is vertical.
		int vchecktransition = 0;
		for (int x = 0; x < verticalprojection.length; x++) {
			if (verticalprojection[x] <= 3) {
				vchecktransition += 1;
			}
		}
		// checking if the transition if horizontal
		int hchecktransition = 0;
		for (int x = 0; x < horizontalprojection.length; x++) {

			if (horizontalprojection[x] <= 3) {
				hchecktransition += 1;
			}
		}

		// Deciding which transition is correct.
		boolean checktextorientation = false;

		if (hchecktransition > vchecktransition) {
			checktextorientation = true;
		}
		
		int a = 0;
		AffineTransform affineTransform = new AffineTransform();
		if (checktextorientation == false) { // This means that do a condition
			// for the horizontal
			// orientation ie. up and down.

			System.out.println("Image is either at correct position or positioned down.");
			int greatno = verticalprojection[0];
			int getindex = 0;
			for (int x = 0; x < verticalprojection.length; x++) {
				if (verticalprojection[x] > greatno) {
					greatno = verticalprojection[x];
					getindex = x;
				}
			}
			// System.out.println("The largest value is "+verticalprojection[getindex]);
			int downcheck = 0;
			int upcheck = 0;
			for (int x = getindex; x < 200; x++) {
				if (verticalprojection[x] > 8) {
					downcheck += 1;
				} else {
					break;

				}

			}
			System.out.println(downcheck);
			for (int x = getindex; x < 200; x--) {
				if (x > 1) {
					if (verticalprojection[x] > 8
							|| verticalprojection[x] == verticalprojection[getindex]) {
						upcheck += 1;

					} else {
						break;
					}
				}
			}
			System.out.println(upcheck);
			if (downcheck > upcheck) {
				System.out.println("Nothing happens");
				a = 1;
				// Do nothing to image as it is in its correct position.
			} else {

				// TODO rotate image 180 degree.
				System.out.println("180 degree rotation.");
				a = 2;
			}

		} else { // This means just do for the left and the right orientation.

			System.out.println("Either image is left rotated or right rotated");

			int greatno = horizontalprojection[0];
			int getindex = 0;

			for (int x = 0; x < 200; x++) {

				if (horizontalprojection[x] > greatno) {
					greatno = horizontalprojection[x];
					getindex = x;

				}
			}

			System.out.println("The largest value is horizontalprojection "
					+ horizontalprojection[getindex]);
			int leftcheck = 0;
			int rightcheck = 0;

			for (int x = getindex; x < 200; x++) {
				if (horizontalprojection[x] > 8) {
					rightcheck += 1;
				} else {
					break;

				}

			}
			System.out.println("rightcheck" + rightcheck);

			for (int x = getindex; x < 200; x--) {
				if (x > 1) {
					if (horizontalprojection[x] > 8
							|| horizontalprojection[x] == horizontalprojection[getindex]) {
						leftcheck += 1;

					} else {
						break;
					}
				}
			}
			System.out.println("leftcheck" + leftcheck);
			if (rightcheck > leftcheck) {
				System.out.println("90 degree rotation");
				// Rotate image by 90 degree
				a = 3;
			} else {
				System.out.println("270 degree rotation");

				// TODO rotate image 270 degree.
				a = 4;
			}

		}

		switch (a) {
		case 1:
			break;
		case 2:
			affineTransform.translate(image.getWidth(), image.getHeight());
			affineTransform.rotate(Math.PI);
			break;
		case 3:
			affineTransform.translate(image.getHeight(), 0);
			affineTransform.rotate(Math.PI / 2);
			break;
		case 4:
			affineTransform.translate(0, image.getWidth());
			affineTransform.rotate(3 * Math.PI / 2);
			break;

		}

		AffineTransformOp affineTransformOp = new AffineTransformOp(
				affineTransform, AffineTransformOp.TYPE_BILINEAR);
		
		BufferedImage destinationImage = new BufferedImage(image.getWidth(),
				image.getHeight(), image.getType());
		
		 affineTransformOp.filter(image, null);
		
		System.out.println(checktextorientation);
	}

}
