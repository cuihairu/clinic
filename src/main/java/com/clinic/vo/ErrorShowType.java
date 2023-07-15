package com.clinic.vo;

public enum ErrorShowType {
    SILENT("SILENT",0), //
    WARN_MESSAGE("WARN_MESSAGE",1), //警告
    ERROR_MESSAGE("ERROR_MESSAGE",2),//错误
    NOTIFICATION("NOTIFICATION",3), // 通知
    REDIRECT("REDIRECT",9);//跳转
    private String name;
    private int value;
    private ErrorShowType(String name,int value){
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
