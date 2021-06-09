package com.example.LastCapston.calling;

import android.util.Log;

import com.example.LastCapston.data.Emotion;
import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.util.RetrofitService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Getter
@Setter
public class EmotionThread extends Thread {

    private String BASE_URL_EMOTION_API = "http://222.232.35.162:12315/api/";

    private RetrofitService retrofitService;
    private Boolean emotionFlag = false;

    private String audioFilePath;
    private String textFilePath;
    private FileOutputStream fos;
    private FileInputStream fis;
    private File audioFile;
    private File textFile;

    private String sttResultMsg;

    private MQTTClient mqttClient = MQTTClient.getInstance();

    @Override
    public void run() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_EMOTION_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        retrofitService = retrofit.create(RetrofitService.class);
        Log.i("Emotion", "retrofitService create");


        try {
            while (true) {
                // emotionFlag == false
                if(!emotionFlag) {
                    synchronized (textFilePath) {
                        textFilePath.wait();
                    }
                    emotionFlag = true;
                }

                // emotionFlag == true
                Log.i("Emotion", sttResultMsg);
                fos = new FileOutputStream(textFilePath);
                fos.write(sttResultMsg.getBytes());
                fis = new FileInputStream(textFilePath);

                requestEmotion();

                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }

                emotionFlag = false;
                Log.i("Emotion", "Emotion Off");
            }
        }  catch (InterruptedException | IOException e) {
            Log.i("Emotion", "Emotion Thread Dead...");
            if(textFile != null) {
                if (textFile.exists()) {
                    textFile.delete();
                }
            }

        }
    }

    private void requestEmotion() {
        textFile = new File(textFilePath);
        audioFile = new File(audioFilePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), textFile);
        MultipartBody.Part textBody = MultipartBody.Part.createFormData("text_file", textFile.getName(), requestBody);

        requestBody = RequestBody.create(MediaType.parse("audio/pcm"), audioFile);
        MultipartBody.Part audioBody = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), requestBody);

        Call<Emotion> getCall = retrofitService.get_post_text_and_voice(textBody, audioBody);
        getCall.enqueue(new Callback<Emotion>() {
            @Override
            public void onResponse(Call<Emotion> call, Response<Emotion> response) {
                if (response.isSuccessful()) {
                    Emotion emotion = response.body();
                    Log.i("Emotion", "emotion = " + emotion.getEmotion());
                    publishTextDataAndEmotionData(sttResultMsg, emotion.getEmotion());
                } else {
                    Log.i("Emotion", "Status Code = " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Emotion> call, Throwable t) {
                Log.i("Emotion", "Fail msg = " + t.getMessage());
            }
        });
    }

    private void publishEmotionData(String emotion) {
        mqttClient.publish(mqttClient.getTopic_emotion(), emotion);
    }
    private void publishTextDataAndEmotionData(String text, String emotion) {
        mqttClient.publish(mqttClient.getTopic_text(), mqttClient.getUserName() + "&" +text+ "&" +emotion);
    }
}