package com.ving.gasmileage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import android.os.Environment;

public class WriteMileageData implements Runnable {

	private MyApplication myApp = null;
	private String file = null;
	private File dir = null;
	private File myFile = null;
	private File backupFile = null;
	private Boolean canceled = false;
	private String errorMsg = null;

	WriteMileageData(String fileName, MyApplication myApp) {
		this.myApp = myApp;
	    file = fileName;
	    dir = new File(Environment.getExternalStorageDirectory().toString(),"/GasMileage");
	    myFile = new File(dir, file);
	    backupFile = new File(dir, file+".backup");
	}

	@Override
	public void run() {
		MileageData md;
		if (myFile.exists()) {
			try {
				InputStream iStream = new FileInputStream(myFile);
				OutputStream oStream = new FileOutputStream(backupFile);
				byte[] buffer = new byte[1024];
		        int read;
		        while ((read = iStream.read(buffer)) != -1) {
		            oStream.write(buffer, 0, read);
		        }
		        iStream.close();
		        iStream = null;
	            oStream.flush();
		        oStream.close();
		        oStream = null;
			}catch(Exception e) {
				canceled = true;
				errorMsg = e.toString();
				myApp.popUpMessage("No Data Written", "Sorry, there was an errror trying to backup the CSV file.\n"+errorMsg);
			}
		}
		if (! canceled) {
			try {
				FileOutputStream oStream =  new FileOutputStream(myFile);
				OutputStreamWriter oStreamWriter = new OutputStreamWriter(oStream);
				BufferedWriter myWriter = new BufferedWriter(oStreamWriter);
				myWriter.write(myApp.toCSV());
				myWriter.close();
			}catch(Exception e) {
				canceled = true;
				errorMsg = e.toString();
				myApp.popUpMessage("No Data Written", "Sorry, there was an error trying to write the file.\n" + errorMsg);
			}
		}
		if (! canceled) {
			myApp.setFileState(true);
			myApp.setChanged(false);
			myApp.toastMessage("Finished Saving CSV File "+file);
		}
	}

}