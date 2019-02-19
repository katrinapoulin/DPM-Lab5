/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * UltrasonicLocalizer.java
 * This class focuses on finding the orientation of the EV3. 
 * We have 2 methods: falling edge and rising edge. One of them will execute depending on the user's input.
 */

package ca.mcgill.ecse211.lab5;

import lejos.hardware.Button;

public class UltrasonicLocalizer extends Thread {

	/**
	 * We start by creating the constants and the instances we need for this class.
	 * The constants were found by experiments. 
	 * - d is the ideal distance between the sensor and the wall, it is a threshold value.
	 * - k is the noise margin we have set, to create an interval of acceptable values 
	 */
	private static final int d = 29; 
	private static final int k = 4; 
	// This is the threshold value that shows if the device is facing the wall at departure (it will be discussed in more detail in fallingEdge())
	private static final int FACING_WALL = 100;
	// This is the threshold value that shows if the device is not facing the wall at departure (it will be discussed in more detail in risingEdge())
	private static final int NOT_FACING_WALL = 3;
	public static final double WHEEL_RADIUS = Lab4.WHEEL_RADIUS; 
	public static final double TRACK = Lab4.TRACK;
	private static final int BASESPEED = 77;
	// This is a threshold value that shows if the device is far enough from the wall to rotate efficiently with the light sensor
	private static final int ERROR_TO_WALL = 15;
	// This value specifies by how much the device should go backward to find black lines
	private static final double DIST_BACK = 6; 		
	private Odometer odo;
	private UltrasonicPoller us;

	public UltrasonicLocalizer(Odometer odo, UltrasonicPoller us) {
		this.odo = odo;
		this.us = us;
	}


