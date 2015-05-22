package com.ving.gasmileage;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;

public class MonthData implements Comparable<MonthData> {
	private String mTitle;
    private float miles;
    private float gallons;
    private float cost;
    private float mpg;
    private float ppg;
    private float ppm;
    private ArrayList<MileageData> mArrayDays;
    
    public MonthData (String title, float m, float g, float c, float mg,
    		float pg, float pm, ArrayList<MileageData> data) {
    	mTitle = title;
    	miles = m;
    	gallons = g;
    	cost = c;
    	mpg = mg;
    	ppg = pg;
    	ppm = pm;
    	mArrayDays = data;
    }
    
    public MonthData (String title, MileageData data) {
    	mTitle = title;
    	miles = data.miles();
    	gallons = data.gallons();
    	cost = data.cost();
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    	mArrayDays = new ArrayList<MileageData>();
    	mArrayDays.add(data);
    }
    
    public int compareTo(MonthData md) {
    	String months[] = new DateFormatSymbols().getMonths();
    	int mth1 = 0;
    	for (int i=0;i<months.length;i++) {
    		if (months[i].equals(mTitle)) {
    			mth1 = i;
    		}
    	}
    	int mth2 = 0;
    	for (int i=0;i<months.length;i++) {
    		if (months[i].equals(md.getTitle())) {
    			mth2 = i;
    		}
    	}
    	int rtn = 0;
    	if (mth1 < mth2) {
    		rtn = -1;
    	} else if (mth1 > mth2) {
    		rtn = 1;
    	}
    	return rtn;
    }
    
    public void add (MileageData data) {
    	miles += data.miles();
    	gallons += data.gallons();
    	cost += data.cost();
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    	mArrayDays.add(data);
    }
    
    public void sort () {
    	Collections.sort(mArrayDays);
    }
    
    public void calcValues () {
    	miles = 0;
    	gallons = 0;
    	cost = 0;
    	for (MileageData md : mArrayDays) {
    		miles += md.miles();
    		gallons += md.gallons();
    		cost += md.cost();
    	}
    	mpg = miles / gallons;
    	ppg = cost / gallons;
    	ppm = cost / miles;
    }
    
    public String toString() {
    	return mTitle + " Average MPG: " + String.format("%.1f", mpg);
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
 
    public ArrayList<MileageData> getArrayDays() {
        return mArrayDays;
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
