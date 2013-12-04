/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gcm.demo.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    //Context context = this.getApplicationContext();

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                
                String message = intent.getStringExtra("msg");
                if(message != null){
               
	                System.out.println(message);
	                
	                if (message.equals("alert")){
	                	sendAlert("Your bike has been stolen find it using the JackerTraker website");
	                }
	                else if (message.equals("coord")){
	                	sendGpsCoor();
	                }
	                else{
	                	Log.i(TAG, "wrong message");
	                }
                }
                else{
                	Log.i(TAG, "no message");
                }
                
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    public void sendGpsCoor(){
    	
    	LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);	
		LocationListener ll = new MyLocationListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, ll);
		
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		System.out.println(location.getTime());
		double pLong = location.getLongitude();
		double pLat = location.getLatitude();
		sendCoor(Double.toString(pLong),Double.toString(pLat));
	}
		
	class MyLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			Log.i(TAG, "Location changed !");
			
		}
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
	
    private void sendCoor(String lon, String lat) {
    	String responseString= null;

    	  try {
    		  System.out.println("long :"+lon+"\nlat :"+lat);
    	    //URI url            = new URI ("http://192.168.1.177:8080/gcm-demo/register?regId="+regid);
			URI url            = new URI ("http://jacker-tracker.appspot.com/updatePhoneCoords?serial=1&lat="+lat+"&lng="+lon);
    	    HttpGet httpGet    = new HttpGet (url);
    	    // defaultHttpClient
    	    HttpParams
    	      httpParameters   = new BasicHttpParams();

    	    // Set the timeout in milliseconds until a connection is established.
    	    // The default value is zero, that means the timeout is not used. 
    	    int timeoutConnection= 3000;
    	    HttpConnectionParams.setConnectionTimeout (httpParameters, timeoutConnection);

    	    // Set the default socket timeout (SO_TIMEOUT) 
    	    // in milliseconds which is the timeout for waiting for data.
    	    int timeoutSocket  = 5000;
    	    HttpConnectionParams.setSoTimeout (httpParameters,timeoutSocket);

    	    DefaultHttpClient httpClient = new DefaultHttpClient (httpParameters);

    	    HttpResponse httpResponse = httpClient.execute(httpGet);
    	    HttpEntity httpEntity = httpResponse.getEntity();
    	    System.out.println(httpResponse.getStatusLine().getStatusCode());
    	    if (httpResponse.getStatusLine().getStatusCode() != 200)
    	    {
    	      Log.e ("hi",
 //   	        context.getString(R.string.app_name),
    	        "Server Call Failed : Got Status Code " + httpResponse.getStatusLine().getStatusCode() + " and ContentType " + httpEntity.getContentType().getValue()
    	                         );
    	      // add code to handle error
    	    }

    	    responseString     = EntityUtils.toString (httpEntity);
    	  } catch (UnsupportedEncodingException e) {
//    	    Log.e(context.getString(R.string.app_name),e.toString(),e);
    	    // add code to handle error
    	  } catch (ClientProtocolException e) {
//    	    Log.e(context.getString(R.string.app_name),e.toString(),e);
    	    // add code to handle error
    	  } catch (IOException e) {
//    	    Log.e(context.getString(R.string.app_name),e.toString(),e);
    	    // add code to handle error
    	  } catch (URISyntaxException e) {
 //   	    Log.e(context.getString(R.string.app_name),e.toString(),e);
    	    // add code to handle error
    	  }
    	  System.out.println("response: "+responseString);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DemoActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_gcm)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    private void sendAlert(String msg) {
    	startActivity(new Intent(this,Alert.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