	/**
	 * This is the falling edge method.
	 * First thing we do after setting the speed and creating our variables, we check is the robot is facing the wall.
	 * We have found that 100 is the best value to check if the device faces the wall. 
	 * In that case, we order it to rotate until the sensor detects more than 100. It then starts the method normally.
	 * We start by moving to the left until we detect a wall. When we do, we stop the wheels, set Theta to 0, and go the other way around.
	 * Since the sensor is still detecting some walls, it will think it needs to stop, so we make it sleep for 2000ms, a value we found most adequate for
	 * the tile size and speed of the device. When he wakes up, he is approximately at the middle of the navigation. The sensor then continues
	 * to detect, until he finds a wall. When he does, we calculate the amount by which it needs to rotate back to get to the 45 degrees orientation.
	 * We find this angle by just dividing by 2 the Theta found at the 2nd wall, since we set Theta to 0 at the first one.
	 * We then rotate back left to the 45 degrees orientation, and finally rotate 45 degrees to find the 0 degrees direction.
	 */
	public void fallingEdge() {
		double theta_right = 0, theta_to45 = 0;
		Lab4.leftMotor.setSpeed(BASESPEED);
		Lab4.rightMotor.setSpeed(BASESPEED);

		while(us.getDistance() < FACING_WALL) {	// The device checks if it is facing the wall
			Lab4.leftMotor.forward();			// and if it is, it rotates right until it doesn't anymore
			Lab4.rightMotor.backward();
		}

		while(true) {							

			Lab4.leftMotor.backward();			// We then rotate to the left to find the first wall
			Lab4.rightMotor.forward();

			if(underTreshold()) {				// When we find it we stop
				Lab4.leftMotor.stop(true);
				Lab4.rightMotor.stop(false);
				break;
			}
		}

		odo.setTheta(0);						// And we set the odometer to zero here

		try {									// Then, we sleep the sensor while he is rotating the other way
			Lab4.leftMotor.forward();
			Lab4.rightMotor.backward();
			UltrasonicPoller.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		while(true) {							// We then travel to the right searching for the next wall

			Lab4.leftMotor.forward();
			Lab4.rightMotor.backward();

			if(underTreshold()) {				// And when we find it, we stop and record the value of theta read by the odometer
				Lab4.leftMotor.stop(true);
				Lab4.rightMotor.stop(false);
				theta_right = odo.getTheta();
				break;
			}
		}

		theta_to45 = 0.5 *theta_right;			// We calculate the value by which it must rotate

		Lab4.leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, theta_to45), true);		// We first rotate by theta_to45 to get to the 45 degrees direction
		Lab4.rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, theta_to45), false);

		Lab4.leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.PI/4), true);			// Then we rotate by 45 to go to 0 direction
		Lab4.rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.PI/4), false);

		Lab4.leftMotor.stop(true);															// we finaly stop and set theta to 0
		Lab4.leftMotor.stop(false);

		odo.setTheta(0);

		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	/**
	 * This is the rising edge method.
	 * First thing we do after setting the speed and creating our variables, we check if the robot is not facing the wall.
	 * We have found that 3 is the best value to check if the device is facing the wall. 
	 * In that case, we order it to rotate until the sensor detects less than 3. It then starts the method normally.
	 * We start by moving to the right until we detect a wall. When we do, we stop the wheels, set Theta to 0, and go the other way around.
	 * Since the sensor is still detecting some walls, it will think it needs to stop, so we make it sleep for 2000ms, a value we found most adequate for
	 * the tile size and speed of the device. When he wakes up, he is approximately at the middle of the navigation. The sensor then continues
	 * to detect, until he finds a rising edge. When he does, we calculate the amount by which it needs to rotate back to get to the 225 degrees orientation.
	 * We find this angle by just dividing by 2 the Theta found at the 2nd wall, since we set Theta to 0 at the first one.
	 * We then rotate back left to the 225 degrees orientation, and finally rotate 135 degrees to the left to find the 0 degrees direction.
	 */
	public void risingEdge() {
		double theta_left = 0, theta_to225 = 0;
		Lab4.leftMotor.setSpeed(BASESPEED);
		Lab4.rightMotor.setSpeed(BASESPEED);

		while(us.getDistance() > NOT_FACING_WALL) {		// We check if it is not facing a wall
			Lab4.leftMotor.backward();					// In that case we rotate until we find one
			Lab4.rightMotor.forward();
		}

		while(true) {									// Then we start the method, we rotate until we find a rising edge

			Lab4.leftMotor.forward();
			Lab4.rightMotor.backward();

			if(aboveTheshold()) {						// When we do, we stop and set theta to 0
				Lab4.leftMotor.stop(true);
				Lab4.rightMotor.stop(false);
				break;
			}
		}

		odo.setTheta(0);

		try {											// We also sleep the sensor for 2000ms for the device to be far from the first point before resuming detection
			Lab4.leftMotor.backward();
			Lab4.rightMotor.forward();
			UltrasonicPoller.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		while(true) {									// Then when we find our 2nd point, we stop, get the theta we traveled and find the theta by which we need to rotate

			Lab4.leftMotor.backward();
			Lab4.rightMotor.forward();

			if(aboveTheshold()) {
				Lab4.leftMotor.stop(true);
				Lab4.rightMotor.stop(false);
				theta_left = odo.getTheta();
				break;
			}
		}

		theta_to225 = 0.5 *theta_left;

		Lab4.leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, theta_to225), true);			// We rotate by the amount needed to get to the 225 degree orientation
		Lab4.rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, theta_to225), false);

		Lab4.leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.toRadians(135)), true);	// Finally we rotate 135 degrees to get to the 0 degrees direction
		Lab4.rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.toRadians(135)), false);

		Lab4.leftMotor.stop(true);
		Lab4.leftMotor.stop(false);
		odo.setTheta(0);

		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}



	public void run() {				// In the run method, we check what is the user's input to know which method to call

		if(Lab4.buttonChoice == Button.ID_LEFT) {
			fallingEdge();
		} else if(Lab4.buttonChoice == Button.ID_RIGHT) {
			risingEdge();
		}

	}
	/**
	 * underTreshold will return true if the distance gathered from the sensor is smaller than the lower limit set of the acceptable interval
	 */
	private boolean underTreshold() {
		return (us.getDistance() < d - k) ;
	}
	/**
	 * aboveTreshold will return true if the distance gathered from the sensor is bigger than the higher limit set of the acceptable interval
	 */	
	private boolean aboveTheshold() {
		return (us.getDistance() > d + k);
	}

	/**
	 * inNoiseMargin should return true if you are inside the interval created by d and k.
	 * We created it to have a better approximation of the real value of theta when we find the rising or falling edge.
	 * We would have taken 2 values, one inside the interval and one that is either above or below, depending on the method used,
	 * and have taken their average (like in the tutorial).
	 * We don't use this method because we found out by experiments that our sensor was too slow and couldn't find 2 values in that interval.
	 * So we left it here to explain what we wanted to do.
	 */
	private boolean inNoiseMargin() {
		return (us.getDistance() >= d - k && us.getDistance() <= d + k);
	}


	/**
	 * This method will check the distance to the wall of the device.
	 * It is called in LightLocalizer before doing anything else.
	 * We rotate first to the left 90 degrees, check the distance, if it is smaller than 15, we go backward by 6cm.
	 * We rotate again to the left 90 degrees and do the same thing.
	 * We do that so that when the device rotates on itself to find the black lines with the light sensor, it actually hits them.
	 * And finally we rotate again 180 degrees to come back to our original position
	 */
	public void checkDistance() {			// Rotate once and check
		Lab4.leftMotor.rotate(-convertAngle(WHEEL_RADIUS,TRACK,Math.PI/2), true);
		Lab4.rightMotor.rotate(convertAngle(WHEEL_RADIUS,TRACK,Math.PI/2), false);
		try {
			Lab4.leftMotor.stop(true);
			Lab4.rightMotor.stop(false);
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		if (us.getDistance() < ERROR_TO_WALL) {	// If it is too close, go back
			Lab4.leftMotor.rotate(-convertDistance(WHEEL_RADIUS,DIST_BACK), true);
			Lab4.rightMotor.rotate(-convertDistance(WHEEL_RADIUS, DIST_BACK), false);
			try {
				Lab4.leftMotor.stop(true);
				Lab4.rightMotor.stop(false);
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		try {		// Rotate a 2nd time and check
			Lab4.leftMotor.rotate(-convertAngle(WHEEL_RADIUS,TRACK,Math.PI/2), true);
			Lab4.rightMotor.rotate(convertAngle(WHEEL_RADIUS,TRACK,Math.PI/2), false);
			Lab4.leftMotor.stop(true);
			Lab4.rightMotor.stop(false);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (us.getDistance() < ERROR_TO_WALL) {	// If it is too close, go back
			Lab4.leftMotor.rotate(-convertDistance(WHEEL_RADIUS,DIST_BACK), true);
			Lab4.rightMotor.rotate(-convertDistance(WHEEL_RADIUS, DIST_BACK), false);
			Lab4.leftMotor.stop(true);
			Lab4.rightMotor.stop(false);

		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// And finally come back to original position
		Lab4.leftMotor.rotate(-convertAngle(WHEEL_RADIUS,TRACK,Math.PI), true);
		Lab4.rightMotor.rotate(convertAngle(WHEEL_RADIUS,TRACK,Math.PI), false);
		Lab4.leftMotor.stop(true);
		Lab4.rightMotor.stop(false);

	}


	/**
	 * Method to convert the desired distance we want to move (in cm) to the number of radians
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180 * distance) / (Math.PI * radius));
	}

	/**
	 * Method to convert the angle to number of degrees for the motors
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, width * angle / 2);
	}

}
