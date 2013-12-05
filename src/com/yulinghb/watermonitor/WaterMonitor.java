package com.yulinghb.watermonitor;

import java.util.Locale;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.service.DataFactory;


/*
 * 水质监测应用                     主应用
 * 根据face选择初始化分页界面或整合界面
 * 分页界面为仿照IN-SITU，横屏滑动的界面
 * 整合界面为主要信息在一起，其余操作放到菜单中
 * 
 */
public class WaterMonitor extends FragmentActivity implements OnClickListener{
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	/*
	 * 记录上次选择的地点信息
	 */
	public static final String MINE_LOCATION_ID = "my location id";
	public static final String MINE_LOCATION_NAME = "my location name";
	public static final String MINE_LOCATION_IMAGE_PATH = "my location image path";
	
	public static final int INTENT_GET_LOCATION_INFO = 10;
	
	public static final int FRAGMENT_LOCATION = 0;
	public static final int FRAGMENT_MONITOR = 1;
	public static final int FRAGMENT_RECORDER = 2;
	public static final int FRAGMENT_CALIBRATION = 3;
	
	/*
	 * 分页界面的容器
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	DataFactory factory;
    FragmentMonitor monitor;
    FragmentLocation location;
    FragmentRecorder recorder;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			setContentView(R.layout.water_monitor);
			
			mViewPager = (ViewPager) findViewById(R.id.pager);
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			
			if (null == mViewPager){
				mViewPager = (ViewPager) findViewById(R.id.pager_large);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}else{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}

			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());

			// Set up the ViewPager with the sections adapter.
			
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
			// When swiping between different sections, select the corresponding
			// tab. We can also use ActionBar.Tab#select() to do this if we have
			// a reference to the Tab.
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
						}
					});
			mViewPager.setCurrentItem(1);
			factory = DataFactory.getInstance();
			factory.init(this);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		factory.onStart();
		
	}

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		factory.onStop();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.water_monitor, menu);
		return false;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return super.onMenuItemSelected(featureId, item);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (10 < requestCode){
			if (null != monitor){
				monitor.onActivityResult(requestCode, resultCode, data);
			}
			if (null != recorder){
				recorder.onActivityResult(requestCode, resultCode, data);
			}
			if (null != location){
				location.onActivityResult(requestCode, resultCode, data);
			}
		}else{
			factory.onActivityResult(requestCode, resultCode, data);
		}
			
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, LocationEntry.CONTENT_URI);
		startActivityForResult(intent, INTENT_GET_LOCATION_INFO);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			switch (position) {
			case FRAGMENT_LOCATION:
				location = new FragmentLocation();
				fragment = location;
				break;
			case FRAGMENT_MONITOR:
				monitor = new FragmentMonitor();
				fragment = monitor;
				break;
			case FRAGMENT_RECORDER:
				recorder = new FragmentRecorder();
				fragment = recorder;
				break;
			default:
				fragment = new FragmentCalibrationDO();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 5 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case FRAGMENT_LOCATION:
				return getString(R.string.title_setting).toUpperCase(l);
			case FRAGMENT_MONITOR:
				return getString(R.string.title_section2).toUpperCase(l);
			case FRAGMENT_RECORDER:
				return getString(R.string.title_section1).toUpperCase(l);
			case FRAGMENT_CALIBRATION:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	
}
