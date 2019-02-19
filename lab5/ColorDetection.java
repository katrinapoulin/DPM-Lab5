package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.lab5.Lab4;

public class ColorDetection {
	
	private static final double[] BLUE_RGB= {0.2826, 0.6361, 0.7093};
	private static final double[] GREEN_RGB= {0.3406, 0.8425, 0.4008};
	private static final double[] YELLOW_RGB= {0.8591, 0.4727, 0.1767};
	private static final double[] RED_RGB= {0.9547, 0.1990, 0.1648};
	private static final double [][] COLOR_VALUE = {BLUE_RGB, GREEN_RGB, YELLOW_RGB, RED_RGB};
	private static final String[] COLOR_NAMES  = {"BLUE", "GREEN", "YELLOW", "RED"};
	private static final int NUMBER_OF_COLORS = 4;
	
	
	static double[] myRGB = {0.8591, 0.4727, 0.1767};
	
	/*public static void main(String[]args) {
		String color = detectColor(myRGB);
		System.out.print(color);
	}*/

	public static void printColor(double[] RGBvalues) {
		
		double [] d = new double[4];
		
		for(int i = 0; i< 3; i++) {
			
			d[i] = Math.sqrt(Math.pow(RGBvalues[0]-COLOR_VALUE[i][0], 2)+Math.pow(RGBvalues[1]-COLOR_VALUE[i][1], 2)+Math.pow(RGBvalues[2]-COLOR_VALUE[i][2], 2));
		}
		
	
		int color = getMin(d);
		
		System.out.println(COLOR_NAMES[color]);
}
	
	public static int getMin(double[] arr){  
		double min = arr[0];
		int index = 0;
		for(int j=0; j < NUMBER_OF_COLORS; j++) {
			if(min>arr[j]) {
				min=arr[j];
				index = j;
			}
		}
		return index;
	}
}
