package com.example.LastCapston.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.LastCapston.calling.CallingViewModel;
import com.example.LastCapston.calling.PlayThread;
import com.example.LastCapston.main.CloudStorage;
import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.main.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

import static android.content.ContentValues.TAG;

public class ClearService extends Service {
    private MQTTClient client = MQTTClient.getInstance();
    private MainViewModel viewModel = MainViewModel.getInstance();
    private CallingViewModel callingViewModel ;
    private CloudStorage storage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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


    @SneakyThrows
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearService", "END");
        //Code here

//        //-----------
//        client.getConnectOptions().setAutomaticReconnect(false);
//        /* Audio 관련 처리 */
//        /* -------------------------- 추가 ------------------------- */
//        /* EmotionThread interrupt */
//        if (callingViewModel.getEmotionFlag()) {
//            callingViewModel.getEmotionThread().setEmotionFlag(false);
//        }
//        callingViewModel.getEmotionThread().interrupt();
//
//        /* SttThread interrupt */
//        if(callingViewModel.getSttFlag()) {
//            callingViewModel.getSttThread().setSttFlag(false);
//        }
//        callingViewModel.getSttThread().interrupt();
//        /* -------------------------------------------------------- */
//
//        /* Audio 관련 처리 */
//        /* RecordThread interrupt */
//        if(callingViewModel.getRecordFlag()) {
//            callingViewModel.getRecordThread().setRecordFlag(false);
//        }
//
//        callingViewModel.getRecordThread().stopRecording();
//        callingViewModel.getRecordThread().interrupt();



        /* topic_audio unsubscribe */
//        if(client.getClient().isConnected()) {
//            try {
//                client.getClient().unsubscribe(client.getTopic_audio());
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        }

//        /* PlayThreadList의 모든 PlayThread interrupt 및 PlayThreadList 초기화*/
//        for(PlayThread playThread : client.getPlayThreadList()) {
//            if(callingViewModel.getPlayFlag()) {
//                playThread.setPlayFlag(false);
//            }
//
//            playThread.stopPlaying();
//            synchronized (playThread.getAudioQueue()) {
//                playThread.getAudioQueue().clear();
//            }
//            playThread.interrupt();
//        }

//        client.getPlayThreadList().clear();

//        //실제 연결 끊음
//        String roomID = client.settingData.getTopic();
//        String user = client.settingData.getUserName();
//
//        //mqtt 나갔다고 알리기
//        client.publish(roomID+"/logout", user);
//        System.out.println(user);
//        //firebase 나가기
//        logout(roomID, user);
//
//        /* firebase 참여자목록 삭제, mqtt 삭제 초기화*/
//
//        //-----------
//        stopSelf();
    }

    /* 로그아웃시 firebase에서 삭제해주는 함수 */
//    public void logout(String roomID, String user) throws Exception {
//        ArrayList<String> userList = viewModel.getUserList();
//        //participantsList = mqttAndroidClient.getParticipantsList();
//        DocumentReference docRef = db.collection("rooms").document(roomID);
//        if (userList.size() == 1) {
//            docRef
//                    .delete()
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.w(TAG, "사람 없음");
//
//                            //view모델 초기화
//                            viewModel.userInit();
//
//                            /* MQTTClient 연결 해제 */
//                            client.disconnect();
//                            client.getParticipantsList().clear();
//                            client.getConnectOptions().setAutomaticReconnect(false);
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(TAG, "로그아웃 방 삭제 실패", e);
//                        }
//
//                    });
//        } else {
//            // Remove the 'capital' field from the document
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("participants", FieldValue.arrayRemove(user));
//            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
//
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    Log.w(TAG, "아직 방에 사람 있음");
//
//
//                    //view모델 초기화
//                    viewModel.userInit();
//
//                    /* MQTTClient 연결 해제 */
//                    client.getParticipantsList().clear();
//                    client.disconnect();
//                    client.getConnectOptions().setAutomaticReconnect(false);
//
//
//                }
//            });
//        }
//
//    }
}