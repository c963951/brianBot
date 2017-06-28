package com.example.bot.spring.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

public class YoutubeService {

    private static YoutubeService instance = new YoutubeService();

    public static YoutubeService getInstance() {
        return instance;
    }
    
    public YoutubeService() {
    }

    public List<Message> getYoutube(String queryTerm) throws IOException {
        
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName("youtube-cmdline-search-sample").build();

        YouTube.Search.List search = youtube.search().list("id,snippet");

        String apiKey = "AIzaSyDrWDpehcmxXo4gaqSL2AttQ3UZudOtgyk";
        search.setKey(apiKey);
        search.setQ(queryTerm);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(1L);
        search.setRegionCode("TW");
        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResultList = searchResponse.getItems();

        if (!searchResultList.isEmpty()) {
            return prettyPrint(searchResultList);
        }
        return null;
    }
    
    private static List<Message> prettyPrint(List<SearchResult> listSearchResults) {
        List<Message> messages = new ArrayList<Message>();
        for (SearchResult singleVideo : listSearchResults) {
            ResourceId rId = singleVideo.getId();
            Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
            if (rId.getKind().equals("youtube#video")) {
                messages.add(new ImageMessage(thumbnail.getUrl(), thumbnail.getUrl()));
                messages.add(new TextMessage(singleVideo.getSnippet().getTitle() + "\r\n"
                        + "https://www.youtube.com/watch?v=" + rId.getVideoId()));
            }
        }
        return messages;
    }

}
