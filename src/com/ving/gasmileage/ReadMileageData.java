package com.ving.gasmileage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.ving.gasmileage.MyApplication.MileageData;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


public class ReadMileageData extends AsyncTask<MyApplication, Integer, MyApplication> {
	private Context mContext;
	private String file = null;
	private File dir = null;
	private File myFile = null;
	private ProgressDialog pd = null;
	private String parsingErrors = "";
	private String errorMsg = null;

	ReadMileageData(Context context, String fileName) {
		Log.i("ReadMileageData","Starting");
		mContext = context;
	    file = fileName;
	    dir = new File(Environment.getExternalStorageDirectory().toString(),"/GasMileage");
	    myFile = new File(dir, file);
	}

	protected void onPreExecute() {
	    pd = ProgressDialog.show(mContext, "Reading", "Reading Data from "+file);
	}

	protected MyApplication doInBackground(MyApplication... myApps) {
		MileageData md;
		Log.i("ReadMileageData","In doInBackground "+myFile.toString());
		try {
			FileInputStream iStream =  new FileInputStream(myFile);
			InputStreamReader iStreamReader = new InputStreamReader(iStream);
			BufferedReader myReader = new BufferedReader(iStreamReader);
			String row = "";
			while ((row = myReader.readLine()) != null) {
				String[] rowData = row.split(",");
				try {
					md = myApps[0].addData(rowData);
				} catch (Exception e) {
//					Log.e("ReadMileageData","Error: "+e.toString());
					parsingErrors += "\n" + row + "\n" + e.toString() + "\n";
				}
			}
			myReader.close();
		}catch(Exception e) {
			Log.e("ReadMileageData","Error Processing file "+e.toString());
			errorMsg = e.toString();
			cancel(true);
		}
		if (! isCancelled()) {
			myApps[0].calcValues();
			Log.i("ReadMileageData","Abount to set up YearList");
//			myApps[0].setupYearList();
		}
		return myApps[0];
	}

	protected void onProgressUpdate(Integer... progress) {

    }
	
	protected void onCancelled(MyApplication myApp) {
		pd.cancel();
		myApp.setFileState(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder.setTitle("No Data Retrieved");
	    builder.setMessage("Sorry, there was an error trying to read the file.\n" + errorMsg);
	    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int arg1) {
		            dialog.dismiss();
		        }});
	    builder.setCancelable(false);
	    AlertDialog myAlertDialog = builder.create();
	    myAlertDialog.show();
	}
	
	protected void onPostExecute(MyApplication myApp) {
		Log.i("ReadMileageData","Finishing");
		myApp.setFileState(true);
		myApp.notifyDataSetChanged(true);
		pd.cancel();
		if (! parsingErrors.equals("")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		    builder.setTitle("Error Parsing Data");
		    builder.setMessage("Sorry, there were error(s) parsing some of the data in the file.\n" + parsingErrors);
		    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int arg1) {
			            dialog.dismiss();
			        }});
		    builder.setCancelable(false);
		    AlertDialog myAlertDialog = builder.create();
		    myAlertDialog.show();
		}
	}
}