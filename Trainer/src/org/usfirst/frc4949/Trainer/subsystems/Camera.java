// RobotBuilder Version: 2.0
//
/* The camera code runs continuously, updating the variable that points
 * to the current location of the target.  It is expected that the program
 * will likely pick up this data for course correction, but actually drive
 * to the target using a routine to steer to a target angle using a faster
 * device, such as a Gyro.
 * 
 * https://wpilib.screenstepslive.com/s/4485/m/24194/l/674733-using-generated-code-in-a-robot-program
 * 
 */

package org.usfirst.frc4949.Trainer.subsystems;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.usfirst.frc4949.Trainer.Logging;
import org.usfirst.frc4949.Trainer.VideoPipeline;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.vision.VisionThread;

/**
 *
 */
public class Camera extends Subsystem {

    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTANTS

    private static final int IMG_WIDTH = 320;
    private static final int IMG_HEIGHT = 240;

  	private VisionThread visionThread;
    private double centerX = 0.0;
    private double centerY = 0.0;
    private double centerX1 = 0.0;
    private double centerY1 = 0.0;
    private double centerX2 = 0.0;
    private double centerY2 = 0.0;
    private double height1 = 0.0;
    private double width1 = 0.0;
    private double height2 = 0.0;
    private double width2 = 0.0;
    private int badframes = 0;
    private int framecount = 0;
    private boolean processingEnabled = false;
    private final Object imgLock = new Object();

    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS


    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void initDefaultCommand() {
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND


    	// END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND

        // Set the default command for a subsystem here.
        // setDefaultCommand(new MySpecialCommand());
    }


    /*
     * The module VideoPipeline.java was created using GRIP against the images
     * provided by WPI and then repointed to use the USB Camera as a source.  It
     * was exported with the option to implement the WPILIB Vision Pipeline.
     * 
     * This thread produces an output for each frame that is captured.
     * 
     */

    public void initThreadToScanEnvironment() {
		Logging.consoleLog("Initializing Thread to Scan Envionment.");
 	
    	UsbCamera cam0 = CameraServer.getInstance().startAutomaticCapture();
    	cam0.setResolution(IMG_WIDTH, IMG_HEIGHT);
    	cam0.setFPS(7);
    	
    	Logging.consoleLog("Next line creates the vision thread.");

    	visionThread = new VisionThread(cam0, 
    			new VideoPipeline(), 
    			pipeline -> {
    				if (processingEnabled) {
	    				Logging.consoleLog("Starting my part of Pipeline.");
	
			        	/*
			        	 * What we really, really want to do here is set up a cross hair, in
			        	 * bright green, centered on the spot where the target is located, so
			        	 * that the driver can judge how close she is to achieving her 
			        	 * goals.  To do this, we have to compute centerX and centerY and
			        	 * then use Imgproc.line() to draw the cross hairs in the middle of
			        	 * the bounding rectangle.
			        	 * 
			        	 * Horizontal line: (0, r.y + (r.height / 2)) 
			        	 *                  (IMG_WIDTH,r.y + (r.height / 2))
			        	 * Vertical line: (r.x + (r.width / 2), 0) 
			        	 *                (r.x + (r.width / 2),IMG_HEIGHT)
			        	 *                
			        	 * We are going to be reusing frame to pass a modified version of the
			        	 * image along to the screen for easier tracking.
			        	 * 
			        	 */
	    				if (!pipeline.filterContoursOutput().isEmpty()) {
	    					
	    					// Check to see how many contours were returned
	    					int numContours = pipeline.filterContoursOutput().size();
	
	    					//get the contours from the vision algorithm
	    					Rect r1 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
							Rect r2 = r1;
	    					if (numContours > 1) {
	        					r2 = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
	       					}
	    					
	    		            synchronized (imgLock) {
		    					//sort the rectangles horizontally
		    					Rect rectLeft = (r1.x<r2.x) ? r1 : r2;
		    					Rect rectRight = (r1.x>r2.x) ? r1 : r2;		
		    					r1 = rectRight;
		    					r2 = rectLeft;
								
		    					//calculate center X and center Y pixels
		    					centerX1 = r1.x + (r1.width/2);
		    					centerY1 = r1.y + (r1.height/2);
		    					centerX2 = r2.x + (r2.width/2);
		    					centerY2 = r2.y + (r2.height/2);
								
		    					centerY = (centerY1 + centerY2)/2;
		    					centerX = (centerX1 + centerX2)/2;
		    					
		    					height1 = r1.height;
		    					width1 = r1.width;
		    					
		    					height2 = r2.height;
		    					width2 = r2.width;
		    					
	    		            }
	    					framecount += 1;
	    				} else {
	    					badframes += 1;
	    				}
	    				Logging.consoleLog("End of Pipeline.");
	
	    				// Timer.delay(0.02);
    				}

    			});	
			visionThread.start();
			Logging.consoleLog("visionThread started.");
		}
    	
