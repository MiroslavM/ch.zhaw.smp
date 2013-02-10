package ch.zhaw.android.had.googlemaps;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

//import android.os.Bundle;
//import android.app.Activity;
//import android.view.Menu;


/*
public class MainActivity extends MapActivity 
{


    MapView mapView; 
    MapController mc;
    GeoPoint p;
    
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        MapController mc = mapView.getController(); 
        switch (keyCode) 
        {
            case KeyEvent.KEYCODE_3:
                mc.zoomIn();
                break;
            case KeyEvent.KEYCODE_1:
                mc.zoomOut();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }    
	
    // Called when the activity is first created. 
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mapView = (MapView) findViewById(R.id.mapView);
        LinearLayout zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
        View zoomView = mapView.getZoomControls(); 
 
        zoomLayout.addView(zoomView, 
            new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, 
                LayoutParams.WRAP_CONTENT)); 
        mapView.displayZoomControls(true);
        
        mc = mapView.getController();
        String coordinates[] = {"1.352566007", "103.78921587"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(17); 
        mapView.invalidate();
    }
 
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
*/

public class MainActivity extends Activity 
{    
    /*
	* You will use a Mixpanel API token to allow your app to send data to Mixpanel. To get your token
	* - Log in to Mixpanel, and select the project you want to use for this application
	* - Click the gear icon in the lower left corner of the screen to view the settings dialog
	* - In the settings dialog, you will see the label "Token", and a string that looks something like this:
	*
	* 32577925fc1d04df5a5ff0f9197bc2b7 
	*/
    public static final String MIXPANEL_API_TOKEN = "32577925fc1d04df5a5ff0f9197bc2b7";

    /*
	* In order for your app to receive push notifications, you will need to enable
	* the Google Cloud Messaging for Android service in your Google APIs console. To do this:
	*
	* - Navigate to https://code.google.com/apis/console
	* - Select "Services" from the menu on the left side of the screen
	* - Scroll down until you see the row labeled "Google Cloud Messaging for Android"
	* - Make sure the switch next to the service name says "On"
	*
	* To identify this application with your Google API account, you'll also need your sender id from Google.
	* You can get yours by logging in to the Google APIs Console at https://code.google.com/apis/console
	* Once you have logged in, your sender id will appear as part of the URL in your browser's address bar.
	* The URL will look something like this:
	*
	* https://code.google.com/apis/console/#project:498202304589:services
	*                                               ^^^^^^^^^^^^
	*
	* The twelve-digit number after 'project:' is your sender id. Paste it below 
	*
	* There are also some changes you will need to make to your AndroidManifest.xml file to
	* declare the permissions and receiver capabilities you'll need to get your push notifications working.
	* You can take a look at this application's AndroidManifest.xml file for an example of what is needed.
	*/
    public static final String ANDROID_PUSH_SENDER_ID = "498202304589";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String trackingDistinctId = getTrackingDistinctId();

        //setContentView(R.layout.activity_main);
        
        mMixpanel = MixpanelAPI.getInstance(this, MIXPANEL_API_TOKEN);
        
        // We also identify the current user with a distinct ID, and
        // register ourselves for push notifications from Mixpanel.
        
        //MixpanelAPI.People people = mMixpanel.getPeople();
        
        
        mMixpanel.identify(trackingDistinctId);//this is the distinct_id value that
        // will be sent with events. If you choose not to set this,
        // the SDK will generate one for you
        
        mMixpanel.getPeople().identify(trackingDistinctId); //this is the distinct_id
        // that will be used for people analytics. You must set this explicitly in order
        // to dispatch people data.
        
        mMixpanel.getPeople().initPushHandling(ANDROID_PUSH_SENDER_ID);

        // You can call enableLogAboutMessagesToMixpanel to see
        // how messages are queued and sent to the Mixpanel servers.
        // This is useful for debugging, but should be disabled in
        // production code.
        mMixpanel.logPosts();

