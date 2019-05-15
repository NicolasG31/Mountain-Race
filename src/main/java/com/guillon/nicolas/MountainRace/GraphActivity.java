package com.guillon.nicolas.MountainRace;

import android.app.ActionBar;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Graph Activity class, which represents the statistics page
 */
public class GraphActivity extends AppCompatActivity {

    /**
     * Array of locations we previously filled
     */
    private ArrayList<Location> locationList;
    /**
     * Different textviews which will display our data
     */
    TextView max_sp, avg_sp, min_sp, dist, time_, max_al, min_al, loss_, gain_;
    /**
     * Maximum and minimum altitude used to draw a graph
     */
    int maxAltitude, minAltitude;
    /**
     * Maximum and minimum speed used to draw a graph
     */
    int maxSpeed, minSpeed;

    /**
     * When the activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Enables the action bar at the top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Binds the textviews with data
        bindUI();
        // Retrieves the data from the previous activity
        locationList = (ArrayList<Location>) getIntent().getExtras().getSerializable("list");
        Log.d("DEBUG", "Number of locations : " + locationList.size());
        retrieveData();
        drawGraph();
    }

    /**
     * Method which prepares the data to draw the graph
     */
    public void drawGraph() {
        // Binds to the custom view
        GraphView graphView = (GraphView)findViewById(R.id.graph_view);
        // Initializes and fills our data arrays
        int graphArray[] = new int[locationList.size()];
        int graphArray2[] = new int[locationList.size()];

        for(int i = 0; i < graphArray.length; ++i) {
            graphArray[i] = (int)Math.round(locationList.get(i).getAltitude());
        }
        for(int i = 0; i < graphArray.length; ++i) {
            graphArray2[i] = (int)Math.round(locationList.get(i).getSpeed());
        }
        // Calls the view method to draw the graph
        graphView.setGraphArray(graphArray, maxAltitude, minAltitude,
                graphArray2, maxSpeed, minSpeed);
    }

    /**
     * Binds UI variables with XML values
     */
    public void bindUI() {
        max_sp = (TextView) findViewById(R.id.max_speed);
        avg_sp = (TextView) findViewById(R.id.average_speed);
        min_sp = (TextView) findViewById(R.id.min_speed);
        dist = (TextView) findViewById(R.id.distance);
        time_ = (TextView) findViewById(R.id.time);
        max_al = (TextView) findViewById(R.id.max_altitude);
        min_al = (TextView) findViewById(R.id.min_altitude);
        loss_ = (TextView) findViewById(R.id.loss);
        gain_ = (TextView) findViewById(R.id.gain);
    }

    /**
     * Retrieves the data and stats displayed on the activity
     */
    public void retrieveData() {
        int i = 0;
        double maxSpeed = locationList.get(0).getSpeed();
        double minSpeed = locationList.get(0).getSpeed();
        double avgSpeed = 0;
        double distance = 0;
        double time;
        double maxAltitude = locationList.get(0).getAltitude();
        double minAltitude = locationList.get(0).getAltitude();
        double savedAlt = locationList.get(0).getAltitude();
        double gain = 0;
        double loss = 0;

        for (Location loc : locationList) {
            // Getting speed data
            double sp = loc.getSpeed();
            avgSpeed += sp;
            if (sp > maxSpeed)
                maxSpeed = sp;
            if (sp < minSpeed)
                minSpeed = sp;

            // Getting distance data
            if (i > 0)
                distance += locationList.get(i).distanceTo(locationList.get(i - 1));

            // Getting altitude data
            double alt = loc.getAltitude();
            if (alt > maxAltitude)
                maxAltitude = alt;
            if (alt < minAltitude)
                minAltitude = alt;
            if (i > 0) {
                if (alt > savedAlt)
                    gain += (alt - savedAlt);
                else
                    loss += (savedAlt - alt);
                savedAlt = alt;
            }
            i++;
        }
        avgSpeed = avgSpeed / locationList.size();
        time = locationList.get(locationList.size() - 1).getTime() - locationList.get(0).getTime();

        // Associates values with the textviews
        max_sp.setText("Max: " + Math.round(maxSpeed) + "m/s");
        avg_sp.setText("Average: " + Math.round(avgSpeed) + "m/s");
        min_sp.setText("Min: " + Math.round(minSpeed) + "m/s");
        dist.setText("Distance \n" + Math.round(distance) + "m");
        time_.setText("Time \n" + Math.round(time/1000) + "s");
        max_al.setText("Max: " + Math.round(maxAltitude) + "m");
        min_al.setText("Min: " + Math.round(minAltitude) + "m");
        gain_.setText("Gain: " + Math.round(gain) + "m");
        loss_.setText("Loss: " + Math.round(loss) + "m");

        // saves the values for the graph
        this.maxAltitude = (int)Math.round(maxAltitude);
        this.minAltitude = (int)Math.round(minAltitude);
        this.maxSpeed = (int)Math.round(maxSpeed);
        this.minSpeed = (int)Math.round(minSpeed);
    }

    /**
     * Defines what happen when we click on the Clear button.
     * It goes back to the main page and warn it to clear the data
     * @param view
     */
    public void onClearClick(View view) {
        Intent res = new Intent(Intent.ACTION_VIEW);
        setResult(1, res);
        finish();
    }

    /**
     * Method which performs the return action bar button
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
