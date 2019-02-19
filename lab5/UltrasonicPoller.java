/**
 * @author Sami Rouhana 260778519
 * @author Lea Kassab 260784886
 * ECSE 211 - Lab 4 Localization 
 * 
 * This lab focuses on the localization of the EV3's coordinates (using a light sensor) and its orientation (using a light sensor)
 * 
 * UltrasonicPoller.java
 * In this class, we fetch the data gathered by the sensor and we filter them 
 */

package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MedianFilter;


public class UltrasonicPoller extends Thread {
	private float[] usData;
	private int distance;
	private final int NUM_SAMPLES = 5;
	MedianFilter mf;

	/**
	 * We gather the data and filter them
	 * with the method Median Filter which takes as arguments the sample provider instance and a number of sample to filter.
	 * With that method we are able to get more accurate results and return better values to our program 
	 */


	public UltrasonicPoller(SampleProvider us) {
		usData = new float[us.sampleSize() * NUM_SAMPLES];
		mf  = new MedianFilter(us, NUM_SAMPLES);
	}


	public void run() {
		while (true) {
			mf.fetchSample(usData, 0);			// We acquire data
			distance = (int) (usData[0] * 100); // We extract from buffer, and cast it to int 

			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getDistance() {				// And here we create a method that returns the distance measured by the sensor
		return distance;
	}

}
