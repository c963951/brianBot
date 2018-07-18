package com.example.bot.spring.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.bot.spring.echo.EchoApplication;
import com.google.common.io.ByteStreams;
import com.linecorp.bot.model.message.AudioMessage;

import lombok.Value;

@Service
public class TextToSpeechService {

    public AudioMessage getTTs(String word) {
        try {
            word = java.net.URLEncoder.encode(word, "UTF-8");
            URL url = new URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
            InputStream audioSrc = urlConn.getInputStream();

            DownloadedContent mp4 = saveContent("mp4", audioSrc);
            return new AudioMessage(mp4.getUri(), 100);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = EchoApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
    }

    private static DownloadedContent saveContent(String ext, InputStream in) {

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(in, outputStream);
            return tempFile;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
}
