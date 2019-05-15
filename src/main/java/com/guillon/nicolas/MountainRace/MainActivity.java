package com.guillon.nicolas.MountainRace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Main activity class, which sets what happens when we start the app
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Button used to start the tracing
     */
    private Button start_btn;
    /**
     * Color displaying the current status of the tracing
     */
    private ImageView signal_color;

    /**
     * Location manager which simply manages our GPS provider
     */
    private LocationManager lm;
    /**
     * Location listener which simply listens to every change in the location
     */
    private LocationListener ll;
    /**
     * List of the locations we save
     */
    private ArrayList<Location> locationList;
    /**
     * Boolean telling if we are recording the location or not
     */
    private boolean tracking;

    /**
     * What happens when the activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the variables
        locationList = new ArrayList<>();
        start_btn = (Button) findViewById(R.id.button);
        signal_color = (ImageView) findViewById(R.id.signal_color);

        tracking = false;
    }

    /**
     * Method which writes a GPX file in the file given with the array of locations given
     * @param file
     * @param points
     */
    public static void writeGPX(File file, ArrayList<Location> points) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx" +
                " xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" " +
                "version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  " +
                "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + file.getName() + "</name><trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        for (Location l : points) {
            segments += "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\">" + "<ele>" + l.getAltitude() + "</ele>" + "<time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
        }

        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
        } catch (IOException e) {}
    }

    /**
     * Method which creates a folder GPStracks if it doesn't exist, and sets the name of the new file
     * to the current date
     */
    private void CreateFile() {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.WRITE_EXTERNAL_STORAGE  },
                    13 );
        }

        String foldername = "GPStracks";
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd':'HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        String filename = date + ".gpx";

        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), foldername);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + foldername, filename);
        try {
            if (!directory.exists())
                directory.mkdirs();
            writeGPX(file, locationList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which defines what happens when we click on the start button.
     * Depending on if we are tracing or not, the button will create a location listener or send
     * the user to the statistics page
     * @param view
     */
    public void onStartClick(View view) {
        if (!tracking) {
            // If we were not tracing already, we initialize a listener and inform the user
            if (addLocationListener() == 1)
                return;
            start_btn.setText(R.string.stop_btn);
            Toast.makeText(this, "Now tracking position",
                    Toast.LENGTH_SHORT).show();
            tracking = true;
        }
        else {
            // Else, we create the new GPX file with the data we retrieved, remove the location listener
            // and we move to the next activity
            start_btn.setText(R.string.start_btn);
            CreateFile();
            if (lm != null) {
                lm.removeUpdates(ll);
                lm = null;
            }
            tracking = false;

            if (locationList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                intent.putExtra("list", locationList);
                startActivityForResult(intent, 1);
            }
            else {
                Toast.makeText(this, "Not enough data to display.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initializes the location listener
     */
    private void InitLocationListener() {
        ll = new LocationListener() {

            /**
             * When the location of the device has changed
             * @param location
             */
            @Override
            public void onLocationChanged(Location location) {
                // Changes the status color to green
                signal_color.setImageResource(R.color.tracking);
                // Add the location to our array
                locationList.add(location);
            }

            /**
             * When the GPS has been disabled
             * @param provider
             */
            @Override
            public void onProviderDisabled(String provider) {
                // Changes the status color to Red
                signal_color.setImageResource(R.color.error);
                Toast.makeText(MainActivity.this, "Location provider disabled",
                        Toast.LENGTH_SHORT).show();
            }

            /**
             * When the GPS provider is enabled
             * @param provider
             */
            @Override
            public void onProviderEnabled(String provider) {
                // if there is a last known location
                if (provider == LocationManager.GPS_PROVIDER) {
                    if ( ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                    }
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        locationList.add(location);
                        signal_color.setImageResource(R.color.tracking);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

    }

    /**
     * Method which defines what we do when the location and storage permissions have been asked
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 11:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    InitLocationListener();
                    if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, ll);
                    start_btn.setText(R.string.stop_btn);
                    Toast.makeText(this, "Now tracking position",
                            Toast.LENGTH_SHORT).show();
                    tracking = true;
                } else {
                    // permission denied
                    Toast.makeText(MainActivity.this, "You need to grant the location and storage access in order to use this application",
                            Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    /**
     * Private method that will add a location listener to the location manager
     * @return 1 if the permissions aren't granted, 0 if they are
     */
    private int addLocationListener() {
        // If the permissions to access position aren't granted we ask the user for them
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE },11 );
            return 1;
        }
        else {
            // If the permissions are granted we initialize the listener
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            InitLocationListener();
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, ll);
        return 0;
    }

    /**
     * Method which defines what heppens when we come back to this activity with a signal from the
     * previous activity
     * @param request
     * @param result
     * @param data
     */
    protected void onActivityResult(int request, int result, Intent data) {
        // If the clear button has been pressed in the graph activity we clear the array
        if (request == 1 && result == 1) {
            locationList = new ArrayList<>();
            Toast.makeText(MainActivity.this, "Previous locations cleared",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
