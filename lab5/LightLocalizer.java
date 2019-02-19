/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * LightLocalizer.java
 * This class focuses on finding the coordinates of the EV3. 
 * We rotate the device on itself and check for black lines, calculate the difference of angle between each pair of black line (Xs and Ys)
 * and calculate the real coordinates of the device
 */

package ca.mcgill.ecse211.lab5;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class LightLocalizer extends Thread {

	private static final double DISTANCE_CENTER_SENSOR = 11.532;	// This is the distance from the center of rotation to the light sensor
	private static final float SENSOR_VALUE = -40;		// This is the difference of value of the sensor between 2 fetched samples

	public static final double WHEEL_R = Lab4.WHEEL_RADIUS; // Radius of wheel
	public static final double TRACK = Lab4.TRACK; // Distance between the center of both wheels


	private EV3ColorSensor lightsensor = Lab4.lightSensor;

	private SampleProvider colorsensor = lightsensor.getMode("Red");
	private float[] colordata = new float[colorsensor.sampleSize()]; // Readings of the sensor

	private Odometer odometer;
	private UltrasonicLocalizer ultraloc;
	private Navigation navig;

	private float color;

	private double[] thetaArray = new double[4];


	public LightLocalizer(Odometer odometer,UltrasonicLocalizer ultraloc, Navigation navig) {
		this.odometer=odometer;
		this.ultraloc=ultraloc;
		this.navig=navig;
	}

	/**
	 * In this method, we need to find the coordinates of the device. 
	 * To do that, we rotate on ourself to find the black lines that form the axis.
	 * We start by checking the distance from the walls and arrange ourselves to touch the lines
	 * Then we start rotating and find the black lines. We note the theta at which we cross each in an array.
	 * We have calculated in checkDistance() the amount by which it had to go back so that the lines 1 & 3 and the lies 0 & 2 are the same coordinate variable
	 * So all we need to do is apply the formula given in the tutorial: x = -distanceToSensor*cos((theta[1]-thet[3])/2)
	 * We do the same for y and set both x and y on the odometer.
	 * Now we can travel to the point (0,0)
	 * When we get there, we just have to rotate to the 0 direction using turnTo in Navigation.
	 */
	
	public void run() {
		lightsensor.setFloodlight(true); // We first turn the sensor's light on

		odometer.setTheta(0);			// We set theta to 0
		ultraloc.checkDistance();		// and we check the distance like described in UltrasonicLocalizer class


		colorsensor.fetchSample(colordata, 0);	// We fetch the color sample from the sensor 
		float lastcolor = colordata[0]*1000;    // We get the color that the sensor reads (*1000 because the original value is too small)
		int i = 0;



		while (i<4) {
			colorsensor.fetchSample(colordata, 0);	    // We fetch again the color sample from the sensor 
			color = colordata[0]*1000;					

			Lab4.leftMotor.backward();					// While we haven't found 4 lines, rotate.
			Lab4.rightMotor.forward();

			if(color-lastcolor<SENSOR_VALUE) {			// When there is a sudden fall of value, it means that there is a black line
				lastcolor=color; 						// We update the last color
				Sound.playNote(Sound.FLUTE, 880, 250);  // We make the sensor beeps when it crosses a black line
				thetaArray[i] = odometer.getTheta();	// We get the values of theta at each black line and store them in an array
				i++;		
			}
		}		

		LCD.clear();
		Lab4.leftMotor.stop(true);						// After the last black line, we stop
		Lab4.rightMotor.stop(false);

		odometer.setX(-DISTANCE_CENTER_SENSOR*Math.cos((thetaArray[2]-thetaArray[0])/2));		// We set the odometer to the real values of X and Y
		odometer.setY(-DISTANCE_CENTER_SENSOR*Math.cos((thetaArray[3]-thetaArray[1])/2));
		navig.travelTo(0, 0);							// And we travel to (0,0)
		LCD.drawString("X="+odometer.getX(), 0, 4);
		LCD.drawString("Y="+odometer.getY(), 0, 5);
		Lab4.leftMotor.stop(true);						// We then stop the motors
		Lab4.rightMotor.stop(false);
		navig.turnTo(0);				// And finally rotate to the 0 direction
	}

}
