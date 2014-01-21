package com.ving.gasmileage;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.ving.gasmileage.MyApplication;
import com.ving.gasmileage.MyApplication.MileageData;
import com.ving.gasmileage.MyApplication.YearData;

public class MainActivity extends Activity {

	private MyApplication myApp = null;
	private Context mContext = null;
	private TextView totalArea = null;
	private TextView lastArea = null;
	private ExpandableListView mExpandableList = null;
	private MPGListAdapter adapter = null;
	private SharedPreferences settings = null;
    private SharedPreferences.Editor editor = null;
	private String csvFile = null;
	private String dateToUse = null;
	private Button dateView = null;
	private int day;
	private int month;
	private int year;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (MyApplication) getApplication();
		setContentView(R.layout.activity_main);
		myApp.setChanged(false);
		mContext = this;
		Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		myApp.setDate(month, year);
		dateToUse = (month+1) + "/" + day + "/" + year;
		myApp.setUp();
		totalArea = (TextView)findViewById(R.id.total);
		lastArea = (TextView)findViewById(R.id.last);
		mExpandableList = (ExpandableListView)findViewById(R.id.result);
		setResultAdapter();
		settings = getPreferences(MODE_PRIVATE);
		editor = settings.edit();
		csvFile = settings.getString("filename", "");
		if (! csvFile.equals("")) {
			ReadMileageData readTask = new ReadMileageData(mContext, csvFile);
			readTask.execute(myApp);
		} else {
			myApp.setFileState(false);
		}
	}
	
	public void setResultAdapter() {
		adapter = new MPGListAdapter(mContext,myApp.getExpArray(),myApp);
		mExpandableList.setAdapter(adapter);
		mExpandableList.setOnItemLongClickListener(new OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		    	Log.i("onItemLongClick","Did a long click "+ExpandableListView.getPackedPositionType(id));
		    	int groupPosition = ExpandableListView.getPackedPositionGroup(id);
		    	int childPosition = ExpandableListView.getPackedPositionChild(id);
		        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		            // You now have everything that you would as if this was an OnChildClickListener() 
		            // Add your logic here.
//		            Log.i("LongPress","did a long press "+groupPosition+" "+childPosition);
		            YearData yd = myApp.getExpArray().get(groupPosition);
		            AlertDialog.Builder yearDialog = new AlertDialog.Builder(mContext);
		            yearDialog.setTitle(yd.getTitle()+" Results");
		            yearDialog.setMessage(yd.toFullString());
		            yearDialog.setCancelable(false);
		            yearDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface di,int id) {
		                	di.dismiss();
		                }
		            });
		            yearDialog.show();

		            // Return true as we are handling the event.
		            return true;
		        }
		        return false;
		    }
		});
		myApp.setExpandAdapter(adapter, totalArea, lastArea, mContext);
	}
	
	public void onClickCallback (View target) {
		
		switch (target.getId()) {
		case R.id.enter:
			View dialogView = getLayoutInflater().inflate(R.layout.add_data_dialog,null);
			final Button datePicker = (Button)dialogView.findViewById(R.id.dateVal);
			datePicker.setText(dateToUse);
			Button pos = (Button)dialogView.findViewById(R.id.enter);
			Button neg = (Button)dialogView.findViewById(R.id.cancel);
			final EditText od = (EditText)dialogView.findViewById(R.id.od);
			final EditText miles = (EditText)dialogView.findViewById(R.id.miles);
			final EditText gallons = (EditText)dialogView.findViewById(R.id.gallons);
			final EditText cost = (EditText)dialogView.findViewById(R.id.cost);
//			final TextView tv = (TextView)findViewById(R.id.result);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogView);
            dialog.setTitle("Enter the new set of mileage data.");
            dialog.setCancelable(false);
            final AlertDialog enterDataDialog = dialog.create();
            final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
				public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
					year = selectedYear;
					month = selectedMonth;
					day = selectedDay;
					dateToUse = (month+1) + "/" + day + "/" + year;
					datePicker.setText(dateToUse);
		        }
		    };
            datePicker.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Log.i("Main","About to call DatePickerDialog");
					DatePickerDialog dpd = new DatePickerDialog(mContext, datePickerListener, year, month, day);
					dpd.show();
				}
            });
            neg.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					enterDataDialog.dismiss();
				}
			});
            pos.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(od.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					String[] newData = new String[5];
					newData[0] = dateToUse;
					newData[1] = od.getText().toString();
					newData[2] = miles.getText().toString();
					newData[3] = gallons.getText().toString();
					newData[4] = cost.getText().toString();
					MileageData md;
					try {
						md = myApp.addData(newData);
						lastArea.setText(md.toFullString());
						myApp.setChanged(true);
					    myApp.notifyDataSetChanged(false);
					    enterDataDialog.dismiss();
					} catch (Exception e) {
						Log.i("Adding New Data","There was an error "+e.toString());
						final AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
			            errorDialog.setTitle("Error");
			            errorDialog.setMessage("There was an error with the value(s) supplied and a mileage record could not be created.\n\n"+e.toString());
			            errorDialog.setCancelable(false);
			            errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface di,int id) {
			                	di.dismiss();
			                }
			            });
			            errorDialog.show();
					}
				}
            });
			enterDataDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		boolean rtn;
		AlertDialog.Builder dialog = null;
		switch (item.getItemId()) {
		case R.id.setFileName:
			Log.i("onOptionsItemSelected","file select");
			View dialogView = getLayoutInflater().inflate(R.layout.setfiledialog,null);
			Button pos = (Button)dialogView.findViewById(R.id.positiveB);
			Button neg = (Button)dialogView.findViewById(R.id.negitiveB);
			final EditText et = (EditText)dialogView.findViewById(R.id.enterFileName);
			et.setText(csvFile);
			final CheckBox cb = (CheckBox)dialogView.findViewById(R.id.replaceDataCB);
			dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogView);
            dialog.setTitle("Set the name of the CSV data file.");
            dialog.setCancelable(false);
            final AlertDialog alertDialog = dialog.create();
			pos.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Log.i("setFileName","pos.OnClickListener");
	            	csvFile = et.getText().toString();
	            	editor.putString("filename", csvFile);
	    		    editor.commit();
	    		    if (cb.isChecked()) {
	    		    	Log.i("setFileName","Selection is checked, erasing all existing data.");
	    		    	myApp.setUp();
	    		    	setResultAdapter();
	    		    } else {
	    		    	Log.i("setFileName","Selection is not checked, keeping all existing data.");
	    		    }
	            	ReadMileageData readTask = new ReadMileageData(mContext, csvFile);
	            	readTask.execute(myApp);
	            	alertDialog.dismiss();
	            }
	        });
			neg.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					alertDialog.dismiss();
				}
			});
            alertDialog.show();
            rtn = true;
            break;
		case R.id.resetFileName:
			csvFile = "";
			editor.putString("filename", csvFile);
		    editor.commit();
		    myApp.setFileState(false);
		    rtn = true;
		    break;
		case R.id.saveFile:
			if ((myApp.changed()) && (! csvFile.equals(""))) {
				WriteMileageData writeTask = new WriteMileageData(mContext, csvFile);
				writeTask.execute(myApp);
			} else if (! myApp.changed()) {
				dialog = new AlertDialog.Builder(mContext);
	            dialog.setTitle("No Changes to Save");
	            dialog.setCancelable(false);
	            dialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog,int id) {
	                	dialog.dismiss();
	                }
	            });
	            dialog.show();
			} else {
				View dialogSaveAs = getLayoutInflater().inflate(R.layout.saveasfiledialog,null);
				Button posSA = (Button)dialogSaveAs.findViewById(R.id.positiveB);
				Button negSA = (Button)dialogSaveAs.findViewById(R.id.negitiveB);
				final EditText etSA = (EditText)dialogSaveAs.findViewById(R.id.enterFileName);
				etSA.setText(csvFile);
				dialog = new AlertDialog.Builder(mContext);
				dialog.setView(dialogSaveAs);
	            dialog.setTitle("Set the name of the CSV data file.");
	            dialog.setCancelable(false);
	            final AlertDialog saveAsDialog = dialog.create();
				posSA.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Log.i("setFileName","pos.OnClickListener");
		            	csvFile = etSA.getText().toString();
		            	editor.putString("filename", csvFile);
		    		    editor.commit();
		    		    WriteMileageData writeTask = new WriteMileageData(mContext, csvFile);
						writeTask.execute(myApp);
		            	saveAsDialog.dismiss();
		            }
		        });
				negSA.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						saveAsDialog.dismiss();
					}
				});
	            saveAsDialog.show();
			}
			rtn = true;
			break;
		case R.id.saveFileAs:
			Log.i("onOptionsItemSelected","save file as...");
			View dialogSaveAs = getLayoutInflater().inflate(R.layout.saveasfiledialog,null);
			Button posSA = (Button)dialogSaveAs.findViewById(R.id.positiveB);
			Button negSA = (Button)dialogSaveAs.findViewById(R.id.negitiveB);
			final EditText etSA = (EditText)dialogSaveAs.findViewById(R.id.enterFileName);
			etSA.setText(csvFile);
			dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogSaveAs);
            dialog.setTitle("Set the name of the CSV data file.");
            dialog.setCancelable(false);
            final AlertDialog saveAsDialog = dialog.create();
			posSA.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Log.i("setFileName","pos.OnClickListener");
	            	csvFile = etSA.getText().toString();
	            	editor.putString("filename", csvFile);
	    		    editor.commit();
	    		    WriteMileageData writeTask = new WriteMileageData(mContext, csvFile);
					writeTask.execute(myApp);
	            	saveAsDialog.dismiss();
	            }
	        });
			negSA.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					saveAsDialog.dismiss();
				}
			});
            saveAsDialog.show();
            rtn = true;
			break;
		case R.id.emailFile:
			if (myApp.changed()) {
				dialog = new AlertDialog.Builder(mContext);
	            dialog.setTitle("Save the file first.");
	            dialog.setCancelable(false);
	            dialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface di,int id) {
	                	di.cancel();
	                }
	            });
	            dialog.show();
			} else if (myApp.fileOK()) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND); 
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Gas Mileage App Data");
//				File dir = new File(Environment.getExternalStorageDirectory().toString(),"/GasMileage");
//				File myFile = new File(dir,csvFile);
				sendIntent.putExtra(Intent.EXTRA_STREAM,
						Uri.parse("file://"+Environment.getExternalStorageDirectory()+"/GasMileage/"+csvFile)); 
				sendIntent.setType("text/csv");
				startActivity(sendIntent);
			}
			rtn = true;
			break;
		case R.id.calcSavings:
			View dialogCalcSavings = getLayoutInflater().inflate(R.layout.calc_savings_dialog,null);
			Button posCS = (Button)dialogCalcSavings.findViewById(R.id.enter);
			Button negCS = (Button)dialogCalcSavings.findViewById(R.id.cancel);
			final EditText etCS = (EditText)dialogCalcSavings.findViewById(R.id.newMPG);
			if (etCS == null) {
				Log.e("calcSavings","etCS is null");
			}
			dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogCalcSavings);
            dialog.setTitle("Caclulate Savings");
            dialog.setCancelable(false);
            final AlertDialog calcSavingsDialog = dialog.create();
			posCS.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etCS.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					float newMPG = Float.parseFloat(etCS.getText().toString());
	            	float savings = myApp.savings(newMPG);
	            	calcSavingsDialog.dismiss();
	            	final AlertDialog.Builder saveDialog = new AlertDialog.Builder(mContext);
		            saveDialog.setTitle("Savings Result");
		            saveDialog.setMessage("With an MPG of "+String.format("%.1f", newMPG)+
		            		", the difference in cost would have been about $"+String.format("%.2f", savings));
		            saveDialog.setCancelable(false);
		            saveDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface di,int id) {
		                	di.dismiss();
		                }
		            });
		            saveDialog.show();
	            }
	        });
			negCS.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					calcSavingsDialog.dismiss();
				}
			});
            calcSavingsDialog.show();
			rtn = true;
			break;
		default:
			rtn = super.onOptionsItemSelected(item);
			break;
		}
		return rtn;
	}
	
	
	
	@Override
	public void onBackPressed() {
		if (myApp.changed()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("File Not Saved, Exit Anyway?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Go Back and Save",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
            dialog.setNegativeButton("Exit Anyway", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    finish();
                }
            });
            dialog.show();
		} else {
			finish();
		}
	}
	
//	@Override
//	public void onResume() {
//		EditText et = null;
//		
//		super.onResume();
//		Log.i("onResume","Resting stuff");
//		myApp = (MyApplication) getApplication();
//		myApp.clearExpandAdapter();
//		setResultAdapter();
//		et = (EditText)findViewById(R.id.od);
//		et.setText("");
//		et = (EditText)findViewById(R.id.miles);
//		et.setText("");
//		et = (EditText)findViewById(R.id.gallons);
//		et.setText("");
//		et = (EditText)findViewById(R.id.cost);
//		et.setText("");
//	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i("onPause","in onPause");
//		if (myApp.changed()) {
//			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//            dialog.setTitle("File Not Saved, Exit Anyway?");
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("Go Back and Save",new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog,int id) {
//                	dialog.cancel();
//                }
//            });
//            dialog.setNegativeButton("Exit Anyway", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog,int id) {
//                    finish();
//                }
//            });
//            dialog.show();
//		}
	}
	
//	@Override
//    protected void onStop() {
//	    super.onStop();	
//	    if ((csvFile != null) && (! csvFile.equals(""))) {
//		    editor.putString("filename", csvFile);
//		    editor.commit();
//	    }
//    }

}