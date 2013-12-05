package com.yulinghb.watermonitor.AppModel;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.WaterMonitorClientContract;

public class RecorderListModel {
//	private static final String TAG = "RecorderListModel";
	private long selected_id = 0;

	public interface RecorderSelectedListener{
		void selectRecorder(Intent data);
		void refresh(Cursor cursor);
	}
   
//	ListView list;
//	Cursor cursor;
	FragmentActivity mActivity;
	RecorderSelectedListener mListener;
	private RecorderListLoader loader;

	public RecorderListModel(Activity activity){
		mActivity = (FragmentActivity)activity;
		loader = new RecorderListLoader();
	}
	
//	public Cursor getCursor(){
////		cursor = mActivity.managedQuery(DataEntry.CONTENT_URI, PROJECTION, null, null,
////                RecorderContract.DEFAULT_SORT_ORDER);
//		
//		return null;
//	}
	
	public void update(){
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.RECORDER_LIST_LOADER, null, loader);
	}
	
	public void deleteOnClick(long deleteid){
		Uri noteUri = ContentUris.withAppendedId(DataEntry.CONTENT_URI, deleteid);
		mActivity.getContentResolver().delete(noteUri, null, null);
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.RECORDER_LIST_LOADER, null, loader);
	}
	
	public void editOnClick(){
		Uri noteUri = ContentUris.withAppendedId(DataEntry.CONTENT_URI, selected_id);
		Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
		intent.putExtra(RecorderEditModel.ACTION, Intent.ACTION_INSERT);
		intent.putExtra(RecorderEditModel.URI, noteUri.toString());
//		if (null == mListener){
//			mActivity.startActivity(intent);
//		}else{
			mListener.selectRecorder(intent);
//		}
	}
	
	public void selectedOnClick(long id){
		selected_id = id;
		
		if (null != mListener){
			Uri noteUri = ContentUris.withAppendedId(DataEntry.CONTENT_URI, selected_id);
			Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
			intent.putExtra(RecorderEditModel.ACTION, Intent.ACTION_EDIT);
			intent.putExtra(RecorderEditModel.URI, noteUri.toString());
			mListener.selectRecorder(intent);
		}
	}
	
	public void onAttach(RecorderSelectedListener listener){
		mListener = listener;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
//		updateSelectedLocation(data);
	}
	
	class RecorderListLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		 /**
	     * The columns we are interested in from the database
	     */
	    private final String[] PROJECTION = new String[] {
	        DataEntry._ID, // 0
	        DataEntry.COLUMN_NAME_TIME, // 1
	    };
	    
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			return new CursorLoader(mActivity, DataEntry.CONTENT_URI,
					PROJECTION, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			mListener.refresh(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			mListener.refresh(null);
		}
		
	}
}
