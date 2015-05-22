package com.ving.gasmileage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.os.Environment;

public class ReadMileageData {
	private String file = null;
	private File dir = null;
	private File myFile = null;
	private Boolean notCanceled = null;
	private ArrayList<String> rowData = null;
	private ArrayList<MileageData> mileageData = null;
	private String errorMsg = null;
	private MileageData md = null;

	ReadMileageData(String fileName) {
	    file = fileName;
	    dir = new File(Environment.getExternalStorageDirectory().toString(),"/GasMileage");
	    myFile = new File(dir, file);
	}

	public Boolean readFile() {
		notCanceled = true;
		mileageData = new ArrayList<MileageData>();
		if (myFile.exists()) {
			try {
				FileInputStream iStream =  new FileInputStream(myFile);
				InputStreamReader iStreamReader = new InputStreamReader(iStream);
				BufferedReader myReader = new BufferedReader(iStreamReader);
				String row;
				rowData = new ArrayList<String>();
				while ((row = myReader.readLine()) != null) {
					rowData.add(row);
				}
				myReader.close();
			}catch(Exception e) {
				errorMsg = e.toString();
				notCanceled = false;
			}
			if (notCanceled) {
				for (String row : rowData) {
					if (! row.equals("")) {
						String[] rowValues = row.split(",");
						try {
							md = new MileageData(rowValues);
							mileageData.add(md);
						} catch (Exception e) {
							notCanceled = false;
							errorMsg += "\n" + row + "\n" + e.toString() + "\n";
						}
					}
				}
			}
		}
		if (! notCanceled) {
			mileageData = null;
		}
		
		return notCanceled;
	}
	
	public String error() {
		return errorMsg;
	}
	
	public ArrayList<MileageData> getMileageData() {
		return mileageData;
	}
}