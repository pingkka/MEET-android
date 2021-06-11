package com.example.LastCapston.calling;

import android.util.Log;

import com.example.LastCapston.main.MQTTClient;
import com.example.LastCapston.util.RetrofitService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Getter
@Setter
public class SttThread extends Thread {

    /* 카카오 음성 REST API Setting 관련 변수 */
    private String BASE_URL_KAKAO_API = "https://kakaoi-newtone-openapi.kakao.com/";
    private String REST_API_KEY = "07c9f7d6c0285ce1b88bcb042e0cfe60";
    private String transferEncoding = "chunked";
    private String contentType = "application/octet-stream";
    private String authorization = "KakaoAK " + REST_API_KEY;

    private RetrofitService retrofitService;
    private boolean sttFlag = false;

    /* audioFile 관련 변수 */
    private String audioFilePath;
    private FileInputStream fis;

    /* 감정분석 API 호출 관련 변수 */
    private Boolean emotionFlag = false;
    private EmotionThread emotionThread;

    private MQTTClient mqttClient = MQTTClient.getInstance();

    @Override
    public void run() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_KAKAO_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        retrofitService = retrofit.create(RetrofitService.class);
        Log.i("Stt", "retrofitService create");


        try {
            while (true) {
                // sttFlag == false
                if(!sttFlag) {
                    synchronized (audioFilePath) {
                        audioFilePath.wait();
                    }
                    sttFlag = true;
                }

                // sttFlag == true
                fis = new FileInputStream(audioFilePath);

                requestStt();

                if (fis != null) {
                    fis.close();
                }
                sttFlag = false;
                Log.i("Stt", "Stt Off");
            }
        } catch (InterruptedException | IOException e) {
            Log.i("Stt", "Stt Thread Dead...");
        }
    }

    private void requestStt() {
        try {
            byte[] audio = new byte[fis.available()];
            while(fis.read(audio) != -1);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), audio);

            Call<ResponseBody> getCall = retrofitService.get_post_pcm(transferEncoding, contentType, authorization, requestBody);
            getCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()) {
                        ResponseBody body = response.body();
                        String result = null;
                        try {
                            result = body.string();
                            Log.i("Stt", result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int startIndex = result.indexOf("{\"type\":\"finalResult\"");
                        int endIndex = result.lastIndexOf('}');
                        Log.i("Stt", "startIndex = " + startIndex + ", endIndex = " + endIndex);

                        if(startIndex > 0 && endIndex > 0) {
                            try {
                                String result_json_string = result.substring(startIndex, endIndex + 1);
                                JSONObject json = new JSONObject(result_json_string);
                                String sttResultMsg = json.getString("value");

                                Log.i("Stt", sttResultMsg);
                                Log.i("Stt", "sttResultMsg Size is " + sttResultMsg.length());
                                if(sttResultMsg.length() > 0) {
                                    emotionFlag = true;
                                    emotionThread.setSttResultMsg(sttResultMsg);
                                    synchronized (emotionThread.getTextFilePath()) {
                                        emotionThread.getTextFilePath().notify();
                                        Log.i("Emotion", "Emotion On");
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else { // errorCalled
                            Log.i("Stt", "errorCalled");
                            publishTextDataAndEmotionData("???", "none");
                        }

                    } else {
                        Log.i("Stt", "Status Code = " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("Stt", "Fail msg = " + t.getMessage());
                }
            });
        } catch (IOException e) {
            Log.i("Stt", "requestStt fail...");
        }
    }

    private void publishTextDataAndEmotionData(String text, String emotion) {
        mqttClient.publish(mqttClient.getTopic_text(), mqttClient.getUserName() + "&" +text+ "&" +emotion);
    }

}