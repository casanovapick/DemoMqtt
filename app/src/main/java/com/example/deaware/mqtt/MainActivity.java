package com.example.deaware.mqtt;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity implements MyMqttService.MyMqttListener {
    ProgressDialog mProgressDialog;
    final String hostaddress = BuildConfig.HOST_ADDRESS;
    String statusTopic = "DW_HACKATHON/+/STATUS";
    final String clientID = "MqttTest";
    final String nodeID = "001";
    MyMqttService mqttService;
    TextView txtCurrentTemp,txtTargetTemp;
    int targetTemp=25;
    Switch mSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMqtt();
    }

    private void initView() {
        mProgressDialog = new ProgressDialog(this);
        txtCurrentTemp = (TextView) findViewById(R.id.txt_temp_current);
        txtTargetTemp = (TextView) findViewById(R.id.txt_temp_target);
        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSwitch.isChecked()){
                    mqttService.setStatus(nodeID,1);
                }else {
                    mqttService.setStatus(nodeID,0);
                }
            }
        });
    }

    private void initMqtt() {

        initView();
        try {
            mqttService = new MyMqttService(hostaddress, clientID);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mqttService.connect();
        mqttService.subscript(statusTopic);
        mqttService.setMqttListener(this);
        mqttService.MqttStartReceive(this);
        mqttService.subscriptNode(nodeID);
        mqttService.setTemp(nodeID, targetTemp, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtTargetTemp.setText("" + targetTemp);
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });
    }

    @Override
    public void onReceive(Message message) {
        try {
            String payload = new String(message.getPayload(), "UTF-8");
            String topic =message.getTopic();
            Log.d("MqttMessage", payload);
            if (topic.contains("TMP")) {
                final JSONObject jsonObject = new JSONObject(payload);
                try {
                    int status = jsonObject.getInt("S");
                    txtCurrentTemp.setText(jsonObject.getString("T"));
                    if(status==1){
                        mSwitch.setChecked(true);
                    }else {
                        mSwitch.setChecked(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (topic.contains("STATUS")) {
                final JSONObject jsonObject = new JSONObject(payload);
                try {
                    int status = jsonObject.getInt("S");
                    if(status==1){
                        mSwitch.setChecked(true);
                    }else {
                        mSwitch.setChecked(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addButtonClick(View view) {
        if(targetTemp+1>30)
            return;
        mqttService.setTemp(nodeID, targetTemp+1, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                targetTemp++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtTargetTemp.setText("" + targetTemp);
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });
    }

    public void minusButtonClick(View view) {
        if(targetTemp-1<18)
            return;
        mqttService.setTemp(nodeID, targetTemp-1, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                targetTemp--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtTargetTemp.setText("" + targetTemp);
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });
    }
}
