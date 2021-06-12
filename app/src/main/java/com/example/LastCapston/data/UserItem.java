package com.example.LastCapston.data;


public class UserItem {

    public String userName;
    public String speakState;
    public String userEmotionIcon;

    public UserItem(String userName){
        this.userName = userName;
        this.speakState = "stop";
        this.userEmotionIcon = "";
    }


}
