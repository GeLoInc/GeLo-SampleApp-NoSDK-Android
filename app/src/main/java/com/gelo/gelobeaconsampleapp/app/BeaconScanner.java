package com.gelo.gelobeaconsampleapp.app;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

import java.util.Arrays;

/**
 * Representation of an iBeacon
 */
class iBeacon {
    String uuid;
    int major;
    int minor;
    int rssi;

    public iBeacon (String uuid, int major, int minor, int rssi) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
    }

    public void updateRSSI (int rssi) {
        this.rssi = rssi;
    }

    public boolean equals (iBeacon beacon) {
        if (beacon.major == this.major && beacon.minor == this.minor) {
            return true;
        }

        return false;
    }
}

interface BeaconFoundCallback {
    void onNearestBeaconChanged(String uuid, int major, int minor);
}

/**
 * Scans for beacons using the GeLo UUID and reports when the nearest beacon changes.
 */
public class BeaconScanner {

    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    final static String GELO_UUID = "11E44F094EC4407E9203CF57A50FBCE0";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconFoundCallback mCallback;
    private iBeacon nearestBeacon;

    public BeaconScanner(Context c) {
        mBluetoothManager = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        nearestBeacon = null;
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScanningForBeacons(BeaconFoundCallback callback) {
        //Check to see if the device supports Bluetooth and that it's turned on
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mCallback = callback;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //For readability we convert the bytes of the UUID into hex
            String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));

            if (UUIDHex.equals(GELO_UUID)) {
                //Bytes 25 and 26 of the advertisement packet represent the major value
                int major = ((scanRecord[25]  & 0xFF) << 8)
                        | (scanRecord[26]  & 0xFF);

                //Bytes 27 and 28 of the advertisement packet represent the minor value
                int minor = ((scanRecord[27] & 0xFF) << 8)
                        | (scanRecord[28] & 0xFF);

                iBeacon beacon = new iBeacon(UUIDHex, major, minor, rssi);

                //RSSI values increase towards zero as the source gets closer to the reciever
                if (nearestBeacon == null || beacon.rssi > nearestBeacon.rssi) {
                    nearestBeacon = beacon;
                    //notify the application that a beacon is closer
                    mCallback.onNearestBeaconChanged(beacon.uuid, beacon.major, beacon.minor);
                //If the beacon we found  is the current nearest, update the RSSI. You may have
                //gotten closer or further away and you don't want to remember an old RSSI
                }else if (beacon.equals(nearestBeacon)) {
                    nearestBeacon.updateRSSI(beacon.rssi);
                }
            }
        }
    };

    private static String convertBytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for ( int i = 0; i < bytes.length; i++ ) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[v >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hex);
    }
}
