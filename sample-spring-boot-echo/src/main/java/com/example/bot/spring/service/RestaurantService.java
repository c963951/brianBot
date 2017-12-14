package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Photo;
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
    for (int i = 1; i < 6; i++) {
      String imageUrl = photoUrl;
      for (Photo p : places.get(i).getPhotos()) {
        imageUrl = imageUrl + p.getReference();
        System.out.println(places.get(i).getPhotos().size());
        if (StringUtils.isEmpty(p.getReference())) {
          continue;
        }
        break;
      }

      CarouselColumn temp =
          new CarouselColumn(
              imageUrl,
              Double.toString(places.get(i).getRating()),
              places.get(i).getVicinity(),
              Arrays.asList(
                  new MessageAction(
                      places.get(i).getName(), places.get(i).getStatus().toString())));
      carusels.add(temp);
    }
    TemplateMessage templateMessage =
        new TemplateMessage("Carousel alt text", new CarouselTemplate(carusels));
    return templateMessage;
  }
}
