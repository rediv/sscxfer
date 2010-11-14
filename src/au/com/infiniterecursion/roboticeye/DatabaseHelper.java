package au.com.infiniterecursion.roboticeye;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String AUTHORITY = "au.com.infiniterecursion.roboticeye";
	public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd.au.com.infiniterecursion.roboticeye";
	public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd.au.com.infiniterecursion.roboticeye";
	
	private static final String DB_FILENAME = "roboticeye.db";
	private static final int DB_VERSION = 2;
	
	public static final String FILENAME_TABLE_NAME = "filename_details";
	public static final String SDFILERECORD_TABLE_NAME = "videofiles";
	
	
	private static final String TAG = "RoboticEye DatabaseHelper";
	
	

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		//
	}

	
	DatabaseHelper(Context context) {
		
		super(context,DB_FILENAME, null, DB_VERSION );
		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 
	     //create the tables

        db.execSQL("CREATE TABLE " + FILENAME_TABLE_NAME + " ("
                        + FilenameDetails._ID + " INTEGER PRIMARY KEY,"
                        + FilenameDetails.NEXT_FILENAME_NUMBER + " INTEGER"                     
                        + " );");
        
        ContentValues vals = new ContentValues();
        vals.put(FilenameDetails.NEXT_FILENAME_NUMBER, 1);
        db.insert(FILENAME_TABLE_NAME, FilenameDetails.NEXT_FILENAME_NUMBER, vals);
        
        
        db.execSQL("CREATE TABLE " + SDFILERECORD_TABLE_NAME + " ("
                + SDFileRecord._ID + " INTEGER PRIMARY KEY,"
                + SDFileRecord.FILENAME + " TEXT,"
                + SDFileRecord.LENGTH_SECS + " INTEGER,"
                + SDFileRecord.CREATED_DATETIME + " INTEGER,"
                + SDFileRecord.VIDEO_AUDIO_CODEC_STRING + " TEXT"
                
                + " );");
 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FILENAME_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SDFILERECORD_TABLE_NAME);
		
		onCreate(db);
	}

	public static final class SDFileRecord implements BaseColumns {
		// This class cannot be instantiated
		private SDFileRecord() {} 

		// The default sort order for this table
		public static final String DEFAULT_SORT_ORDER = "created_datetime DESC";

		// The content:// style URL for this table
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/appstats");

		// The MIME type providing a directory of appstats.
		public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + ".filenamedetails";

		// The MIME type providing a sub-directory of a single note.
		public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + ".filenamedetails";

		// Table columns
	
		public static final String FILENAME = "filename";
		public static final String LENGTH_SECS = "length_secs";
		public static final String VIDEO_AUDIO_CODEC_STRING="codec_str";
		public static final String CREATED_DATETIME = "created_datetime";
		
		
	}
	
	public static final class FilenameDetails implements BaseColumns {
		// This class cannot be instantiated
		private FilenameDetails() {} 

		// The default sort order for this table
		public static final String DEFAULT_SORT_ORDER = "DESC";

		// The content:// style URL for this table
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/appstats");

		// The MIME type providing a directory of appstats.
		public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + ".filenamedetails";

		// The MIME type providing a sub-directory of a single note.
		public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + ".filenamedetails";

		// Table columns
	
		public static final String NEXT_FILENAME_NUMBER = "next_filename_number";
		
	}
}