	/**
	 * In the sample, the center of the target in the X axis is used immediately
	 * to steer the robot on the line to the target.  In the original code, it was
	 * written up in the main robot sequence.  Here, the code for Scan the Environment
	 * is placed into a separate subsystem and the exposed values are expected to be
	 * invoked as part of a separate command that is called sequentially in a command 
	 * group.
	 * 
	 * The value centerX is the center of the rectangle.  Our camera is centered on the 
	 * front of the vehicle, so the center of the image is the y axis of the vehicle.
	 * By calculating how far the center of the rectangle is from the center of the 
	 * image we derive the turning angle that the vehicle has to take to approach more
	 * closely to the target.
	 * 
	 * Since the camera is fixed, there is a direct relationship between the height of
	 * the target in pixels and the distance to the target.
	 * 
	 * Note that the way this routine is coded, it always returns the last known position of the 
	 * target.  It does not start the image processing over again.
	 * 
	 * @return
	 */
	public double getTurn() {
	    double centerX;
	    double centerY;
	    double centerX1;
	    double centerY1;
	    double centerX2;
	    double centerY2;
	    double height1;
	    double width1;
	    double height2;
	    double width2;

	    synchronized (imgLock) {
	        centerX = this.centerX;
	        centerY = this.centerY;
	        centerX1 = this.centerX1;
	        centerY1 = this.centerY1;
	        centerX2 = this.centerX2;
	        centerY2 = this.centerY2;
	        width1 = this.width1;
	        height1 = this.height1;
	        width2 = this.width2;
	        height2 = this.height2;
	    }

		Logging.consoleLog("Framecount,%d,%d,%.2f,%.2f, "
				+ "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f %.2f,%.2f", 
				framecount, badframes,centerX,centerY,
				centerX1, centerY1, width1, height1,
				centerX2, centerY2, width2, height2);

		double turn = centerX - (IMG_WIDTH / 2);
		return turn;
	}
	
	/*
	 * The playing field is 1,646 cm long and 823 cm wide.  The closest 
	 * we can measure the height of the target it will be exactly 240
	 * pixels in height.  Seven feet, or 213 cm, resulted in a height of 
	 * 23 pixels.
	 */
	public double getDistance() {
	    double centerX;
	    double centerY;
	    double centerX1;
	    double centerY1;
	    double centerX2;
	    double centerY2;
	    double height1;
	    double width1;
	    double height2;
	    double width2;

	    synchronized (imgLock) {
	        centerX = this.centerX;
	        centerY = this.centerY;
	        centerX1 = this.centerX1;
	        centerY1 = this.centerY1;
	        centerX2 = this.centerX2;
	        centerY2 = this.centerY2;
	        width1 = this.width1;
	        height1 = this.height1;
	        width2 = this.width2;
	        height2 = this.height2;
	    }

		Logging.consoleLog("Framecount,%d,%d,%.2f,%.2f, "
				+ "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f %.2f,%.2f", 
				framecount, badframes,centerX,centerY,
				centerX1, centerY1, width1, height1,
				centerX2, centerY2, width2, height2);

		double distance = (height1 + height2)/2;
		return this.height1;
	}

	public double getCenterY() {
		return this.centerY;
	}

	public double getCenterX() {
		return this.centerX;
	}

	public void disableProcessing() {
		processingEnabled = false;
	}

	public void enableProcessing() {
		processingEnabled = true;
	}
	
	public int getFrameCount() {
		return this.framecount;
	}
}
