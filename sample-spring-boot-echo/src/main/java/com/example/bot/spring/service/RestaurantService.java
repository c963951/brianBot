package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.linecorp.bot.model.action.PostbackAction;
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
        Param a = new Param("types");
        a.value("food");
        GooglePlaces client = new GooglePlaces(apiKey);
        List<Place> places = client.getNearbyPlaces(lat, lng, 500, a);
        List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
        for (int i = 0; i < 5; i++) {
            String imageUrl = places.get(i).getIconUrl();
            System.out.println(imageUrl);
            CarouselColumn temp = new CarouselColumn(imageUrl, Double.toString(places.get(i).getRating()),
                    "",
                    Arrays.asList(new URIAction(places.get(i).getName(), ""),
                            new PostbackAction("Say hello1",
                                    "hello こんにちは")));
            carusels.add(temp);
        }
        TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", new CarouselTemplate(carusels));
        return templateMessage;
    }

}
