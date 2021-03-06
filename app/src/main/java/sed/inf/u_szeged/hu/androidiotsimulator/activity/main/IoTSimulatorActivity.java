package sed.inf.u_szeged.hu.androidiotsimulator.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesActivity;

public class IoTSimulatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_simulator);

        MobIoTApplication.setActivity(this);

        findViewById(R.id.goto_cloud_settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IoTSimulatorActivity.this, CloudActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.goto_devices_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IoTSimulatorActivity.this, DevicesActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.informations_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IoTSimulatorActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

}
