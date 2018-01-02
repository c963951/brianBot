package com.example.bot.spring.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.TrackSearchRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.ClientCredentials;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.Track;

public class SpotifyService {

  private static SpotifyService instance = new SpotifyService();

  private static final String clientId = "a2c0f7c2f8b340d79d8c9febdf1753a3";
  private static final String clientSecret = "7ebddff0b9fc473fa26e89f4e15ee09c";
  private static final String redirectURI = "";

  public static SpotifyService getInstance() {
    return instance;
  }

  public SpotifyService() {}

  public List<Message> getSpotify(String queryTerm) throws IOException {
    Api api =
        Api.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .redirectURI(redirectURI)
            .build();
    ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();
    SettableFuture<ClientCredentials> responseFuture = request.getAsync();
    Futures.addCallback(
        responseFuture,
        new FutureCallback<ClientCredentials>() {
          public void onSuccess(ClientCredentials clientCredentials) {
            System.out.println(
                "Successfully retrieved an access token! " + clientCredentials.getAccessToken());
            System.out.println(
                "The access token expires in " + clientCredentials.getExpiresIn() + " seconds");

            api.setAccessToken(clientCredentials.getAccessToken());
          }

          public void onFailure(Throwable throwable) {}
        });
    TrackSearchRequest result = api.searchTracks(queryTerm).limit(1).build();
    List<Message> messages = new ArrayList<Message>();
    try {
      Page<Track> trackSearchResult = result.get();
      for (Track t : trackSearchResult.getItems()) {
        messages.add(
            new ImageMessage(
                t.getAlbum().getImages().stream().findFirst().get().getUrl(),
                t.getAlbum().getImages().stream().findFirst().get().getUrl()));
        messages.add(new TextMessage(t.getExternalUrls().get("spotify")));
      }
      System.out.print(
          trackSearchResult.getItems().stream().findFirst().get().getExternalUrls().get("spotify"));
    } catch (Exception e) {
      System.out.println("Something went wrong!" + e.getMessage());
    }
    return messages;
  }
}
