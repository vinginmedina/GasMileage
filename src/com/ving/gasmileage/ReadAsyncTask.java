package com.ving.gasmileage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class ReadAsyncTask extends AsyncTask<MyApplication, Integer, MyApplication> {
	
	private MainActivity mainAct;
	private Context mContext;
	private ProgressDialog pd = null;
	private String csvFile = null;
	private String errorMsg = null;
	private ReadMileageData rmd = null;
	
	ReadAsyncTask(String filename, MainActivity act, Context cntx) {
		csvFile = filename;
		mainAct = act;
		mContext = cntx;
	}
	
	@Override
	protected void onPreExecute() {
	    pd = new ProgressDialog(mContext);
	    pd.setIndeterminate(true);
	    pd.setIndeterminateDrawable(mainAct.getResources().getDrawable(R.drawable.progress_dialog_anim));
	    pd.setCancelable(false);
	    pd.setTitle("Reading");
	    pd.setMessage("Reading the CSV File...");
	    pd.show();
	}
	
	@Override
	protected MyApplication doInBackground(MyApplication... myApps) {
		rmd = new ReadMileageData(csvFile);
		if (! rmd.readFile()) {
			errorMsg = rmd.error();
			cancel(true);
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
		for (MileageData md : rmd.getMileageData()) {
			myApp.addData(md);
		}
		myApp.notifyDataSetChanged(true);
		myApp.setFileState(true);
		pd.dismiss();
	}

}
