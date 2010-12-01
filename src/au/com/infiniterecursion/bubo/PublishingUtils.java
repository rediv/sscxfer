package au.com.infiniterecursion.bubo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import au.com.infiniterecursion.bubo.R;

public class PublishingUtils {

	private static final String TAG = "RoboticEye-PublishingUtils";
	
	private File folder;
	private Resources res;
	private DBUtils dbutils;
	
	PublishingUtils(Resources res, DBUtils dbutils) {
		 	
		this.res = res;
		this.dbutils = dbutils;
		folder = new File(Environment.getExternalStorageDirectory()
				+ res.getString(R.string.rootSDcardFolder));
		
	}
	
	public static String showDate(long timemillis) {
		
		if (timemillis <= 0) 	
			return "N/A";
	
		Calendar cal;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss");
		cal = Calendar.getInstance();
		cal.setTimeInMillis(timemillis);
		return sdf.format(cal.getTime());
		
	}
	
	/*
	 * 
	 * Methods for publishing the video
	 */

	public void doPOSTtoVideoBin(final Activity activity,
			final Handler handler, final String video_absolutepath,
			final String emailAddress, final long sdrecord_id) {

		Log.d(TAG, "doPOSTtoVideoBin starting");
		
		
		// Make the progress bar view visible.
		((RoboticEyeActivity) activity).startedUploading();
		
		new Thread(new Runnable() {
			public void run() {
				// Do background task.
				
				Resources res = activity.getResources();
				
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(
						CoreProtocolPNames.PROTOCOL_VERSION,
						HttpVersion.HTTP_1_1);

				URI url = null;
				try {
					url = new URI(res.getString(R.string.http_videobin_org_add));
				} catch (URISyntaxException e) {
					// Ours is a fixed URL, so not likely to get here.
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
					
				}
				HttpPost post = new HttpPost(url);
				MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);

				File file = new File(video_absolutepath);
				entity.addPart(res.getString(R.string.video_bin_API_videofile), new FileBody(file));

				try {
					entity.addPart(res.getString(R.string.video_bin_API_api), new StringBody("1", "text/plain",
							Charset.forName("UTF-8")));
				} catch (IllegalCharsetNameException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
					
				} catch (UnsupportedCharsetException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
				} catch (UnsupportedEncodingException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
				}

				post.setEntity(entity);

				// Here we go!
				String response = null;
				try {
					response = EntityUtils.toString(client.execute(post)
							.getEntity(), "UTF-8");
				} catch (ParseException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
				} catch (ClientProtocolException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
				} catch (IOException e) {
					//error
					e.printStackTrace();
					((RoboticEyeActivity) activity).finishedUploading(false);
					return;
				}

				client.getConnectionManager().shutdown();

				Log.d(TAG, " got back " + response);

				// XXX should this be another auto-email this to user preference
				// ?
				// stuck on YES here, if email is defined.

				if (emailAddress != null && response != null) {

					// XXX convert EmailSender to use IR controlled system.

					EmailSender sender = new EmailSender("intothemist",
							"#!$tesla."); // SUBSTITUTE HERE
					try {
						sender.sendMail("Robotic Eye automatic email.", // subject.getText().toString(),
								"URL of video is  " + response, // body.getText().toString(),
								emailAddress, // from.getText().toString(),
								emailAddress // to.getText().toString()
						);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}

				//Log record of this URL in POSTs table
				dbutils.creatHostDetailRecordwithNewVideoUploaded(sdrecord_id, res.getString(R.string.http_videobin_org_add) , response, "");

				// Use the handler to execute a Runnable on the
				// main thread in order to have access to the
				// UI elements.
				handler.postDelayed(new Runnable() {
					public void run() {
						// Update UI

						//Indicate back to calling activity the result!
						// update uploadInProgress state also.
						
						((RoboticEyeActivity) activity).finishedUploading(true);

						
						
						
						new AlertDialog.Builder(activity)
						.setMessage(R.string.video_bin_uploaded_ok)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).show();
						
					}
				}, 0);
			}
		}).start();

	}

	public void doVideoFTP(final Activity activity,
			final String latestVideoFile_filename,
			final String latestVideoFile_absolutepath) {

		Log.d(TAG, "doVideoFTP starting");

		//XXX convert to Thread!

		// Make the progress bar view visible.
		((RoboticEyeActivity) activity).startedUploading();

		// FTP; connect preferences here!
		//
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity.getBaseContext());
		String ftpHostName = prefs.getString("defaultFTPhostPreference", null);
		String ftpUsername = prefs.getString("defaultFTPusernamePreference",
				null);
		String ftpPassword = prefs.getString("defaultFTPpasswordPreference",
				null);

		// use name of local file.
		String ftpRemoteFtpFilename = latestVideoFile_filename;

		// FTP
		FTPClient ftpClient = new FTPClient();
		InetAddress uploadhost = null;
		try {

			uploadhost = InetAddress.getByName(ftpHostName);
		} catch (UnknownHostException e1) {
			// If DNS resolution fails then abort immediately - show dialog to
			// inform user first.
			e1.printStackTrace();
			Log.e(TAG, " got exception resolving " + ftpHostName
					+ " - video uploading failed.");
			uploadhost = null;
		}

		if (uploadhost == null) {

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			new AlertDialog.Builder(activity)
					.setMessage(R.string.cant_find_upload_host)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							})

					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();

			return;
		}

		try {
			ftpClient.connect(uploadhost);
		} catch (SocketException e) {
			// These exceptions will be essentially caught by our check of
			// ftpclient.login immediately below.
			// if you cant connect you wont be able to login.
			e.printStackTrace();
		} catch (UnknownHostException e) {
			//
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}

		boolean reply = false;
		try {

			reply = ftpClient.login(ftpUsername, ftpPassword);
		} catch (IOException e) {
			//
			e.printStackTrace();
			Log.e(TAG, " got exception on ftp.login - video uploading failed.");
		}

		// check the reply code here
		// If we cant login, abort after showing user a dialog.
		if (!reply) {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				//
				e.printStackTrace();
			}

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			new AlertDialog.Builder(activity)
					.setMessage(R.string.cant_login_upload_host)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							})

					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).show();

			return;
		}

		// Set File type to binary
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e) {
			//
			e.printStackTrace();
		}

		// Construct the input strteam to send to Ftp server, from the local
		// video file on the sd card
		BufferedInputStream buffIn = null;
		File file = new File(latestVideoFile_absolutepath);

		try {
			buffIn = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			//
			e.printStackTrace();
			Log.e(TAG,
					" got exception on local video file - video uploading failed.");

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			// This is a bad error, lets abort.
			// XXX user dialog ?! shouldnt happen, but still...
			return;
		}

		ftpClient.enterLocalPassiveMode();

		try {
			// UPLOAD THE LOCAL VIDEO FILE.
			ftpClient.storeFile(ftpRemoteFtpFilename, buffIn);
		} catch (IOException e) {
			//
			e.printStackTrace();
			Log.e(TAG, " got exception on storeFile - video uploading failed.");

			// XXX user dialog ?! shouldnt happen, but still...

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			return;
		}
		try {
			buffIn.close();
		} catch (IOException e) {
			//
			e.printStackTrace();
			Log.e(TAG, " got exception on buff.close - video uploading failed.");

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			return;
		}
		try {
			ftpClient.logout();
		} catch (IOException e) {
			//
			e.printStackTrace();
			Log.e(TAG, " got exception on ftp logout - video uploading failed.");

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			return;
		}
		try {
			ftpClient.disconnect();
		} catch (IOException e) {
			//
			e.printStackTrace();
			Log.e(TAG,
					" got exception on ftp disconnect - video uploading failed.");

			// Hide the progress bar
			((RoboticEyeActivity) activity).finishedUploading(false);

			return;
		}

		// If we get here, it all worked out.
		// Hide the progress bar
		((RoboticEyeActivity) activity).finishedUploading(true);


	}

	public void launchEmailIntentWithCurrentVideo(final Activity activity,
			final String latestVideoFile_absolutepath) {
		Log.d(TAG, "launchEmailIntentWithCurrentVideo starting");

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setType("video/mp4");
		i.putExtra(Intent.EXTRA_STREAM,
				Uri.parse("file://" + latestVideoFile_absolutepath));
		activity.startActivity(i);
	}

	public void launchVideoPlayer(final Activity activity,
			final String movieurl) {

		try {
			Intent tostart = new Intent(Intent.ACTION_VIEW);
			tostart.setDataAndType(Uri.parse(movieurl), "video/*");
			activity.startActivity(tostart);
		} catch (android.content.ActivityNotFoundException e) {
			Log.e(TAG, " Cant start activity to show video!");
			
			
			new AlertDialog.Builder(activity)
			.setMessage(R.string.cant_show_video)
			.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

						}
					})

			.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

						}
					}).show();
			
			return;
		}

	}


	public File selectFilenameAndCreateFile(String filenameConventionPrefence) {
		// Video file name selection process
		String new_videofile_name = res.getString(R.string.defaultVideoFilenamePrefix);
		String file_ext_name = ".mp4";
		
		if (filenameConventionPrefence.compareTo(res.getString(R.string.filenameConventionDefaultPreference)) == 0) {
			//The default is by date
			SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); 
			Calendar cal = Calendar.getInstance();
			Date now = cal.getTime();
			String newDateStr = postFormater.format(now); 
			
			new_videofile_name += newDateStr + file_ext_name;
			
			
				
		} else {
			//Sequentially 
			
			//look into database for this number
			int next_number = dbutils.getNextFilenameNumberAndIncrement();
			
			//XXX deal with -1 error condition
			
			new_videofile_name += next_number + file_ext_name;
			
		}
		
		File tempFile = new File(folder.getAbsolutePath(), new_videofile_name);
		return tempFile;
	}
	
	
	public boolean deleteVideo(String movieuri) {
		Log.d(TAG, "deleteVideo with " + movieuri);
		
		File tempFile = new File(movieuri);
		
		return tempFile.delete();
		
	}
}