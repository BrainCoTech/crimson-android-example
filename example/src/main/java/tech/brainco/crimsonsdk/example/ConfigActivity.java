package tech.brainco.crimsonsdk.example;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import tech.brainco.crimsonsdk.CrimsonDevice;
import tech.brainco.crimsonsdk.CrimsonDeviceListener;
import tech.brainco.crimsonsdk.CrimsonSDK;
import tech.brainco.crimsonsdk.DeviceInfo;
import tech.brainco.crimsonsdk.IMU;
import tech.brainco.crimsonsdk.IMUSampleRate;


public class ConfigActivity extends BaseActivity {
    private static final String TAG = "ConfigActivity";
    private CrimsonDevice device = null;

    MenuItem disconnectButton;
    MenuItem connectButton;
    MenuItem shutDownButton;

    private TextView manufacturerNameText;
    private TextView modelNumberText;
    private TextView serialNumberText;
    private TextView hardwareRevisionText;
    private TextView firmwareRevisionText;

    private Spinner spinnerAfeSampleRate;
    private Spinner spinnerDataChannel;
    private Spinner spinnerLeadOffOption;
    private Spinner spinnerRldChannel;
    private Spinner spinnerLeadOffChannel;
    private Spinner spinnerImuSampleRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        setupViews();
        setupSpinners();

        // Setup device
        device = getSelectedCrimsonDevice();
        DeviceListener listener = new DeviceListener();

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(device.getName());
        actionBar.setSubtitle(device.getId());
        device.setListener(listener);

