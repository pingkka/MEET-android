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
        String roomID = client.settingData.getTopic();
        String user = client.settingData.getUserName();
        client.publish(roomID+"/logout", user);
    }

}