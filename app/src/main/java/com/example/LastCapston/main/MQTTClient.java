package com.example.LastCapston.main;


import android.content.Context;
import android.util.Log;

import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.calling.CallingViewModel;
import com.example.LastCapston.calling.PlayThread;
import com.example.LastCapston.data.SendText;
import com.example.LastCapston.data.UserItem;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MQTTClient implements MqttCallbackExtended {

    private static MQTTClient instance;

    /* MQTT 관련 변수 */
    private MqttClient client;
    private MqttClient client2;
    private String BROKER;
    private int qos = 0;

    public MQTTSettingData settingData = MQTTSettingData.getInstance();
    private MqttConnectOptions connectOptions;

    /* TOPIC */
    private String topic;
    private String topic_audio;
    private String topic_login;
    private String topic_login_audio;
    private String topic_notifyUSer;
    private String topic_logout;
    private String topic_text;
    private String topic_emotion;

    private String userName;

    public CallingViewModel callingViewModel;
    private MainViewModel mainViewModel;

    //정리해야 할 변수
    public ArrayList<String> participantsList = new ArrayList<>(100);
    private List<PlayThread> playThreadList = new ArrayList<>(100);




    private String recvMsg;
    private String beforeLoginUser = "";
    private String beforeLogoutUser = "";
    private Context context;

    public static MQTTClient getInstance() {
        if(instance == null) {
            instance = new MQTTClient();
        }
        return instance;
    }


    public void init(String topic, String userName, String ip, String port, MainViewModel mainViewModel) {
        connect(ip, port, topic, userName);

        this.topic = topic;
        this.userName = userName;

        participantsList.clear();
        playThreadList.clear();

        topic_audio = topic + "/audio";
        topic_login = topic + "/login";
        topic_login_audio = topic + "/loginAudio";
        topic_notifyUSer = topic + "/login/notifyUser";
        topic_logout = topic + "/logout";
        topic_text = topic + "/text";
        topic_emotion = topic + "/emotion";

        this.mainViewModel = mainViewModel;
    }

    public void connect(String ip, String port, String topic, String userName) {
        try {
            BROKER = "tcp://" + ip + ":" + port;

            client = new MqttClient(BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            client2 = new MqttClient(BROKER, MqttClient.generateClientId(), new MemoryPersistence());

            client.setCallback(this);

            connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(userName);
            connectOptions.setMaxInflight(5000);
            connectOptions.setKeepAliveInterval(1000);
            connectOptions.setCleanSession(true);

            connectOptions.setAutomaticReconnect(true);

            client.connect(connectOptions);
            client2.connect(connectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
            client.close();
            client2.disconnect();
            client2.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic, this.qos);
            Log.i("MQTT", "SUB " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /* 모든 topic subscribe */
    public void subscribeAll() {
        subscribe(topic);
        subscribe(topic_audio);
        subscribe(topic_login_audio);
        subscribe(topic_login);
        subscribe(topic_notifyUSer);
        subscribe(topic_logout);
        subscribe(topic_text);
        subscribe(topic_emotion);
    }

    /* 문자 data publish */
    public void publish(String topic, String msg) {
        try {
            client.publish(topic, new MqttMessage(msg.getBytes()));
            Log.i("MQTT", "PUB :" + topic + msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /* 오디오 data publish */
    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, new MqttMessage(payload));
            Log.i("MQTT", "PUB :" + topic);
        } catch (MqttException e) {
            e.printStackTrace();
            if(callingViewModel.getRecordThread().isAlive()) {
                callingViewModel.getRecordThread().interrupt();
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i("Lost Connection.", String.valueOf(cause.getCause()));
        cause.printStackTrace();
    }


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        recvMsg = new String(message.getPayload(), "UTF-8");
        Log.i("MQTT", "messageArrived " + topic);
        /* /roomID/login */
        if (topic.equals(topic_login)) {
            String name = new String(message.getPayload(), "UTF-8");

            //가끔 mqtt를 여러번 인식하는 오류가 있어서 두번 인식하지 못하게 하는 코드
            if (!beforeLoginUser.equals(name)) {
                UserItem newUser = new UserItem(name);
                setParticipantsList(newUser);

                //로그인 topic을 받으면 저장
                mainViewModel.setLoginUser(name);
                beforeLoginUser = name;
            }
        }

        if(topic.equals(topic_login_audio)) {
            String name = new String(message.getPayload(), "UTF-8");

            PlayThread playThread = new PlayThread();
            playThread.setUserName(name);
            if(callingViewModel.getPlayFlag())
                playThread.setPlayFlag(true);
            playThread.start();

            playThreadList.add(playThread);
            Log.i("MQTT", "playThreadList add " + name);
            publish(topic_notifyUSer, userName);
        }


        /* /roomID/notifyUser */
        if (topic.equals(topic_notifyUSer)) {
            String name = new String(message.getPayload(), "UTF-8");

            UserItem newUser = new UserItem(name);
            setParticipantsList(newUser);

            PlayThread playThread = new PlayThread();
            playThread.setUserName(name);
            if(callingViewModel.getPlayFlag())
                playThread.setPlayFlag(true);
            playThread.start();

            playThreadList.add(playThread);
            Log.i("MQTT", "playThreadList add " + name); // 나오지 않음
        }

        /* /roomID/audio */
        if(topic.equals(topic_audio)) {
            byte[] messageData = message.getPayload();

            byte[] nameData = Arrays.copyOfRange(messageData, 0, messageData.length - 80000);
            String sender = new String(nameData);

            if (sender.equals(userName)) return;
            byte[] audioData = Arrays.copyOfRange(messageData, nameData.length, messageData.length);
            for (PlayThread playThread: playThreadList) {
                if (playThread.getUserName().equals(sender)) {
                    if (playThread.getAudioQueue().size() >= 5) {
                        playThread.getAudioQueue().clear();
                    }
                    playThread.getAudioQueue().add(audioData);
                    break;
                }
            }
        }

        /* roomID/text */
        if(topic.equals(topic_text)) {
            String text = new String(message.getPayload(), "UTF-8");
            int idx = text.indexOf("&");
            String sendTextUser = text.substring(0, idx);
            String sendtext = text.substring(idx + 1);
            Log.i("MQTT", "sendTextUser = " + sendTextUser);
            Log.i("MQTT", "sendtext = " + sendtext);
            SendText sendText = new SendText(sendTextUser, sendtext);
            mainViewModel.setCurrentText(sendText);
            Log.i("MQTT", "text = " + text);

        }

        /* roomID/emotion */
        if(topic.equals(topic_emotion)) {
            String emotion = new String(message.getPayload(), "UTF-8");
            Log.i("MQTT", "emotion = " + emotion);
        }

        /* roomID/logout */
        if(topic.equals(topic_logout)){
            String name = new String(message.getPayload(), "UTF-8");


            for(int i=0; i<participantsList.size(); i++) {
                if(participantsList.get(i).equals(name)) {
                    if(!beforeLogoutUser.equals(name)) {
                        mainViewModel.setLogoutUser(name);//화면에 퇴장 알리기
                        beforeLogoutUser = name;
                        Log.i("MQTT", "userList remove " + name);
                    }
                    //참여인원들에게 나갔다고 알리기
                    UserItem newUser = new UserItem(name);
                    deleteParticipant(newUser);
                    break;
                }
            }

            for(int i=0; i<playThreadList.size(); i++) {
                if(playThreadList.get(i).getUserName().equals(name)) {
                    playThreadList.get(i).setPlayFlag(false);
                    playThreadList.get(i).getAudioQueue().clear();
                    playThreadList.get(i).stopPlaying();
                    playThreadList.get(i).interrupt();
                    Log.i("MQTT", "before playThreadList remove " + name + "size(" + playThreadList.size() + ")");
                    playThreadList.remove(i);
                    Log.i("MQTT", "after playThreadList remove " + name + "size(" + playThreadList.size() + ")");
                }
            }
        }







        /* Other Topic */
    }


    //participantsList
    public void setParticipantsList( UserItem user) throws Exception {

        if(!participantsList.contains(user.userName)){

            mainViewModel.addUserItem(user);
            participantsList.add(user.userName);
            Log.i("MQTT", "userList add " + user);


        }
    }


    public void deleteParticipant(UserItem user) throws Exception {
        participantsList.remove(user.userName);
        mainViewModel.deleteUsersItem(user.userName);
    }

    public void setContext(Context context) {
        this.context = context;
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private boolean containsUserList(String name) {
        for (String user : participantsList) {
            if (user.equals(name)) {
                return true;
            }
        }

        return false;
    }
}
