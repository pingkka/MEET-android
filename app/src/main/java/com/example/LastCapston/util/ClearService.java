package com.example.LastCapston.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.LastCapston.calling.CallingViewModel;
import com.example.LastCapston.calling.PlayThread;
import com.example.LastCapston.main.CloudStorage;
import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.main.MainViewModel;

import org.eclipse.paho.client.mqttv3.MqttException;

public class ClearService extends Service {
    private MQTTClient client = MQTTClient.getInstance();
    private MainViewModel viewModel = MainViewModel.getInstance();
    private CallingViewModel callingViewModel;
    private CloudStorage storage;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearService", "END");
        //Code here
        //-----------
        client.getConnectOptions().setAutomaticReconnect(false);
        /* Audio 관련 처리 */
        /* -------------------------- 추가 ------------------------- */
        /* EmotionThread interrupt */
        if (callingViewModel.getEmotionFlag()) {
            callingViewModel.getEmotionThread().setEmotionFlag(false);
        }
        callingViewModel.getEmotionThread().interrupt();

        /* SttThread interrupt */
        if(callingViewModel.getSttFlag()) {
            callingViewModel.getSttThread().setSttFlag(false);
        }
        callingViewModel.getSttThread().interrupt();
        /* -------------------------------------------------------- */

        /* Audio 관련 처리 */
        /* RecordThread interrupt */
        if(callingViewModel.getRecordFlag()) {
            callingViewModel.getRecordThread().setRecordFlag(false);
        }

        callingViewModel.getRecordThread().stopRecording();
        callingViewModel.getRecordThread().interrupt();



        /* topic_audio unsubscribe */
        if(client.getClient().isConnected()) {
            try {
                client.getClient().unsubscribe(client.getTopic_audio());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        /* PlayThreadList의 모든 PlayThread interrupt 및 PlayThreadList 초기화*/
        for(PlayThread playThread : client.getPlayThreadList()) {
            if(callingViewModel.getPlayFlag()) {
                playThread.setPlayFlag(false);
            }

            playThread.stopPlaying();
            synchronized (playThread.getAudioQueue()) {
                playThread.getAudioQueue().clear();
            }
            playThread.interrupt();
        }

        client.getPlayThreadList().clear();

        //실제 연결 끊음


        /* firebase 참여자목록 삭제, mqtt 삭제 초기화*/
        String roomID = client.settingData.getTopic();
        String user = client.settingData.getUserName();
        System.out.println(viewModel.getUserList().toString());
        //storage.logout(roomID, user);
        //나갔다고 알리기
        client.publish(roomID+"/logout", user);
        //-----------
        stopSelf();
    }
}