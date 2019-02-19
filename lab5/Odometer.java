/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * Odometer.java
 * This class focuses on the coordinates and the orientation of the device.
 * It is very similar to the one used in lab2 and 3
 */


package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

 
public class Odometer extends Thread {
	//Constants
	private static final long ODOMETER_PERIOD = 25; //update period in ms

	// Robot coordinates
	private double x, y, theta;

	//Motors 'previous' tacho count
	private int leftMotorTachoCount, rightMotorTachoCount;

	//Motors
	private EV3LargeRegulatedMotor leftMotor, rightMotor;


	// lock object for mutual exclusion
	private Object lock;

	
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	/**
	 * Method that will be executed once the Odometer thread is started
	 * Inspired by lab 2
	 */
	public void run() {

		long updateStart, updateEnd; //start and end
		leftMotor.flt(true);
		rightMotor.flt(false);
		
		while (true) {

			updateStart = System.currentTimeMillis();

			int currentTachoLeft = leftMotor.getTachoCount(); //get tacho left count
			int currentTachoRight = rightMotor.getTachoCount(); //get current tacho right count

			double dL = Math.PI * Lab4.WHEEL_RADIUS * (currentTachoLeft - leftMotorTachoCount) / 180;	// Difference in radians of left wheel rotation
			double dR = Math.PI * Lab4.WHEEL_RADIUS * (currentTachoRight - rightMotorTachoCount) / 180;	// Difference in radians of right wheel rotation

			// Update Tacho counts
			leftMotorTachoCount = currentTachoLeft;
			rightMotorTachoCount = currentTachoRight;
			
			double dDist = (dL + dR) * 0.5; // Distance moved this frame
			double dTheta = (dL - dR) / Lab4.TRACK; // Change in orientation

			synchronized (lock) {
				theta += dTheta;
				x += dDist * Math.sin(theta);
				y += dDist * Math.cos(theta);
				theta = (theta + 2*Math.PI) % (2*Math.PI); // Theta is within 0 and 2pi
			}


			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {}
			}
		}
	}

	/*
	 * Position
	 */

	//Getters

	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta * 180 / Math.PI; //display theta in degrees
		}
	}

	/**
	 * Getter for X coordinate
	 * @return x value
	 */
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	/**
	 * Getter for Y coordinate
	 * @return y value
	 */
	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	/**
	 * Getter for Theta orientation
	 * @return theta
	 */
	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	
	//Setters 

	/**
	 * Set X
	 * @param x
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	/**
	 * Set Y
	 * @param y
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	/**
	 * Set Theta
	 * @param theta
	 */
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/*
	 * Tacho counts
	 */
	
	//Getters

	/**
	 * Get left motor tacho count
	 * @return leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * Get left motor tacho count
	 * @return leftMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	
	//Setters 

	/**
	 * Set left motor tacho count
	 * @param leftMotorTachoCount
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * Set right motor tacho count
	 * @param rightMotorTachoCount
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}