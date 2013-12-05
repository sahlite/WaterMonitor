package com.yulinghb.watermonitor.AppModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import com.yulinghb.watermonitor.R;
import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.RecorderFile;
import com.yulinghb.watermonitor.WaterMonitorClientContract;
import com.yulinghb.watermonitor.DataManager.DataClient;
import com.yulinghb.watermonitor.service.DataFactory;

public class LocationEditModel{
	public static final int TAKE_PHOTO_CODE = 13;
	public static final int PICK_IMAGE_CODE = 14;
	public static final int PICK_LOCAT_CODE = 15;
	
	public static final String URI = "location edit uri";
	public static final String ACTION = "location edit action";

	private static final String TAG = "LocationEditModel";
	
	public interface LocationShowBaseInfo{
		void showinfo(String name, String description);
		void createlocation();
	}
	
	public interface LocationShowImage{
		void showImage(Bitmap data);
		void showDefaultImage();
		int getImageWidth();
		int getImageHeight();
	}
	
	public interface LocationShowGPS{
		void showGPS(double longtitude, double latitude);
	}

    

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    
	private Uri mUri;
	private Uri mImageUri;
//    private Cursor mCursor;
	private String sImagePath;
	
	private double vLatitude;
	private double vLongtitude;
	private LocationEditorClient client;
	
	LocationShowBaseInfo mInfoCallback;
	LocationShowImage mImageCallback;
	LocationShowGPS mGPSCallback;
	
	FragmentActivity mActivity;
	LocationEditorLoader mLocationLoader;
	ImageLoader mImageLoader;
	
	public LocationEditModel(Activity activity, Intent intent, LocationShowBaseInfo infoCallback,
			LocationShowImage imageCallback, LocationShowGPS gpsCallback){
        mInfoCallback = infoCallback;
        mImageCallback = imageCallback;
        mGPSCallback = gpsCallback;
        mActivity = (FragmentActivity)activity;
        
        mLocationLoader = new LocationEditorLoader();
        mImageLoader = new ImageLoader();
        client = new LocationEditorClient();
        DataFactory factory = DataFactory.getInstance();
        factory.putClient(client);
	}
	
