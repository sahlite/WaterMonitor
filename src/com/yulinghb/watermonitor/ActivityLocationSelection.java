package com.yulinghb.watermonitor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.AppModel.LocationListModel;
import com.yulinghb.watermonitor.AppModel.LocationListModel.LocationSelectedOnListener;

public class ActivityLocationSelection extends FragmentActivity implements OnItemClickListener, LocationSelectedOnListener{

	private Cursor cursor;
	private LocationListModel model;
	SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_list);
		model = new LocationListModel(this);
		model.onAttach(this);
//		cursor = model.getCursor();
		model.update();
        // Used to map notes entries from the database to views
		adapter = new SimpleCursorAdapter(this, R.layout.location_list_item, cursor,
              new String[] {LocationEntry.COLUMN_NAME_LOCATION}, new int[] {R.id.view_location_list_item_name}, 0);
        
        ListView list;
        list = (ListView)findViewById(R.id.lv_setting_location_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		model.selectedLocationOnClick(arg2);
		finish();
	}

	@Override
	public void refresh(Cursor cursor) {
		// TODO Auto-generated method stub
		adapter.swapCursor(cursor);
	}

	@Override
	public void selectLocation(Intent data) {
		// TODO Auto-generated method stub
		
	}

}
