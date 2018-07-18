package com.example.bot.spring.echo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.bot.spring.service.GasService;
import com.example.bot.spring.service.HoroscopeService;
import com.example.bot.spring.service.PlaceService;
import com.example.bot.spring.service.PttService;
import com.example.bot.spring.service.RateService;
import com.example.bot.spring.service.RestaurantService;
import com.example.bot.spring.service.SpotifyService;
import com.example.bot.spring.service.TextToSpeechService;
import com.example.bot.spring.service.WeatherService;
import com.example.bot.spring.service.YoutubeService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {

    private final String channelToken = "OD6ub5Qyystuid9ouEmNPBRLFmQTyeAbEX9ngG3WB9Scma4cDIM5qanrZ5dmJgnoKPxGwMQlsyDC8jm3p7LMLinTKRJDuMBrJ4ACM0egQIppZBoCGtCYA0rgBp8PSb8EkJppGlP0BhaWFVaeiyQddwdB04t89/1O/w1cDnyilFU=";

    @Autowired
    private LineMessagingClient lineMessagingClient;
    
    public static Path downloadedContentDir;

    public static void main(String[] args) throws Exception {
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) throws Exception {
        List<Message> Messages = new ArrayList<Message>();
        Source source = event.getSource();
        String pushId = source.getUserId();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }
        Messages.add(getPost(event.getMessage().getLatitude(), event.getMessage().getLongitude()));
        push(channelToken, pushId, Messages);
    }

    @EventMapping
    public void handleDefaultMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        Source source = event.getSource();
        List<Message> Messages = new ArrayList<Message>();
        String pushId = source.getUserId();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }

        String message = ((TextMessageContent)event.getMessage()).getText();
        String keyworad = message.substring(1);
        if (message.startsWith("%")) {
            Messages.add(PTT(keyworad));
        }
        else if (message.startsWith("#")) {
            Messages.add(Horoscope(keyworad));
        }
        else if (message.startsWith("&")) {
            Messages.addAll(getYoutube(keyworad));
        }
        else if (message.startsWith("$")) {
            Messages.add(Weather(keyworad));
        }
        else if (message.startsWith("##")) {
            Messages.add(TTs(message.substring(2)));
        }
        else if (message.startsWith("-r ")) {
            Messages.add(getRate(StringUtils.removeStart(message, "-r ")));
        }
        else if (message.startsWith("-gas")) {
            Messages.add(getGas());
        }
        else if (message.startsWith("spotify")) {
            Messages.addAll(getSpotify(StringUtils.removeStart(message, "spotify ")));
        }
        else if (message.equals("Botbye")) {
            if (source instanceof GroupSource) {
                lineMessagingClient.leaveGroup(((GroupSource)source).getGroupId()).get();
            }
            else if (source instanceof RoomSource) {
                lineMessagingClient.leaveRoom(((RoomSource)source).getRoomId()).get();
            }
            return;
        }
        if (Messages.isEmpty()) {
            return;
        }
        push(channelToken, pushId, Messages);
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws Exception {
        Source source = event.getSource();
        String pushId = source.getUserId();
        String data = event.getPostbackContent().getData();
        List<Message> Messages = new ArrayList<>();
        if (source instanceof GroupSource) {
            pushId = ((GroupSource)source).getGroupId();
        }
        else if (source instanceof RoomSource) {
            pushId = ((RoomSource)source).getRoomId();
        }
        Messages.add(getPlaces(data));
        push(channelToken, pushId, Messages);
    }

    public static TextMessage PTT(String message) {
        String result = PttService.getInstance().getPttMessage(message);
        return new TextMessage(result);
    }

    public static TextMessage Weather(String message) {
        String result = WeatherService.getInstance().getWeather(message);
        return new TextMessage(result);
    }
    
    public static AudioMessage TTs(String message) {
        return TextToSpeechService.getInstance().getTTs(message);
    }

    public static TextMessage Horoscope(String message) {
        String result = HoroscopeService.getInstance().getHoroscope(message);
        return new TextMessage(result);
    }

    public static List<Message> getYoutube(String message) throws Exception {
        List<Message> result = YoutubeService.getInstance().getYoutube(message);
        return result;
    }

    public static TemplateMessage getRestaurant(double lat, double lng) throws Exception {
        TemplateMessage result = RestaurantService.getInstance().getRestaurant(lat, lng);
        return result;
    }

    public static TextMessage getRate(String message) {
        String result = RateService.getInstance().getRateMessage(message);
        return new TextMessage(result);
    }

    public static TextMessage getGas() {
        String result = GasService.getInstance().getGasMessage();
        return new TextMessage(result);
    }

    public static List<Message> getSpotify(String message) throws Exception {
        List<Message> result = SpotifyService.getInstance().getSpotify(message);
        return result;
    }

    public static TemplateMessage getPost(double lat, double lng) throws Exception {
        TemplateMessage result = PlaceService.getInstance().getCarousel(lat, lng);
        return result;
    }

    public static TemplateMessage getPlaces(String data) throws Exception {
        TemplateMessage result = PlaceService.getInstance().getGooglePlaces(data);
        return result;
    }

    private void push(@NonNull String channelToken, @NonNull String pushId, @NonNull List<Message> messages)
            throws Exception {
        try {
            CompletableFuture<BotApiResponse> response = lineMessagingClient
                    .pushMessage(new PushMessage(pushId, messages));
            // Response<BotApiResponse> response =
            // LineMessagingServiceBuilder
            // .create(channelToken)
            // .build()
            // .pushMessage((new PushMessage(pushId, messages)))
            // .execute();
            // System.out.println(response.code() + " " + response.message());
            System.out.println(response.get().getMessage() + " " + response.get().getDetails().toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
