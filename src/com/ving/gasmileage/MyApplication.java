package com.ving.gasmileage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.text.DateFormatSymbols;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

public class MyApplication extends Application {
	
	public final static int MTD = 1;
	public final static int YTD = 2;
	public final static int TOTAL = 3;
	
	private Boolean change = null;
	private Boolean fileGood = null;
	private Context mContext = null;
	private TextView totalView = null;
	private TextView latestView = null;
//	private ArrayList<MileageData> mileageArray = null;
	private String rowData = null; 
	private ArrayList<YearData> expandDataArray = null;
	private MPGListAdapter expandableAdapter = null;
	private int currMth;
	private int currYear;
	private float totalMiles;
	private float totalGallons;
	private float totalCost;
	private float totalMPG;
	private float totalPPG;
	private float totalPPM;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void setChanged(Boolean newValue) {
		change = newValue;
	}
	
	public Boolean changed() {
		return change;
	}
	
	public void setFileState(Boolean newValue) {
		fileGood = newValue;
	}
	
	public Boolean fileOK() {
		return fileGood;
	}
	
	public void setDate(int newMth, int newYear) {
		currMth = newMth;
		currYear = newYear;
	}
	
	public void setUp() {
		Log.i("MyApplication","Setting mileageArray to empty list");
//		mileageArray = new ArrayList<MileageData>();
//		expandableAdapter = null;
		expandDataArray = new ArrayList<YearData>();
		rowData = "";
		totalMiles = 0;
		totalGallons = 0;
		totalCost = 0;
		totalMPG = 0;
		totalPPG = 0;
		totalPPM = 0;
	}
	
	public ArrayList<YearData> getExpArray() {
		return expandDataArray;
	}
	
	public void clearRowData() {
		rowData = "";
	}
	
	public void saveRow(String row) {
		rowData += row + "\n";
	}
	
	public String getRowData() {
		return rowData;
	}
	
	public void setExpandAdapter(MPGListAdapter newAdapter, TextView totalArea, TextView lastArea, Context newContext) {
		Log.i("myApplication","Saving List Adapter");
		expandableAdapter = newAdapter;
		totalView = totalArea;
		latestView = lastArea;
		mContext = newContext;
	}
	
	public void notifyDataSetChanged() {
		if (expandableAdapter != null) {
			expandableAdapter.notifyDataSetChanged();
		}
	}
	
	public void notifyDataSetChanged(Boolean showLast) {
		this.calcValues();
		if (expandDataArray.size() > 0) {
			totalView.setText(this.toString());
		} else {
			totalView.setText("");
			latestView.setText("");
		}
		if (showLast) {
			if (expandDataArray.size() > 0) {
				int lastYear = expandDataArray.size()-1;
				Log.i("notifyDataSetChanged","YearArray last entry "+lastYear);
				int lastMonth = expandDataArray.get(lastYear).getArrayMonths().size()-1;
				int lastDay = expandDataArray.get(lastYear).getArrayMonths().get(lastMonth).getArrayDays().size()-1;
				MileageData lastrow = expandDataArray.get(lastYear).getArrayMonths().get(lastMonth).getArrayDays().get(lastDay);
				latestView.setText(lastrow.toFullString());
			}
		}
	}
	
	public MileageData addData(String[] newData) throws Exception {
		MileageData row = null;
		try {
			row = new MileageData(newData);
		}catch(Exception e) {
			Log.e("addData","Error: "+e.toString());
			throw e;
		}
//		mileageArray.add(row);
		String year = String.format("%d", row.getYear());
		String month = row.getMonth();
		YearData yearData = null;
		MonthData monthData = null;
		for (YearData yd : expandDataArray) {
			if (year.equals(yd.getTitle())) {
				yearData = yd;
			}
		}
		if (yearData != null) {
			for (MonthData md : yearData.getArrayMonths()) {
				if (month.equals(md.getTitle())) {
					monthData = md;
				}
			}
			if (monthData != null) {
				monthData.add(row);
			} else {
				monthData = new MonthData(month, row);
				yearData.add(monthData);
			}
		} else {
			monthData = new MonthData(month, row);
			yearData = new YearData(year, monthData);
			expandDataArray.add(yearData);
			notifyDataSetChanged();
		}
		return row;
	}
	
	public void deleteData(MileageData md, MonthData mth) {
		if (mth.getArrayDays().size() == 1) {
			String year = md.purchaseYear();
			YearData yr = null;
			for (YearData yd : expandDataArray) {
				if (year.equals(yd.getTitle())) {
					yr = yd;
				}
			}
			if (yr.getArrayMonths().size() == 1) {
				expandDataArray.remove(yr);
			} else {
				yr.getArrayMonths().remove(mth);
			}
		} else {
			mth.getArrayDays().remove(md);
		}
		this.calcValues();
	}
	
	public void sortMileageData() {
//		Collections.sort(mileageArray);
//		Log.i("sortMileageData","About to sort expandDataArray");
		Collections.sort(expandDataArray);
		for (YearData yd : expandDataArray) {
//			Log.i("sortMileageData", "About to sort year "+yd.getTitle());
			yd.sort();
			for (MonthData md : yd.getArrayMonths()) {
//				Log.i("sortMileageData","About to sort month "+md.getTitle());
				md.sort();
			}
		}
	}
	
//	public int length() {
//		return mileageArray.size();
//	}
	
