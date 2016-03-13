package com.ving.gasmileage;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {

	private static final int MY_PERMISSIONS_REQUEST_WRITE = 1;
	private static final int UNDEF = 0;
	private static final int SWITCH_CAR = 1;
	private static final int DELETE_CAR = 2;
	private static final int SELECT_FOR_EDIT = 3;
	private MyApplication myApp = null;
	private MainActivity myActivity = null;
	private Context mContext = null;
	private TextView carName = null;
	private TextView totalArea = null;
	private EditText editEntry = null;
	private ArrayAdapter<String> listAdapter = null;
	private ExpandableListView mExpandableList = null;
	private MPGListAdapter adapter = null;
	private SharedPreferences settings = null;
	private String dateToUse = null;
	private Button dateView = null;
	private int day;
	private int month;
	private int year;
	private Boolean continueFlag;
	private int currentFunction;
	private AlertDialog listViewDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myApp = (MyApplication) getApplication();
		setContentView(R.layout.activity_main);
		myApp.setChanged(false);
		myActivity = this;
		mContext = this;
		Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		dateToUse = (month+1) + "/" + day + "/" + year;
		myApp.setUp();
		carName = (TextView)findViewById(R.id.carName);
		totalArea = (TextView)findViewById(R.id.total);
		mExpandableList = (ExpandableListView)findViewById(R.id.result);
		setResultAdapter();
		settings = getPreferences(MODE_PRIVATE);
		myApp.setSharedPreferences(settings);
		if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
	                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
	                MY_PERMISSIONS_REQUEST_WRITE);
		} else {
			InitData initTask = new InitData(this, mContext);
			initTask.execute(myApp);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
	        String permissions[], int[] grantResults) {
	    switch (requestCode) {
	        case MY_PERMISSIONS_REQUEST_WRITE: {
	            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
	            	InitData initTask = new InitData(this, mContext);
	    			initTask.execute(myApp);
	            } else {
	            	AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
	            	errorDialog.setTitle("Without permission this application cannot function.");
            		errorDialog.setCancelable(false);
            		errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
    	                public void onClick(DialogInterface dialog,int id) {
    	                	dialog.dismiss();
    	                	finish();
    	                }
    	            });
            		errorDialog.show();
	            }
	            return;
	        }
	    }
	}
	
	public void setResultAdapter() {
		adapter = new MPGListAdapter(mContext,myApp.getExpArray(),myApp);
		mExpandableList.setAdapter(adapter);
		mExpandableList.setOnItemLongClickListener(new OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		    	int groupPosition = ExpandableListView.getPackedPositionGroup(id);
		    	int childPosition = ExpandableListView.getPackedPositionChild(id);
		        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
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
		            return true;
		        }
		        return false;
		    }
		});
		myApp.setStuff(adapter, carName, totalArea, mContext, myActivity, getResources().getString(R.string.external_dir));
	}
	
	public void onClickCallback (View target) {
		
		if (myApp.carInfo().currentCar() == null) {
			final AlertDialog.Builder noCarDialog = new AlertDialog.Builder(mContext);
			noCarDialog.setTitle("No Car Selected");
			if (myApp.carInfo().size() > 0) {
				noCarDialog.setMessage("There isn't an active car. Please select "	+
						getResources().getString(R.string.switchCar)	+
						" or " + getResources().getString(R.string.addNewCar) + " from the menu.");
			} else {
				noCarDialog.setMessage("There isn't an active car. Please select " +
						getResources().getString(R.string.addNewCar) + " from the menu to create a car.");
			}
			noCarDialog.setCancelable(false);
			noCarDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface di,int id) {
                	di.dismiss();
                }
            });
			noCarDialog.show();
		} else {
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
				final EditText notes = (EditText)dialogView.findViewById(R.id.notes);
				final CheckBox calcMiles = (CheckBox)dialogView.findViewById(R.id.calcCheckBox);
				calcMiles.setChecked(false);
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
			    calcMiles.setOnCheckedChangeListener(new OnCheckedChangeListener () {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (calcMiles.isChecked()) {
							String error = null;
							String lastOD = null;
							int lastODValue = 0;
							int currODValue = 0;
							if (od.getText().toString().equals("")) {
								error = "Current OD Value not set";
							} else {
								try {
									lastOD = myApp.lastOD(dateToUse);
									if (lastOD.equals("")) {
										error = "Couldn't find previous OD value";
									} else {
										lastODValue = Integer.parseInt(lastOD);
										currODValue = Integer.parseInt(od.getText().toString());
										if (currODValue <= lastODValue) {
											error = "Entered OD value (" + currODValue +
													") must be greater than previous OD value (" +
													lastODValue + ")";
										}
									}
								} catch (Exception e) {
									error = e.toString();
								}
							}
							if (error == null) {
								miles.setText(String.valueOf(currODValue - lastODValue));
								miles.setEnabled(false);
							} else {
								calcMiles.setChecked(false);
								final AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
					            errorDialog.setTitle("Error");
					            errorDialog.setMessage("Could not calculate the trip mileage.\n\n"+error);
					            errorDialog.setCancelable(false);
					            errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					                public void onClick(DialogInterface di,int id) {
					                	di.dismiss();
					                }
					            });
					            errorDialog.show();
							}
			    		} else {
			    			miles.setText("");
			    			miles.setEnabled(true);
			    		}
					}
			    });
	            datePicker.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
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
						String[] newData = new String[6];
						newData[0] = dateToUse;
						newData[1] = od.getText().toString();
						newData[2] = miles.getText().toString();
						newData[3] = gallons.getText().toString();
						newData[4] = cost.getText().toString();
						newData[5] = notes.getText().toString();
						MileageData md;
						try {
							md = new MileageData(newData);
							myApp.addData(md);
							myApp.setChanged(true);
						    myApp.notifyDataSetChanged(false);
						    enterDataDialog.dismiss();
						} catch (Exception e) {
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
			case R.id.seeCurrent:
				View dialogViewLast = getLayoutInflater().inflate(R.layout.last_fillup_dialog,null);
				Button okBtn = (Button)dialogViewLast.findViewById(R.id.positiveB);
				TextView lastFillup = (TextView)dialogViewLast.findViewById(R.id.last);
				lastFillup.setText(myApp.lastFillup());
				dialog = new AlertDialog.Builder(mContext);
				dialog.setView(dialogViewLast);
	            dialog.setCancelable(false);
				final AlertDialog lastFilldialog = dialog.create();
				lastFilldialog.setView(dialogViewLast);;
				lastFilldialog.setCancelable(false);
				okBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						lastFilldialog.dismiss();
					}
				});
				lastFilldialog.show();
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View target, int position,
			long id) {
		final String itemValue = listAdapter.getItem(position);
		switch (currentFunction) {
		case SELECT_FOR_EDIT:
			if ((editEntry != null) &&
					(! editEntry.getEditableText().toString().endsWith("Backup File"))) {
				editEntry.setText(itemValue);
			}
			break;
		case SWITCH_CAR:
			myApp.carInfo().setCurrentCar(itemValue);
			SaveConfiguration saveConfig = new SaveConfiguration(myApp);
			Thread saveConfigThread = new Thread(saveConfig);
			saveConfigThread.start();
			myApp.setUp();
	    	setResultAdapter();
	    	myApp.setChanged(false);
	    	listViewDialog.dismiss();
	    	ReadAsyncTask readTask = new ReadAsyncTask(myApp.carInfo().csvFile(), myActivity, mContext);
	    	readTask.execute(myApp);
	    	currentFunction = UNDEF;
	    	break;
		case DELETE_CAR:
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Do you really want to delete "+itemValue+"?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                	myApp.carInfo().delete(itemValue);
                	if (myApp.carInfo().currentCar() == null) {
                		myApp.setUp();
            	    	setResultAdapter();
            	    	myApp.setChanged(false);
            	    	myApp.notifyDataSetChanged(true);
                	}
                	currentFunction = UNDEF;
                	dialog.cancel();
                	listViewDialog.dismiss();
                	SaveConfiguration saveConfig = new SaveConfiguration(myApp);
					Thread saveConfigThread = new Thread(saveConfig);
					saveConfigThread.start();
                	if (myApp.carInfo().currentCar() == null) {
                		if (myApp.carInfo().size() > 0) {
                			switchCarDialog();
                		} else {
        	    			addCarDialog();
        	    		}
                	}
                }
            });
            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });
            dialog.show();
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
		boolean rtn = true;
		AlertDialog.Builder dialog = null;
		
		switch (item.getItemId()) {
		case R.id.setFileName:
			readFileDialog();
            break;
		case R.id.changeCarName:
			changeNameDialog();
			break;
		case R.id.addNewCar:
			addCarDialog();
		    break;
		case R.id.switchCar:
			switchCarDialog();
			break;
		case R.id.deleteCar:
			deleteCarDialog();
			break;
		case R.id.saveFile:
			if ((myApp.changed()) && (! myApp.carInfo().csvFile().equals(""))) {
				myApp.toastMessage("Saving CSV File "+myApp.carInfo().csvFile());
				WriteMileageData writeTask = new WriteMileageData(myApp.carInfo().csvFile(), myApp);
				Thread saveDataThread = new Thread(writeTask);
				saveDataThread.start();
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
				saveAsDialog();
			}
			break;
		case R.id.saveFileAs:
			saveAsDialog();
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
				String csvFileName = "file://" + Environment.getExternalStorageDirectory() +
						"/" + getResources().getString(R.string.external_dir) + "/" +
						myApp.carInfo().csvFile();
				Intent sendIntent = new Intent(Intent.ACTION_SEND); 
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Gas Mileage App Data for " + myApp.carInfo().currentCar());
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(csvFileName)); 
				sendIntent.setType("text/csv");
				try {
					startActivity(sendIntent);
				} catch (android.content.ActivityNotFoundException ex) {
				    myApp.toastMessage("There are no email clients installed.");
				}
			}
			break;
		case R.id.calcSavings:
			calcSavingsDialog();
			break;
		case R.id.about:
			View dialogViewAbout = getLayoutInflater().inflate(R.layout.about_dialog,null);
			Button okBtn = (Button)dialogViewAbout.findViewById(R.id.positiveB);
			TextView versionTV = (TextView)dialogViewAbout.findViewById(R.id.versionNum);
			PackageInfo pinfo;
			String version;
			try {
				pinfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
				version = "Version: " + pinfo.versionName;
			} catch (NameNotFoundException e) {
				version = "Version: N/A";
			}
			versionTV.setText(version);
			Button emailBtn = (Button)dialogViewAbout.findViewById(R.id.emailAddr);
			final String emailAddr = getResources().getString(R.string.emailAddr);
			emailBtn.setText(emailAddr);
			emailBtn.setTextColor(Color.BLUE);
			emailBtn.setPaintFlags(emailBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			Button webSiteBtn = (Button)dialogViewAbout.findViewById(R.id.webSite);
			final String webSite = getResources().getString(R.string.webpage);
			webSiteBtn.setText(webSite);
			webSiteBtn.setTextColor(Color.BLUE);
			webSiteBtn.setPaintFlags(emailBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			dialog = new AlertDialog.Builder(mContext);
			dialog.setView(dialogViewAbout);
            dialog.setCancelable(false);
			final AlertDialog versionDialog = dialog.create();
			versionDialog.setView(dialogViewAbout);;
			versionDialog.setCancelable(false);
			final String subject = "Gas Mileage App " + version;
			emailBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					versionDialog.dismiss();
					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {emailAddr});
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
					sendIntent.setType("message/rfc822");
					try {
						startActivity(sendIntent);
					} catch (android.content.ActivityNotFoundException ex) {
					    myApp.toastMessage("There are no email clients installed. "+ex.toString());
					}
				}
            });
			webSiteBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					versionDialog.dismiss();
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(webSite));
					startActivity(browserIntent);
				}
			});
			okBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					versionDialog.dismiss();
				}
			});
			versionDialog.show();
			break;
		default:
			rtn = super.onOptionsItemSelected(item);
			break;
		}
		return rtn;
	}
	
	public Boolean fileExists(String csvFile) {
		File file = null;
		if (csvFile.endsWith(".backup")) {
			file = new File(myApp.baseDirectory(), csvFile);
		} else {
			file = new File(myApp.baseDirectory(), myApp.carInfo().prepCSVFileName(csvFile));
		}
		Boolean rtn = file.canRead();
		
		return rtn;
	}
	
	public ArrayList<String> existCSVFiles() {
		ArrayList<String> existCSVFiles = new ArrayList<String>();
		File files[] = myApp.baseDirectory().listFiles();
		if ((files != null) && (files.length > 0)) {
			for (File file : files) {
				String fileName = file.getName();
				if ((! myApp.carInfo().csvFileList().contains(fileName)) && (fileName.endsWith(".csv"))) {
					existCSVFiles.add(fileName);
				}
			}
		}
		
		return existCSVFiles;
	}
	
	public void readFileDialog() {
		View dialogView = getLayoutInflater().inflate(R.layout.set_file_dialog,null);
		Button pos = (Button)dialogView.findViewById(R.id.positiveB);
		Button neg = (Button)dialogView.findViewById(R.id.negitiveB);
		editEntry = (EditText)dialogView.findViewById(R.id.enterFileName);
		editEntry.setText(myApp.carInfo().csvFile());
		final CheckBox rfCb = (CheckBox)dialogView.findViewById(R.id.replaceDataCB);
		final CheckBox ufCb = (CheckBox)dialogView.findViewById(R.id.useNewFileCB);
		final CheckBox rbCb = (CheckBox)dialogView.findViewById(R.id.restoreBackupCB);
		rbCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		       @Override
		       public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	   if (rbCb.isChecked()) {
		    		   rfCb.setChecked(true);
		    		   rfCb.setEnabled(false);
		    		   ufCb.setChecked(false);
		    		   ufCb.setEnabled(false);
		    		   editEntry.setText(myApp.carInfo().currentCar()+" Backup File");
		    		   editEntry.setEnabled(false);
		    	   } else {
		    		   rfCb.setEnabled(true);
		    		   ufCb.setEnabled(true);
		    		   editEntry.setText(myApp.carInfo().csvFile());
		    		   editEntry.setEnabled(true);
		    	   }
		       }
		   }
		);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, existCSVFiles());
		ListView listView = (ListView) dialogView.findViewById(R.id.fileList);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((OnItemClickListener) this);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogView);
        dialog.setTitle("Read in a new file or restore from a backup.");
        dialog.setCancelable(false);
        currentFunction = SELECT_FOR_EDIT;
        listViewDialog = dialog.create();
		pos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String fileName;
				if (rfCb.isChecked()) {
					fileName = myApp.carInfo().csvFile() + ".backup";
				} else {
					fileName = editEntry.getText().toString();
				}
				continueFlag = fileExists(fileName);
            	if ((continueFlag) && (ufCb.isChecked())) {
            		continueFlag = myApp.carInfo().setCsvFile(fileName);
            		if (continueFlag) {
            			SaveConfiguration saveConfig = new SaveConfiguration(myApp);
    					Thread saveConfigThread = new Thread(saveConfig);
    					saveConfigThread.start();
            		}
            	}
            	if (continueFlag) {
	    		    if (rfCb.isChecked()) {
	    		    	myApp.setUp();
	    		    	setResultAdapter();
	    		    	if (rfCb.isChecked()) {
	    		    		myApp.setChanged(true);
	    		    	} else {
	    		    		myApp.setChanged(false);
	    		    	}
	    		    } else {
	    		    	myApp.setChanged(true);
	    		    }
	    		    editEntry = null;
	    		    currentFunction = UNDEF;
	    		    listViewDialog.dismiss();
            		ReadAsyncTask readTask = new ReadAsyncTask(fileName, myActivity, mContext);
            		readTask.execute(myApp);
            	} else {
            		AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
            		if (rbCb.isChecked()) {
            			errorDialog.setTitle("Sorry, there is no backup file available.");
            		} else {
            			errorDialog.setTitle("The filename, "+fileName+", is either already in use by another car, or doesn't exist.");
            		}
            		errorDialog.setCancelable(false);
            		errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
    	                public void onClick(DialogInterface dialog,int id) {
    	                	dialog.dismiss();
    	                }
    	            });
            		errorDialog.show();
            	}
            }
        });
		neg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editEntry = null;
				currentFunction = UNDEF;
				listViewDialog.dismiss();
			}
		});
		listViewDialog.show();
	}
	
	public void changeNameDialog() {
		View dialogChangeName = getLayoutInflater().inflate(R.layout.change_name_dialog, null);
		Button pos = (Button)dialogChangeName.findViewById(R.id.positiveB);
		Button neg = (Button)dialogChangeName.findViewById(R.id.negitiveB);
		final EditText carEntry = (EditText)dialogChangeName.findViewById(R.id.enterCarName);
		carEntry.setText(myApp.carInfo().currentCar());
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogChangeName);
        dialog.setTitle("Change the name of the car");
        dialog.setCancelable(false);
        final AlertDialog chgNameDialog = dialog.create();
        pos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String carName = carEntry.getText().toString();
				if ((carName.equals(""))) {
					AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
					errorDialog.setTitle("You must enter a new name for the car or click Cancel.");
					errorDialog.setCancelable(false);
					errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.dismiss();
						}
					});
					errorDialog.show();
				} else {
					if (myApp.carInfo().setCarName(carName)) {
						SaveConfiguration saveConfig = new SaveConfiguration(myApp);
						Thread saveConfigThread = new Thread(saveConfig);
						saveConfigThread.start();
						myApp.notifyDataSetChanged(true);
						chgNameDialog.dismiss();
					} else {
						AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
						errorDialog.setTitle("That car name is already in use.");
						errorDialog.setCancelable(false);
						errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								dialog.dismiss();
							}
						});
						errorDialog.show();
					}
				}
			}
		});
        neg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				chgNameDialog.dismiss();
			}
		});
        chgNameDialog.show();
	}
	
	public void switchCarDialog() {
		if (myApp.changed()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Current File Not Saved, Switch Car Anyway?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Go Back and Save",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
            dialog.setNegativeButton("Clear Data and Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    switchCarHL();
                }
            });
            dialog.show();
		} else {
			switchCarHL();
		}
	}
	
	public void switchCarHL() {
		View dialogSwitchCar = getLayoutInflater().inflate(R.layout.pick_car_dialog, null);
		Button neg = (Button)dialogSwitchCar.findViewById(R.id.negitiveB);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, myApp.carInfo().carList());
		ListView listView = (ListView) dialogSwitchCar.findViewById(R.id.fileList);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((OnItemClickListener) this);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogSwitchCar);
        dialog.setTitle("Select the car to use.");
        dialog.setCancelable(false);
        listViewDialog = dialog.create();
        currentFunction = SWITCH_CAR;
        neg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
    		    currentFunction = UNDEF;
				listViewDialog.dismiss();
			}
		});
        listViewDialog.show();
	}
	
	public void addCarDialog() {
		if (myApp.changed()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Current File Not Saved, Add New Car Anyway?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Go Back and Save",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
            dialog.setNegativeButton("Clear Data and Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    addCarHL();
                }
            });
            dialog.show();
		} else {
			addCarHL();
		}
	}
	
	public void addCarHL() {
		View dialogAddCar = getLayoutInflater().inflate(R.layout.new_car_dialog, null);
		Button pos = (Button)dialogAddCar.findViewById(R.id.positiveB);
		Button neg = (Button)dialogAddCar.findViewById(R.id.negitiveB);
		final EditText carEntry = (EditText)dialogAddCar.findViewById(R.id.enterCarName);
		editEntry = (EditText)dialogAddCar.findViewById(R.id.csvFile);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, existCSVFiles());
		ListView listView = (ListView) dialogAddCar.findViewById(R.id.fileList);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((OnItemClickListener) this);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogAddCar);
		dialog.setTitle("Add A New Car");
		dialog.setCancelable(false);
		currentFunction = SELECT_FOR_EDIT;
		listViewDialog = dialog.create();
		pos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String carName = carEntry.getText().toString();
				final String csvFile = editEntry.getText().toString();
				if ((carName.equals("")) || (csvFile.equals(""))) {
					AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
					errorDialog.setTitle("You must enter both the car name and the csv file name.");
					errorDialog.setCancelable(false);
					errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.dismiss();
						}
					});
					errorDialog.show();
				} else {
					if (myApp.carInfo().add(carName, csvFile)) {
						myApp.carInfo().setCurrentCar(carName);
						SaveConfiguration saveConfig = new SaveConfiguration(myApp);
						Thread saveConfigThread = new Thread(saveConfig);
						saveConfigThread.start();
						myApp.setFileState(false);
						myApp.setUp();
						setResultAdapter();
						myApp.setChanged(false);
						myApp.notifyDataSetChanged(true);
						editEntry = null;
						currentFunction = UNDEF;
						listViewDialog.dismiss();
						if (fileExists(csvFile)) {
							ReadAsyncTask readTask = new ReadAsyncTask(csvFile, myActivity, mContext);
							readTask.execute(myApp);
						}
					} else {
						AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
						errorDialog.setTitle("Either the car name or csv file is already in use.");
						errorDialog.setCancelable(false);
						errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								dialog.dismiss();
							}
						});
						errorDialog.show();
					}
				}
			}
		});
		neg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				editEntry = null;
				currentFunction = UNDEF;
				listViewDialog.dismiss();
			}
		});
		listViewDialog.show();
	}
	
	public void saveAsDialog() {
		View dialogSaveAs = getLayoutInflater().inflate(R.layout.save_as_file_dialog,null);
		Button posSA = (Button)dialogSaveAs.findViewById(R.id.positiveB);
		Button negSA = (Button)dialogSaveAs.findViewById(R.id.negitiveB);
		final EditText etSA = (EditText)dialogSaveAs.findViewById(R.id.enterFileName);
		etSA.setText(myApp.carInfo().csvFile());
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogSaveAs);
        dialog.setTitle("Set the name of the CSV data file.");
        dialog.setCancelable(false);
        final AlertDialog saveAsDialog = dialog.create();
		posSA.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final String fileName = etSA.getText().toString();
            	if (myApp.carInfo().setCsvFile(fileName)) {
					myApp.toastMessage("Saving CSV File "+fileName);
					WriteMileageData writeTask = new WriteMileageData(fileName, myApp);
					Thread saveDataThread = new Thread(writeTask);
					saveDataThread.start();
					SaveConfiguration saveConfig = new SaveConfiguration(myApp);
					Thread saveConfigThread = new Thread(saveConfig);
					saveConfigThread.start();
	            	saveAsDialog.dismiss();
            	} else {
            		AlertDialog.Builder errorDialog = new AlertDialog.Builder(mContext);
            		errorDialog.setTitle("The filename, "+fileName+", is already in use.");
            		errorDialog.setCancelable(false);
            		errorDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
    	                public void onClick(DialogInterface dialog,int id) {
    	                	dialog.dismiss();
    	                }
    	            });
            		errorDialog.show();
            	}
            }
        });
		negSA.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveAsDialog.dismiss();
			}
		});
        saveAsDialog.show();
	}
	
	public void deleteCarDialog() {
		if (myApp.changed()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Current File Not Saved, Delete Car Anyway?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Go Back and Save",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
            dialog.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    deleteCarHL();
                }
            });
            dialog.show();
		} else {
			deleteCarHL();
		}
	}
	
	public void deleteCarHL() {
		View dialogSwitchCar = getLayoutInflater().inflate(R.layout.pick_car_dialog, null);
		Button neg = (Button)dialogSwitchCar.findViewById(R.id.negitiveB);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, myApp.carInfo().carList());
		ListView listView = (ListView) dialogSwitchCar.findViewById(R.id.fileList);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((OnItemClickListener) this);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setView(dialogSwitchCar);
        dialog.setTitle("Select the car to delete.");
        dialog.setCancelable(false);
        listViewDialog = dialog.create();
        currentFunction = DELETE_CAR;
        neg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
    		    currentFunction = UNDEF;
				listViewDialog.dismiss();
			}
		});
        listViewDialog.show();
	}
	
	public void calcSavingsDialog() {
		View dialogCalcSavings = getLayoutInflater().inflate(R.layout.calc_savings_dialog,null);
		Button posCS = (Button)dialogCalcSavings.findViewById(R.id.enter);
		Button negCS = (Button)dialogCalcSavings.findViewById(R.id.cancel);
		final EditText etCS = (EditText)dialogCalcSavings.findViewById(R.id.newMPG);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setContentView(R.layout.activity_main);
	    carName = (TextView)findViewById(R.id.carName);
		totalArea = (TextView)findViewById(R.id.total);
		mExpandableList = (ExpandableListView)findViewById(R.id.result);
		setResultAdapter();
		myApp.notifyDataSetChanged(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

}