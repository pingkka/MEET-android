package com.example.LastCapston.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.data.MessageItem;
import com.example.LastCapston.data.SendText;
import com.example.LastCapston.data.UserItem;
import com.example.LastCapston.data.UserSpeakState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainViewModel extends ViewModel {

    private static MainViewModel instance = new MainViewModel();
    /* MQTT 관련 변수 */
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private MutableLiveData<String> ip = new MutableLiveData<>();
    private MutableLiveData<String> port = new MutableLiveData<>();
    private MutableLiveData<String> topic = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();
    private MutableLiveData<String> makeRoomUser = new MutableLiveData<>();

    public MutableLiveData<ArrayList<UserItem>> userListData = new MutableLiveData<ArrayList<UserItem>>();
    public ArrayList<UserItem> userList = new ArrayList<UserItem>();
    public MutableLiveData<ArrayList<MessageItem>> mutableTextList = new MutableLiveData<ArrayList<MessageItem>>();
    public ArrayList<MessageItem> textList = new ArrayList<MessageItem>();
//    public MutableLiveData<String> loginUser = new MutableLiveData<>();
//    public MutableLiveData<String> logoutUser = new MutableLiveData<>();
//    public MutableLiveData<SendText> currentText = new MutableLiveData<>();
    public MutableLiveData<UserSpeakState> userSpeakState = new MutableLiveData<>();

    public Boolean autoSaveFlag = false;
    public File msgDir;
    public File msgFile;
    public String msgFileName;

    public MainViewModel() {
        /* 변수 초기화 */
//        ip.setValue("113.198.82.77"); // BUG
        ip.setValue("223.194.155.72");
        port.setValue("1883");
        topic.setValue("");
        userName.setValue("");
    }

    /* 싱글톤으로 객체 생성 */
    public static MainViewModel getInstance() {
        return instance;
    }

    /* settingData class에  ip, port, topic, username을 저장 */
    public void clickSubmit() {
        settingData.setIp(ip.getValue());
        settingData.setPort(port.getValue());
        settingData.setTopic(topic.getValue());
        settingData.setUserName(userName.getValue());
    }

    public void initMQTTSettingData() {
        /* 변수 초기화 */
        topic.setValue("");
        userName.setValue("");
    }

    public void mainViewMoedlInit() {
        /* 변수 초기화 */
        userList = new ArrayList<UserItem>();
        userListData = new MutableLiveData<ArrayList<UserItem>>();
        mutableTextList = new MutableLiveData<ArrayList<MessageItem>>();
        textList = new ArrayList<MessageItem>();

//        currentText = new MutableLiveData<>();
//        loginUser = new MutableLiveData<>();
//        logoutUser = new MutableLiveData<>();
    }

    public void addUserItem(UserItem user){
        userList.add(user);
        userListData.postValue(userList);
    }

    public void addMessageItem(MessageItem data){
        textList.add(data);
        mutableTextList.postValue(textList);
    }

    public void editUserSpeakState(String speakName, String speakState){
        Log.i("MQTT", "speakUser = " + speakName);
        Log.i("MQTT", "speakState = " + speakState);

        for(int i = 0; i < userList.size(); i++){
            String name = userList.get(i).userName;

            if(name.equals(speakName) && speakState.equals("start")){
                userList.get(i).speakState = "start";
                userListData.postValue(userList);
            }else if(name.equals(speakName) && speakState.equals("stop")){
                userList.get(i).speakState = "stop";
                userListData.postValue(userList);
            }
        }
    }

    public void updateUserListEmotion(String username, String image) {
        for(int i = 0; i < userList.size(); i++) {
            String name = userList.get(i).userName;

            if (name.equals(username)) {
                userList.get(i).userEmotionIcon = image;
                userListData.postValue(userList);
            }
        }
    }

    public void deleteUsersItem(String user) {
        for(int i = 0; i < userList.size(); i++){
            String name = userList.get(i).userName;

            if(name.equals(user)){
                userList.remove(i);
                userListData.postValue(userList);
            }
        }
    }

    /* Getter */
    public ArrayList<String> getUserList() {
        ArrayList<String> users =  new ArrayList<String>();
        for(int i = 0; i < userList.size(); i++){
            users.add(userList.get(i).userName);
        }
        return users;
    }

    public ArrayList<MessageItem> getTestList() {
        return textList;
    }

//    public SendText getCurrentText() {
//        return currentText.getValue();
//    }
//
//    public String getLoginUser() {
//        return loginUser.getValue();
//    }
//
//    public String getLogoutUser() {
//        return logoutUser.getValue();
//    }

    public UserSpeakState getUserSpeakState() { return userSpeakState.getValue(); }

    /* Setter */
//    public void setLoginUser(String user){
//        loginUser.postValue(user);
//    }
//    public void setCurrentText(SendText text){
//        currentText.postValue(text);
//    }
//
//    public void setLogoutUser(String user){
//        logoutUser.postValue(user);
//    }

    public void setTopic(String topic) {
        this.topic.setValue(topic);
    }

    public void setName(String name) {
        this.userName.setValue(name);
    }

    public void setMakeRoomUser(String makeRoomUser) {
        this.makeRoomUser.setValue(makeRoomUser);
    }

    public String getMakeRoomUser() {
        return makeRoomUser.getValue();
    }

    public void setUserSpeakState(UserSpeakState userSpeakState){
        this.userSpeakState.postValue(userSpeakState);
    }

    // 대화내용을 저장할 파일명 지정 함수
    public void msgFileInit() {
        /* 파일 이름 : 날짜시간_방이름.txt 또는 방이름_날짜시간.txt */
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmm");
        Date currentTime = new Date();
        String date = dateFormat.format(currentTime);

        msgFileName = "/" + topic.getValue() + "_" + date + ".txt";
    }

    /* 대화 내용 저장 함수 (saveFlag 0 : 전체 내용 저장, 1: 채팅 하나씩 저장*/
    public void conversationSave(int saveFlag) {
        try {
            FileWriter fileWriter;
            BufferedWriter bufferedWriter;

            msgFile = new File(msgDir + msgFileName);
            if(!msgFile.exists()) {
                msgFile.createNewFile();
            }

//            Log.d("Conversation", "msgFilename : " + msgFileName + "생성");

            /* 파일에 대화 내용 쓰기 */
            if(saveFlag == 0) {
                fileWriter = new FileWriter(msgFile, false);
                bufferedWriter = new BufferedWriter(fileWriter);

                for (MessageItem msgItem : textList) {
                    // [보낸사람] [시간] [감정] 대화 내용
                    bufferedWriter.append("[" + msgItem.getName() + "] ");
                    bufferedWriter.append("[" + msgItem.getTime() + "] ");
                    bufferedWriter.append("[" + msgItem.getImg() + "] ");
                    bufferedWriter.append(msgItem.getContent());
                    bufferedWriter.newLine();

//                    Log.d("Conversation", "msg0 : " + "[" + msgItem.getName() + "] " + "[" + msgItem.getTime() + "] " + "[" + msgItem.getImg() + "] " + msgItem.getContent());
                }
            }
            else {
                fileWriter = new FileWriter(msgFile, true);
                bufferedWriter = new BufferedWriter(fileWriter);

                MessageItem msgItem = textList.get(textList.size()-1);
                // [보낸사람] [시간] [감정] 대화 내용
                bufferedWriter.append("[" + msgItem.getName() + "] ");
                bufferedWriter.append("[" + msgItem.getTime() + "] ");
                bufferedWriter.append("[" + msgItem.getImg() + "] ");
                bufferedWriter.append(msgItem.getContent());
                bufferedWriter.newLine();

//                Log.d("Conversation", "msg1 : " + "[" + msgItem.getName() + "] " + "[" + msgItem.getTime() + "] " + "[" + msgItem.getImg() + "] " + msgItem.getContent());
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}