package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  public TemplateMessage getCarousel() {
    List<CarouselColumn> carusels = new ArrayList<>();

    CarouselColumn temp1 =
        new CarouselColumn("", "", "", Arrays.asList(new PostbackAction("找餐廳", "restaurant")));
    carusels.add(temp1);
    CarouselColumn temp2 =
        new CarouselColumn("", "", "", Arrays.asList(new PostbackAction("找加油站", "gas_station")));
    carusels.add(temp2);
    CarouselColumn temp3 =
        new CarouselColumn("", "", "", Arrays.asList(new PostbackAction("找停車場", "parking")));
    carusels.add(temp3);
    TemplateMessage templateMessage = new TemplateMessage("food", new CarouselTemplate(carusels));
    return templateMessage;
  }
}