        // People analytics must be identified separately from event analytics.
        // The data-sets are separate, and may have different unique keys (distinct_id).
        // We recommend using the same distinct_id value for a given user in both,
        // and identifying the user with that id as early as possible.
        
        setContentView(R.layout.activity_main);
        
        Button gmapsButton = (Button)findViewById(R.id.buttonSwitchGMaps);
        gmapsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(), GMapActivity.class);
				Bundle b = new Bundle();
				if(currentLong == currentLat 
                   && currentLong == -1d )
				{
					setCurrentLongLatitude();					
				}
					
				b.putDouble("long", currentLong); // give gmap activity, local longitude information
				b.putDouble("lat", currentLat); // give gmap activity, local latitude information
				myIntent.putExtras(b); //Put geocord to gmaps Intent
				v.getContext().startActivity(myIntent);
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        long nowInHours = hoursSinceEpoch();
        int hourOfTheDay = hourOfTheDay();

        // For our simple test app, we're interested tracking
        // when the user views our application.

        // It will be interesting to segment our data by the date that they
        // first viewed our app. We use a
        // superProperty (so the value will always be sent with the
        // remainder of our events) and register it with
        // registerSuperPropertiesOnce (so no matter how many times
        // the code below is run, the events will always be sent
        // with the value of the first ever call for this user.)
        // all the change we make below are LOCAL. No API requests are made.
        try {
            JSONObject properties = new JSONObject();
            properties.put("first viewed on", nowInHours);
            properties.put("user domain", "(unknown)"); // default value
            mMixpanel.registerSuperPropertiesOnce(properties);
        } catch (JSONException e) {
            throw new RuntimeException("Could not encode hour first viewed as JSON");
        }

        // Now we send an event to Mixpanel. We want to send a new
        // "App Resumed" event every time we are resumed, and
        // we want to send a current value of "hour of the day" for every event.
        // As usual,all of the user's super properties will be appended onto this event.
        try {
            JSONObject properties = new JSONObject();
            properties.put("hour of the day", hourOfTheDay);
            mMixpanel.track("App Resumed", properties);
        } catch(JSONException e) {
            throw new RuntimeException("Could not encode hour of the day in JSON");
        }
    }

    // Associated with the "Send to Mixpanel" button in activity_main.xml
    public void sendToMixpanel(View view) {

        EditText firstNameEdit = (EditText) findViewById(R.id.edit_first_name);
        EditText lastNameEdit = (EditText) findViewById(R.id.edit_last_name);
        EditText emailEdit = (EditText) findViewById(R.id.edit_email_address);

        String firstName = firstNameEdit.getText().toString();
        String lastName = lastNameEdit.getText().toString();
        String email = emailEdit.getText().toString();

        MixpanelAPI.People people = mMixpanel.getPeople();

        // Update the basic data in the user's People Analytics record.
        // Unlike events, People Analytics always stores the most recent value
        // provided.
        people.set("$first_name", firstName);
        people.set("$last_name", lastName);
        people.set("$email", email);

        
        setCurrentLongLatitude();
		
		JSONObject jsonGpsCord = new JSONObject();
        try {
        	jsonGpsCord.put("Latitude", currentLat);
        	jsonGpsCord.put("Longitude", currentLong);
        } catch(JSONException e) { }
        
        people.set("GPSCord", jsonGpsCord);
		people.append("GPSCordList", jsonGpsCord);
		
		mMixpanel.track("LastSentGPS", jsonGpsCord);
        
        // We also want to keep track of how many times the user
        // has updated their info.
        people.increment("Update Count", 1L);

        // Mixpanel events are separate from Mixpanel people records,
        // but it might be valuable to be able to query events by
        // user domain (for example, if they represent customer organizations).
        //
        // We use the user domain as a superProperty here, but we call registerSuperProperties
        // instead of registerSuperPropertiesOnce so we can overwrite old values
        // as we get new information.
        try {
            JSONObject domainProperty = new JSONObject();
            domainProperty.put("user domain", domainFromEmailAddress(email));
            mMixpanel.registerSuperProperties(domainProperty);
        } catch (JSONException e) {
            throw new RuntimeException("Cannot write user email address domain as a super property");
        }

        // In addition to viewing the updated record in mixpanel's UI, it might
        // be interesting to see when and how many and what types of users
        // are updating their information, so we'll send an event as well.
        // You can call track with null if you don't have any properties to add
        // to an event (remember all the established superProperties will be added
        // before the event is dispatched to Mixpanel)
        mMixpanel.track("update info button clicked", null);
        
        // Send Message instant. 
        mMixpanel.flush();
    }
    
    private void setCurrentLongLatitude()
    {
        LocationManager locationManager =
        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyLocationListener lmh = new MyLocationListener();
		String mlocProvider;
		Criteria hdCrit = new Criteria();
		hdCrit.setAccuracy(Criteria.ACCURACY_COARSE);
		mlocProvider = locationManager.getBestProvider(hdCrit, true);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 1, lmh);
		Location currentLocation = locationManager.getLastKnownLocation(mlocProvider);
		locationManager.removeUpdates(lmh);
		currentLat = currentLocation.getLatitude();
	    currentLong = currentLocation.getLongitude();
    }


 
    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        super.onDestroy();
    }
    
