package com.example.bot.spring.echo.texttospeech.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Voice {

    @SerializedName("languageCode")
    @Expose
    private String languageCode;
    @SerializedName("name")
    @Expose
    private String name;

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}