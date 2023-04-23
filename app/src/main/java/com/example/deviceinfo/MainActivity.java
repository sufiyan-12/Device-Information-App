package com.example.deviceinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.graphics.ImageFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ArrayList<Model> itemList;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private TextView Accelerometer,Gyroscope,Barometer,RotationVector,ProximitySensor,AmbientLightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVariables();

        // Retrieving the device manufacturer
        String manufacturer = Build.MANUFACTURER;

        // Retrieving the device model name and number
        String model = Build.MODEL;
        String modelNumber = Build.DEVICE;

        // Retrieving the device RAM size
        long totalMemory = 0L;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
            String line;
            Pattern pattern = Pattern.compile("MemTotal:\\s*(\\d+)\\s*kB");
            Matcher matcher;
            while ((line = reader.readLine()) != null) {
                matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    totalMemory = Long.parseLong(matcher.group(1));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long ramSize = (long) Math.ceil((totalMemory / (1024.0 * 1024)));

        // Retrieving the device storage size
        long totalStorage = 0L;
        try {
            Process process = Runtime.getRuntime().exec("df");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            double storageSize = 0.0;
            Pattern pattern = Pattern.compile("/storage/\\w+");
            Matcher matcher;
            while ((line = reader.readLine()) != null) {
                matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String mountPoint = matcher.group(0);
                    line = line.replace(mountPoint, "");
                    String[] splitLine = line.trim().split("\\s+");
                    storageSize = Double.parseDouble(splitLine[1]) / 1024.0;
                    totalStorage += storageSize;
                }
            }
            totalStorage = (long) Math.ceil(storageSize / 1024);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieving the battery current charging level
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
        int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPercentage = (float) batteryLevel / batteryScale * 100;

        // Retrieving the Android version
        String androidVersion = Build.VERSION.RELEASE;

        // Retrieving the camera megapixel and aperture
        int megapixels = 0;
        float aperture = 0f;
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
                if (sizes != null && sizes.length > 0) {
                    megapixels = (int) ((sizes[0].getWidth() * sizes[0].getHeight()) / 1000000);
                }
                float[] apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (apertures != null && apertures.length > 0) {
                    aperture = apertures[0];
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Get CPU information
        String cpuInfo = "";
        byte[] byteArry = new byte[1024];
        String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
        String Holder = "";
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(DATA);

            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();

            while(inputStream.read(byteArry) != -1){

                Holder = Holder + new String(byteArry);
            }

            inputStream.close();

        } catch(IOException ex){

            ex.printStackTrace();
        }
        cpuInfo = Holder;

        // Get GPU information
        String gpuInfo = "";

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        gpuInfo = configurationInfo.getGlEsVersion();

        // Retrieving the live sensor reading
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor ambientLightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Registering the sensor listeners
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener( this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener( this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener( this, ambientLightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Retrieving the display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Storing Values into the lis
        itemList.add(new Model( "Manufacturer: ", manufacturer));
        itemList.add(new Model( "Model Name: ", model));
        itemList.add(new Model( "Model Number: ", modelNumber));
        itemList.add(new Model( "RAM: " , ramSize + " GB"));
        itemList.add(new Model( "Storage: ", totalStorage + " GB"));
        itemList.add(new Model( "Battery Level: ", batteryPercentage + "%"));
        itemList.add(new Model( "Android Version: ", androidVersion));
        itemList.add(new Model( "Camera Megapixel: ", String.valueOf(megapixels)));
        itemList.add(new Model( "Camera Aperture: ", String.valueOf(aperture)));
        itemList.add(new Model( "CPU: ", cpuInfo));
        itemList.add(new Model( "GPU: ", gpuInfo));
        itemList.add(new Model("Screen Resolution", screenWidth + " x " + screenHeight));



        mainAdapter = new MainAdapter(this, itemList);
        recyclerView.setAdapter(mainAdapter);
    }

    private void initVariables() {
        itemList = new ArrayList();
        recyclerView = findViewById(R.id.recyclerView);
        Accelerometer = findViewById(R.id.Accelerometer);
        Gyroscope = findViewById(R.id.Gyroscope);
        Barometer = findViewById(R.id.Barometer);
        RotationVector = findViewById(R.id.RotationVector);
        ProximitySensor = findViewById(R.id.ProximitySensor);
        AmbientLightSensor = findViewById(R.id.AmbientLightSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    // Retrieving the live sensor reading
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float xAcc = event.values[0];
                float yAcc = event.values[1];
                float zAcc = event.values[2];
                 Accelerometer.setText("x: "+xAcc + ",\n y: " + yAcc + ",\n z: " + zAcc);
                break;
            case Sensor.TYPE_GYROSCOPE:
                float xGyro = event.values[0];
                float yGyro = event.values[1];
                float zGyro = event.values[2];
                Gyroscope.setText("x: " + xGyro + ",\n y: " + yGyro + ",\n z: " + zGyro);
                break;
            case Sensor.TYPE_PRESSURE:
                float pressure = event.values[0];
                Barometer.setText(pressure + " hPa");
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                float xRot = event.values[0];
                float yRot = event.values[1];
                float zRot = event.values[2];
                RotationVector.setText("x: " + xRot + ",\n y: " + yRot + ",\n z: " + zRot );
                break;
            case Sensor.TYPE_PROXIMITY:
                float proximity = event.values[0];
                ProximitySensor.setText(String.valueOf(proximity));
                break;
            case Sensor.TYPE_LIGHT:
                float light = event.values[0];
                AmbientLightSensor.setText(String.valueOf(light));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}