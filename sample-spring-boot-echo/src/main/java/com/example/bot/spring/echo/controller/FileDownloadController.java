package com.example.bot.spring.echo.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bot.spring.echo.pojo.AudioConfig;
import com.example.bot.spring.echo.pojo.Input;
import com.example.bot.spring.echo.pojo.TextToSpeech;
import com.example.bot.spring.echo.pojo.Voice;
import com.example.bot.spring.echo.service.ReplyService.AudioContent;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Controller
public class FileDownloadController {

    @GetMapping(value = "/tts/{word}")
    public @ResponseBody void download(HttpServletResponse response, @PathVariable("word") String word)
            throws IOException {
        URL url = new URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word);
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
        urlConn.addRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        InputStream inn = urlConn.getInputStream();
        int cl = urlConn.getContentLength();
        response.reset();
        response.setContentType("audio/x-m4a");
        response.setHeader("alt-svc", "quic=:443; ma=2592000; v=44,43,39,35");
        response.setHeader("x-content-type-options", "nosniff");
        response.setHeader("x-xss-protection", "1; mode=block");
        response.setHeader("status", "200");
        response.setHeader("server", "HTTP");
        response.setContentLength(cl);
        FileCopyUtils.copy(inn, response.getOutputStream());
        inn.close();
        return;
    }

    @GetMapping(value = "/googleTTs/{voice}/{word}")
    public @ResponseBody void googleTTs(HttpServletResponse response, @PathVariable("voice") String voice,
            @PathVariable("word") String word) throws IOException {
        String v1 = "en-US";
        String v2 = "en-AU-Standard-C";
        if ("ja".equals(voice)) {
            v1 = "ja-JP";
            v2 = "ja-JP-Standard-A";
        }
        else if ("ko".equals(voice)) {
            v1 = "ko-KR";
            v2 = "ko-KR-Standard-A";
        }
        else if ("es".equals(voice)) {
            v1 = "es-ES";
            v2 = "es-ES-Standard-A";
        }
        else if ("de".equals(voice)) {
            v1 = "de-DE";
            v2 = "de-DE-Standard-A";
        }
        else if ("fr".equals(voice)) {
            v1 = "fr-CA";
            v2 = "fr-CA-Standard-A";
        }
        Gson gson = new Gson();
        TextToSpeech tts = new TextToSpeech();
        tts.setAudioConfig(new AudioConfig("MP3", "0.00", "1.00"));
        tts.setInput(new Input(URLDecoder.decode(word, "UTF-8")));
        tts.setVoice(new Voice(v1, v2));
        String json = gson.toJson(tts);
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            Request request = new Request.Builder().url(
                    "https://texttospeech.googleapis.com/v1/text:synthesize?key=AIzaSyDrWDpehcmxXo4gaqSL2AttQ3UZudOtgyk")
                    .post(body).build();
            Response resp = client.newCall(request).execute();
            AudioContent audioContent = gson.fromJson(resp.body().string(), AudioContent.class);
            byte[] bytes = Base64.getDecoder().decode(audioContent.getAudioContent());
            ByteArrayInputStream oInstream = new ByteArrayInputStream(bytes);
            response.reset();
            response.setContentType("audio/x-m4a");
            response.setHeader("alt-svc", "quic=:443; ma=2592000; v=44,43,39,35");
            response.setHeader("x-content-type-options", "nosniff");
            response.setHeader("x-xss-protection", "1; mode=block");
            response.setHeader("status", "200");
            response.setHeader("server", "HTTP");
            response.setContentLength(bytes.length);
            FileCopyUtils.copy(oInstream, response.getOutputStream());
            oInstream.close();
        }
        catch (IOException ex) {}
        return;
    }
}
