package com.example.last_capston;

public class MessageItem {
    //내용
    private String content;
    //이름
    private String name;
    //뷰타입(왼쪽, 가운데, 오른쪽)
    private int viewType;

    public MessageItem(String content, String name, int viewType){
        this.content = content;
        this.viewType = viewType;
        this.name = name;
    }

    public String getContent(){ return content;}
    public String getName(){ return name;}
    public int getViewType(){ return viewType;}

}
