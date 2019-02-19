/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * Navigation.java
 * This class focuses on the travel of the device as well as its rotation
 * It is greatly inspired by the one used in lab 3
 */


package ca.mcgill.ecse211.lab5;


import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor){
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	//Constants
	private static final int FWD_SPEED = 100;
	private static final int ROTATION_SPEED = 50;
	private static final double WHEEL_R = Lab4.WHEEL_RADIUS; // Wheel radius
	private static final double TRACK = Lab4.TRACK; // Wheel base
	private static final double PI = Math.PI;
	
	private static boolean navigating = false;

	
	/**
	 * Travels to waypoint
	 */
	public void travelTo(double x, double y) {
		
		//Reset motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(3000);
		}
		
		navigating = true;
		
		// We compute the turn angle
		double dX = x - odometer.getX(); //remaining x distance
		double dY = y - odometer.getY(); //remaining y distance
		double turn_angle = Math.atan2(dX, dY);
		
		// We rotate
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		turnTo(turn_angle);
		
		double distance = Math.hypot(dX, dY);
		
		// We move to waypoint
		leftMotor.setSpeed(FWD_SPEED);
		rightMotor.setSpeed(FWD_SPEED);
		leftMotor.rotate(convertDistance(distance),true);
		rightMotor.rotate(convertDistance(distance),false);
	}
	
	/** 
	 * Takes the new heading as input and make the robot turn to it
	 * 
	 * @param: double theta that represents an angle in radians
	 */
	public void turnTo(double theta) {
		
		double angle = getMinAngle(theta-odometer.getTheta());
		
		leftMotor.rotate(convertAngle(angle),true);
		rightMotor.rotate(-convertAngle(angle),false);
	}
	
	/**
	 * Gets the smallest value (between 180 and -180) of an angle
	 */
	public double getMinAngle(double angle){
		if (angle > PI) {  //Pi = 180 degrees
			angle -= 2*PI; 
		} else if (angle < -PI) {
			angle = angle + 2*PI;
		}
		return angle;
	}
	
	/** 
	 * Returns the status of the device (navigating or not)
	 */
	public boolean isNavigating() {
		return navigating;
	}
	  /**
	   * This method allows the conversion of a distance to the total rotation of each wheel need to
	   * cover that distance.
	   */
	private int convertDistance(double distance){
		return (int) (360*distance/(2*PI*WHEEL_R));
	}
	/**
	 * This method takes the difference between the new heading and the current heading as an input and 
	 * returns the total rotation that each wheel has to do to change the robot's heading
	 */
	private int convertAngle(double angle){
		return convertDistance(TRACK*angle/2);
	}


}
