package com.example.LastCapston.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.LastCapston.data.MQTTSettingData;
import com.example.LastCapston.data.SendText;
import com.example.LastCapston.data.UserItem;
import com.example.LastCapston.data.UserSpeakState;

import java.util.ArrayList;

import lombok.Getter;

import static android.media.CamcorderProfile.get;


@Getter
public class MainViewModel extends ViewModel {

    private static MainViewModel instance = new MainViewModel();
    /* MQTT 관련 변수 */
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private MutableLiveData<String> ip = new MutableLiveData<>();
    private MutableLiveData<String> port = new MutableLiveData<>();
    private MutableLiveData<String> topic = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();

    public MutableLiveData<ArrayList<UserItem>> userListData = new MutableLiveData<ArrayList<UserItem>>();
    public ArrayList<UserItem> userList = new ArrayList<UserItem>();
    public MutableLiveData<String> loginUser = new MutableLiveData<>();
    public MutableLiveData<String> logoutUser = new MutableLiveData<>();
    public MutableLiveData<SendText> currentText = new MutableLiveData<>();
    public MutableLiveData<UserSpeakState> userSpeakState = new MutableLiveData<>();

    public MainViewModel() {
        /* 변수 초기화 */

        ip.setValue("223.194.153.241"); // 지호
        port.setValue("1883");
//        ip.setValue("113.198.82.77");
//        port.setValue("1883");
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
        loginUser = new MutableLiveData<>();
        logoutUser = new MutableLiveData<>();
    }


    public void addUserItem(UserItem user){
        userList.add(user);
        userListData.postValue(userList);
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

    public SendText getCurrentText() {
        return currentText.getValue();
    }

    public String getLoginUser() {
        return loginUser.getValue();
    }

    public String getLogoutUser() {
        return logoutUser.getValue();
    }



    /* Setter */
    public void setLoginUser(String user){
        loginUser.postValue(user);
    }
    public void setTopic(String topic) {
        this.topic.setValue(topic);
    }

    public void setName(String name) {
        this.userName.setValue(name);
    }
    public void setCurrentText(SendText text){
        currentText.postValue(text);
    }
    public void setUserSpeakState(UserSpeakState userSpeakState){
        this.userSpeakState.postValue(userSpeakState);
    }
    public void setLogoutUser(String user){
        logoutUser.postValue(user);
    }

}
