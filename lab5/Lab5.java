package ca.mcgill.ecse211.lab5;

import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab5 {
	
	final TextLCD lcd = LocalEV3.get().getTextLCD(); // To access the screen

	public static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C")); //Right motor connected to port D
	public static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4")); //Light sensor connected to port S3
	public SampleProvider color = colorSensor.getRGBMode();
	public static final SensorData myData = new SensorData();
	
	
	public static void main(String[] args) {
	myData.start();
	sensorMotor.setSpeed(50);
	//double [] RGBVal = myData.getRGB();
	
	DecimalFormat numberFormat = new DecimalFormat("######0.000");
	LCD.drawString(numberFormat.format(colorSensor.sampleSize()),0,3);
	sensorMotor.rotate(180);
	sensorMotor.rotate(-180);
	
	LCD.clear();
	ColorDetection.printColor(myData.RGBVal);
	
	Button.waitForAnyPress();
	}
	
}