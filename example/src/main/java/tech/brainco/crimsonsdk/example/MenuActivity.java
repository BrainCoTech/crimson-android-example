package tech.brainco.crimsonsdk.example;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void deviceFeaturesClick(View v) {
        Intent intent = new Intent(this, DeviceActivity.class);
        startActivity(intent);
    }

    public void otaFeaturesClick(View v) {
    }

    public void deviceConfigClick(View v) {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

}