package com.example.bot.spring.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.linecorp.bot.model.message.AudioMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextToSpeechService {

    private static TextToSpeechService instance = new TextToSpeechService();

    public static TextToSpeechService getInstance() {
        return instance;
    }

    public TextToSpeechService() {}

    public AudioMessage getTTs(String word) throws LineUnavailableException, UnsupportedAudioFileException {
        try {
            word = java.net.URLEncoder.encode(word, "UTF-8");
            URL url = new URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
            InputStream in = urlConn.getInputStream();
            return new AudioMessage(
                    "https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word, in.read()/10);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
