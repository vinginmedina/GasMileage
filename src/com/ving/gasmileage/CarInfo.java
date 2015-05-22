package com.ving.gasmileage;

import java.io.File;
import java.util.ArrayList;

public class CarInfo {
	
	private ArrayList<String> cars = null;
	private ArrayList<String> csvFiles = null;
	private int current;
	
	CarInfo () {
		cars = new ArrayList<String>();
		csvFiles = new ArrayList<String>();
		current = -1;
	}
	
	public int size() {
		return cars.size();
	}
	
	public String prepCSVFileName(String csvFile) {
		
		String fileName = csvFile.replaceAll(",", "").replaceAll(";", "");
		char fileSep = File.separatorChar;
		char escape = '\\'; // ... or some other legal char.
		int len = fileName.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
		    char ch = fileName.charAt(i);
		    if (ch < ' ' || ch >= 0x7F || ch == fileSep
		        || (ch == '.' && i == 0) // we don't want to collide with "." or ".."!
		        || ch == escape) {
		        if (ch < 0x10) {
		            sb.append('0');
		        }
		        sb.append(Integer.toHexString(ch));
		    } else {
		        sb.append(ch);
		    }
		}
		String rtn = sb.toString();
		if (! rtn.endsWith(".csv")) {
			rtn += ".csv";
		}
		
		return rtn;
	}
	
	public Boolean add(String newCar, String newCsvFile) {
		Boolean canAdd = true;
		String carValue = newCar.replaceAll(";", "").replaceAll(",","");
		String csvValue = prepCSVFileName(newCsvFile);
		if ((cars.contains(carValue)) || (csvFiles.contains(csvValue))) {
			canAdd = false;
		}
		if (canAdd) {
			cars.add(carValue);
			csvFiles.add(csvValue);
		}
		
		return canAdd;
	}
	
	public void save(String saveData) {
		String dataArray[] = saveData.split(";");
		for (int i=0;i<dataArray.length;i++) {
			String carFile[] = dataArray[i].split(",");
			this.add(carFile[0], carFile[1]);
		}
	}
	
	public void delete(String car) {
		int i = cars.indexOf(car);
		if (i >= 0) {
			cars.remove(i);
			csvFiles.remove(i);
		}
		if (current == i) {
			current = -1;
		}
	}
	
	public void setCurrentCar(String car) {
		if (car != null) {
			int i = cars.indexOf(car);
			if (i >= 0) {
				current = i;
			}
		}
	}
	
	public String currentCar() {
		String rtn = null;
		if (current != -1) {
			rtn = cars.get(current);
		}
		
		return rtn;
	}
	
	public String csvFile() {
		String rtn = null;
		if (current != -1) {
			rtn = csvFiles.get(current);
		}
		
		return rtn;
	}
	
	public String csvFile(String car) {
		String csvFile = null;
		int i = cars.indexOf(car);
		if (i >= 0) {
			csvFile = csvFiles.get(i);
		}
		
		return csvFile;
	}
	
	public Boolean setCarName(String newCar) {
		Boolean didUpdate = false;
		if (current != -1) {
			String carValue = newCar.replaceAll(";", "").replaceAll(",","");
			didUpdate = true;
			for (int i = 0; i < cars.size(); i++) {
				if ((i != current) && (carValue.equals(cars.get(i)))) {
					didUpdate = false;
				}
			}
			if (didUpdate) {
				cars.set(current, carValue);
			}
		}
		
		return didUpdate;
	}
	
	public Boolean setCsvFile(String newCsvFile) {
		Boolean didUpdate = false;
		if (current != -1) {
			String csvValue = prepCSVFileName(newCsvFile);
			didUpdate = true;
			for (int i = 0; i < csvFiles.size(); i++) {
				if ((i != current) && (csvValue.equals(csvFiles.get(i)))) {
					didUpdate = false;
				}
			}
			if (didUpdate) {
				csvFiles.set(current, csvValue);
			}
		}
		
		return didUpdate;
	}
	
	public ArrayList<String> carList() {
		return cars;
	}
	
	public ArrayList<String> csvFileList() {
		return csvFiles;
	}

	public String toString() {
		String rtn = "";
		int i = 0;
		while (i < cars.size()) {
			if (! rtn.equals("")) {
				rtn += ";";
			}
			rtn += cars.get(i) + "," + csvFiles.get(i);
			i++;
		}
		
		return rtn;
	}
}
