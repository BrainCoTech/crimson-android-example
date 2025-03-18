package tech.brainco.crimsonsdk.example;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import java.util.Arrays;
import java.util.Locale;

import tech.brainco.crimsonsdk.BrainWave;
import tech.brainco.crimsonsdk.CrimsonDevice;
import tech.brainco.crimsonsdk.CrimsonDeviceListener;
import tech.brainco.crimsonsdk.CrimsonOTA;
import tech.brainco.crimsonsdk.CrimsonSDK;
import tech.brainco.crimsonsdk.DFUCallback;
import tech.brainco.crimsonsdk.DeviceInfo;
import tech.brainco.crimsonsdk.EEG;
import tech.brainco.crimsonsdk.IMU;

public class DeviceActivity extends BaseActivity {
    private static final String TAG = "DeviceActivity";
    MenuItem disconnectButton;
    MenuItem connectButton;
    MenuItem shutDownButton;

    private CrimsonDevice device = null;

    private TextView batteryLevelText;
    private TextView eegMetaDataText;
    private TextView eegDataText;
    private TextView deviceConnectivityText;
    private TextView deviceContactStateText;
    private TextView deviceAttentionText;
    private TextView deviceMeditationText;

    private TextView deviceDelta;
    private TextView deviceTheta;
    private TextView deviceAlpha;
    private TextView deviceLowBeta;
    private TextView deviceHighBeta;
    private TextView deviceGamma;
    private Button deviceDataStreamButton;
    private Button devicePairButton;
    private TextView imuDataText;

    private boolean paired = false;
    private boolean pairing = false;

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        setupViews();
        DeviceListener listener = new DeviceListener();

