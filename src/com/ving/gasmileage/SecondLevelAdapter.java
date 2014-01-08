package com.ving.gasmileage;

import java.util.ArrayList;

import com.ving.gasmileage.MyApplication.YearData;
import com.ving.gasmileage.MyApplication.MonthData;
import com.ving.gasmileage.MyApplication.MileageData;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

public class SecondLevelAdapter extends BaseExpandableListAdapter {
	private ArrayList<MonthData> mMonthData;
	private LayoutInflater inflater;
	private Context mContext;
	private MyApplication myApp;
	
	public SecondLevelAdapter(Context context, ArrayList<MonthData> mthData, MyApplication app) {
		mContext = context;
		mMonthData = mthData;
		myApp = app;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {   
		return childPosition;
	}
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {   
		return childPosition;
	}
	
	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
//		TextView tv = new TextView(Home.this);
//		tv.setText("child");
//		tv.setPadding(15, 5, 5, 5);
//		tv.setBackgroundColor(Color.YELLOW);
//		tv.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		return tv;
		if (view == null) {
            view = inflater.inflate(R.layout.list_item_day, parent, false);
        }
		final MonthData mth = mMonthData.get(groupPosition);
		final MileageData md = mMonthData.get(groupPosition).getArrayDays().get(childPosition);
		TextView textView = (TextView) view.findViewById(R.id.list_day_text_view);
		textView.setText(md.toString());
		view.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
//				Log.i("getChildView","Did a long press");
				AlertDialog.Builder fillupDialog = new AlertDialog.Builder(mContext);
	            fillupDialog.setTitle(md.purchaseDateString()+" Fillup Info");
	            fillupDialog.setMessage(md.toFullString());
	            fillupDialog.setCancelable(false);
	            fillupDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface di,int id) {
	                	di.dismiss();
	                }
	            });
	            fillupDialog.setNegativeButton("Delete Entry",new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface di,int id) {
	            		myApp.deleteData(md, mth);
	            		myApp.setChanged(true);
	            		myApp.notifyDataSetChanged(true);
	            		di.dismiss();
	            	}
	            });
	            fillupDialog.show();
				return true;
			}
		});
		return view;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
//		Log.i("SecondLevelAdapter","Child Count "+groupPosition+": "+mMonthData.get(groupPosition).getArrayDays().size());
		return mMonthData.get(groupPosition).getArrayDays().size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {   
		return groupPosition;
	}
	
	@Override
	public int getGroupCount() {
//		Log.i("SecondLevelAdapter","Group Count: "+mMonthData.size());
		return mMonthData.size();
//		return 1;
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
		if (view == null) {
            view = inflater.inflate(R.layout.list_item_month, parent, false);
        }
	    TextView textView = (TextView) view.findViewById(R.id.list_month_text_view);
	    textView.setText(mMonthData.get(groupPosition).toString());
//	    view.setOnClickListener(new OnClickListener() {
//	    	public boolean onClick(View v) {
//	    		Log.i("GetGroupView","on click");
//	    		
//	    	}
//	    }
//	    view.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				Log.i("getGroupView","Did a long press");
//				return false;
//			}
//		});
	    return view;
	}
	
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
}
