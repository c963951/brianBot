package com.example.bot.spring.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

public class SpotifyService {

    private static SpotifyService instance = new SpotifyService();

    private static final String clientId = "a2c0f7c2f8b340d79d8c9febdf1753a3";
    private static final String clientSecret = "7ebddff0b9fc473fa26e89f4e15ee09c";
    private static final String redirectURI = "";

    public static SpotifyService getInstance() {
        return instance;
    }

    public SpotifyService() {}

    public List<Message> getSpotify(String queryTerm) throws IOException, InterruptedException, ExecutionException {
        SpotifyApi api = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setRedirectUri(null).build();
        ClientCredentialsRequest request = api.clientCredentials().build();
        Future<ClientCredentials> responseFuture = request.executeAsync();
        ClientCredentials clientCredentials = responseFuture.get();
        api.setAccessToken(clientCredentials.getAccessToken());
        SearchTracksRequest result = api.searchTracks(queryTerm).limit(1).build();
        List<Message> messages = new ArrayList<Message>();
        try {
            Paging<Track> trackPaging = result.execute();
            for (Track t : trackPaging.getItems()) {
                String url = "";
                for (Image img : t.getAlbum().getImages()) {
                    messages.add(new ImageMessage(img.getUrl(), img.getUrl()));
                    break;
                }
                messages.add(new TextMessage(t.getExternalUrls().get("spotify")));
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
        }
        return messages;
    }
}
