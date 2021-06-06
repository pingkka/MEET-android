package com.example.last_capston.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.last_capston.data.MQTTSettingData;
import com.example.last_capston.data.SendText;
import com.example.last_capston.data.UserItem;

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
    public MutableLiveData<Boolean> enterFlag = new MutableLiveData<>();
    public MutableLiveData<SendText> currentText = new MutableLiveData<>();


    public MainViewModel() {
        /* 변수 초기화 */

        ip.setValue("172.30.1.10"); // 지호
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

    }

    public void userInit() {
        /* 변수 초기화 */

        userList = new ArrayList<UserItem>();
        userListData = new MutableLiveData<ArrayList<UserItem>>();
        loginUser = new MutableLiveData<>();
        logoutUser = new MutableLiveData<>();
        enterFlag = new MutableLiveData<>();
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

    public Boolean getEnterFlag(){
        return enterFlag.getValue();
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
    public void setLogoutUser(String user){
        logoutUser.postValue(user);
    }
    public void setEnterFlag(Boolean flag){
        enterFlag.postValue(flag);
    }

}
