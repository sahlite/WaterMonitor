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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yulinghb.watermonitor.FragmentDeletePrompt.OnDeleteListener;
import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.AppModel.LocationEditModel;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowBaseInfo;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowGPS;
import com.yulinghb.watermonitor.AppModel.LocationEditModel.LocationShowImage;
import com.yulinghb.watermonitor.AppModel.LocationListModel;
import com.yulinghb.watermonitor.AppModel.LocationListModel.LocationSelectedOnListener;

public class FragmentLocation extends Fragment implements OnItemClickListener, OnItemLongClickListener,
				LocationSelectedOnListener, LocationShowBaseInfo, LocationShowImage, LocationShowGPS, OnDeleteListener{
	// list part
	private Button btn_create;
	private ListView lv_locations;
	private LocationListModel listModel;
	
	// item part
	private boolean hasLocation;
	private EditText etLocation;
	private EditText etDescription;
	private Button bSave;
	private ImageButton bCamera;
	private ImageButton bImage;
	private ImageButton bGPS;
	private ImageView iImage;
	private TextView vtLatitude;
	private TextView vtLongtitude;
	private long delete_id;
	
	LocationEditModel model;
	SimpleCursorAdapter adapter;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			menu.clear();
			MenuItem item = menu.add(getString(R.string.txt_create));
			item.setIcon(android.R.drawable.ic_menu_add);
//			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					listModel.createOnClick();
					return true;
				}
			});
			
			if (hasLocation){
				item = menu.add(getString(R.string.txt_save));
				item.setIcon(R.drawable.ic_save);
//				item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						model.saveOnClick(etLocation.getText().toString(), etDescription.getText().toString());
						return true;
					}
				});
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		View rootView = inflater.inflate(
				R.layout.fragment_location, container, false);
		
		// list part which must be here
		listModel = new LocationListModel(getActivity());
		listModel.onAttach(this);
		
		btn_create = (Button)rootView.findViewById(R.id.btn_create_location);
		btn_create.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listModel.createOnClick();
			}
		});

        // Used to map notes entries from the database to views
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.noteslist_item, null,
	              new String[] { LocationEntry.COLUMN_NAME_LOCATION }, new int[] { android.R.id.text1 }, 0);
        
        lv_locations = (ListView)rootView.findViewById(R.id.lv_setting_location_list);
        lv_locations.setAdapter(adapter);
        lv_locations.setOnItemClickListener(this);
        lv_locations.setOnItemLongClickListener(this);
        
//        listModel.update();
        // item part which is only available in large screen
        hasLocation = false;
        if (null != rootView.findViewById(R.id.part_location)){
        	hasLocation = true;
        	model = new LocationEditModel(getActivity(), getActivity().getIntent(), this ,this ,this);
    		
    		etLocation = (EditText)rootView.findViewById(R.id.et_salinity);
    		etDescription = (EditText)rootView.findViewById(R.id.editText3);
    		bSave = (Button)rootView.findViewById(R.id.btn_save);
    		if (null != bSave){
    			bSave.setOnClickListener(new View.OnClickListener() {
        			@Override
        			public void onClick(View v) {
        				model.saveOnClick(etLocation.getText().toString(), etDescription.getText().toString());
        			}
        		});
    		}  		
    		
    		bCamera = (ImageButton)rootView.findViewById(R.id.btn_camera);
    		bCamera.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				model.takePhotoOnClick();
    			}
    		});
    		
    		bImage = (ImageButton)rootView.findViewById(R.id.btn_image);
    		bImage.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				model.takeImageOnClick();
    			}
    		});
    		iImage = (ImageView)rootView.findViewById(R.id.iv_pic);
    		
    		bGPS = (ImageButton)rootView.findViewById(R.id.btn_gps);
    		bGPS.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				model.getGPSOnClick();
    			}
    		});
    		
    		vtLatitude = (TextView)rootView.findViewById(R.id.view_latitude_value);
    		vtLongtitude = (TextView)rootView.findViewById(R.id.view_longtitude_value);
    		
//    		model.update();
        }

		return rootView;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		listModel.update();
//		if (hasLocation){
//			model.update();
//		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (hasLocation){
//			model.onPause();
		}
	}

	public void update(){
		if (hasLocation){
			model.update();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (Activity.RESULT_OK == resultCode){
			switch(requestCode){
				case LocationEditModel.TAKE_PHOTO_CODE:
				{
					model.onTakePhotoResult(data);
				}
				break;
				
				case LocationEditModel.PICK_IMAGE_CODE:
				{
					model.onTakeImageResult(data);
				}
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	@Override
	public void onItemClick(AdapterView<?> adpater, View v, int position, long id) {
		// TODO Auto-generated method stub
		listModel.selectedLocationOnClick(position);
	}

	@Override
	public void showGPS(double longtitude, double latitude) {
		// TODO Auto-generated method stub
		vtLatitude.setText(String.valueOf(latitude));
		vtLongtitude.setText(String.valueOf(longtitude));
	}

	@Override
	public void showImage(Bitmap data) {
		// TODO Auto-generated method stub
		iImage.setImageBitmap(data);
	}

	@Override
	public void showinfo(String name, String description) {
		// TODO Auto-generated method stub
		etLocation.setText(name);
		etDescription.setText(description);
	}

	@Override
	public void selectLocation(Intent data) {
		// TODO Auto-generated method stub
        if (hasLocation){
        	getActivity().setIntent(data);
        	model.update();
        }else{
        	getActivity().startActivity(data);
        }
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		delete_id = arg3;
		FragmentDeletePrompt prompt = new FragmentDeletePrompt();
		prompt.setCallback(this);
		prompt.show(getFragmentManager(), getString(R.string.txt_delete_quire));
		
		return true;
	}

	@Override
	public void comfirm() {
		// TODO Auto-generated method stub
		listModel.deleteOnClick(delete_id);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public int getImageWidth() {
		// TODO Auto-generated method stub
		return iImage.getWidth();
	}

	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return iImage.getHeight();
	}

	@Override
	public void refresh(Cursor cursor) {
		// TODO Auto-generated method stub
		adapter.swapCursor(cursor);
	}

	@Override
	public void createlocation() {
		// TODO Auto-generated method stub
		listModel.update();
	}

	@Override
	public void showDefaultImage() {
		// TODO Auto-generated method stub
		iImage.setImageResource(R.drawable.logo);
	}

	@Override
	public int getMessageId() {
		// TODO Auto-generated method stub
		return R.string.txt_delete_quire;
	}
}
