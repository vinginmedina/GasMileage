package com.ving.gasmileage;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

public class MPGListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private LayoutInflater inflater;
    private ArrayList<YearData> mYearData;
    private MyApplication myApp;
 
    public MPGListAdapter(Context context, ArrayList<YearData> yearData, MyApplication app){
    	mContext = context;
        mYearData = yearData;
        myApp = app;
        inflater = LayoutInflater.from(context);
    }
 
    @Override
    public int getGroupCount() {
        return mYearData.size();
    }
 
    @Override
    public int getChildrenCount(int i) {
        return 1;
    }
 
    @Override
    public Object getGroup(int i) {
        return mYearData.get(i);
    }
 
    @Override
    public Object getChild(int i, int i1) {
        return mYearData.get(i).getArrayMonths().get(i1);
    }
 
    @Override
    public long getGroupId(int i) {
        return i;
    }
 
    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }
 
    @Override
    public boolean hasStableIds() {
        return true;
    }
 
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
 
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_year, viewGroup,false);
        }
 
        TextView textView = (TextView) view.findViewById(R.id.list_year_text_view);
        textView.setText(getGroup(i).toString());
 
        return view;
    }
 
    @Override
    public View getChildView(final int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        CustExpListView SecondLevelexplv = new CustExpListView(mContext);
        SecondLevelexplv.setAdapter(new SecondLevelAdapter(mContext, mYearData.get(i).getArrayMonths(),myApp));
        SecondLevelexplv.setGroupIndicator(null);
        
        SecondLevelexplv.setOnItemLongClickListener(new OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		    	int groupPosition = ExpandableListView.getPackedPositionGroup(id);
		        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		        	MonthData md = mYearData.get(i).getArrayMonths().get(groupPosition);
		            AlertDialog.Builder monthDialog = new AlertDialog.Builder(mContext);
		            monthDialog.setTitle(md.getTitle()+" "+mYearData.get(i).getTitle()+" Results");
		            monthDialog.setMessage(md.toFullString());
		            monthDialog.setCancelable(false);
		            monthDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface di,int id) {
		                	di.dismiss();
		                }
		            });
		            monthDialog.show();

		            return true;
		        }
		        return false;
		    }
		});
        return SecondLevelexplv;
    }
 
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
 
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

}