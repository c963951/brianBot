package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class RestaurantService {

  private static RestaurantService instance = new RestaurantService();

  public static RestaurantService getInstance() {
    return instance;
  }

  public RestaurantService() {}

  public TemplateMessage getRestaurant(double lat, double lng) {
    String apiKey = "AIzaSyB6t-XO4BEyDh1jBzHmeZn5hVB0WQkZLe8";
    GooglePlaces client = new GooglePlaces(apiKey);
    List<Place> places =
        client.getNearbyPlaces(
            lat,
            lng,
            1000,
            Param.name("type").value("restaurant"),
            Param.name("language").value("zh-TW"));
    List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
    String photoUrl =
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key="
            + apiKey
            + "&photoreference=";
    for (Place p : places) {
      JSONObject json = p.getJson();
      if (json.isNull("photos")) continue;
      String imageUrl =
          photoUrl + json.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
      CarouselColumn temp =
          new CarouselColumn(
              imageUrl,
              Double.toString(p.getRating()),
              p.getVicinity(),
              Arrays.asList(new MessageAction(p.getName(), p.getStatus().toString())));
      carusels.add(temp);
    }
    TemplateMessage templateMessage =
        new TemplateMessage("Carousel alt text", new CarouselTemplate(carusels));
    return templateMessage;
  }
}
