package com.example.LastCapston.calling;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.LastCapston.main.MQTTClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordThread extends Thread {

    /* AudioRecord Setting 관련 변수 */
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 16000;
    private int channelCount = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = sampleRate*5; // MQTTClient의 messageArrived(topic_audio)부분 수정, PlayThread의 bufferSize 수정

    private AudioRecord audioRecord;
    private boolean recordFlag = false;
    private byte[] audioData;

    private MQTTClient mqttClient = MQTTClient.getInstance();

    private byte[] nameData = mqttClient.getUserName().getBytes();

    /* ---------- Button Click 방식 ---------- */
//    private List<byte[]> audioQueue = Collections.synchronizedList(new ArrayList<>(2)); // 10초
    /* -------------------------------------- */

    /* API 호출 관련 변수 */
    private String audioFilePath;
    private FileOutputStream fos;
    private File audioFile;

    private boolean sttFlag = false;
    private SttThread sttThread;

    @Override
    public void run() {
        audioData = new byte[bufferSize];

        audioRecord = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelCount)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

        audioRecord.startRecording();
        Log.i("Audio", "Start Recording");

        try {
            while(true) {
                // recordFlag == false
                if(!recordFlag) {
                    audioRecord.stop();
//
//                    /* ---------- Button Touch 방식 ---------- */
//                    audioFile = new File(audioFilePath);
//                    Log.i("Audio", "audiFile size is " + audioFile.length());
//                    if(audioFile.length() > 0) {
//                        if(fos != null) {
//                            fos.close();
//                        }
//                        sttFlag = true;
//                        synchronized (sttThread.getAudioFilePath()) {
//                            sttThread.getAudioFilePath().notify();
//                            Log.i("Stt", "Stt On");
//                        }
//                    }
                    /* ------------------------------------- */

                    synchronized (audioRecord) {
                        audioRecord.wait();
                    }
                    recordFlag = true;

                    /* ---------- Button Touch 방식 ---------- */
//                    fos = new FileOutputStream(audioFilePath);
                    /* --------------------------------------- */
                }

                audioRecord.startRecording();

                // recordFlag == true
                int ret = audioRecord.read(audioData, 0 , bufferSize);
                Log.i("Audio", "read byte is " + ret);

                publishAudioData(audioData);

                /* ---------- Button Touch 방식 ---------- */
                //fos.write(audioData, 0, bufferSize); // 파일에 읽은 audioData 저장
                /* -------------------------------------- */



                /*---------- Button Click 방식 ----------*/
                /*
                audioQueue.add(audioData);
                if(audioQueue.size() >= 2) {
                    fos = new FileOutputStream(audioFilePath);
                    for (byte[] audio : audioQueue) {
                        fos.write(audio, 0, bufferSize);
                    }
                    if(fos != null) {
                        fos.close();
                    }

                    audioQueue.clear();

                    audioFile = new File(audioFilePath);
                    Log.i("Audio", "audiFile size is " + audioFile.length());

                    if(audioFile.length() > 0) {
                        sttFlag = true;
                        synchronized (sttThread.getAudioFilePath()) {
                            sttThread.getAudioFilePath().notify();
                            Log.i("Stt", "Stt On");
                        }
                    }
                }
                */
                /*---------------------------------- */

            }
        } catch (InterruptedException E) {
            Log.i("Audio", "Audio Thread Dead...");
            if(audioFile.exists()) {
                audioFile.delete();
            }
        }
    }

    /* Audio 리소스 해제 */
    public void stopRecording() {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        Log.i("Audio", "Stop Recording");
    }

    /* nameData + readData */
    private void publishAudioData(byte[] audioData) {
        byte[] messageData = new byte[nameData.length + audioData.length];

        System.arraycopy(nameData, 0, messageData, 0, nameData.length);
        System.arraycopy(audioData, 0, messageData, nameData.length, audioData.length);

        mqttClient.publish(mqttClient.getTopic_audio(), messageData);
    }


}