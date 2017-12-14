package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.linecorp.bot.model.action.URIAction;
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
            Param.name("type").value("food"),
            Param.name("language").value("zh-TW"));
    List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
    String mapUrl = "https://www.google.com/maps/search/?api=1&query= &query_place_id=";
    String photoUrl =
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key="
            + apiKey
            + "&photoreference=";
    places.sort(
        (Place p1, Place p2) ->
            p1.getRating() > p2.getRating() ? -1 : (p1.getRating() < p2.getRating() ? 1 : 0));
    for (Place p : places) {
      JSONObject json = p.getJson();
      if (json.isNull("photos")) continue;
      String imageUrl =
          photoUrl + json.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
      if (carusels.size() == 5) break;
      String detail = "店名:" + p.getName() + "\n" + "營業狀態:" + p.getStatus().toString();
      CarouselColumn temp =
          new CarouselColumn(
              imageUrl,
              detail,
              "地址:" + p.getVicinity(),
              Arrays.asList(new URIAction("map位置點我", mapUrl + p.getPlaceId())));
      carusels.add(temp);
    }
    TemplateMessage templateMessage = new TemplateMessage("位置找餐廳", new CarouselTemplate(carusels));
    return templateMessage;
  }
}
