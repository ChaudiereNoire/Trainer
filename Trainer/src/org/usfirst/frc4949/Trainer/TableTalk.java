package org.usfirst.frc4949.Trainer;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class TableTalk {
	NetworkTable table;
	
	public void initializeTables() {
		table = NetworkTable.getTable("/vision/gear_hanger");
		double[] defaultValue = new double[0];
		table.putNumberArray("x",defaultValue);
		table.putNumberArray("y",defaultValue);
		table.putNumberArray("height",defaultValue);
		table.putNumberArray("width",defaultValue);
		table.putNumber("pan", 0);
		table.putNumber("tilt", 0);
		table.putBoolean("runPipe", true);
		table.putNumber("servo1_aim", 0);
		table.putNumber("servo2_aim", 0);

	}
	
	public void readTable() {
		double[] defaultValue = new double[0];
		double[] xValues = table.getNumberArray("x",defaultValue);
		double[] yValues = table.getNumberArray("y",defaultValue);
		double[] hValues = table.getNumberArray("height",defaultValue);
		double[] wValues = table.getNumberArray("width",defaultValue);
		double panAngle = table.getNumber("pan", 0);
		double tiltAngle = table.getNumber("tilt", 0);
		/*
		 * Find the number of objects returned and see if you can figure
		 * out which, if any, of the targets are represented.
		 */
		if (xValues.length > 2) {
			
		}
	}
	
	/*
	 * Command files on the Raspberry Pi.
	 * /home/pi/StartScan.sh
	 *   Kicks off the python implementation of GRIP
	 * /home/pi/vision/start_mjpg_streamer.sh
	 *   Kicks off mpeg stream on port 1180 
	 * /home/pi/vision/start_grip.sh
	 *   Kicks off the GRIP implementation on the Pi
	 * /home/pi/vision/start_vision.sh
	 *   Kicks off the entire stream, mpeg streamer and GRIP
	 */
	public void startScan() {
		enableScan();		
        Robot.piShell.executeFile("./StartScan.sh");		
	}
	
	public void startStream() {
        Robot.piShell.executeFile("./vision/start_mjpg_streamer.sh");				
	}
	
	public void startGRIP() {
        Robot.piShell.executeFile("./vision/start_grip.sh");				
	}
	
	public void start_vision() {
        Robot.piShell.executeFile("./vision/start_vision.sh");				
	}
	
	/*
	 * The pan and tilt mechanism on the Raspberry Pi can only go from 90
	 * degrees to -90 degrees.  The values do not need to be limited before 
	 * being added to the tables because they are bounded on the receiving 
	 * end. 
	 */
	public void setTilt(double tiltValue) {		
		table.putNumber("servo1_aim", tiltValue);
	}
	
	public void setPan(double panValue) {
		table.putNumber("servo2_aim", panValue);
	}
	
	/*
	 * The Raspberry Pi scan program runs as long as the Network
	 * Table variable runPipe is true.  Enable the variable before calling
	 * the routine to start the scan.
	 */
	public void enableScan()  {
		table.putBoolean("runPipe", true);
	}

	public void killScan()  {
		table.putBoolean("runPipe", false);
	}

}