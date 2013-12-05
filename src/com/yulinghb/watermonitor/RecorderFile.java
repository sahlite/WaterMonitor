package com.yulinghb.watermonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/*
 * 创建图片，记录文件的工具类
 */
public class RecorderFile {
	private static final String APP_PATH = "WaterMonitor";
	private static final String DATA_FILE_NAME = "data.csv";
	private static File data_file;

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	/* Get a file created in public directory */
	public static File createexternalfile(String albumName) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            ""), albumName);
//	    if (!file.mkdirs()) {
//	        Log.e(LOG_TAG, "Directory not created");
//	    }
	    if (!file.exists()){
	    	try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return file;
	}
	
	/* Get a file created in application directory */
	public static File createexternalfile(Context context, String albumName) {
	    // Get the directory for the app's private pictures directory. 
	    File file = new File(context.getExternalFilesDir(
	            ""), albumName);
//	    if (!file.mkdirs()) {
//	        Log.e(LOG_TAG, "Directory not created");
//	    }
	    if (!file.exists()){
	    	try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return file;
	}
	
	public static void write(File file, String data){
		FileOutputStream outputStream;

		try {
		  outputStream = new FileOutputStream(file);
		  outputStream.write(data.getBytes());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}
	}
	
	public static String read(File file){
		FileInputStream inputStream;
		String data = "";

		try {
		    byte[] input_byte = new byte[1024];
		    inputStream = new FileInputStream(file);
		    inputStream.read(input_byte);
		    data = new String(input_byte);
		    inputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}	
		
		return data;
	}
	/*
	 * 创建记录文件
	 */
	public static void create() throws IOException{
//		File storageDir = new File(
//			    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), 
//			    APP_PATH
//				);       
//		storageDir.mkdirs();
//	    data_file = File.createTempFile(DATA_FILE_NAME, null, storageDir);
		data_file = createexternalfile(DATA_FILE_NAME);
	}
	
	/*
	 * 写入数据记录到文件
	 */
	public static void write(String data){
		if ((null != data_file)&&(data_file.exists())){
			write(data_file, data);
//			FileOutputStream outputStream;  
//			try {   
//				outputStream = new FileOutputStream(data_file);
//				outputStream.write(data.getBytes());   
//				outputStream.close(); 
//			} catch (Exception e) {
//				e.printStackTrace(); 
//			}
		}
	}
	
	/*
	 * 从文件中读取数据记录
	 */
	public static String read(){
		if ((null != data_file)&&(data_file.exists())){
//			FileInputStream inputStream;  
//			byte[] buffer = new byte[1024];
//			int bytes = 0;
//			
//			try {   
//				inputStream = new FileInputStream(data_file);
//				bytes = inputStream.read(buffer);  
//				inputStream.close(); 
//			} catch (Exception e) {
//				e.printStackTrace(); 
//			}
//			char[] data=new char[1024];
//			for(int i=0; i < bytes; i++){
//				data[i] = (char)buffer[i];
//			}
//			return String.valueOf(data, 0, bytes);
			String data = read(data_file);
			return data;
		}else{
			return "no data";
		}
	}
	
	/*
	 * 获取当前的数据记录文件
	 */
	public static File get(){
		return data_file;
	}
	
	/*
	 * 创建照片文件
	 */
	public static File createImageFile() throws IOException{
//		File storageDir = new File(
//			    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), 
//			    APP_PATH
//				);       
//		storageDir.mkdirs();
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "PIC_" + timeStamp + ".jpg";
	    File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), 
	    		APP_PATH + "/" + imageFileName);//File.createFile(imageFileName, null, storageDir);
	    image.createNewFile();
	    if (!image.exists()){
	    	try {
	    		image.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return image;
	}
	
	/*
	 * 从指定路径获得bitmap
	 */
	public static Bitmap getBitmap(Context context, String sImagePath, String sDefault, int targetW, int targetH){
		Bitmap bitmap = null;
		if (sDefault == sImagePath){
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		}else{
			// Get the dimensions of the View
//		    int targetW = 300;//iImage.getWidth();
//		    int targetH = 225;//iImage.getHeight();
		  
		    // Get the dimensions of the bitmap
		    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		    bmOptions.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(sImagePath, bmOptions);
		    int photoW = bmOptions.outWidth;
		    int photoH = bmOptions.outHeight;
		  
		    // Determine how much to scale down the image
		    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		  
		    // Decode the image file into a Bitmap sized to fill the View
		    bmOptions.inJustDecodeBounds = false;
		    bmOptions.inSampleSize = scaleFactor;
		    bmOptions.inPurgeable = true;
		  
		    bitmap = BitmapFactory.decodeFile(sImagePath, bmOptions);
		}
		return bitmap;
	}
}
