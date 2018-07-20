package com.example.bot.spring.echo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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
        response.setContentType("audio/mpeg");
        response.setHeader("Referer", "http://translate.google.com/");
        response.setHeader("Content-Disposition", "attachment; filename=test.mp3");
        FileCopyUtils.copy(inn, response.getOutputStream());
    }
}