	public void update(){
		
		Intent intent = mActivity.getIntent();
		final String action = intent.getStringExtra(ACTION);
        if (Intent.ACTION_EDIT.equals(action)) {
        	mState = STATE_EDIT;
        	
            // Requested to edit: set that state, and the data being edited.
            mUri = Uri.parse(intent.getStringExtra(URI));
            
            // Get the note!
//            mCursor = mActivity.managedQuery(mUri, PROJECTION, null, null, null);
            mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.LOCATION_EDITOR_LOADER, null, mLocationLoader);
        } else if (Intent.ACTION_INSERT.equals(action)) {
        	mState = STATE_INSERT;
        	
        	// Requested to edit: set that state, and the data being edited.
            mUri = intent.getData();
            
        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
        }
    	mInfoCallback.showinfo(mActivity.getString(R.string.txt_unkown), 
    			mActivity.getString(R.string.txt_unkown));
    	mImageCallback.showDefaultImage();
		mGPSCallback.showGPS(0, 0);
	}
	
	public void saveOnClick(String location, String descrption){
		if (null == mUri){
			return;
		}
		ContentValues values = new ContentValues();
		values.put(LocationEntry.COLUMN_NAME_LOCATION, location);
		values.put(LocationEntry.COLUMN_NAME_DESCRIPTION, descrption);
		values.put(LocationEntry.COLUMN_NAME_LATITUDE, vLatitude);
		values.put(LocationEntry.COLUMN_NAME_LONGTITUDE, vLongtitude);
		if ((null != sImagePath)&&("" != sImagePath))
			values.put(LocationEntry.COLUMN_NAME_IMAGE, sImagePath);
		if (STATE_EDIT == mState){
			mActivity.getContentResolver().update(mUri, values, null, null);
		}else if (STATE_INSERT == mState){
			mActivity.getContentResolver().insert(mUri, values);
		}
		mUri = null;
//		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.LOCATION_EDITOR_LOADER, null, loader);
		mInfoCallback.createlocation();
	}
	
	public void cancelOnClick(){
	}
	
	public void takePhotoOnClick(){
		File imageFile;
		try {
			imageFile = RecorderFile.createImageFile();
			sImagePath = imageFile.getAbsolutePath();
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra("output", Uri.fromFile(imageFile));
			final PackageManager packageManager = mActivity.getPackageManager();
		    List<ResolveInfo> list =
		            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		    if (list.size() > 0){
		    	mActivity.startActivityForResult(intent, TAKE_PHOTO_CODE);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void takeImageOnClick(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		mActivity.startActivityForResult(intent, PICK_IMAGE_CODE);	
	}
	
	public void getGPSOnClick(){
		client.sendLocationRequest();
	}

	public void onTakePhotoResult(Intent data){
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File(sImagePath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    mActivity.sendBroadcast(mediaScanIntent);
	    
	    showImage();
	}
	
	public void onTakeImageResult(Intent data){
		mImageUri = data.getData();   
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.IMAGE_LOADER, null, mImageLoader);
	}
	
	void showImage(){
		if ((null == sImagePath)||("" == sImagePath)){
			mImageCallback.showDefaultImage();
		}else{
			int targetW = mImageCallback.getImageWidth(); //300;//iImage.getWidth();
		    int targetH = mImageCallback.getImageHeight(); //225;//iImage.getHeight();

		    Bitmap bitmap = RecorderFile.getBitmap(mActivity, sImagePath, 
		    									mActivity.getString(R.string.txt_unkown), targetW, targetH);
		    mImageCallback.showImage(bitmap);
		}
	}
	
	class LocationEditorClient extends DataClient implements WaterMonitorClientContract{
		LocationEditorClient(){
			super(TYPE_LOCATION_INFO, ORITATION_IN);
		}

		@Override
		protected void doJob(Message msg) {
			if (-1 == msg.arg2){
				Toast.makeText(mActivity, mActivity.getString(R.string.txt_locating_fail), Toast.LENGTH_SHORT).show();
			}else if (0 == msg.arg2){
				Location location = (Location)msg.obj;
				if (null != location){
					vLatitude = location.getLatitude();
					vLongtitude = location.getLongitude();
					mGPSCallback.showGPS(vLongtitude, vLatitude);
				}
			}
		}
		
		void sendLocationRequest(){
			send(TYPE_LOCATION_INFO, ORITATION_OUT, -1, null);
		}

		@Override
		public void sendExpired() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class LocationEditorLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		/**
	     * Standard projection for the interesting columns of a normal note.
	     */
	    private final String[] PROJECTION = new String[] {
	        LocationEntry._ID, // 0
	        LocationEntry.COLUMN_NAME_LOCATION, //1
	        LocationEntry.COLUMN_NAME_DESCRIPTION, // 2
	        LocationEntry.COLUMN_NAME_IMAGE, // 3
	        LocationEntry.COLUMN_NAME_LATITUDE, // 4
	        LocationEntry.COLUMN_NAME_LONGTITUDE, // 4
	    };
	    
	    /** The index of the time column */
	    private static final int COLUMN_INDEX_LOCATION = 1;
	    /** The index of the temperature column */
	    private static final int COLUMN_INDEX_DESCRIPTION = 2;
	    /** The index of the depth column */
	    private static final int COLUMN_INDEX_IMAGE = 3;
	    /** The index of the rdo column */
	    private static final int COLUMN_INDEX_LATITUDE = 4;
	    /** The index of the rdo column */
	    private static final int COLUMN_INDEX_LONGTITUDE = 5;
	    
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub

			return new CursorLoader(mActivity, mUri,
					PROJECTION, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub

			if (0 < cursor.getCount()){
				cursor.moveToFirst();
				mInfoCallback.showinfo(cursor.getString(COLUMN_INDEX_LOCATION), 
						cursor.getString(COLUMN_INDEX_DESCRIPTION));
				sImagePath = cursor.getString(COLUMN_INDEX_IMAGE);
//				if ((null != sImagePath)&&("" != sImagePath)){
					showImage();
//				}
				
				vLatitude = cursor.getDouble(COLUMN_INDEX_LATITUDE);
				vLongtitude = cursor.getDouble(COLUMN_INDEX_LONGTITUDE);
				mGPSCallback.showGPS(vLongtitude, vLatitude);
			}else{
				
			}
            
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			mInfoCallback.showinfo(mActivity.getString(R.string.txt_unkown), 
        			mActivity.getString(R.string.txt_unkown));
			
			mGPSCallback.showGPS(0, 0);
		}
		
	}
	
	class ImageLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		String [] proj={MediaStore.Images.Media.DATA};  
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			return new CursorLoader(mActivity, mImageUri,
					proj, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  
	        cursor.moveToFirst();  
	          
	        sImagePath = cursor.getString(column_index);  
	        
	        showImage();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
		}
		
	}
}
