package com.example.LastCapston.data;

import lombok.Getter;

@Getter
public class MessageItem {
    //내용
    private String content;
    //이름
    private String name;
    //감정
    private String img;
    //뷰타입(왼쪽, 가운데, 오른쪽)
    private int viewType;
    //시간
    private String time;

    public MessageItem(String content, String name, String img, String time, int viewType ){
        this.content = content;
        this.viewType = viewType;
        this.name = name;
        this.img = img;
        this.time = time;
    }

}
