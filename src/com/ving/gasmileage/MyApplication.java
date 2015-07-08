package com.ving.gasmileage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.io.File;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

public class MyApplication extends Application {
	
	public final static int MTD = 1;
	public final static int YTD = 2;
	public final static int TOTAL = 3;
	
	private MainActivity myActivity = null;
	private SharedPreferences settings = null;
	private CarInfo carInfo = null;
	private File baseDir = null;
	private Boolean change = null;
	private Boolean fileGood = null;
	private Context mContext = null;
	private TextView carView = null;
	private TextView totalView = null;
	private ArrayList<YearData> expandDataArray = null;
	private MPGListAdapter expandableAdapter = null;
	private float totalMiles;
	private float totalGallons;
	private float totalCost;
	private float totalMPG;
	private float totalPPG;
	private float totalPPM;
	private MileageData lastrow = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		carInfo = new CarInfo();
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
	
	public void setUp() {
		expandDataArray = new ArrayList<YearData>();
		totalMiles = 0;
		totalGallons = 0;
		totalCost = 0;
		totalMPG = 0;
		totalPPG = 0;
		totalPPM = 0;
		lastrow = null;
	}
	
	public void setSharedPreferences(SharedPreferences sp) {
		settings = sp;
	}
	
	public SharedPreferences getSharedPreferences() {
		return settings;
	}
	
	public CarInfo carInfo() {
		return carInfo;
	}
	
	public ArrayList<YearData> getExpArray() {
		return expandDataArray;
	}
	
	public void setStuff(MPGListAdapter newAdapter, TextView carName, TextView totalArea, Context newContext, MainActivity act, String externalDir) {
		expandableAdapter = newAdapter;
		carView = carName;
		totalView = totalArea;
		mContext = newContext;
		myActivity = act;
		baseDir = new File(Environment.getExternalStorageDirectory().toString(),externalDir);
	}
	
	public MainActivity getActivity() {
		return myActivity;
	}
	
	public File baseDirectory() {
		return baseDir;
	}
	
	public void notifyDataSetChanged() {
		if (expandableAdapter != null) {
			totalView.post(new Runnable() {
				@Override
				public void run() {
					expandableAdapter.notifyDataSetChanged();
				}
			});
			
		}
	}
	
	public void notifyDataSetChanged(final Boolean showLast) {
		this.calcValues();
		final String totalValues = this.toString();
		totalView.post(new Runnable() {
			@Override
			public void run() {
				expandableAdapter.notifyDataSetChanged();
				if (carInfo.currentCar() != null) {
					carView.setText("Car Name: "+carInfo.currentCar());
				} else {
					carView.setText("Select a Car");
				}
				if (expandDataArray.size() > 0) {
					totalView.setText(totalValues);
				} else {
					totalView.setText("");
					lastrow = null;
				}
				if (showLast) {
					if (expandDataArray.size() > 0) {
						int lastYear = expandDataArray.size()-1;
						int lastMonth = expandDataArray.get(lastYear).getArrayMonths().size()-1;
						int lastDay = expandDataArray.get(lastYear).getArrayMonths().get(lastMonth).getArrayDays().size()-1;
						lastrow = expandDataArray.get(lastYear).getArrayMonths().get(lastMonth).getArrayDays().get(lastDay);
					}
				}
			}
		});
	}
	
	public String lastFillup() {
		String rtn = "No Data Available";
		
		if (lastrow != null) {
			rtn = lastrow.toFullString();
		}
		
		return rtn;
	}
	
	public void addData(MileageData row) {
		lastrow = row;
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
		}
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
		Collections.sort(expandDataArray);
		for (YearData yd : expandDataArray) {
			yd.sort();
			for (MonthData md : yd.getArrayMonths()) {
				md.sort();
			}
		}
	}
	
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
	
	public String lastOD(String dateToUse) throws Exception {
		String rtn = "";
		int month;
		int day;
		int year;
		String[] dateVals = dateToUse.split("/");
		if (dateVals.length != 3) {
			throw new IllegalArgumentException("Date should be in format mm/dd/yyyy");
		}
		if (dateVals[0].equals("")) {
			throw new IllegalArgumentException("Value for month is empty");
		} else {
			month = Integer.parseInt(dateVals[0]) - 1;
		}
		if (dateVals[1].equals("")) {
			throw new IllegalArgumentException("Value for day is empty");
		} else {
			day = Integer.parseInt(dateVals[1]);
		}
		if (dateVals[2].equals("")) {
			throw new IllegalArgumentException("Value for year is empty");
		}
		Calendar purchaseDate = Calendar.getInstance();
		purchaseDate.set(Integer.parseInt(dateVals[2]), Integer.parseInt(dateVals[0]) - 1, Integer.parseInt(dateVals[1]));
		for (YearData yD : expandDataArray) {
			for (MonthData mD : yD.getArrayMonths()) {
				for (MileageData md: mD.getArrayDays()) {
					if (md.purchaseDate().before(purchaseDate)) {
						rtn = md.OD();
					}
 				}
		    }
		}
		
		return rtn;
	}
	
	public float savings(float newMPG) {
        float rtn = 0;
        if (newMPG != 0) {
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
	
	public void toastMessage(final String msg) {
		totalView.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(myActivity, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public void popUpMessage(final String title, final String msg) {
		totalView.post(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(title);
				builder.setMessage(msg);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
				builder.setCancelable(false);
				AlertDialog myAlertDialog = builder.create();
				myAlertDialog.show();
			}
		});
	}
}
