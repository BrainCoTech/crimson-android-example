package tech.brainco.crimsonsdk.example;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tech.brainco.crimsonsdk.CrimsonDevice;
import tech.brainco.crimsonsdk.CrimsonDeviceScanListener;
import tech.brainco.crimsonsdk.CrimsonError;
import tech.brainco.crimsonsdk.CrimsonPermissions;
import tech.brainco.crimsonsdk.CrimsonSDK;

public class ScanActivity extends BaseActivity {
    private String TAG = "ScanActivity";
    private List<CrimsonDevice> devices = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        RecyclerView deviceListView = findViewById(R.id.device_list);
        deviceListView.setHasFixedSize(true);
        deviceListAdapter = new DeviceListAdapter(this);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        Log.i(TAG, "C-SdkVersion: " + CrimsonSDK.getSdkVersion());
        CrimsonSDK.setDevTaskSession(CrimsonSDK.DevTaskSession.FOCUS);
        CrimsonSDK.setLogLevel(CrimsonSDK.LogLevel.INFO);
        /*
        CrimsonSDK.setLogCallback(new OnLogCallback()  {
            public void invoke(String msg) {
                //  TODO: saveLogMessageToFile(msg);
            }
        });
        */
        CrimsonSDK.registerBLEStateReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CrimsonSDK.unregisterBLEStateReceiver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
    }

    private void scanDevices() {
        if (CrimsonSDK.isScanning) {
            dismissLoadingDialog();
            CrimsonSDK.stopScan();
            return;
        }

        if (!CrimsonPermissions.checkBluetoothFeature(this)) {
            showMessage("BLE not supported");
            return;
        }

        BluetoothAdapter adapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            this.startActivity(enableBtIntent);
            return;
        }

        // check Permissions
        if (!CrimsonPermissions.checkPermissions(this)) {
            CrimsonPermissions.requestPermissions(this);
            return;
        }

        showLoadingDialog();
        CrimsonSDK.startScan(new CrimsonDeviceScanListener() {
            @Override
            public void onBluetoothAdapterStateChange(int state) {
                Log.i(TAG, "BluetoothAdapter state=" + state);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        // restart scan
                        scanDevices();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        CrimsonSDK.stopScan();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFoundDevices(List<CrimsonDevice> results) {
                if (!results.isEmpty()) dismissLoadingDialog();
                devices = results;
                deviceListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(CrimsonError error) {
                dismissLoadingDialog();
                showMessage(error.getMessage());
            }
        }, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_device) {
            scanDevices();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {
        private final ScanActivity context;

        static class DeviceViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView nameTextView;
            TextView idTextView;
            TextView inPairingTextView;
            TextView rssiTextView;

            DeviceViewHolder(View view) {
                super(view);
                nameTextView = view.findViewById(R.id.item_device_info_name);
                idTextView = view.findViewById(R.id.item_device_info_id);
                inPairingTextView = view.findViewById(R.id.item_inPairingMode);
                rssiTextView = view.findViewById(R.id.item_rssi);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        DeviceListAdapter(ScanActivity context) {
            this.context = context;
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_info, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            final CrimsonDevice device = context.devices.get(position);
            holder.nameTextView.setText(device.getName());
            holder.idTextView.setText(device.getId());
            holder.rssiTextView.setText(String.valueOf(device.getRssi()));
            holder.inPairingTextView.setText(device.isInPairingMode() ? "配对模式" : "普通模式");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MenuActivity.class);
                //intent.putExtra("deviceId", device.getId());

                CrimsonSDK.stopScan();
                GlobalDevice.device = device;
                setSelectedCrimsonDevice(device);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return context.devices.size();
        }
    }
}

class GlobalDevice {
    static CrimsonDevice device = null;
}