	public void calcValues() {
		totalMiles = 0;
		totalGallons = 0;
		totalCost = 0;
		sortMileageData();
		for (YearData yd : expandDataArray) {
			yd.calcValues();
			totalMiles += yd.miles();
			totalGallons += yd.gallons();
			totalCost += yd.cost();
		}
		if ((totalMiles > 0) && (totalGallons > 0) && (totalCost > 0)) {
			totalMPG = totalMiles / totalGallons;
			totalPPG = totalCost / totalGallons;
			totalPPM = totalCost / totalMiles;
			notifyDataSetChanged();
		}
	}
	
	public String toString() {
		return this.toString(TOTAL);
	}
	
	public String toString(int flag) {
		String rtn = "";
		switch (flag) {
//		case MTD:
//			rtn = "Miles: " + String.format("%.0f", mtdMiles) + " Gallons: " +
//		            String.format("%.1f",mtdGallons) + "\nCost: $" +
//					String.format("%.2f", mtdCost) +
//		            " Gas Price: $" + String.format("%.2f",mtdPPG) + "\nMPG: " +
//					String.format("%.1f", mtdMPG) +
//					" PPM: $" + String.format("%.2f", mtdPPM);
//			break;
//		case YTD:
//			rtn = "Miles: " + String.format("%.0f", ytdMiles) + " Gallons: " +
//		            String.format("%.1f",ytdGallons) + "\nCost: $" +
//					String.format("%.2f", ytdCost) +
//					" Gas Price: $" + String.format("%.2f",ytdPPG) + "\nMPG: " +
//					String.format("%.1f", ytdMPG) +
//					" PPM: $" + String.format("%.2f", ytdPPM);
//			break;
		case TOTAL:
			rtn = "Miles: " + String.format("%.0f", totalMiles) + " Gallons: " +
		            String.format("%.1f",totalGallons) + "\nCost: $" +
					String.format("%.2f", totalCost) +
					" Gas Price: $" + String.format("%.2f",totalPPG) + "\nMPG: " +
					String.format("%.1f", totalMPG) +
					" PPM: $" + String.format("%.2f", totalPPM);
			break;
		}
		return rtn;
	}
	
	public String toCSV() {
		String rtn = "";
		for (YearData yD : expandDataArray) {
			for (MonthData mD : yD.getArrayMonths()) {
				for (MileageData md: mD.getArrayDays()) {
					rtn += md.toCSV();
				}
		    }
		}
		return rtn;
	}
	
	public float savings(float newMPG) {
        float rtn = 0;
        if (newMPG != 0) {
	        Log.i("MyApp","Calculating savings using new MPG of "+newMPG);
	        for (YearData yD : expandDataArray) {
		        for (MonthData mD : yD.getArrayMonths()) {
		        	for (MileageData md : mD.getArrayDays()) {
			        	rtn += md.savings(newMPG);
		        	}
		        }
	        }
	        if (rtn < 0) {
	        	rtn = rtn * -1;
	        }
        }
        return rtn;
	}
	
	public class MileageData implements Comparable<MileageData>{
		private Calendar purchaseDate;
		private String od;
		private float trip;
		private float gallons;
		private float price;
		private float dpg;
		private float mpg;
		private float ppm;
		
		public MileageData(String[] newData) throws Exception {
//			Log.i("mileageData","Creating new object");
//			Log.i("mileageData","Date value: "+newData[0]);
//			Log.i("mileageData","od value: "+newData[1]);
//			Log.i("mileageData","trip value: "+newData[2]);
//			Log.i("mileageData","gallons value: "+newData[3]);
//			Log.i("mileageData","price value: "+newData[4]);
			try{
				String[] dateVals = newData[0].split("/");
//				Log.i("mileageData","Date Values Month: "+dateVals[0]+" Day: "+dateVals[1]+" Year: "+dateVals[2]);
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
//			Log.i("mileageData","Finished, new object ready");
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
//				Log.i("MileageData","Calc Savings with newMPG of "+newMPG+" difference is "+rtn);
			}
			return rtn;
		}
		
		public boolean currentYear (int year) {
//			Log.i("currentYear","year: "+year+" value: "+purchaseDate.get(Calendar.YEAR));
			return year == purchaseDate.get(Calendar.YEAR);
		}
		
		public boolean currentMonth (int month) {
//			Log.i("currentMonth","month: "+month+" value: "+purchaseDate.get(Calendar.MONTH));
			return month == purchaseDate.get(Calendar.MONTH);
		}
		
		public int getYear() {
			return purchaseDate.get(Calendar.YEAR);
		}
		
		public String getMonth() {
			return new DateFormatSymbols().getMonths()[purchaseDate.get(Calendar.MONTH)];
		}
	}
	
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
	    	notifyDataSetChanged();
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
//	    	Log.i("compareTo MD","Compare "+mTitle+"("+mth1+") to "+md.getTitle()+"("+mth2+")"+" result: "+rtn);
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
	    	notifyDataSetChanged();
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
}
