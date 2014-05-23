package com.gelo.gelobeaconsampleapp.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {
    BeaconScanner scanner;
    TextView uuidView;
    TextView majorView;
    TextView minorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uuidView = (TextView)findViewById(R.id.uuidLabel);
        majorView = (TextView)findViewById(R.id.majorLabel);
        minorView = (TextView)findViewById(R.id.minorLabel);

        scanner = new BeaconScanner(getApplicationContext());
        scanner.startScanningForBeacons(new BeaconFoundCallback() {
            @Override
            public void onNearestBeaconChanged(final String uuid, final int major, final int minor) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uuidView.setText(uuid);
                        majorView.setText(Integer.toString(major));
                        minorView.setText(Integer.toString(minor));
                    }
                });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
