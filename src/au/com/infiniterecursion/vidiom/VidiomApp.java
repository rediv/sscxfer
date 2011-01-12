package au.com.infiniterecursion.vidiom;

/*
 * Main Vidiom Application 
 * 
 * AUTHORS:
 * 
 * Andy Nicholson
 * 
 * 2010
 * Copyright Infinite Recursion Pty Ltd.
 */



import android.app.Application;

import android.util.Log;
import au.com.infiniterecursion.vidiom.facebook.SessionStore;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class VidiomApp extends Application {
	
	private static final String FACEBOOK_APP_ID = "175287182490445";
	private static final String FACEBOOK_APP_KEY = "2841286527d46765c6823bc9b08fdad9";
	private static final String FACEBOOK_APP_SECRET = "ed97e81ca8b2327aaaa9291979f0e8cb";
	public static final String[] FB_LOGIN_PERMISSIONS = new String[] {"publish_stream", "read_stream", "offline_access", "video_upload"};
	private static String TAG ="RoboticEye-MainApp";
		
	//Facebook associated objects
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;


	private boolean isUploading;
	
	/*
	 * On application startup, get the home position from the preferences.
	 * 
	 * (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	public void onCreate() {
    	super.onCreate();
    	Log.i(TAG, "*** onCreate called ***");
		
		
		 //Facebook init
        mFacebook = new Facebook(FACEBOOK_APP_ID);
        mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		
		SessionStore.restore(mFacebook, this);
		isUploading = false;
    }

    public void onTerminate() {
    	Log.i(TAG, "*** OnTerminate called ***");
    	super.onTerminate();
    }
    
    
    public Facebook getFacebook() {
    	return mFacebook;
    }
    
    public AsyncFacebookRunner getAsyncFacebookRunner() {
    	return mAsyncRunner;
    	
    }
    
   public static String getFacebookAPIkey () {
	   return FACEBOOK_APP_KEY;
   }

   public static String getFacebookAPIsecret() {
	// 
	return FACEBOOK_APP_SECRET;
   }

   public boolean isUploading() {
	   return isUploading;
   }
   
   public void setUploading() {
	   isUploading = true;
   }
   
   public void setNotUploading() {
	   isUploading = false;
   }
}