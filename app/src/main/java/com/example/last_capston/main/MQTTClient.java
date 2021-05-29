package com.example.last_capston.main;


import android.util.Log;

import com.example.last_capston.calling.CallingViewModel;
import com.example.last_capston.calling.PlayThread;

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
    private String topic_notifyUSer;
    private String topic_logout;
    private String userName;

    private static CallingViewModel callingViewModel;


    //정리해야 할 변수
    public ArrayList<String> participantsList = new ArrayList<>(100);

    private List<PlayThread> playThreadList = new ArrayList<>(100);



    private MainViewModel mainViewModel;
    private String recvMsg;
    private String beforeLoginUser = "";
    private String beforeLogoutUser = "";


    public static MQTTClient getInstance() {
        if(instance == null) {
            instance = new MQTTClient();
        }
        return instance;
    }


    public void init(String topic, String userName, String ip, String port, CallingViewModel callingViewModel, MainViewModel mainViewModel) {
        connect(ip, port, topic, userName);

        this.topic = topic;
        this.userName = userName;

        participantsList.clear();
        playThreadList.clear();

        topic_audio = topic + "/audio";
        topic_login = topic + "/login";
        topic_notifyUSer = topic + "/login/notifyUser";
        topic_logout = topic + "/logout";
        this.callingViewModel = callingViewModel;
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

    public void subscribeAll() {
        subscribe(topic);
        subscribe(topic_audio);
        subscribe(topic_login);
        subscribe(topic_notifyUSer);
        subscribe(topic_logout);
    }

    public void publish(String topic, String msg) {

        try {
            client.publish(topic, new MqttMessage(msg.getBytes()));

            Log.i("MQTT", "PUB " + topic + recvMsg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, new MqttMessage(payload));
            Log.i("MQTT", "PUB " + topic);
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
        Log.d("Message arrived : ", "topic:" + topic + recvMsg);
        /* /roomID/login */
        if (topic.equals(topic_login)) {
            String name = new String(message.getPayload(), "UTF-8");
            Log.i("MQTT", "userList add " + name);
            if (!beforeLoginUser.equals(name)) {
                setParticipantsList(recvMsg);
                mainViewModel.setLoginUser(recvMsg);
                beforeLoginUser = recvMsg;
                publish(topic_notifyUSer, userName);

                PlayThread playThread = new PlayThread();
                playThread.setUserName(name);

//                if(callingViewModel.getPlayFlag())
//                    playThread.setPlayFlag(true);
//                playThread.start();
//
//                playThreadList.add(playThread);
//                Log.i("MQTT", "playThreadList add " + name);
            }
        }


        /* /roomID/notifyUser */
        if (topic.equals(topic_notifyUSer)) {
            String name = new String(message.getPayload(), "UTF-8");
            setParticipantsList(name);
            Log.i("MQTT", "userList add " + name);

            PlayThread playThread = new PlayThread();
            playThread.setUserName(name);
//            if(callingViewModel.getPlayFlag())
//                playThread.setPlayFlag(true);
//            playThread.start();
//
//            playThreadList.add(playThread);
//            Log.i("MQTT", "playThreadList add " + name); // 나오지 않음
        }

        /* /roomID/audio */
        if(topic.equals(topic_audio)) {
            byte[] messageData = message.getPayload();

            byte[] nameData = Arrays.copyOfRange(messageData, 0, messageData.length - 80000);
            String sender = new String(nameData);

            if (sender.equals(userName)) return;
            byte[] audioData = Arrays.copyOfRange(messageData, nameData.length, messageData.length);
//            for (PlayThread playThread: playThreadList) {
//                if (playThread.getUserName().equals(sender)) {
//                    if (playThread.getAudioQueue().size() >= 5) {
//                        playThread.getAudioQueue().clear();
//                    }
//                    playThread.getAudioQueue().add(audioData);
//                    break;
//                }
//            }
        }

        /* roomID/logout */
        if(topic.equals(topic_logout)){
            if(!beforeLogoutUser.equals(recvMsg)) {
                mainViewModel.setLogoutUser(recvMsg);
                beforeLogoutUser = recvMsg;
            }
            //참여인원들에게 나갔다고 알리기
            deleteParticipant(recvMsg);
        }

        if(topic.equals(topic_logout)) {
            String name = new String(message.getPayload(), "UTF-8");
            if(!beforeLogoutUser.equals(name)) {
                mainViewModel.setLogoutUser(name);
                beforeLogoutUser = name;
                Log.i("MQTT", "userList remove " + name);
            }
            //참여인원들에게 나갔다고 알리기
            deleteParticipant(recvMsg);


            if(!userName.equals(name)  ) {

//                for(int i=0; i<participantsList.size(); i++) {
//                    if(participantsList.get(i).equals(name)) {
//                        participantsList.remove(i);
//                        mainViewModel.deleteUsersList(name);
//                        Log.i("MQTT", "userList remove " + name);
//                        break;
//                    }
//                }

//                for(int i=0; i<playThreadList.size(); i++) {
//                    if(playThreadList.get(i).getUserName().equals(name)) {
//                        playThreadList.get(i).setPlayFlag(false);
//                        playThreadList.get(i).getAudioQueue().clear();
//                        playThreadList.get(i).stopPlaying();
//                        playThreadList.get(i).interrupt();
//                        Log.i("MQTT", "before playThreadList remove " + name + "size(" + playThreadList.size() + ")");
//                        playThreadList.remove(i);
//                        Log.i("MQTT", "after playThreadList remove " + name + "size(" + playThreadList.size() + ")");
//                    }
//                }
            }
        }

        /* Other Topic */

//        if (topic.equals(topic_login)) {
//            if(!beforeLoginUser.equals(recvMsg)) {
//                setParticipantsList(recvMsg);
//                mainViewModel.setLoginUser(recvMsg);
//                beforeLoginUser = recvMsg;
//                publish(topic + "/notifyUser", userName);
//            }
//        }else if (topic.equals(topic_notifyUSer)) {
//            setParticipantsList(recvMsg);
//        }else if(topic.equals(topic_logout)){
//            if(!beforeLogoutUser.equals(recvMsg)) {
//                mainViewModel.setLogoutUser(recvMsg);
//                beforeLogoutUser = recvMsg;
//            }
//            //참여인원들에게 나갔다고 알리기
//            deleteParticipant(recvMsg);
//        }
    }


    public void setParticipantsList( String user) throws Exception {
        if(participantsList.contains(user)){
        }
        else {
            participantsList.add(user);
            mainViewModel.setUserList(user);
        }
    }


    public void deleteParticipant(String user) throws Exception {
        participantsList.remove(user);
        mainViewModel.deleteUsersList(user);
    }





    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("Message with ", token + " delivered.");
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
