package com.yulinghb.watermonitor;

import android.net.Uri;
import android.provider.BaseColumns;

/*
 * 数据记录，地点信息的数据库参考类
 */
public final class RecorderContract {
	
	public static final String AUTHORITY = "com.yulinghb.watermonitor.provider.recorder";

	private RecorderContract(){}
	
	public static final class DataEntry implements BaseColumns{
		// This class cannot be instantiated
        private DataEntry() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/data");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lingyuhb.data";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lingyuhb.data";
        
		public static final String TABLE_NAME = "data_entry";
		public static final String COLUMN_NAME_LOCATION_ID = "location_id";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_TEMP = "temp";
		public static final String COLUMN_NAME_DEPTH = "depth";
		public static final String COLUMN_NAME_RDO = "rdo";
		public static final String COLUMN_NAME_SAT = "sat";
		public static final String COLUMN_NAME_ORP = "orp";
		public static final String COLUMN_NAME_PH = "ph";
		public static final String COLUMN_NAME_ACT = "act";
		public static final String COLUMN_NAME_BARO = "baro";
	}
	
	public static final class LocationEntry implements BaseColumns{
		// This class cannot be instantiated
        private LocationEntry() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/location");
        public static final Uri IMAGE_AND_NAME_URI = Uri.parse("content://" + AUTHORITY + "/location/image_and_name");
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lingyuhb.location";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lingyuhb.location";
        
        
		public static final String TABLE_NAME = "location_entry";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_IMAGE = "image";
		public static final String COLUMN_NAME_LATITUDE = "longtitude";
		public static final String COLUMN_NAME_LONGTITUDE = "latitude";
	}
	
	/**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";
	
}
