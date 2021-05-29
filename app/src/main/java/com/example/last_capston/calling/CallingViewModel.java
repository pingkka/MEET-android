package com.example.last_capston.calling;

import android.util.Log;

import androidx.lifecycle.ViewModel;


import com.example.last_capston.main.MQTTClient;
import com.example.last_capston.main.MQTTSettingData;
import com.example.last_capston.main.MainViewModel;

import org.eclipse.paho.client.mqttv3.MqttException;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallingViewModel extends ViewModel {

    /* MQTT 관련 변수 */
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private MainViewModel viewModel = MainViewModel.getInstance();
    private String ip;
    private String port;
    private String topic;
    private String userName;

    /* Audio 관련 변수 */
    private Boolean recordFlag = false;
    private Boolean playFlag = false;
    private RecordThread recordThread;

    public CallingViewModel() {
        this.ip = settingData.getIp();
        this.port = settingData.getPort();
        this.topic = settingData.getTopic();
        this.userName = settingData.getUserName();


        recordThread = new RecordThread();
        recordThread.start();

    }

    public Boolean clickMic() {
        // recordFlag == false
        if(!recordFlag) {
            recordFlag = true;
            synchronized (recordThread.getAudioRecord()) {
                recordThread.getAudioRecord().notify();
                Log.i("Audio", "Mic On");
            }

            return true;
        }

        // recordFlag == true
        recordFlag = false;
        recordThread.setRecordFlag(recordFlag);
        Log.i("Audio", "Mic Off");

        return false;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
