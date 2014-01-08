package com.ving.gasmileage;

import java.util.ArrayList;

import com.ving.gasmileage.MyApplication.YearData;
import com.ving.gasmileage.MyApplication.MonthData;
import com.ving.gasmileage.CustExpListView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
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
    //counts the number of group/parent items so the list knows how many times calls getGroupView() method
    public int getGroupCount() {
//    	Log.i("MPGListAdapter","Group Count: "+mYearData.size());
        return mYearData.size();
    }
 
    @Override
    //counts the number of children items so the list knows how many times calls getChildView() method
    public int getChildrenCount(int i) {
//    	Log.i("MPGListAdapter","Child Count "+i+": "+mYearData.get(i).getArrayMonths().size());
//        return mYearData.get(i).getArrayMonths().size();
        return 1;
    }
 
    @Override
    //gets the title of each parent/group
    public Object getGroup(int i) {
        return mYearData.get(i);
    }
 
    @Override
    //gets the name of each item
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
    //in this method you must set the text to see the parent/group on the list
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
 
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_year, viewGroup,false);
        }
 
        TextView textView = (TextView) view.findViewById(R.id.list_year_text_view);
        //"i" is the position of the parent/group in the list
        textView.setText(getGroup(i).toString());
 
        //return the entire view
//        view.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				Log.i("getGroupMPGView","Did a long press");
//				return false;
//			}
//		});
        return view;
    }
 
    @Override
    //in this method you must set the text to see the children on the list
    public View getChildView(final int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        CustExpListView SecondLevelexplv = new CustExpListView(mContext);
        SecondLevelexplv.setAdapter(new SecondLevelAdapter(mContext, mYearData.get(i).getArrayMonths(),myApp));
        SecondLevelexplv.setGroupIndicator(null);
        
        SecondLevelexplv.setOnItemLongClickListener(new OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		    	int groupPosition = ExpandableListView.getPackedPositionGroup(id);
		        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
		            // You now have everything that you would as if this was an OnChildClickListener() 
		            // Add your logic here.
//		            Log.i("LongPress","did a long press "+groupPosition+" "+childPosition);
		        	MonthData md = mYearData.get(i).getArrayMonths().get(groupPosition);
		            AlertDialog.Builder monthDialog = new AlertDialog.Builder(mContext);
		            monthDialog.setTitle(md.getTitle()+" Results");
		            monthDialog.setMessage(md.toFullString());
		            monthDialog.setCancelable(false);
		            monthDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface di,int id) {
		                	di.dismiss();
		                }
		            });
		            monthDialog.show();

		            // Return true as we are handling the event.
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
        /* used to make the notifyDataSetChanged() method work */
        super.registerDataSetObserver(observer);
    }

}