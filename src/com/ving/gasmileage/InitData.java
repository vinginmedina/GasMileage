package com.ving.gasmileage;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InitData extends AsyncTask<MyApplication, Integer, MyApplication> {
	
	private Context mContext;
	private MainActivity mainAct;
	private SharedPreferences settings = null;
	private ProgressDialog pd = null;
	private String csvFile = null;
	private String errorMsg = null;
	private Boolean oldData = null;
	private ReadMileageData rmd = null;
	
	InitData(MainActivity act, Context context) {
		mainAct = act;
		mContext = context;
	    oldData = true;
	}

	@Override
	protected void onPreExecute() {
	    pd = new ProgressDialog(mContext);
	    pd.setIndeterminate(true);
	    pd.setIndeterminateDrawable(mainAct.getResources().getDrawable(R.drawable.progress_dialog_anim));
	    pd.setCancelable(false);
	    pd.setTitle("Initalizing");
	    pd.setMessage("Setting up Data");
	    pd.show();
	}

	@Override
	protected MyApplication doInBackground(MyApplication... myApps) {
		settings = myApps[0].getSharedPreferences();
		if (! myApps[0].baseDirectory().exists()) {
			if (! myApps[0].baseDirectory().mkdirs()) {
				errorMsg = "Unable to create directory "+myApps[0].baseDirectory().getPath();
				cancel(true);
			}
		}
		if (!isCancelled()) {
			String data = settings.getString("CarInfo", "");
			if (! data.equals("")) {
				myApps[0].carInfo().save(data);
				data = settings.getString("CurrentCar", "");
				myApps[0].carInfo().setCurrentCar(data);
				csvFile = myApps[0].carInfo().csvFile();
				oldData = false;
			}
			if (oldData) {
				csvFile = settings.getString("filename", "");
				if ((! csvFile.equals("")) && (! csvFile.endsWith(".csv"))) {
					String newCsvFile = csvFile + ".csv";
					File oldFile = new File(myApps[0].baseDirectory(),csvFile);
					File newFile = new File(myApps[0].baseDirectory(),newCsvFile);
					if (oldFile.exists()) {
						if (oldFile.renameTo(newFile)) {
							csvFile = newCsvFile;
						} else {
							errorMsg = "Error: cannot rename " + csvFile + 
									" to " + newCsvFile;
							cancel(true);
						}
					} else if (newFile.exists()) {
						csvFile = newCsvFile;
					}
				}
			}
			myApps[0].setFileState(false);
			if ((!isCancelled()) && (csvFile != null) && (! csvFile.equals(""))) {
				rmd = new ReadMileageData(csvFile);
				if (! rmd.readFile()) {
					errorMsg = rmd.error();
					cancel(true);
				}
			}
		}
		return myApps[0];
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
    }
	
	@Override
	protected void onCancelled(MyApplication myApp) {
		pd.dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder.setTitle("Error in Initalization");
	    builder.setMessage("Sorry, there was an error trying to initalize the data.\n\n" + errorMsg);
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int arg1) {
		            dialog.dismiss();
		        }});
	    builder.setCancelable(false);
	    AlertDialog myAlertDialog = builder.create();
	    myAlertDialog.show();
	}

	protected void onPostExecute(final MyApplication myApp) {
		if (rmd != null) {
			for (MileageData md : rmd.getMileageData()) {
				myApp.addData(md);
			}
			myApp.setFileState(true);
		} else {
			myApp.setFileState(false);
		}
		myApp.setChanged(false);
		myApp.notifyDataSetChanged(true);
		pd.dismiss();
		View dialogView = null;
		AlertDialog.Builder dialog = null;
		if ((oldData) && (! csvFile.equals(""))) {
			dialogView = myApp.getActivity().getLayoutInflater().inflate(R.layout.assign_name_dialog,null);
			Button pos = (Button)dialogView.findViewById(R.id.positiveB);
			final EditText et = (EditText)dialogView.findViewById(R.id.enterCarName);
			TextView fntv = (TextView)dialogView.findViewById(R.id.csvFile);
			fntv.setText(csvFile);
			dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogView);
            dialog.setTitle("Set the name of the car");
            dialog.setCancelable(false);
            final AlertDialog alertDialog = dialog.create();
			pos.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String carName = et.getText().toString();
					if (carName.equals("")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					    builder.setTitle("Must Enter a Car Name");
					    builder.setMessage("You must enter a name for this car before you can continue.");
					    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int arg1) {
						            dialog.dismiss();
						        }});
					    builder.setCancelable(false);
					    AlertDialog myAlertDialog = builder.create();
					    myAlertDialog.show();
					} else {
						myApp.carInfo().add(carName, csvFile);
						myApp.carInfo().setCurrentCar(carName);
						SaveConfiguration saveConfig = new SaveConfiguration(myApp);
						Thread saveConfigThread = new Thread(saveConfig);
						saveConfigThread.start();
						myApp.notifyDataSetChanged(true);
						alertDialog.dismiss();
					}
	            }
	        });
            alertDialog.show();
		} else if ((oldData) && (csvFile.equals(""))) {
			myApp.getActivity().addCarDialog();
		}
	}
}
