import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;


public class OrientationDetection {

	File file;
	ArrayList<Integer>wvalleycount = new ArrayList<Integer>();
	ArrayList<Integer>bvalleycount = new ArrayList<Integer>();
	BufferedImage image;
	int width;
	int threshold =140;
	int height;
	int rows;
	int cols;
	int countblocks=0;
	BufferedImage mini_image;
	int sizeofarray;
	int getimageindex;
	int tempb;
	int[] horizontalprojection = new int[200];
	int[] verticalprojection = new int[200];
	Boolean checktextorientation;
	
	
	
	public void doeverything(){
		
		file = new File("/home/shubhas/Desktop/what.png");
		
		
		try {
			FileInputStream fis = new FileInputStream(file);
			image = ImageIO.read(fis);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		width = image.getWidth();
		height = image.getHeight();
		rows = width/200;
		System.out.println(rows);
		cols = height/200;
		System.out.println(cols);
		int chunks = rows * cols;
		

	

		/*int chunkWidth = image.getWidth()/cols; // determines the chunk width and height
        int chunkHeight = image.getHeight()/rows;*/
		/*int chunkWidth = 200; // determines the chunk width and height
        int chunkHeight = 200;
        int count = 0;
        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                //Initialize the image array with image chunks
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                // draws the image chunk
                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }
        System.out.println("Splitting done");
        //writing mini images into image files
    
        for (int i = 0; i < imgs.length; i++) {
            try {
				ImageIO.write(imgs[i], "png",new File("/home/shubhas/Desktop/demo/img"+i+".png"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }*/
        System.out.println("Mini images created");
		
        // for counting contiguous white and black pixels
        // Here we pass the image to CountPixel function
       /* for(int i=0;i<12;i++){
        	File minifile = new File("/home/shubhas/Desktop/demo/img"+i+".png");
        	try{
        		FileInputStream fist = new FileInputStream(minifile);
        		 mini_image = ImageIO.read(fist);
        	}catch(IOException eq){
        		eq.printStackTrace();
        	}
        	
        	CountPixel(mini_image);
        	countblocks+=1;
        	
        }*/
        System.out.println("Diagonal pixel counting finished.");
		selectImage();
		
	}	// doeverything() ends here
	
	
	public void CountPixel(BufferedImage takeimage){
		
		this.mini_image = takeimage;
		int bcount=0;
		int maxbcount=0;
		int wcount=0;
		int maxwcount=0;
		int mwidth = mini_image.getWidth();
		int mheight = mini_image.getHeight();
		for(int i=0;i<mwidth;i++){
			for(int j=0;j<mheight;j++){
				if(i==j){
				
				int mpixel = mini_image.getRGB(i, j);
				Color mgetcolor = new Color(mpixel);
				if(mgetcolor.getRed()==0){	//blackpixel encounterd
					bcount+=1
					if(bcount>maxbcount){
						maxbcount=bcount;
					}
					wcount=0;
				}
				else{
					wcount+=1;
					if(wcount>maxwcount){
						maxwcount=wcount;
						bcount=0;
					}
				}
				
			}
			}
		}
		wvalleycount.add(maxwcount);
		bvalleycount.add(maxbcount);
		sizeofarray = wvalleycount.size();
		
	}	//CountPixel() ends here.
	
	public void selectImage(){
		System.out.println("Process for selecting Image has Started.");
		ArrayList<Integer> tempwvalleycount = new ArrayList<Integer>(wvalleycount); 
		ArrayList<Integer> tempbvalleycount = new ArrayList<Integer>(bvalleycount); 
		System.out.println(wvalleycount);
		System.out.println(bvalleycount);
		System.out.println("The new Formed arrays are:\n");
		if (sizeofarray == 0)
		    return;
		
		
		for(int i=sizeofarray-1;i>0;i--){
			if(bvalleycount.get(i)>20){
				wvalleycount.remove(i);
				bvalleycount.remove(i);
				sizeofarray = bvalleycount.size();
				
				
			}
		}
		
		for(int i=0;i<sizeofarray;i++){
			if(bvalleycount.get(i)>20){
				wvalleycount.remove(i);
				bvalleycount.remove(i);
				sizeofarray = bvalleycount.size();
				
				
			}
		}
		
		System.out.println(wvalleycount);	// showing the arraylist to check.
		System.out.println(bvalleycount);
		int small = wvalleycount.get(0);
		
		
		
		
		for (int i = 0; i < sizeofarray; i++)
		{
		    if (wvalleycount.get(i) < small)
		    {
		        small = wvalleycount.get(i);
		        tempb = bvalleycount.get(i);
		        getimageindex = i;
		      
		    }
		    
		    
		}
		
		getimageindex=0;
		for(int i=0;i<tempbvalleycount.size();i++){
			if(small == tempwvalleycount.get(i) && tempb == tempbvalleycount.get(i)){
				  getimageindex = i;
				
			}
		}
		
	/*	System.out.println("The new Size of array is :"+sizeofarray);
		System.out.println("The selected ImageIndex is:"+getimageindex);	//gives the actual image index for further processing.
	*/
		
		
		System.out.println("SelectImage() finished.");
		projection();
		
	}	//selectImage() ends here.
	
	public void projection(){
		
		
		System.out.println("The new image index is"+getimageindex);
		File finalfile = new File("/home/shubhas/Desktop/demo/img"+getimageindex+".png");
		try {
			this.mini_image = ImageIO.read(finalfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	int miniwidth = mini_image.getWidth();
		int miniheight = mini_image.getHeight();
		System.out.println(miniwidth);
	
		//vertical projection starts here.
		for(int x=0;x<200;x++){	//vertical projection i.e. you count pixel with respect to x axis.
			int tempcount = 0;
			for(int y=0;y<200;y++){
				 int pixel = mini_image.getRGB(x, y);
				Color temp = new Color(pixel);
				if(temp.getRed()==0){	//black pixel encountered.
					tempcount+=1;
				}
			
				
				
			}
			horizontalprojection[x] = tempcount;
			
		}
		
		
	
		
		
		//vertical projection ends.
		// horizontal projection count starts here
		
		
		// if there are values less than 5 then we assume that it is written horizontally
		// if its not then vertically.
	
		for(int x=0;x<miniheight;x++){	//horizontal projection i.e. you count pixel with respect to x axis.
			int tempcount = 0;
			for(int y=0;y<miniwidth;y++){
				int getp = mini_image.getRGB(y,x);
				Color tempcolor = new Color(getp);
				if(tempcolor.getRed()==0){	//black pixel encountered.
					tempcount+=1;
				}
			
				
				
			}
			verticalprojection[x] = tempcount;
			
		}
		System.out.println("");
		for(int x=0;x<200;x++){
			System.out.println(horizontalprojection[x]);
		}
		// checking if the transition is vertical.
		int vchecktransition=0;
		for(int x=0;x<200;x++){
			
			
			
			if(verticalprojection[x]<=3){
				vchecktransition+=1;
				
			}
			
			
		
	}

		
		//checking if the transition if horizontal
		
		
	int hchecktransition=0;
	for(int x=0;x<200;x++){
	
			if(horizontalprojection[x]<=3){
				hchecktransition+=1;
				
			}
	
		
	}
	
	//Deciding which transition is correct.
	
	if(hchecktransition>vchecktransition){
		checktextorientation=true;
		
	}else{
		checktextorientation=false;
	}
	
		
		
		//horizontal projection ends here.
		
		//to check if there is no of zero valleys i.e. to see if there exist white pixels.
		
		System.out.println("Function Porjection is finished");
		correctorientation();
		
	}	//projection() ends here.
	
	public void correctorientation(){
		
		int a=0;
		 AffineTransform affineTransform = new AffineTransform();
		
		if(checktextorientation==false){	// This means that do a condition for the horizontal orientation ie. up and down.
			
			System.out.println("Image is either at correct position or positioned down.");
			int greatno = verticalprojection[0];
			int getindex=0;
			
			
			for(int x=0;x<200;x++){
				
				if(verticalprojection[x]>greatno){
					greatno = verticalprojection[x];
					getindex = x;
					
				}
			}
			
			//System.out.println("The largest value is "+verticalprojection[getindex]);
			int downcheck=0;
			int upcheck=0;
			for(int x=getindex;x<200;x++){
				if(verticalprojection[x]>8){
					downcheck+=1;
				}else{
					break;
					
				}
					
			}
			System.out.println(downcheck);
			for(int x=getindex;x<200;x--){
				if(x>1){
				if(verticalprojection[x]>8 ||verticalprojection[x]==verticalprojection[getindex]){
					upcheck+=1;
					
				}else{
					break;
				}
				}	
			}
			System.out.println(upcheck);
			if(downcheck>upcheck){
				System.out.println("Nothing happens");
				a=1;
				//Do nothing to image as it is in its correct position.
			}else{
				
				//TODO rotate image 180 degree.
				System.out.println("180 degree rotation.");
				a=2;
			}
			
			
			
			
			
		}else{	// This means just do for the left and the right orientation.
			

			System.out.println("Either image is left rotated or right rotated");
			
			
			int greatno = horizontalprojection[0];
			int getindex=0;
			
			
			for(int x=0;x<200;x++){
				
				if(horizontalprojection[x]>greatno){
					greatno = horizontalprojection[x];
					getindex = x;
					
				}
			}
			
			System.out.println("The largest value is horizontalprojection "+horizontalprojection[getindex]);
			int leftcheck=0;
			int rightcheck=0;
			
			for(int x=getindex;x<200;x++){
				if(horizontalprojection[x]>8){
					rightcheck+=1;
				}else{
					break;
					
				}
					
			}
			System.out.println("rightcheck"+rightcheck);
			
			for(int x=getindex;x<200;x--){
				if(x>1){
				if(horizontalprojection[x]>8 ||horizontalprojection[x]==horizontalprojection[getindex]){
					leftcheck+=1;
					
				}else{
					break;
				}
				}	
			}
			System.out.println("leftcheck"+leftcheck);
			if(rightcheck>leftcheck){
				System.out.println("90 degree rotation");
				//Rotate image by 90 degree
				a=3;
			}else{
				System.out.println("270 degree rotation");

				//TODO rotate image 270 degree.
				a=4;
			}
			
			
			
			
	
			
		}

		switch(a){
		case 1:
			 break;
		case 2:
			affineTransform.translate(width, height);
        	affineTransform.rotate(Math.PI);
        break;
		case 3:
			 affineTransform.translate(height, 0);
			 affineTransform.rotate( Math.PI / 2);
       break;
		case 4:
			 affineTransform.translate(0, width);
        affineTransform.rotate(3 * Math.PI / 2);
        break;
       
		}
		
		
		
		
		 AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);  
	        BufferedImage destinationImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
	        destinationImage = affineTransformOp.filter(image, null);
	        try {
				ImageIO.write(destinationImage, "png", new File("/home/shubhas/Desktop/what1.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
		
		
		
		
		System.out.println(checktextorientation);
	}	//correctorientation() ends here.

	
	
}		//main class ends here.




