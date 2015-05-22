package com.ving.gasmileage;

import android.content.Context;
import android.widget.ExpandableListView;

public class CustExpListView extends ExpandableListView {

	int intGroupPosition, intChildPosition, intGroupid;
	
	public CustExpListView(Context mpgListAdapter) {
		super(mpgListAdapter);     
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(960, MeasureSpec.AT_MOST);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(28800, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}  
}