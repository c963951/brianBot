package com.example.bot.spring.echo.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.bot.spring.echo.service.ReplyService.AudioContent;
import com.example.bot.spring.echo.texttospeech.pojo.AudioConfig;
import com.example.bot.spring.echo.texttospeech.pojo.Input;
import com.example.bot.spring.echo.texttospeech.pojo.TextToSpeech;
import com.example.bot.spring.echo.texttospeech.pojo.Voice;
import com.google.gson.Gson;

@Controller
public class FileDownloadController {

    @GetMapping(value = "/tts/{word}")
    public @ResponseBody void download(HttpServletResponse response, @PathVariable("word") String word)
            throws IOException {
        URL url = new URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q="
                + URLEncoder.encode(word, "UTF-8"));
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
        urlConn.addRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        InputStream inn = urlConn.getInputStream();
        response.reset();
        response.setContentType("audio/mpeg");
        response.setHeader("alt-svc", "quic=:443; ma=2592000; v=44,43,39,35");
        response.setHeader("x-content-type-options","nosniff");
        response.setHeader("x-xss-protection", "1; mode=block");
        response.setHeader("cache-control", "private, max-age=86400");
        response.setHeader("status", "200");
        response.setHeader("server", "HTTP");
        FileCopyUtils.copy(inn, response.getOutputStream());
    }

    @GetMapping(value = "/googleTTs/{word}")
    public @ResponseBody void googleTTs(HttpServletResponse response, @PathVariable("word") String word)
            throws IOException, UnsupportedAudioFileException {
        Gson gson = new Gson();
        TextToSpeech tts = new TextToSpeech();
        tts.setAudioConfig(new AudioConfig());
        tts.setInput(new Input());
        tts.setVoice(new Voice());
        tts.getAudioConfig().setAudioEncoding("LINEAR16");
        tts.getAudioConfig().setPitch("0.00");
        tts.getAudioConfig().setSpeakingRate("0.75");
        tts.getInput().setText(word);
        tts.getVoice().setLanguageCode("en-US");
        tts.getVoice().setName("en-AU-Standard-C");
        String json = gson.toJson(tts);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(
                    "https://texttospeech.googleapis.com/v1beta1/text:synthesize?key=AIzaSyCxcqIXbtwQxokBl8CxFEFBChXWU35-5QQ");
            StringEntity params = new StringEntity(json);
            request.addHeader("content-type", "application/json; charset=utf-8");
            request.setEntity(params);
            HttpResponse resp = httpClient.execute(request);
            String result = EntityUtils.toString(resp.getEntity(), "UTF-8");
            AudioContent audioContent = gson.fromJson(result, AudioContent.class);
            byte[] bytes = Base64.getDecoder().decode(audioContent.getAudioContent());
            ByteArrayInputStream oInstream = new ByteArrayInputStream(bytes);
            response.reset();
            response.setContentType("audio/mpeg");
            response.setHeader("alt-svc", "quic=:443; ma=2592000; v=44,43,39,35");
            response.setHeader("x-content-type-options","nosniff");
            response.setHeader("x-xss-protection", "1; mode=block");
            response.setHeader("cache-control", "private, max-age=86400");
            response.setHeader("status", "200");
            response.setHeader("server", "HTTP");
            FileCopyUtils.copy(oInstream, response.getOutputStream());
        }
        catch (IOException ex) {}
    }
}
