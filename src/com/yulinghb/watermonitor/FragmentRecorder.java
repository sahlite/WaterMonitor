package com.yulinghb.watermonitor;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yulinghb.watermonitor.FragmentDeletePrompt.OnDeleteListener;
import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.AppModel.RecorderEditModel;
import com.yulinghb.watermonitor.AppModel.RecorderEditModel.RecorderUpdateOnClick;
import com.yulinghb.watermonitor.AppModel.RecorderListModel;
import com.yulinghb.watermonitor.AppModel.RecorderListModel.RecorderSelectedListener;

/**
 * 记录信息列表
 */
public class FragmentRecorder extends Fragment implements OnItemClickListener,
				RecorderSelectedListener, RecorderUpdateOnClick, OnClickListener,  OnItemLongClickListener, OnDeleteListener{
//	private static final String TAG = "RecorderFragment";

	// list part
	private ListView lv_recorders;
//	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private RecorderListModel listModel;
	
	// item part
	private boolean hasRecorder;
	private TextView vTime;
    private TextView vTemp;
    private TextView vRDO;
    private TextView vSAT;
    private TextView vBaro;
    private TextView vLocation;
    private ImageView ivLocation;
    private Button bEmail;
    
    private RecorderEditModel model;
    private long delete_id;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		if ((hasRecorder)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
			menu.clear();
			MenuItem item = menu.add(getString(R.string.txt_send_email));
			item.setIcon(android.R.drawable.ic_menu_send);
//			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener(){

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					if (hasRecorder){
						model.emailOnClick();
					}
					return true;
				}
				
			});
		}
//		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(
				R.layout.fragment_recorder, container, false);

		listModel = new RecorderListModel(getActivity());
		listModel.onAttach(this);
		
//		cursor = listModel.getCursor();

		listModel.update();
		
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.noteslist_item, null,
              new String[] { DataEntry.COLUMN_NAME_TIME }, new int[] { android.R.id.text1 }, 0);
        
        lv_recorders = (ListView)rootView.findViewById(R.id.lv_recorders_list);
        lv_recorders.setAdapter(adapter);
        lv_recorders.setOnItemClickListener(this);
        lv_recorders.setOnItemLongClickListener(this);
        
        hasRecorder = false;
        if (null != rootView.findViewById(R.id.part_recorder)){
        	hasRecorder = true;
        	model = new RecorderEditModel(getActivity(), this);
            
            vTime = (TextView) rootView.findViewById(R.id.view_time_value);
            vTemp = (TextView) rootView.findViewById(R.id.view_detail_temp_value);
            vRDO = (TextView) rootView.findViewById(R.id.view_detail_rdo_value);
            vSAT = (TextView) rootView.findViewById(R.id.view_detail_rdo_sat_value);
            vBaro = (TextView) rootView.findViewById(R.id.view_detail_baro_value);
            vLocation = (TextView)rootView.findViewById(R.id.view_detail_location_name);
            ivLocation = (ImageView)rootView.findViewById(R.id.image_detail_location_image);
            bEmail = (Button) rootView.findViewById(R.id.btn_email);
            bEmail.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View v) {
    				// TODO Auto-generated method stub
    				model.emailOnClick();
    			}
            	
            });
            vLocation.setOnClickListener(this);
            ivLocation.setOnClickListener(this);
            
            model.update();
        }
		
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		// TODO Auto-generated method stub
//		recorder_id = id;
		listModel.selectedOnClick(id);
	}


	@Override
	public void selectRecorder(Intent data) {
		// TODO Auto-generated method stub

        if (hasRecorder){
        	getActivity().setIntent(data);
        	model.update();
        }else{
        	getActivity().startActivity(data);
        }
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		model.changeLocationOnClick();
	}


	@Override
	public void updateTimeOnClick(String time) {
		// TODO Auto-generated method stub
		vTime.setText(time);
	}

	@Override
	public void updateDOOnClick(String doValue) {
		// TODO Auto-generated method stub
		
		vRDO.setText(doValue);
	}

	@Override
	public void updateTempOnClick(String temperature) {
		// TODO Auto-generated method stub
		vTemp.setText(temperature);
	}

	@Override
	public void updateSATOnClick(String sat) {
		// TODO Auto-generated method stub
		vSAT.setText(sat);
	}

	@Override
	public void updateBaroOnClick(String baro) {
		// TODO Auto-generated method stub
		vBaro.setText(baro);
	}

	@Override
	public void updateLocationOnClick(String location) {
		// TODO Auto-generated method stub
		vLocation.setText(location);
	}

	@Override
	public void updateImageOnClick(Bitmap data) {
		// TODO Auto-generated method stub
		ivLocation.setImageBitmap(data);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
//		Intent intent = new Intent(getActivity(), ActivityRecorderDeleteDialog.class);
//		intent.putExtra(ActivityRecorderDeleteDialog.LOCATION_DELETE_ID, arg3);
//		getActivity().startActivity(intent);
		
		delete_id = arg3;
		FragmentDeletePrompt prompt = new FragmentDeletePrompt();
		prompt.setCallback(this);
		prompt.show(getFragmentManager(), getString(R.string.txt_delete_quire));
		
		return true;
	}


	@Override
	public void comfirm() {
		// TODO Auto-generated method stub
//		Uri noteUri = ContentUris.withAppendedId(DataEntry.CONTENT_URI, delete_id);
//		getActivity().getContentResolver().delete(noteUri, null, null);
		listModel.deleteOnClick(delete_id);
	}


	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getImageWidth() {
		// TODO Auto-generated method stub
		return ivLocation.getWidth();
	}


	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return ivLocation.getHeight();
	}


	@Override
	public void refresh(Cursor cursor) {
		// TODO Auto-generated method stub
		adapter.swapCursor(cursor);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if ((null != model)&&(Activity.RESULT_OK == resultCode)){
			switch(requestCode){
				case RecorderEditModel.INTENT_GET_LOCATION:
				{
					model.onActivityResult(requestCode, resultCode, data);
				}
				break;
			}
		}
		
	}

	@Override
	public void updateDefaultImage() {
		// TODO Auto-generated method stub
		ivLocation.setImageResource(R.drawable.logo);
	}

	@Override
	public int getMessageId() {
		// TODO Auto-generated method stub
		return R.string.txt_delete_quire;
	}
	
}