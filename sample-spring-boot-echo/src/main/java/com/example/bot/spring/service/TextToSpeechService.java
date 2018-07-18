package com.example.bot.spring.service;

import java.io.BufferedInputStream;
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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.bot.spring.echo.EchoApplication;
import com.google.common.io.ByteStreams;
import com.linecorp.bot.model.message.AudioMessage;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextToSpeechService {

    private static TextToSpeechService instance = new TextToSpeechService();

    public static TextToSpeechService getInstance() {
        return instance;
    }

    public TextToSpeechService() {}

    public AudioMessage getTTs(String word) throws LineUnavailableException, UnsupportedAudioFileException {
        log.info("word(*******************"+word);
        try {
            word = java.net.URLEncoder.encode(word, "UTF-8");
            URL url = new URL("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.addRequestProperty("User-Agent", "Mozilla/4.76");
            InputStream in = urlConn.getInputStream();
            AudioInputStream audioSrc = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            log.info("audioSrc=========="+audioSrc.read());
            System.out.println("audioSrc=========="+audioSrc.read());
            Clip clip = AudioSystem.getClip();
            clip.open(audioSrc);
            Double d = clip.getMicrosecondLength() / 1000000D;
            return new AudioMessage("https://translate.google.com/translate_tts?ie=UTF-8&tl=zh-tw&client=tw-ob&q=" + word, d.intValue());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = EchoApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
    }

    private String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
    }

    private DownloadedContent saveContent(String ext, InputStream in) {

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
    public class DownloadedContent {
        Path path;
        String uri;
    }
}
