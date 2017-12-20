package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class PlaceService {

  private static PlaceService instance = new PlaceService();

  public static PlaceService getInstance() {
    return instance;
  }

  public PlaceService() {}

  public TemplateMessage getCarousel(double lat, double lng) {
    List<CarouselColumn> carusels = new ArrayList<>();
    CarouselColumn temp1 =
        new CarouselColumn(
            null,
            null,
            "googleMap",
            Arrays.asList(
                new PostbackAction("找餐廳", lat + "," + lng + ",restaurant"),
                new PostbackAction("找咖啡廳", lat + "," + lng + ",cafe"),
                new PostbackAction("找bar", lat + "," + lng + ",bar")));
    carusels.add(temp1);
    CarouselColumn temp2 =
        new CarouselColumn(
            null,
            null,
            "googleMap",
            Arrays.asList(
                new PostbackAction("找捷運", lat + "," + lng + ",subway_station"),
                new PostbackAction("找停車場", lat + "," + lng + ",parking"),
                new PostbackAction("找加油站", lat + "," + lng + ",gas_station")));
    carusels.add(temp2);
    TemplateMessage templateMessage =
        new TemplateMessage("findPlace", new CarouselTemplate(carusels));
    return templateMessage;
  }

  public TemplateMessage getGooglePlaces(String data) {
    List<String> datas = Arrays.asList(StringUtils.split(data, ","));
    if (datas.size() != 3) return null;
    double lat = Double.parseDouble(datas.get(0));
    double lng = Double.parseDouble(datas.get(1));
    String place = datas.get(2);
    String apiKey = "AIzaSyB6t-XO4BEyDh1jBzHmeZn5hVB0WQkZLe8";
    GooglePlaces client = new GooglePlaces(apiKey);
    List<Place> places =
        client.getNearbyPlaces(
            lat, lng, 1000, Param.name("type").value(place), Param.name("language").value("zh-TW"));
    List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
    String photoUrl =
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key="
            + apiKey
            + "&photoreference=";
    if ("restaurant".equals(place)) {
      places.sort(
          (Place p1, Place p2) ->
              p1.getRating() > p2.getRating() ? -1 : (p1.getRating() < p2.getRating() ? 1 : 0));
    }
    for (Place p : places) {
      JSONObject json = p.getJson();
      if (json.isNull("photos")) continue;
      String imageUrl =
          photoUrl + json.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
      if (carusels.size() == 5) break;
      String title = p.getName();
      String detail =
          "評分:" + p.getRating() + "\r\n營業狀態:" + p.getStatus() + "\r\n地址:" + p.getVicinity();
      if (detail.length() > 60) detail = detail.substring(0, 60);
      if (title.length() > 40) title = title.substring(0, 40);
      CarouselColumn temp =
          new CarouselColumn(
              imageUrl,
              title,
              detail,
              Arrays.asList(
                  new URIAction(
                      "map location",
                      "https://www.google.com/maps/search/?api=1&query=%20&query_place_id="
                          + p.getPlaceId())));
      carusels.add(temp);
    }
    TemplateMessage templateMessage = new TemplateMessage(place, new CarouselTemplate(carusels));
    return templateMessage;
  }
}
