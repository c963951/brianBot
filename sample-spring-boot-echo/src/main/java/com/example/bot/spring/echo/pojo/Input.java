package com.example.bot.spring.echo.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Input {

    @SerializedName("text")
    @Expose
    private String text;

    public Input(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
