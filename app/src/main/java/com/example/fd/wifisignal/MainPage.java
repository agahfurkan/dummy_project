package com.example.fd.wifisignal;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SENSOR_SERVICE;


/*
 * Created by fd on 1/14/2018.
 */

public class MainPage extends Fragment implements View.OnClickListener, SensorEventListener {
    @BindView(R.id.wifi_recyclerView)
    RecyclerView wifiRecyclerView;
    @BindView(R.id.lowFrequency)
    CheckBox lowFrequency;
    @BindView(R.id.highFrequency)
    CheckBox highFrequency;
    @BindView(R.id.magnetic_sensor_checkBox)
    CheckBox includeMagneticSensor;
    @BindView(R.id.orientation_checkBox)
    CheckBox includeOrientation;
    @BindView(R.id.start_button)
    Button startButton;
    @BindView(R.id.stop_button)
    Button stopButton;
    @BindView(R.id.point_no)
    EditText pointNo;
    @BindView(R.id.point_name)
    EditText pointName;
    @BindView(R.id.time_spinner)
    Spinner timeSpinner;
    @BindView(R.id.txt_spinner)
    Spinner txtFileSpinner;
    @BindView(R.id.time_counter)
    TextView timeCounter;
    @BindView(R.id.select_file)
    Button selectFile;
    @BindView(R.id.floor_edittext)
    EditText floorEdittext;
    @BindView(R.id.building_id_edittext)
    EditText buildingIdEdittext;
    @BindView(R.id.latitude_textview)
    TextView latitudeTextview;
    @BindView(R.id.longitude_textview)
    TextView longitudeTextview;
    @BindView(R.id.orientation_textView)
    TextView orientationTextView;
    private WifiManager wifiManager;
    private WifiReceiver receiverWifi;
    private WifiModel wifiModel;
    private String[] dizi = {"1", "3", "5", "10", "20", "240"};
    private File folder;
    private int count = 1;
    private Handler handler;
    private Runnable runnableCode;
    private boolean writeData = false;
    private LocationManager locationManager;
    private WifiListAdapter wifiListAdapter;
    private SensorManager sensorManager;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private List<PointModel> listFromTxt;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float Mx, My, Mz;
    private double latitude, longitude;
    private List<String> pointNameList;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View pageView = inflater.inflate(R.layout.fragment_layout, container, false);
        ButterKnife.bind(this, pageView);
        wifiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        receiverWifi = new WifiReceiver();
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity(), "Open Location Pls !", Toast.LENGTH_LONG).show();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_textview, dizi);
        timeSpinner.setAdapter(arrayAdapter);
        stopButton.setEnabled(false);
        stopButton.setAlpha(0.5f);

        folder = Environment.getExternalStoragePublicDirectory("WifiData");
        folder.mkdir();
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new Handler();
        runnableCode = new Runnable() {
            @Override
            public void run() {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    wifiManager.startScan();
                }
                handler.postDelayed(runnableCode, 1000);
            }
        };
        handler.post(runnableCode);
        List<WifiModel> list = new ArrayList<>();
        wifiListAdapter = new WifiListAdapter(list);
        wifiRecyclerView.setAdapter(wifiListAdapter);
        return pageView;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);//guncelleme sikligi
        locationRequest.setFastestInterval(0);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();
                latitudeTextview.setText(String.valueOf(latitude));
                longitudeTextview.setText(String.valueOf(longitude));
            }
        };
        LocationServices.getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, locationCallback, null);
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void writeData(File myFile, String data) throws IOException {
        FileOutputStream fileOutputStream = null;
        fileOutputStream = new FileOutputStream(myFile, true);
        String[] filePath = new String[1];
        filePath[0] = myFile.getPath();
        fileOutputStream.write((data + "\n").getBytes());
        fileOutputStream.close();
        MediaScannerConnection.scanFile(getActivity(), filePath, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 666) {//file select
            listFromTxt = new ArrayList<>();
            InputStream inputStream = null;
            String line = null;
            pointNameList = new ArrayList<>();
            try {
                inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));
                while ((line = reader.readLine()) != null) {
                    String[] temp = line.split("&");
                    PointModel pointModel = new PointModel(temp[0], temp[2], temp[1]);
                    listFromTxt.add(pointModel);
                    pointNameList.add(temp[0]);
                }
                arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_textview, pointNameList);
                txtFileSpinner.setAdapter(arrayAdapter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @OnClick({R.id.start_button, R.id.stop_button, R.id.select_file, R.id.select_previous_item, R.id.select_next_item})
    public void onClick(View v) {
        if (v.getId() == R.id.start_button) {//start button
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getActivity(), "Open Location Pls !", Toast.LENGTH_SHORT).show();
            } else {
                if (txtFileSpinner.getSelectedItem() != null || (!pointNo.getText().toString().equals("") && !pointName.getText().toString().equals(""))) {
                    getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    pointNo.setEnabled(false);
                    pointName.setEnabled(false);
                    timeSpinner.setEnabled(false);
                    stopButton.setAlpha(1);
                    pointName.setAlpha(0.5f);
                    pointNo.setAlpha(0.5f);
                    timeSpinner.setAlpha(0.5f);
                    startButton.setAlpha(0.5f);
                    writeData = true;
                } else {
                    Toast.makeText(getActivity(), "Lütfen nokta adı giriniz.", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (v.getId() == R.id.stop_button) {//stop button
            startButton.setEnabled(true);
            pointNo.setEnabled(true);
            pointName.setEnabled(true);
            stopButton.setEnabled(false);
            writeData = false;
            timeSpinner.setEnabled(true);

            stopButton.setAlpha(0.5f);
            pointName.setAlpha(1);
            pointNo.setAlpha(1);
            timeSpinner.setAlpha(1);
            startButton.setAlpha(1);
            count = 1;
            // getActivity().unregisterReceiver(receiverWifi);
        } else if (v.getId() == R.id.select_file) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Txt file."), 666);
        } else if (v.getId() == R.id.select_previous_item) {
            if (txtFileSpinner.getSelectedItem() != null) {
                int index = txtFileSpinner.getSelectedItemPosition();
                if (index != 0) {
                    index = index - 1;
                }
                if (txtFileSpinner.getCount() > 0) {
                    txtFileSpinner.setSelection(index);
                }
            }
        } else if (v.getId() == R.id.select_next_item) {
            if (txtFileSpinner.getSelectedItem() != null) {
                int index = txtFileSpinner.getSelectedItemPosition();
                if (index != txtFileSpinner.getCount() - 1) {
                    index = index + 1;
                }
                if (txtFileSpinner.getCount() > 0) {
                    txtFileSpinner.setSelection(index);
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values, 0, mLastAccelerometer, 0, sensorEvent.values.length);
            mLastAccelerometerSet = true;
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0, mLastMagnetometer, 0, sensorEvent.values.length);
            mLastMagnetometerSet = true;
            Mx = sensorEvent.values[0];
            My = sensorEvent.values[1];
            Mz = sensorEvent.values[2];
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            orientationTextView.setText(String.valueOf((int) (Math.toDegrees(mOrientation[0]) + 360) % 360));
            //Log.d("OrientationTestActivity", String.format("Orientation: %f, %f, %f", mOrientation[0], mOrientation[1], mOrientation[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class WifiReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                if (count > Integer.valueOf(timeSpinner.getSelectedItem().toString())) {
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                    pointNo.setEnabled(false);
                    pointName.setEnabled(false);
                    stopButton.setAlpha(0.5f);
                    pointName.setAlpha(0.5f);
                    pointNo.setAlpha(0.5f);
                    timeSpinner.setAlpha(1);
                    startButton.setAlpha(1);
                    if (pointNo.getText() != null && !pointNo.getText().toString().equals("")) {
                        pointNo.setText(String.valueOf(Integer.valueOf(pointNo.getText().toString()) + 1));
                    }
                    writeData = false;
                    timeSpinner.setEnabled(true);
                    count = 1;
                    timeCounter.setText("KAYDEDILDI");
                    Uri notification = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.soundtrack);
                    Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
                    r.play();
                    getActivity().unregisterReceiver(receiverWifi);
                    //pointNameList.remove(txtFileSpinner.getSelectedItemPosition());
                    arrayAdapter.remove(txtFileSpinner.getSelectedItem().toString());
                    arrayAdapter.notifyDataSetChanged();
                }
                File myFile = null, myFile2 = null;
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                if (writeData) {
                    if (!pointName.getText().toString().equals("")) {
                        myFile = new File(folder, "a" + pointName.getText().toString() + pointNo.getText().toString() + ".txt");
                        myFile2 = new File(folder, "b" + pointName.getText().toString() + pointNo.getText().toString() + ".txt");
                    } else {
                        myFile = new File(folder, "a" + txtFileSpinner.getSelectedItem().toString() + ".txt");
                        myFile2 = new File(folder, "b" + txtFileSpinner.getSelectedItem().toString() + ".txt");
                    }

                    try {
                        if (!myFile.exists()) {//First write
                            if (txtFileSpinner.getSelectedItem() != null) {
                                for (int x = 0; x < listFromTxt.size(); x++) {
                                    if (listFromTxt.get(x).getPointName().equals(txtFileSpinner.getSelectedItem().toString())) {
                                        writeData(myFile, "Device:" + Build.MODEL);
                                        Calendar calendar = Calendar.getInstance();
                                        Date date = calendar.getTime();
                                        SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy");
                                        String formattedDate = df.format(date);
                                        writeData(myFile, "Date:" + formattedDate);
                                        writeData(myFile, "StartTime:" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                                        writeData(myFile, "BuildingID/Floor:" + buildingIdEdittext.getText() + "/" + floorEdittext.getText());
                                        writeData(myFile, "PointID:" + listFromTxt.get(x).getPointName());
                                        writeData(myFile, "Aprx GNSS Coordinates");
                                        writeData(myFile, latitude + "&" + longitude);
                                        writeData(myFile, "Coordinates");
                                        writeData(myFile, listFromTxt.get(x).getX() + "$" + listFromTxt.get(x).getY());
                                        if (includeOrientation.isChecked()) {
                                            writeData(myFile, "Orientation:" + (int) (Math.toDegrees(mOrientation[0]) + 360) % 360);
                                        } else {
                                            writeData(myFile, "Orientation:" + "Not Included");
                                        }
                                        if (includeMagneticSensor.isChecked()) {
                                            writeData(myFile, "Magnetic Field:" + Mx + "&" + My + "%" + Mz);
                                        } else {
                                            writeData(myFile, "Magnetic Field:" + "Not Included");
                                        }

                                        break;
                                    }
                                }
                            } else {
                                writeData(myFile, "Device:" + Build.MODEL);
                                Calendar calendar = Calendar.getInstance();
                                Date date = calendar.getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy");
                                String formattedDate = df.format(date);
                                writeData(myFile, "Date:" + formattedDate);
                                writeData(myFile, "StartTime:" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                                writeData(myFile, "BuildingID/Floor:" + buildingIdEdittext.getText() + "/" + floorEdittext.getText());
                                writeData(myFile, "PointID:" + pointName.getText() + pointNo.getText());
                                writeData(myFile, "Aprx GNSS Coordinates");
                                writeData(myFile, latitude + "&" + longitude);
                                writeData(myFile, "Coordinates");
                                writeData(myFile, "No Coordinates");
                                writeData(myFile, "Orientation:" + (int) (Math.toDegrees(mOrientation[0]) + 360) % 360);
                                writeData(myFile, "Magnetic Field:" + Mx + "&" + My + "%" + Mz);
                            }
                        }
                        if (!myFile2.exists()) {//first write
                            if (txtFileSpinner.getSelectedItem() != null) {
                                for (int x = 0; x < listFromTxt.size(); x++) {
                                    if (listFromTxt.get(x).getPointName().equals(txtFileSpinner.getSelectedItem().toString())) {
                                        writeData(myFile2, "Device:" + Build.MODEL);
                                        Calendar calendar = Calendar.getInstance();
                                        Date date = calendar.getTime();
                                        SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy");
                                        String formattedDate = df.format(date);
                                        writeData(myFile2, "Date:" + formattedDate);
                                        writeData(myFile2, "StartTime:" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                                        writeData(myFile2, "BuildingID/Floor:" + buildingIdEdittext.getText() + "/" + floorEdittext.getText());
                                        writeData(myFile2, "PointID:" + listFromTxt.get(x).getPointName());
                                        writeData(myFile2, "Aprx GNSS Coordinates");
                                        writeData(myFile2, latitude + "&" + longitude);
                                        writeData(myFile2, "Coordinates");
                                        writeData(myFile2, listFromTxt.get(x).getX() + "$" + listFromTxt.get(x).getY());
                                        writeData(myFile2, "Orientation:" + (int) (Math.toDegrees(mOrientation[0]) + 360) % 360);
                                        writeData(myFile2, "Magnetic Field:" + Mx + "&" + My + "%" + Mz);
                                        break;
                                    }
                                }
                            } else {
                                writeData(myFile2, "Device:" + Build.MODEL);
                                Calendar calendar = Calendar.getInstance();
                                Date date = calendar.getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy");
                                String formattedDate = df.format(date);
                                writeData(myFile2, "Date:" + formattedDate);
                                writeData(myFile2, "StartTime:" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                                writeData(myFile2, "BuildingID/Floor:" + buildingIdEdittext.getText() + "/" + floorEdittext.getText());
                                writeData(myFile2, "PointID:" + pointName.getText() + pointNo.getText());
                                writeData(myFile2, "Aprx GNSS Coordinates");
                                writeData(myFile2, latitude + "&" + longitude);
                                writeData(myFile2, "Coordinates");
                                writeData(myFile2, "No Coordinates");
                                writeData(myFile2, "Orientation:" + (int) (Math.toDegrees(mOrientation[0]) + 360) % 360);
                                writeData(myFile2, "Magnetic Field:" + Mx + "&" + My + "%" + Mz);
                            }
                        }
                        writeData(myFile, String.valueOf(count));
                        writeData(myFile2, String.valueOf(count));
                        timeCounter.setText(String.valueOf(count));
                        count++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                List<WifiModel> tempList = new ArrayList<>();
                for (int a = 0; a < mScanResults.size(); a++) {
                    wifiModel = new WifiModel();
                    wifiModel.setSsid(mScanResults.get(a).SSID + ": " + mScanResults.get(a).BSSID);
                    wifiModel.setLevel(String.valueOf(mScanResults.get(a).level));
                    wifiModel.setFreq(mScanResults.get(a).frequency);
                    wifiModel.setBssid(mScanResults.get(a).BSSID);
                    tempList.add(wifiModel);
                }
                wifiListAdapter.setWifiList(tempList);
                Collections.sort(tempList);
                List<WifiModel> temp = wifiListAdapter.getWifiList();
                if (writeData) {
                    for (int x = 0; x < temp.size(); x++) {
                        try {
                            if (temp.get(x).getFreq() < 3000) {
                                if (lowFrequency.isChecked()) {
                                    // freq=String.valueOf(temp.get(x).getFreq()).substring(0, 1) + "." + String.valueOf(temp.get(x).getFreq()).substring(1, 2)
                                    writeData(myFile, "BSSID&" + temp.get(x).getBssid() + "&" + temp.get(x).getLevel());
                                }
                            } else {
                                if (highFrequency.isChecked()) {
                                    writeData(myFile2, "BSSID&" + temp.get(x).getBssid() + "&" + temp.get(x).getLevel());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
