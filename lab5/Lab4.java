/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * Lab4.java
 * This is the main class, where we create all the instances of sensor and motors, constants, and start all the threads.
 * We also create a display for the user to select which method he wants to use.
 */


package ca.mcgill.ecse211.lab5;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class Lab4 {


	// Static Resources created
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")); //Left motor connected to port B
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B")); //Right motor connected to port D
	public static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1")); //US sensor connected to port S1
	public static final EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2")); //Light sensor connected to port S2
	public static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3")); //Light sensor connected to port S3

	// Button to be selected
	public static int buttonChoice; 

	// Constants created
	public static final double WHEEL_RADIUS = 2.1; //Radius of wheel
	public static final double TRACK = 11.3; //Distance between the center of both wheels


	/**
	 * 
	 * This is the main method where we start the program and activate all the threads. 
	 * We show our display on the screen and start the program according to the user's input
	 * 
	 */
	public static void main(String[] args) {


		final TextLCD lcd = LocalEV3.get().getTextLCD(); // To access the screen
		Odometer odometer = new Odometer(leftMotor, rightMotor); // We create an instance of the Odometer class
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, lcd); // We create an instance of the OdometryDisplay class
		UltrasonicPoller usPoller = new UltrasonicPoller(usSensor); // We create an instance of the UltracsonicPoller class
		UltrasonicLocalizer usloc = new UltrasonicLocalizer(odometer, usPoller); // We create an instance of the UltracsonicLocalizer class
		Navigation navig = new Navigation(odometer, leftMotor, rightMotor); // We create an instance of the Navigation class
		LightLocalizer lightLoc = new LightLocalizer(odometer, usloc, navig); // We create an instance of the LightLocalizer class

		do {
			// We start by clearing the display
			lcd.clear();

			//We ask the user which method he wants to use
			lcd.drawString("   Localization   ", 0, 0); 
			lcd.drawString(" Falling |  >>>>  ", 0, 1); // The left arrow will select the Falling edge method
			lcd.drawString("  Edge   | Rising ", 0, 2); // The right arrow will select the Rising edge method
			lcd.drawString("  <<<<   | Edge   ", 0, 3); 

			buttonChoice = Button.waitForAnyPress();	// And we wait for the user to make his selection
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);


		/**
		 *  If he chooses falling edge, we clear the display, write FALLING EDGE, and start all the threads (the poller,
		 *  the odometer, the display and the UltrasonicLocalization Class) and finally we call the falling edge method from the UltrasonicLocalizer class.
		 *  Then, when this method is over, we come back here and wait for any press from the user to continue with the program
		 *  and do the light localization
		 */
		if(buttonChoice == Button.ID_LEFT) { 

			lcd.clear();
			lcd.drawString("  FALLING EDGE  ", 0, 3);
			usPoller.start();
			odometer.start();
			odometryDisplay.start();
			usloc.start();
			Button.waitForAnyPress();
			lightLoc.start();

			
			/**
			 *  If the user chooses rising edge, we clear the display, write RISING EDGE, and start all the threads (the poller,
			 *  the odometer, the display and the UltrasonicLocalization Class) 
			 *  Then, when this method is over, we come back here and wait for any press from the user to continue with the program
			 *  and do the light localization
			 */
		} else if(buttonChoice == Button.ID_RIGHT) { 

			lcd.clear();
			lcd.drawString("  RISING EDGE  ", 0, 3);
			usPoller.start();
			odometer.start();
			odometryDisplay.start();
			usloc.start();
			Button.waitForAnyPress();
			lightLoc.start();

		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}

