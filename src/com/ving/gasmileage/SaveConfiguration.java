package com.ving.gasmileage;

import android.content.SharedPreferences;

public class SaveConfiguration implements Runnable {
	
	private MyApplication myApp = null;
	private SharedPreferences.Editor editor = null;
	
	SaveConfiguration (MyApplication app) {
		myApp = app;
		editor = myApp.getSharedPreferences().edit();
	}
	
	@Override
	public void run() {
		editor.clear();
		if (myApp.carInfo().size() > 0) {
			editor.putString("CarInfo",myApp.carInfo().toString());
			editor.putString("CurrentCar", myApp.carInfo().currentCar());
		}
		editor.commit();
		myApp.toastMessage("Configuration Saved.");
	}

}
