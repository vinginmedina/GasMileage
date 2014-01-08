package com.ving.gasmileage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import com.ving.gasmileage.MyApplication.MileageData;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class WriteMileageData extends AsyncTask<MyApplication, Integer, MyApplication> {
	Context mContext;
	private String file = null;
	private File dir = null;
	private File myFile = null;
	ProgressDialog pd = null;
	public String errorMsg = null;

	WriteMileageData(Context context, String fileName) {
		Log.i("WriteMileageData","Starting");
		mContext = context;
	    file = fileName;
	    dir = new File(Environment.getExternalStorageDirectory().toString(),"/GasMileage");
	    myFile = new File(dir, file);
	}

	protected void onPreExecute() {
	    pd = ProgressDialog.show(mContext, "Writing", "Writing Data to "+file);
	}

	protected MyApplication doInBackground(MyApplication... myApps) {
		MileageData md;
		Log.i("WriteMileageData","In doInBackground "+myFile.toString());
		try {
			dir.mkdirs();
			FileOutputStream iStream =  new FileOutputStream(myFile);
			OutputStreamWriter iStreamWriter = new OutputStreamWriter(iStream);
			BufferedWriter myWriter = new BufferedWriter(iStreamWriter);
			myWriter.write(myApps[0].toCSV());
			myWriter.close();
		}catch(Exception e) {
			Log.e("WriteMileageData","Error Processing file "+e.toString());
			errorMsg = e.toString();
			cancel(true);
		}
		return myApps[0];
	}

	protected void onProgressUpdate(Integer... progress) {

    }
	
	protected void onCancelled(MyApplication myApp) {
		pd.cancel();
		myApp.setFileState(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder.setTitle("No Data Written");
	    builder.setMessage("Sorry, there was an error trying to write the file.\n" + errorMsg);
	    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int arg1) {
	            dialog.dismiss();
	        }});
	    builder.setCancelable(false);
	    AlertDialog myAlertDialog = builder.create();
	    myAlertDialog.show();
	}
	
	protected void onPostExecute(MyApplication myApp) {
		Log.i("WriteMileageData","Finishing");
		myApp.setFileState(true);
		myApp.setChanged(false);
		pd.cancel();
	}
}