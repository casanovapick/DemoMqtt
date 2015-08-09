package com.example.deaware.mqtt;

import android.app.Activity;
import android.util.Log;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 * Created by KIK on 8/9/15 AD.
 */
public class MyMqttService {

    public interface MyMqttListener{
        void onReceive(Message message);
    }
    private MyMqttListener myMqttListener;

    private String hostAddress;
    private String clientID;
    private MQTT mMqtt;
    private FutureConnection connection;
    private Thread receiveThread;

    public MyMqttService(String hostAddress, String clientID) throws URISyntaxException {
        this.hostAddress = hostAddress;
        this.clientID = clientID;
        mMqtt = new MQTT();
        mMqtt.setClientId(clientID);
        mMqtt.setHost(hostAddress);
        connection = mMqtt.futureConnection();
    }

    public void connect() {
        connection.connect().then(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.d("connect","onSuccess");
            }

            @Override
            public void onFailure(Throwable value) {
                Log.d("connect","onFailure");
            }
        });
    }

    public void MqttStartReceive(final Activity activity) {
        if(receiveThread==null){
            receiveThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!receiveThread.isInterrupted()) {
                            final Message message = connection.receive().await();
                            message.ack();
                            if(myMqttListener!=null){
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        myMqttListener.onReceive(message);
                                    }
                                });

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            );
        }
        receiveThread.start();
    }

    public void MqttStopReceive(){
        if(receiveThread!=null){
            receiveThread.interrupt();
            receiveThread = null;
        }
    }

    public void setMqttListener(MyMqttListener myMqttListener) {
        this.myMqttListener = myMqttListener;
    }

    public void subscript(String topic){
        connection.subscribe(new Topic[]{new Topic(topic, QoS.AT_MOST_ONCE)}).then(new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                try {
                    Log.d("subscriptOnSuccess",new String(value,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });
    }

    public void subscriptNode(String nodeID){
        connection.subscribe(new Topic[]{new Topic("DW_HACKATHON/"+nodeID+"/TMP", QoS.AT_MOST_ONCE)}).then(new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                try {
                    Log.d("subscriptOnSuccess",new String(value,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });
    }

    public void setTemp(String nodeID,int temp,Callback<Void> callback){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("T",""+temp);
            jsonObject.put("S",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            connection.publish("DW_HACKATHON/"+nodeID+"/SET",jsonObject.toString().getBytes("UTF-8"),QoS.AT_MOST_ONCE,false).then(callback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setStatus(String nodeID,int status){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("S",status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            connection.publish("DW_HACKATHON/"+nodeID+"/SET",jsonObject.toString().getBytes("UTF-8"),QoS.AT_MOST_ONCE,false);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
