package com.ving.gasmileage;

import java.util.ArrayList;
import java.util.Collections;

public class YearData implements Comparable<YearData> {
	private String mTitle;
    private float miles;
    private float gallons;
    private float cost;
    private float mpg;
    private float ppg;
    private float ppm;
    private ArrayList<MonthData> mArrayMonths;
    
    public YearData (String title, float m, float g, float c, float mg,
    		float pg, float pm, ArrayList<MonthData> data) {
    	mTitle = title;
    	miles = m;
    	gallons = g;
    	cost = c;
    	mpg = mg;
    	ppg = pg;
    	ppm = pm;
    	mArrayMonths = data;
    }
    
    public YearData (String title, MonthData data) {
    	mTitle = title;
    	miles = data.miles();
    	gallons = data.gallons();
    	cost = data.cost();
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    	mArrayMonths = new ArrayList<MonthData>();
    	mArrayMonths.add(data);
    }
    
    public int compareTo(YearData yd) {
    	int y1 = Integer.parseInt(mTitle);
    	int y2 = Integer.parseInt(yd.getTitle());
    	int rtn = 0;
    	if (y1 < y2) {
    		rtn = -1;
    	} else if (y1 > y2) {
    		rtn = 1;
    	}
    	return rtn;
    }
    
    public void add (MonthData data) {
    	miles += data.miles();
    	gallons += data.gallons();
    	cost += data.cost();
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    	mArrayMonths.add(data);
    }
    
    public void sort() {
    	Collections.sort(mArrayMonths);
    }
    
    public void calcValues() {
    	miles = 0;
    	gallons = 0;
    	cost = 0;
    	for (MonthData md : mArrayMonths) {
    		md.calcValues();
    		miles += md.miles();
    		gallons += md.gallons();
    		cost += md.cost();
    	}
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    }
    
    public String toString() {
    	return mTitle + "  Average MPG: " + String.format("%.1f",  mpg);
    }
    
    public String toFullString() {
    	String rtn = "Total Miles: " + String.format("%.1f",miles) + "\nTotal Gallons: " +
    	        String.format("%.3f",gallons) + "\nTotal Cost: $" + String.format("%.2f",cost) +
    	        "\nAvarage MPG: " + String.format("%.1f", mpg) + "\nAverage Gas Price: $" +
    	        String.format("%.2f", ppg) + "\nAverage Price per Mile: $" + String.format("%.2f", ppm);
    	return rtn;
    }
 
    public String getTitle() {
        return mTitle;
    }
 
    public ArrayList<MonthData> getArrayMonths() {
        return mArrayMonths;
    }
    
    public float miles() {
    	return miles;
    }
    
    public float gallons() {
    	return gallons;
    }
    
    public float cost() {
    	return cost;
    }
    
    public float mpg() {
    	return mpg;
    }
    
    public float ppm() {
    	return ppm;
    }
    
    public float ppg() {
    	return ppg;
    }
}
