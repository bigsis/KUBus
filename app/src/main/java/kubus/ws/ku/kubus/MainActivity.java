package kubus.ws.ku.kubus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener {
    private final short state_standby = 0;
    private final short state_selection = 1;
    private final short state_notification = 2;
    private short state;

    private final String distance_100 = "100";
    private final String distance_200 = "200";
    private final String distance_500 = "500";
    private final String distance_700 = "700";
    private final String distance_1000 = "1000";

    private final String ALL_LINE = "All Line";
    private final String LINE_1 = "Line 1";
    private final String LINE_2 = "Line 2";
    private final String LINE_3 = "Line 3";
    private final String LINE_4 = "Line 4";
    private final String LINE_5 = "Line 5";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private LatLng userPos;
    private Requesttask rt;
    private URL url;
    private LocationManager lm;
    private double lat, lng;
    private Timer timer;
    private TimerTask task;
    private HashMap<String,Marker> markers;
    private Spinner distanceSpinner;
    private Spinner lineSpinner;
    private Button curPosBtn;
    private LatLng curPos;
    private Button selBtn;
    private Button cancelBtn;
    private WebSocketConnection wsc = new WebSocketConnection();
    private ParseBusXml pbx;
    private MarkerController mc;
    private final String wsuri = "180.183.100.98";
    private final String webPort = ":8081";
    private final String socPort = ":8080";
    private List<Marker> selectedMarker = null;
    private Circle circle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        matchComponent();
        setState(state_standby);
        pbx = ParseBusXml.getInstance();

        setUpMapIfNeeded();
        markers = new HashMap<String, Marker>();
        mc = MarkerController.getInstance(mMap, markers);

        openSoc();
//        openWeb();
        try {
            setClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        addBusLineToSpinner();
    }

    protected  void setClient() throws IOException {
        if(task != null)
        timer.schedule(task,0,15000);
        addComponent();
    }

    private void matchComponent() {System.out.println("init ");
        selBtn = (Button)findViewById(R.id.select_button);
        cancelBtn = (Button)findViewById(R.id.cancel_button);
        distanceSpinner = (Spinner) findViewById(R.id.distance_spinner);
        System.out.println("spi"+ distanceSpinner);
        lineSpinner = (Spinner)findViewById(R.id.bus_line_spinner);
        curPosBtn = (Button)findViewById(R.id.current_location_button);
        System.out.println("init component");
    }

    private void addComponent(){
        addDistanceSpinner();
        addBusLineToSpinner();
        setCurPosBtn();
        setSelectBtn();
        setCancelBtn();
        Toast.makeText(getApplicationContext(), "Add Busline Already", Toast.LENGTH_SHORT).show();
    }

    private void setState(short state) {
        this.state = state;
        if (this.state == state_standby) {
            distanceSpinner.setVisibility(View.INVISIBLE);
            cancelBtn.setVisibility(View.INVISIBLE);
            selBtn.setText("Select");
            if(circle != null)
                circle.setVisible(false);
            Toast.makeText(getApplicationContext(), "Standby", Toast.LENGTH_SHORT).show();
        }
        else if (this.state == state_selection) {
            distanceSpinner.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            if(circle != null)
                circle.setVisible(true);
            selBtn.setText("Done");
            Toast.makeText(getApplicationContext(), "Selection", Toast.LENGTH_SHORT).show();
        }
        else {
            alertBus();
            cancelBtn.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Notificatoin", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDistanceSpinner()
    {
        String[] distance = {distance_100, distance_200, distance_500, distance_700, distance_1000};
        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,distance);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        System.out.println("spin:" + distanceSpinner+", adap:" + distanceAdapter );
        distanceSpinner.setAdapter(distanceAdapter);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(circle != null)
                    circle.setRadius(Double.parseDouble(distanceSpinner.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addBusLineToSpinner()
    {
//        Set<String> temp = markers.keySet();
//        String[] temp2 = temp.toArray(new String[0]);
//        Set<String> busLine = new HashSet<String>()Set<String>();
//        for(String s : temp2)
//        {
//            busLine.add(markers.get(s).getSnippet());
//        }
//        temp2 = busLine.toArray(new String[0]);

        String[] temp = {ALL_LINE,LINE_1,LINE_2,LINE_3,LINE_4,LINE_5};

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,temp);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        System.out.println("adap: " + myAdapter);
        System.out.println("line: "+lineSpinner);
        lineSpinner.setAdapter(myAdapter);
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] busID = markers.keySet().toArray(new String[0]);
                int lineFilter = parent.getSelectedItemPosition();

                if (lineFilter == 0)
                    for (String s : busID)
                        markers.get(s).setVisible(true);

                else
                    for (String s : busID)
                    {
                        Marker marker = markers.get(s);
                        if (lineFilter == Integer.parseInt(marker.getSnippet()))
                            marker.setVisible(true);
                        else
                            marker.setVisible(false);
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSelectBtn(){
        selBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == state_standby) {
                    setState(state_selection);
                    mc.setAllAlpha();
                    selectedMarker = new ArrayList<Marker>();
                }
                else if(state == state_selection) {
                    setState(state_notification);
                }
                else {
                    setState(state_standby);
                    mc.reAllAlpha();
                }
            }
        });
    }

    private void setCancelBtn(){
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setState(state_standby);
            }
        });
    }

    private void setCurPosBtn(){
        curPosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToCurrent();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        SupportMapFragment mapFrag=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mapFrag.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
        mMap.setOnMarkerClickListener(this);
    }

    public void moveToCurrent(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPos, 15));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(state == state_selection){
            if(selectedMarker.remove(marker))
            {
                mc.setAlpha(marker);
            }
            else
            {
                selectedMarker.add(marker);
                mc.reAlpha(marker);
            }
            Toast.makeText(getApplicationContext(), selectedMarker.toString(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            curPos = new LatLng(loc.getLatitude(), loc.getLongitude());
            lat = loc.getLatitude();
            lng = loc.getLongitude();

            if( marker != null ){
                marker.setPosition(new LatLng(lat, lng));
                circle.setCenter(new LatLng(lat, lng));
                circle.setRadius(Double.parseDouble(distanceSpinner.getSelectedItem().toString()));
            }
            else{
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(lat, lng))
                        .radius(Double.parseDouble(distanceSpinner.getSelectedItem().toString()))
                        .strokeColor(0x0B610B)
                        .fillColor(0x5500ff00)
                        .visible(false)

                );
                moveToCurrent();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean isNetwork =
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPS =
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isNetwork) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                    , 5000, 10, listener);
            Location loc = lm.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            if(loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
            }
        }