        device = getSelectedCrimsonDevice();

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(device.getName());
        actionBar.setSubtitle(device.getId());
        device.setListener(listener);
        deviceDataStreamButton.setOnClickListener(v -> dataStreamClick());
        devicePairButton.setOnLongClickListener(v -> {
            Log.d("TEST_ID", CrimsonDevice.getDeviceId());
            return true;
        });
        devicePairButton.setOnClickListener(v -> pairAction());
        if (device.isConnected()) {
            Log.i(TAG, "device is connected already, startEEG");
            startEEG();
        }
    }

    private void setupViews() {
        batteryLevelText = findViewById(R.id.battery_level);
        eegMetaDataText = findViewById(R.id.eeg_meta_data);
        eegDataText = findViewById(R.id.eeg_data_text);
        imuDataText = findViewById(R.id.imu_data_text);

        deviceConnectivityText = findViewById(R.id.device_connectivity);
        deviceContactStateText = findViewById(R.id.device_contact_state);
        deviceAttentionText = findViewById(R.id.device_attention);
        deviceMeditationText = findViewById(R.id.device_meditation);

        deviceDelta = findViewById(R.id.device_delta);
        deviceTheta = findViewById(R.id.device_theta);
        deviceAlpha = findViewById(R.id.device_alpha);
        deviceLowBeta = findViewById(R.id.device_low_beta);
        deviceHighBeta = findViewById(R.id.device_high_beta);
        deviceGamma = findViewById(R.id.device_gamma);

        devicePairButton = findViewById(R.id.btn_device_pair);
        deviceDataStreamButton = findViewById(R.id.btn_device_data_stream);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        shutDownButton = menu.findItem(R.id.action_shut_down);
        disconnectButton = menu.findItem(R.id.action_device_disconnect);
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
            Intent myIntent = new Intent(DeviceActivity.this, ScanActivity.class);
            DeviceActivity.this.startActivity(myIntent);
        } else {
            Log.d(TAG, "Unknown action");
        }
        return super.onOptionsItemSelected(item);
    }


    private class DeviceListener extends CrimsonDeviceListener {
        @Override
        public void onDeviceInfoReady(DeviceInfo info) {
            Log.i(TAG, "onDeviceInfoReady, info: " + info);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBatteryLevelChange(int batteryLevel) {
            Log.d(TAG, "batteryLevel=" + batteryLevel);
            if (batteryLevel >= 0 && batteryLevel <= 100) {
                batteryLevelText.setText("Battery Level: " + batteryLevel + "%");
            } else {
                batteryLevelText.setText("Battery Level: Unknown");
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onConnectivityChange(int connectivity) {
            Log.d(TAG, "connectivity=" + connectivity);
            connectButton.setVisible(false);
            disconnectButton.setVisible(false);
            shutDownButton.setVisible(false);
            connectButton.setTitle("");
            disconnectButton.setTitle("");

            if (connectivity == CrimsonSDK.Connectivity.CONNECTING) {
                deviceConnectivityText.setText("CONNECTING");
                connectButton.setVisible(true);
                connectButton.setEnabled(false);
                connectButton.setTitle("CONNECTING");

            } else if (connectivity == CrimsonSDK.Connectivity.CONNECTED) {
                deviceConnectivityText.setText("CONNECTED");
                shutDownButton.setVisible(true);
                disconnectButton.setVisible(true);
                disconnectButton.setEnabled(true);
                disconnectButton.setTitle("Disconnect");
                pairAction();

            } else if (connectivity == CrimsonSDK.Connectivity.DISCONNECTED) {
                deviceConnectivityText.setText("DISCONNECTED");
                paired = false;
                connectButton.setVisible(true);
                connectButton.setEnabled(true);
                connectButton.setTitle("Connect");

            } else if (connectivity == CrimsonSDK.Connectivity.DISCONNECTING) {
                deviceConnectivityText.setText("DISCONNECTING");
                disconnectButton.setVisible(true);
                disconnectButton.setEnabled(false);
                disconnectButton.setTitle("DISCONNECTING");
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onContactStateChange(int state) {
            Log.i(TAG, "ContactState=" + state);
            if (state == CrimsonSDK.ContactState.CONTACT) deviceContactStateText.setText("CONTACT");
            else if (state == CrimsonSDK.ContactState.NO_CONTACT)
                deviceContactStateText.setText("NO_CONTACT");
            else if (state == CrimsonSDK.ContactState.UNKNOWN)
                deviceContactStateText.setText("UNKNOWN");
        }

        @Override
        public void onOrientationChange(int orientation) {
           Log.d(TAG, "orientation=" + orientation);
        }

        @Override
        public void onRawEEGData(EEG data) {
            eegMetaDataText.setText(String.format(Locale.getDefault(), "SN:%d SR:%.1f",
                    data.getSequenceNumber(),
                    data.getSampleRate()));

            eegDataText.setText(Arrays.toString(data.getEEGData()));
        }

        @Override
        public void onEEGData(EEG data) {
            eegMetaDataText.setText(String.format(Locale.getDefault(), "SN:%d SR:%.1f",
                    data.getSequenceNumber(),
                    data.getSampleRate()));

            eegDataText.setText(Arrays.toString(data.getEEGData()));
        }

        @Override
        public void onIMUData(IMU data) {
            imuDataText.setText(data.toString());
        }

        @Override
        public void onBrainWave(BrainWave wave) {
            deviceDelta.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getDelta()));
            deviceTheta.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getTheta()));
            deviceAlpha.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getAlpha()));
            deviceLowBeta.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getLowBeta()));
            deviceHighBeta.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getHighBeta()));
            deviceGamma.setText(String.format(Locale.getDefault(), "%.4f",
                    wave.getGamma()));
        }

        @Override
        public void onAttention(float attention) {
            Log.d(TAG, "attention=" + attention);
            deviceAttentionText.setText(String.valueOf(attention));
        }

        @Override
        public void onMeditation(float meditation) {
            Log.d(TAG, "meditation=" + meditation);
            deviceMeditationText.setText(String.valueOf(meditation));
        }
    }

    @SuppressLint("SetTextI18n")
    public void dataStreamClick() {
        if (deviceDataStreamButton.getText().toString().equals("Start")) {
            startEEG();
        } else {
            stopEEG();
        }
    }

    private void doDfu() {
        boolean ret = device.isNewFirmwareAvailable();
        Log.i(TAG, "isNewFirmwareAvailable=" + ret);
        Log.i(TAG, "latestVersion=" + CrimsonOTA.latestVersion);
        Log.i(TAG, "desc=" + CrimsonOTA.desc);

        device.startDfu(this, new DFUCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "OTA Success");
            }

            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, e.getMessage());
            }

            @Override
            public void onProgress(int progress) {
                Log.i(TAG, "progress=" + progress);
            }
        });
    }

    public void shutDownClick() {
        device.shutdown(error -> {
            if (error != null) {
                Log.i("shutdown:" + error.getCode(), ", message=" + error.getMessage());
            }
        });
    }

    private void startEEG() {
        //device.setAttentionModel(0);
        device.startEEGStream(error -> {
            if (error != null) {
                Log.i(TAG, "startEEGStream:" + error.getCode() + ", message=" + error.getMessage());
            } else {
                Log.i(TAG, "startEEGStream success");
                deviceDataStreamButton.setText("Stop");

                // NOTE: start imu if need check device orientation
                startIMU();
            }
        });
        //getSysInfo();
    }

    private void getSysInfo() {
        device.getSysInfo((deviceId, msgId, sysInfo) -> {
            Log.i(TAG, "getSysInfo, deviceId=" + deviceId + ", msgId=" + msgId + ", sysInfo=" + sysInfo);
            // deviceId=58:94:B2:00:A3:0F, msgId=2, sysInfo=SysInfo{firmware_info='445e34e:1634262291:RELEASE', sleep_idle_time_sec=600, vibration_intensity=14}
        });
    }

    private void configAFE() {
        try {
            Thread.sleep(1500); // 1.1.6 固件已Fixed
            device.configAFE(CrimsonSDK.AFESampleRate.SR1000,
                    CrimsonSDK.AFEDataChannel.CH1,
                    CrimsonSDK.AFELeadOffOption.DC_6NA,
                    CrimsonSDK.AFEDataChannel.CH2,
                    CrimsonSDK.AFEDataChannel.BOTH,
                    e -> {
                        if (e == null) {
                            Log.i(TAG, "configAFE success");
                            startIMU();
                        }
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopEEG() {
        device.stopEEGStream(error -> {
            if (error != null) {
                Log.i(TAG, "stopEEGStream:" + error.getCode() + ", message=" + error.getMessage());
            } else {
                Log.i(TAG, "stopEEGStream success");
                deviceDataStreamButton.setText("Start");
            }
        });
    }

    private void startIMU() {
        device.startIMU(error -> {
            if (error != null) {
                Log.i(TAG, "startIMU:" + error.getCode() + ", message=" + error.getMessage());
            } else {
                Log.i(TAG, "startIMU success");
            }
        });
    }

    private void stopIMU() {
        device.stopIMU(error -> {
            if (error != null) {
                Log.i(TAG, "stopIMU:" + error.getCode() + ", message=" + error.getMessage());
            } else {
                Log.i(TAG, "stopIMU success");
            }
        });
    }

    private void pairAction() {
        if (device.isConnected()) {
            if (!pairing) {
                if (paired) {
                    showShortMessage("Already paired");
                } else {
                    pairing = true;
                    devicePairButton.setText("Pairing");
                    if (device.isInPairingMode()) {
                        device.pair(error -> {
                            pairing = false;
                            if (error == null) {
                                paired = true;
                                startEEG();
                                devicePairButton.setText("Paired");
                            } else {
                                devicePairButton.setText("Pair");

                                if (error.getCode() == 3) {
                                    showShortMessage("配对失败");
                                } else {
                                    showShortMessage("Pair failed " + error.getMessage());
                                }
                            }
                        });
                    } else if (device.isInNormalMode()) {
                        device.validatePairInfo(error -> {
                            pairing = false;
                            if (error == null) {
                                paired = true;
                                startEEG();
                                devicePairButton.setText("Paired");
                            } else {
                                devicePairButton.setText("Pair");
                                if (error.getCode() == 4) {
                                    showMessage("检验配对信息失败，去重新配对");
                                } else {
                                    showMessage("Validate pair info failed " + error.getMessage());
                                }
                            }
                        });
                    } else {
                        pairing = false;
                        showShortMessage("Unknown device mode");
                        devicePairButton.setText("Pair");
                    }
                }
            } else {
                showShortMessage("Pairing");
            }
        } else {
            showMessage("Must be connected before pair");
        }
    }
}