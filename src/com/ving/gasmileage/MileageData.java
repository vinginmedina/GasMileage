package com.ving.gasmileage;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class MileageData implements Comparable<MileageData> {
	
	private Calendar purchaseDate;
	private String od;
	private float trip;
	private float gallons;
	private float price;
	private float dpg;
	private float mpg;
	private float ppm;
	
	public MileageData(String[] newData) throws Exception {
		try{
			String[] dateVals = newData[0].split("/");
			if (dateVals.length != 3) {
				throw new IllegalArgumentException("Date should be in format mm/dd/yyyy");
			}
			if (dateVals[0].equals("")) {
				throw new IllegalArgumentException("Value for month is empty");
			}
			if (dateVals[1].equals("")) {
				throw new IllegalArgumentException("Value for day is empty");
			}
			if (dateVals[2].equals("")) {
				throw new IllegalArgumentException("Value for year is empty");
			}
			purchaseDate = Calendar.getInstance();
			purchaseDate.set(Integer.parseInt(dateVals[2]), Integer.parseInt(dateVals[0]) - 1, Integer.parseInt(dateVals[1]));
			od = newData[1];
			if (newData[2].equals("")) {
				throw new IllegalArgumentException("Value for miles is empty");
			}
			trip = Float.parseFloat(newData[2]);
			if (trip == 0) {
				throw new IllegalArgumentException("Value for miles is 0");
			}
			if (newData[3].equals("")) {
				throw new IllegalArgumentException("Value for gallons is empty");
			}
			gallons = Float.parseFloat(newData[3]);
			if (gallons == 0) {
				throw new IllegalArgumentException("Value for gallons is 0");
			}
			if (newData[4].equals("")) {
				throw new IllegalArgumentException("Value for price is empty");
			}
			price = Float.parseFloat(newData[4].replace("$", ""));
			if (price == 0) {
				throw new IllegalArgumentException("Value for price is 0");
			}
			dpg = price / gallons;
			mpg = trip / gallons;
			ppm = price / trip;
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	public MileageData(String[] newData, String oldOD) throws Exception {
		this(calcTripMiles(newData, oldOD));
	}
	
	private static String[] calcTripMiles(String[] newData, String oldOD) throws Exception {
		String[] newNewData = new String[5];
		try{
			if ((newData[1] == null) || (newData[1].equals(""))) {
				throw new IllegalArgumentException("Current OD Value not set");
			}
			if ((oldOD == null) || (oldOD.equals(""))) {
				throw new IllegalArgumentException("Couldn't find previous OD value");
			}
			int trpMiles = Integer.parseInt(newData[1]) - Integer.parseInt(oldOD);
			if (trpMiles < 1) {
				throw new IllegalArgumentException("Entered OD value (" + newData[1] +
						") less than previous OD value (" + oldOD + ")");
			}
			newNewData[0] = newData[0];
			newNewData[1] = newData[1];
			newNewData[2] = String.valueOf(trpMiles);
			newNewData[3] = newData[3];
			newNewData[4] = newData[4];
		}catch(Exception e){
			throw new Exception(e);
		}
		
		return newNewData;
	}
	
	public int compareTo(MileageData md) {
		int rtn = this.purchaseDate.compareTo(md.purchaseDate);
		
		return rtn;
	}
	
	public String purchaseDateString() {
		int month = purchaseDate.get(Calendar.MONTH);
		int day = purchaseDate.get(Calendar.DAY_OF_MONTH);
		int year = purchaseDate.get(Calendar.YEAR);
		month++;
		String rtn = month + "/" + day + "/" + year;
		return rtn;
	}
	
	public String purchaseYear() {
		return String.format("%d", purchaseDate.get(Calendar.YEAR));
	}
	
	public String toString() {
		String rtn = "Date: " + this.purchaseDateString() + " MPG: " + String.format("%.1f", mpg);
		return rtn;
	}
			
	public String toFullString() {
		String rtn = "Date: " + this.purchaseDateString() +
				"\nOdometer Reading: " + od +
				"\nMiles: " + String.format("%.1f",trip) + "\nGallons: " + String.format("%.3f",gallons) +
				"\nCost: $" + String.format("%.2f",price) +
				"\nGas Price: $" + String.format("%.2f",dpg) + "\nMPG: " +
				String.format("%.1f", mpg) +
				"\nPPM: $" + String.format("%.2f", ppm);
		return rtn;
	}
	
	public String toCSV() {
		String rtn = this.purchaseDateString() + "," + od + "," +
	            String.format("%.1f", trip) + "," + String.format("%.3f", gallons) +
	            ",$" + String.format("%.2f", price) + ",$" + String.format("%.2f", dpg) +
	            "," + String.format("%.1f", mpg) + ",$" + String.format("%.2f", ppm) + "\n";
		return rtn;
	}
	
	public Calendar purchaseDate() {
		return purchaseDate;
	}
	
	public String OD() {
		return od;
	}
	
	public float miles() {
		return trip;
	}
	
	public float gallons() {
		return gallons;
	}
	
	public float cost() {
		return price;
	}
	
	public float mpg() {
		return mpg;
	}
	
	public float ppg() {
		return dpg;
	}
	
	public float ppm() {
		return ppm;
	}
	
	public float savings(float newMPG) {
		float rtn = 0;
		if (newMPG != 0) {
			rtn = price - (trip / newMPG * dpg);
		}
		return rtn;
	}
	
	public boolean currentYear (int year) {
		return year == purchaseDate.get(Calendar.YEAR);
	}
	
	public boolean currentMonth (int month) {
		return month == purchaseDate.get(Calendar.MONTH);
	}
	
	public int getYear() {
		return purchaseDate.get(Calendar.YEAR);
	}
	
	public String getMonth() {
		return new DateFormatSymbols().getMonths()[purchaseDate.get(Calendar.MONTH)];
	}
}
