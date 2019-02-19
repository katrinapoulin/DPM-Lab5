package ca.mcgill.ecse211.lab5;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

import java.text.DecimalFormat;

import ca.mcgill.ecse211.lab5.Lab5;

public class SensorData extends Thread{

	double []RGBdata= new double[3];
	double [] canScan = new double[3];
	private float[] colorData;
	SampleProvider colorSensor;
	double [] RGBVal;
	
	/**
	 * Method that sums up 10 samples of the Color Sensor's RGB values and computes the mean of R, G, and B values.
	 * 
	 * @return Array of mean R, G, B values
	 */
	
	 //get the sensor for color
	
	public SensorData() {
		
		this.colorSensor = Lab5.colorSensor;
		Lab5.colorSensor.setCurrentMode("RGB");//we setup the sensor in RGB mode to get the reflected light
	    this.colorData = new float[3];
	}
     
	public void run() {
		while(true) {
			
		}
		try {
			RGBVal = getRGB();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
    
    
	public double[] getRGB() throws InterruptedException {
		
		/*int i=0;
		while(i < 10) {
			
			colorSensor.fetchSample(colorData, 0);
			
			canScan[0] = colorData[0];	
			canScan[1] = colorData[1];
			canScan[2] = colorData[2];
			
		    RGBdata[0] += canScan[0];
		    RGBdata[1] += canScan[1];
		    RGBdata[2] += canScan[2];
		    
		    i++;
	}
		
		RGBdata[0]= RGBdata[0]/(10.0);
		RGBdata[1]= RGBdata[1]/(10.0);
		RGBdata[2]= RGBdata[2]/(10.0);
		
           DecimalFormat numberFormat = new DecimalFormat("######0.000");

		
		LCD.drawString(numberFormat.format(RGBdata[0]),0,0);// We fetch again the color sample from the sensor 
		LCD.drawString(numberFormat.format(RGBdata[1]),0,1);
		LCD.drawString(numberFormat.format(RGBdata[2]),0,2);
		LCD.drawString(numberFormat.format(colorSensor.sampleSize()),0,3);

		Thread.sleep(100);*/
		colorSensor.fetchSample(colorData, 0);
		double normalized_R =colorData[0]/Math.sqrt(colorData[0]*colorData[0]+colorData[1]*colorData[1]+colorData[2]*colorData[2]);
		double nomorlized_G =colorData[1]/Math.sqrt(colorData[0]*colorData[0]+colorData[1]*colorData[1]+colorData[2]*colorData[2]);
		double normalized_B =colorData[2]/Math.sqrt(colorData[0]*colorData[0]+colorData[1]*colorData[1]+colorData[2]*colorData[2]);
		
		
		
		return new double[] {normalized_R, nomorlized_G, normalized_B};
   }
}