//    @Override
//    protected boolean isRouteDisplayed() {
//        return false;
//    }
    
    ////////////////////////////////////////////////////

    private String getTrackingDistinctId() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        String ret = prefs.getString(MIXPANEL_DISTINCT_ID_NAME, null);
        if (ret == null) {
            ret = generateDistinctId();
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString(MIXPANEL_DISTINCT_ID_NAME, ret);
            prefsEditor.commit();
        }

        return ret;
    }
    
    // These disinct ids are here for the purposes of illustration.
    // In practice, there are great advantages to using distinct ids that
    // are easily associated with user identity, either from server-side
    // sources, or user logins. A common best practice is to maintain a field
    // in your users table to store mixpanel distinct_id, so it is easily
    // accesible for use in attributing cross platform or server side events.
    private String generateDistinctId() {
        Random random = new Random();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }
    
    ///////////////////////////////////////////////////////
    // conveniences

    private int hourOfTheDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private long hoursSinceEpoch() {
        Date now = new Date();
        long nowMillis = now.getTime();
        return nowMillis / 1000 * 60 * 60;
    }

    private String domainFromEmailAddress(String email) {
        String ret = "";
        int atSymbolIndex = email.indexOf('@');
        if ((atSymbolIndex > -1) && (email.length() > atSymbolIndex)) {
            ret = email.substring(atSymbolIndex + 1);
        }

        return ret;
    }
    
    private MixpanelAPI mMixpanel;
    private static final String MIXPANEL_DISTINCT_ID_NAME = "Mixpanel Example $distinctid";
    private double currentLong = -1d;
    private double currentLat = -1d;
    
    public class MyLocationListener implements LocationListener
    {
    	
    	@Override
    	public void onLocationChanged(Location loc)
    	{
    		loc.getLatitude();
    		loc.getLongitude();
    		String Text = "My current location is: " +
    		"Latitud = " + loc.getLatitude() +
    		"Longitud = " + loc.getLongitude();
    		Toast.makeText( getApplicationContext(),
    		Text,
    		Toast.LENGTH_SHORT).show();
    	}
    	
    	@Override
    	public void onProviderDisabled(String provider)
    	{
    		Toast.makeText( getApplicationContext(),
    		"Gps Disabled",
    		Toast.LENGTH_SHORT ).show();
    	}
    	
    	@Override
    	public void onProviderEnabled(String provider)
    	{
    		Toast.makeText( getApplicationContext(),
    		"Gps Enabled",
    		Toast.LENGTH_SHORT).show();
    	}
    	
    	@Override
    	public void onStatusChanged(String provider, int status, Bundle extras)	
    	{
    	
    	}

    }/* End of Class MyLocationListener */

}