        manufacturerNameText.setText(device.getManufacturerName());
        modelNumberText.setText(device.getModelNumber());
        serialNumberText.setText(device.getSerialNumber());
        hardwareRevisionText.setText(device.getHardwareRevision());
        firmwareRevisionText.setText(device.getFirmwareRevision());
    }


    private void setupViews() {
        manufacturerNameText = findViewById(R.id.device_manufacturer_name);
        modelNumberText = findViewById(R.id.device_model_number);
        serialNumberText = findViewById(R.id.device_serial_number);
        hardwareRevisionText = findViewById(R.id.device_hardware_revision);
        firmwareRevisionText = findViewById(R.id.device_firmware_revision);

        spinnerAfeSampleRate = findViewById(R.id.spinner_afe_sample_rate);
        spinnerDataChannel = findViewById(R.id.spinner_data_channel);
        spinnerLeadOffOption = findViewById(R.id.spinner_lead_off_option);
        spinnerRldChannel = findViewById(R.id.spinner_rld_channel);
        spinnerLeadOffChannel = findViewById(R.id.spinner_lead_off_channel);
        spinnerImuSampleRate = findViewById(R.id.spinner_imu_sample_rate);
    }

    private void setupSpinners() {


        ArrayAdapter<String> afeSampleRateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : CrimsonSDK.AFESampleRate.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) afeSampleRateAdapter.add(field.getName());
        }
        afeSampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAfeSampleRate.setAdapter(afeSampleRateAdapter);
        spinnerAfeSampleRate.setSelection(0);


        ArrayAdapter<String> afeDataChannelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : CrimsonSDK.AFEDataChannel.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) afeDataChannelAdapter.add(field.getName());
        }
        afeSampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataChannel.setAdapter(afeDataChannelAdapter);
        spinnerDataChannel.setSelection(0);


        ArrayAdapter<String> afeDataLeadOffOptionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : CrimsonSDK.AFELeadOffOption.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()))
                afeDataLeadOffOptionAdapter.add(field.getName());
        }
        afeDataLeadOffOptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeadOffOption.setAdapter(afeDataLeadOffOptionAdapter);
        spinnerLeadOffOption.setSelection(0);

        ArrayAdapter<String> imuDataSampleRateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : IMUSampleRate.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()))
                imuDataSampleRateAdapter.add(field.getName());
        }
        imuDataSampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImuSampleRate.setAdapter(imuDataSampleRateAdapter);
        spinnerImuSampleRate.setSelection(0);

        ArrayAdapter<String> leadOffChannelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : CrimsonSDK.AFEDataChannel.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) leadOffChannelAdapter.add(field.getName());
        }
        leadOffChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeadOffChannel.setAdapter(leadOffChannelAdapter);
        spinnerLeadOffChannel.setSelection(0);

        ArrayAdapter<String> rldChannelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (Field field : CrimsonSDK.AFEDataChannel.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) rldChannelAdapter.add(field.getName());
        }
        rldChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRldChannel.setAdapter(rldChannelAdapter);
        spinnerRldChannel.setSelection(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        disconnectButton = menu.findItem(R.id.action_device_disconnect);
        shutDownButton = menu.findItem(R.id.action_shut_down);
        connectButton = menu.findItem(R.id.action_device_connect);

        if (device.isConnected()) {
            disconnectButton.setVisible(true);
            shutDownButton.setVisible(true);
            connectButton.setVisible(false);

        } else {
            connectButton.setVisible(true);
            shutDownButton.setVisible(false);
            disconnectButton.setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_device_connect) {
            device.connect(this);
        } else if (itemId == R.id.action_device_disconnect) {
            device.disconnect();
        } else if (itemId == R.id.action_shut_down) {
            shutDownClick();
            Toast.makeText(getApplicationContext(), "shutting down device...", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(ConfigActivity.this, ScanActivity.class);
            ConfigActivity.this.startActivity(myIntent);
        } else {
            Log.d(TAG, "Unknown action");
        }
        return super.onOptionsItemSelected(item);
    }


    private class DeviceListener extends CrimsonDeviceListener {

        @Override
        public void onConnectivityChange(int connectivity) {
            if (connectivity == CrimsonSDK.Connectivity.CONNECTED) {
                connectButton.setVisible(false);
                disconnectButton.setVisible(true);
                shutDownButton.setVisible(true);
            } else if (connectivity == CrimsonSDK.Connectivity.DISCONNECTED) {
                connectButton.setVisible(true);
                disconnectButton.setVisible(false);
                shutDownButton.setVisible(false);
            } else if (connectivity == CrimsonSDK.Connectivity.CONNECTING) {
                connectButton.setTitle("Connecting");
            } else if (connectivity == CrimsonSDK.Connectivity.DISCONNECTING) {
                disconnectButton.setTitle("Disconnecting");
            }

        }

        @Override
        public void onIMUData(IMU data) {
            Log.d(ConfigActivity.class.getSimpleName(), data.toString());
        }

        @Override
        public void onDeviceInfoReady(DeviceInfo info) {
            manufacturerNameText.setText(info.manufacturerName);
            modelNumberText.setText(info.modelNumber);
            serialNumberText.setText(info.serialNumber);
            hardwareRevisionText.setText(info.hardwareRevision);
            firmwareRevisionText.setText(info.firmwareRevision);
        }
    }

    public void sendAFEConfiguration(View v) {
        if (device.isConnected()) {
            try {
                int afeSampleRate = CrimsonSDK.AFESampleRate.class.getDeclaredField((String) spinnerAfeSampleRate.getSelectedItem()).getInt(null);
                int afeDataChannel = CrimsonSDK.AFEDataChannel.class.getDeclaredField((String) spinnerDataChannel.getSelectedItem()).getInt(null);
                int leadOffOption = CrimsonSDK.AFELeadOffOption.class.getDeclaredField((String) spinnerLeadOffOption.getSelectedItem()).getInt(null);
                int rldOption = CrimsonSDK.AFEDataChannel.class.getDeclaredField((String) spinnerRldChannel.getSelectedItem()).getInt(null);
                int leadOffChannel = CrimsonSDK.AFEDataChannel.class.getDeclaredField((String) spinnerLeadOffChannel.getSelectedItem()).getInt(null);

                Toast.makeText(getApplicationContext(), "sending AFE configuration...", Toast.LENGTH_SHORT).show();

                device.configAFE(afeSampleRate, afeDataChannel, leadOffOption, leadOffChannel, rldOption, error -> {
                    if (error != null) {
                        Log.i("configAFE:" + error.getCode(), "configAFE " + error.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.i("Error", e.getMessage());
            }
        } else {
            deviceNotConnectedAlert();
        }
    }


    public void sendIMUConfiguration(View v) {
        if (device.isConnected()) {
            try {
                Toast.makeText(getApplicationContext(), "sending ACC configuration...", Toast.LENGTH_SHORT).show();
                device.startIMU(error -> {
                    if (error != null) {
                        Log.d(ConfigActivity.class.getSimpleName(), error.toString());
                    }
                });
            } catch (Exception e) {
                Log.i("Error", e.getMessage());
            }
        } else {
            deviceNotConnectedAlert();
        }

    }


    public void renameClick(View v) {
        if (device.isConnected()) {
            renameDialog();
        } else {
            deviceNotConnectedAlert();
        }
    }


    private void renameDialog() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(ConfigActivity.this);
        LayoutInflater inflater = LayoutInflater.from(ConfigActivity.this);
        View myView = inflater.inflate(R.layout.device_rename, null);

        final AlertDialog dialog = myDialog.create();
        dialog.setView(myView);

        TextView deviceName = myView.findViewById(R.id.device_name);
        EditText edtDeviceNewName = myView.findViewById(R.id.edt_device_new_name);
        Button btnSave = myView.findViewById(R.id.btn_save);

        deviceName.setText(device.getName());

        btnSave.setOnClickListener(v -> {
            String deviceNewName = edtDeviceNewName.getText().toString().trim();

            if (TextUtils.isEmpty(deviceNewName)) {
                edtDeviceNewName.setError("Required Field..");
                return;
            }

            device.setDeviceName(deviceNewName, error -> {
                if (error != null) {
                    Log.i("setDeviceName:" + error.getCode(), "setDeviceName " + error.getMessage());
                }
            });

            dialog.dismiss();
        });

        dialog.show();

    }


    public void redClick(View v) {
        if (device.isConnected()) {
            device.setLEDColor(255, 0, 0, error -> {
                if (error != null) {
                    Log.i("setLEDColor:" + error.getCode(), "setLEDColor:red: " + error.getMessage());
                }
            });
        } else {
            deviceNotConnectedAlert();
        }
    }


    public void greenClick(View v) {
        if (device.isConnected()) {
            device.setLEDColor(0, 255, 0, error -> {
                if (error != null) {
                    Log.i("setLEDColor:" + error.getCode(), "setLEDColor:green: " + error.getMessage());
                }
            });
        } else {
            deviceNotConnectedAlert();
        }
    }


    public void blueClick(View v) {
        if (device.isConnected()) {
            device.setLEDColor(0, 0, 255, error -> {
                if (error != null) {
                    Log.i("setLEDColor:" + error.getCode(), "setLEDColor:blue: " + error.getMessage());
                }
            });
        } else {
            deviceNotConnectedAlert();
        }
    }


    public void shutDownClick() {
        if (device.isConnected()) {
            device.shutdown(error -> {
                if (error != null) {
                    Log.i("shutdown:" + error.getCode(), "shutdown" + error.getMessage());
                }
            });
        } else {
            deviceNotConnectedAlert();
        }
    }
}