//        if(isGPS) {
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
//                    , 5000, 10, listener);
//            Location loc = lm.getLastKnownLocation(
//                    LocationManager.GPS_PROVIDER);
//            if(loc != null) {
//                lat = loc.getLatitude();
//                lng = loc.getLongitude();
//            }
//        }
    }

    public void onPause(){
        super.onPause();
        lm.removeUpdates(listener);
    }

    public void openWeb(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                new Requesttask(mMap, markers).execute("http://"+ wsuri + webPort +"/bus");
            }
        };

    }

    public void openSoc(){
        try {
            wsc.connect("ws://" + wsuri + socPort, new WebSocketHandler(){
                @Override
                public void onOpen() {

                    wsc.sendTextMessage("Hello, world!");
                }

                @Override
                public void onTextMessage(String payload) {
                    mc.setBusLocation(pbx.parseXmlToBusWebSer(payload));
                    if (state == state_notification && selectedMarker != null) {
                        alertBus();
                    }

                }

                @Override
                public void onClose(int code, String reason) {
//                    Log.d(TAG, "Connection lost.");
                }
            });
        } catch (WebSocketException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void alertBus(){

        if (checkDistance()) {
            Context context = this.getApplicationContext();

            Toast.makeText(getApplicationContext(), checkDistance() + "", Toast.LENGTH_SHORT).show();
            NotificationManager notificationManager
                    = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(context);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent
                    = PendingIntent.getActivity(context, 0, intent, 0);

            builder
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("KUBus")
                    .setContentText("this is a book")
                    .setContentInfo("kok")
                    .setTicker("The winter is coming")
                    .setLights(0xFFFF0000, 500, 500) //setLights (int argb, int onMs, int offMs)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getValidRingtoneUri(context))
                    .setDefaults(Notification.DEFAULT_VIBRATE);

            Notification notification = builder.getNotification();

            notificationManager.notify(R.drawable.ic_launcher, notification);

        }

    }
    private  double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515* 1.609344;
//        Toast.makeText(getApplicationContext(), dist+"", Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), distanceSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

        return (dist*1000);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private boolean checkDistance(){
        for(Marker m : selectedMarker) {
            if (Double.parseDouble(distanceSpinner.getSelectedItem().toString()) > distance(lat, lng, m.getPosition().latitude, m.getPosition().longitude)) {
                return true;
            }
        }
        return false;
    }
}
