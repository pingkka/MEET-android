package com.example.LastCapston.calling;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;


import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.main.MainViewModel;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallingViewModel extends AndroidViewModel {

   // private static CallingViewModel instance;
    /* MQTT 관련 변수 */
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private String ip;
    private String port;
    private String topic;
    private String userName;

    private String baseFilePath;

    /* Audio 관련 변수 */
    private Boolean recordFlag = false;
    private Boolean playFlag = true;
    private RecordThread recordThread;

    /* 카카오 음성 REST API */
    private Boolean sttFlag = false;
    private SttThread sttThread;

    /* 감정분석 API */
    private Boolean emotionFlag = false;
    private EmotionThread emotionThread;

   // /* 싱글톤으로 객체 생성 */
    //public static CallingViewModel getInstance() {
        //return instance;
    //}
    public CallingViewModel(Application application) {
        super(application);
        this.ip = settingData.getIp();
        this.port = settingData.getPort();
        this.topic = settingData.getTopic();
        this.userName = settingData.getUserName();

        client.setCallingViewModel(this);

        /* --------------- 수정 --------------- */
        Context context = getApplication().getApplicationContext();
        baseFilePath = context.getExternalFilesDir(null).toString();
        String textFilePath = baseFilePath + "/text.txt";
        String audioFilePath = baseFilePath + "/record.pcm";

        emotionThread = new EmotionThread();
        emotionThread.setAudioFilePath(audioFilePath);
        emotionThread.setTextFilePath(textFilePath);
        emotionThread.start();

        sttThread = new SttThread();
        sttThread.setAudioFilePath(audioFilePath);
        sttThread.setEmotionThread(emotionThread);
        sttThread.start();

        recordThread = new RecordThread();
        recordThread.setAudioFilePath(audioFilePath);
        recordThread.setSttThread(sttThread);
        recordThread.start();
        /*--------------------------------------*/
        client.publish(client.getTopic_login_audio(), userName);
    }

//    public Boolean clickMic() {
//        // recordFlag == false
//        if(!recordFlag) {
//            recordFlag = true;
//            synchronized (recordThread.getAudioRecord()) {
//                recordThread.getAudioRecord().notify();
//                Log.i("Audio", "Mic On");
//            }
//
//            return true;
//        }
//
//        // recordFlag == true
//        recordFlag = false;
//        recordThread.setRecordFlag(recordFlag);
//        Log.i("Audio", "Mic Off");
//
//        return false;
//    }


    public void touchMic() {
        if(!recordFlag) { // recordFlag == false
            recordFlag = true;
            synchronized (recordThread.getAudioRecord()) {
                recordThread.getAudioRecord().notify();
                Log.i("Audio", "Mic On");
            }
        }

        else { // recordFlag == true
            recordFlag = false;
            recordThread.setRecordFlag(recordFlag);
            Log.i("Audio", "Mic Off");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
