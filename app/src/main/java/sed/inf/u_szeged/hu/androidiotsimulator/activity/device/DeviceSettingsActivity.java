package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.ParameterAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.JsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.Sensor;
import sed.inf.u_szeged.hu.androidiotsimulator.views.ExpandedListView;

public class DeviceSettingsActivity extends AppCompatActivity {


    public static final String KEY_ORGANIZATION_ID = "ORGANIZATION_ID";
    public static final String KEY_TYPE_ID = "TYPE_ID";
    public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
    public static final String KEY_TOKEN = "TOKEN";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_FREQ = "FREQ";
    public static final String KEY_EDIT_IT = "EDIT_IT";
    public static final String KEY_SENSORS = "SENSORS";
    public static final String KEY_REPLAY_LOCATION = "REPLAYLOCATION";
    public static final int MSG_W_DELETE_PARAMETER = 39;
    private static final int IMPORT_REPLAY_LOCATION_REQ_CODE = 6544;
    private static ParameterAdapter adapter;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_W_DELETE_PARAMETER:
                    System.out.println("msg warning");
                    deleteParamter(msg.arg1);
                    break;

            }

        }
    };
    String editId;
    ExpandedListView listView;
    File myExternalFile;
    Gson gson = new Gson();
    String replayFileLocation;
    Switch aSwitch;

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private void deleteParamter(int position) {
        System.out.println("DELETE " + adapter.getItem(position));
        adapter.remove(adapter.getItem(position));
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMPORT_REPLAY_LOCATION_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();
                    System.out.println("Uri = " + uri.toString());
                    try {
                        // Get the file path from the URI
                        final String path = FileUtils.getPath(this, uri);
                        Toast.makeText(DeviceSettingsActivity.this,
                                "File imported: " + path, Toast.LENGTH_LONG).show();
                        replayFileLocation = path;
                        System.out.print("replayFileLocation= " + replayFileLocation);
                        ((TextView) findViewById(R.id.replay_import_location)).setText(replayFileLocation);
                    } catch (Exception e) {
                        System.out.println("DeviceSettingsActivity" + " File select error" + e);
                    }
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        MobIoTApplication.setActivity(this);

        listView = (ExpandedListView) findViewById(R.id.list);
        SensorDataWrapper sdw;

        aSwitch = (Switch) findViewById(R.id.sw_random);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (on) {
                    //Do something when Switch button is on/checked
                    replayFileLocation = "random";
                    findViewById(R.id.parameter_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.replay_container).setVisibility(View.GONE);

                } else {
                    //Do something when Switch is off/unchecked
                    findViewById(R.id.parameter_container).setVisibility(View.GONE);
                    findViewById(R.id.replay_container).setVisibility(View.VISIBLE);


                }
            }
        });

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            System.out.println("if");
            setData(bundle);
            editId = bundle.getString(KEY_EDIT_IT);
            sdw = SensorDataWrapper.sensorDataFromSerial(bundle.getString(KEY_SENSORS));

        } else {
            System.out.println("else");
            sdw = SensorDataWrapper.sensorDataFromSerial("empty1+1+5*empty2+-1+30");
            System.out.println("DeviceSettingsActivity bundle null");
            initTypeSpinner(0);

        }


        adapter = new ParameterAdapter(sdw.getList(), getApplicationContext());
        listView.setAdapter(adapter);
        getApplicationContext().setTheme(R.style.AppTheme);


        ((Button) findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtras(getData());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        ((Button) findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("result", getData());
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });


        ((Button) findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(new SensorData("empty", "0", "0"));
            }
        });


        ((Button) findViewById(R.id.save_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDeviceToJson();


            }
        });

        ((Button) findViewById(R.id.replay_import_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });


        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            ((Button) findViewById(R.id.save_btn)).setEnabled(false);
        }


        listView.setOnTouchListener(new View.OnTouchListener()

                                    {
                                        // Setting on Touch Listener for handling the touch inside ScrollView
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            // Disallow the touch request for parent scroll on touch of child view
                                            v.getParent().requestDisallowInterceptTouchEvent(true);
                                            return false;
                                        }
                                    }

        );


    }

    private void saveDeviceToJson() {
        try {
            myExternalFile = new File(getExternalFilesDir("SavedDevices"), ((EditText) findViewById(R.id.device_id_et)).getText() + ".json");
            FileOutputStream fos = new FileOutputStream(myExternalFile);

            JsonDevice jsonDevice = new JsonDevice();
            jsonDevice.setOrganizationId((String) ((Spinner) findViewById(R.id.orgid_spinner)).getSelectedItem());
            jsonDevice.setDeviceId(((EditText) findViewById(R.id.device_id_et)).getText().toString());
            jsonDevice.setTypeId(((EditText) findViewById(R.id.type_id_et)).getText().toString());
            jsonDevice.setToken(((EditText) findViewById(R.id.token_et)).getText().toString());
            jsonDevice.setType(String.valueOf(((Spinner) findViewById(R.id.type_spinner)).getSelectedItem()));
            jsonDevice.setFreq(Double.parseDouble(((EditText) findViewById(R.id.freq_value_et)).getText().toString()));

            List<Sensor> list = new ArrayList<>();
            SensorDataWrapper sensorDataWrapper = new SensorDataWrapper(adapter.getResult());
            for (SensorData sd : sensorDataWrapper.getList()) {
                Sensor sensor = new Sensor();
                sensor.setName(sd.getName());
                sensor.setMin(Integer.valueOf(sd.getMinValue()));
                sensor.setMax(Integer.valueOf(sd.getMaxValue()));
                list.add(sensor);
            }

            jsonDevice.setSensors(list);
            jsonDevice.setReplayFileLocation(replayFileLocation); //TODO: // FIXME: 1/27/2017

            String outputString = gson.toJson(jsonDevice);
            fos.write(outputString.getBytes());
            fos.close();
            Toast.makeText(DeviceSettingsActivity.this, "File saved: " + ((EditText) findViewById(R.id.device_id_et)).getText() + ".json", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> loadOrganizationIds() {
        String clouds = MobIoTApplication.loadData(MobIoTApplication.KEY_CLOUDS);
        System.out.println("clouds: " + clouds);
        ArrayList<String> orgIds = new ArrayList<>();

        if (clouds != null && !clouds.equals("")) {
            StringTokenizer st = new StringTokenizer(clouds, "<");
            while (st.hasMoreTokens()) {
                String cloudSerial = st.nextToken();
                orgIds.add(CloudSettingsWrapper.fromSerial(cloudSerial).getOrganizationID());
            }
        } else {
            // orgIds.add("null");
        }

        return orgIds;
    }

    private void initOrgIdSpinner(String defaultOrgId) {
        Spinner spinner = (Spinner) findViewById(R.id.orgid_spinner);
        ArrayList<String> organizationIds = loadOrganizationIds();
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, organizationIds);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        int selectedPos = organizationIds.indexOf(defaultOrgId);
        spinner.setSelection(selectedPos);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Nothing to do here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initTypeSpinner(int selectedPos) {
        Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.device_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);

        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EditText freqET = (EditText) findViewById(R.id.freq_value_et);
//                EditText minET = (EditText)findViewById(R.id.min_value_et);
//                EditText maxET = (EditText)findViewById(R.id.max_value_et);

                switch (position) {
                    case 0:
                        freqET.setEnabled(true);
//                        minET.setEnabled(true);
//                        maxET.setEnabled(true);
                        break;
                    case 1:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
//                        minET.setText("-25");
//                        minET.setEnabled(false);
//                        minET.setText("25");
//                        maxET.setEnabled(false);
                        break;
                    case 2:
                        freqET.setText("10.0");
                        freqET.setEnabled(false);
//                        minET.setText("25");
//                        minET.setEnabled(false);
//                        minET.setText("85");
//                        maxET.setEnabled(false);
                        break;
                    case 3:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
//                        minET.setText("-25");
//                        minET.setEnabled(false);
//                        minET.setText("25");
//                        maxET.setEnabled(false);
                        break;

                }

                String item = (String) parent.getItemAtPosition(position);
                //Toast.makeText(DeviceSettingsActivity.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

    private Bundle getData() {
        Bundle bundle = new Bundle();

        String type_id = ((EditText) findViewById(R.id.type_id_et)).getText().toString();
        String device_id = ((EditText) findViewById(R.id.device_id_et)).getText().toString();
        String token = ((EditText) findViewById(R.id.token_et)).getText().toString();
        String organization_id = (String) ((Spinner) findViewById(R.id.orgid_spinner)).getSelectedItem();
        String type = (String) ((Spinner) findViewById(R.id.type_spinner)).getSelectedItem();
        String freq = ((EditText) findViewById(R.id.freq_value_et)).getText().toString();
        SensorDataWrapper paramResults = new SensorDataWrapper(adapter.getResult());

        System.out.println("getData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\norganization_id:" + organization_id +
                "\ntype:" + type +
                "\nfreq:" + freq +
                "\nsensordata: " + paramResults +
                "\nreplaylocation:" + replayFileLocation);

        bundle.putString(KEY_TYPE_ID, type_id);
        bundle.putString(KEY_DEVICE_ID, device_id);
        bundle.putString(KEY_TOKEN, token);
        bundle.putString(KEY_ORGANIZATION_ID, organization_id);
        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_FREQ, freq);
        bundle.putString(KEY_SENSORS, paramResults.toString());
        bundle.putString(KEY_REPLAY_LOCATION, replayFileLocation);

        if (editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private void setData(Bundle bundle) {
        String type_id = bundle.getString(KEY_TYPE_ID);
        if (type_id != null && !type_id.trim().equals("")) {
            ((EditText) findViewById(R.id.type_id_et)).setText(type_id);
        }

        String device_id = bundle.getString(KEY_DEVICE_ID);
        if (device_id != null && !device_id.trim().equals("")) {
            ((EditText) findViewById(R.id.device_id_et)).setText(device_id);
        }

        String token = bundle.getString(KEY_TOKEN);
        if (token != null && !token.trim().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setText(token);
        }

        initOrgIdSpinner(bundle.getString(KEY_ORGANIZATION_ID));


        String type = bundle.getString(KEY_TYPE);
        String[] deviceTypes = getResources().getStringArray(R.array.device_types);
        for (int i = 0; i < deviceTypes.length; i++) {
            if (type.trim().equals(deviceTypes[i].trim())) {
                initTypeSpinner(i);
                break;
            } else {
                if (i == deviceTypes.length - 1) {
                    initTypeSpinner(0);
                }
            }
        }

        String freq = bundle.getString(KEY_FREQ);
        if (freq != null && !freq.trim().equals("")) {
            ((EditText) findViewById(R.id.freq_value_et)).setText(freq);
        }


        if (!Objects.equals(bundle.getString(KEY_REPLAY_LOCATION), "random")) {
            aSwitch.setChecked(false);
            ((TextView) findViewById(R.id.replay_import_location)).setText(bundle.getString(KEY_REPLAY_LOCATION));

            //    ((TextView) findViewById(R.id.replay_import_location)).setText("ooooooo");
            replayFileLocation = bundle.getString(KEY_REPLAY_LOCATION);
            //findViewById(R.id.replay_import_btn).setVisibility(View.VISIBLE);
            //((LinearLayout) findViewById(R.id.replay_container)).setVisibility(View.VISIBLE);
        } else {
            replayFileLocation = "random";
        }

        System.out.println("setData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\nfreq:" + freq);
    }


    private void showFileChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "choose file");
        try {
            startActivityForResult(intent, IMPORT_REPLAY_LOCATION_REQ_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
            System.out.println("Can't show the file chooser: " + e);
        }
    }


}
