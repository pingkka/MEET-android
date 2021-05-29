package com.example.last_capston.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class MainViewModel extends ViewModel {

    private static MainViewModel instance = new MainViewModel();
    /* MQTT 관련 변수 */
    private MQTTSettingData settingData = MQTTSettingData.getInstance();

    private MutableLiveData<String> ip = new MutableLiveData<>();
    private MutableLiveData<String> port = new MutableLiveData<>();
    private MutableLiveData<String> topic = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();


    public ArrayList<String> userList = new ArrayList<String>();
    public MutableLiveData<ArrayList<String>> userListLivedata = new MutableLiveData<ArrayList<String>>();


    public MutableLiveData<String> loginUser = new MutableLiveData<>();
    public MutableLiveData<String> logoutUser = new MutableLiveData<>();
    public MutableLiveData<Boolean> enterFlag = new MutableLiveData<>();



    public MainViewModel() {
        /* 변수 초기화 */
        //ip.setValue("172.30.1.12"); // 경진
        ip.setValue("192.168.200.105"); // 지호
        port.setValue("1883");
        topic.setValue("");
        userName.setValue("");

    }

    public static MainViewModel getInstance() {
        return instance;
    }


    public void clickSubmit() {
        settingData.setIp(ip.getValue());
        settingData.setPort(port.getValue());
        settingData.setTopic(topic.getValue());
        settingData.setUserName(userName.getValue());

        // 여기서 Fragment 화면 전환 가능?
    }

    public void userInit() {
        /* 변수 초기화 */
        userList = new ArrayList<String>();
        userListLivedata = new MutableLiveData<ArrayList<String>>();
        loginUser = new MutableLiveData<>();
        logoutUser = new MutableLiveData<>();
        enterFlag = new MutableLiveData<>();
    }


    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUserList(String user){
        userList.add(user);
        userListLivedata.postValue(userList);
    }

    public void setLoginUser(String user){
        loginUser.postValue(user);

//        GlobalScope.launch {
//            loginUser = user;
//            liveDataLoginUser.postValue(user);
//        }
    }

    public String getLoginUser() {
        return loginUser.getValue();
    }

    public void setLogoutUser(String user){
        logoutUser.postValue(user);
    }

    public String getLogoutUser() {
        return logoutUser.getValue();
    }


    public void deleteUsersList(String user) {

        userList.remove(user);
        userListLivedata.postValue(userList);

    }


    public void setEnterFlag(Boolean flag){
        enterFlag.setValue(flag);
    }

    /* Setter */
    public void setTopic(String topic) {
        this.topic.setValue(topic);
    }

    public void setName(String name) {
        this.userName.setValue(name);
    }


}
