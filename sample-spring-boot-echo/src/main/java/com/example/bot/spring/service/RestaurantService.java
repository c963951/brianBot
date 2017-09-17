package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Param[] params = new Param[2];
        Param b = new Param("type");
        b.value("restaurant");
        Param a = new Param("language");
        a.value("zh-TW");
        params[0] = a;
        params[1] = b;
        GooglePlaces client = new GooglePlaces(apiKey);
        List<Place> places = client.getNearbyPlaces(lat, lng, 500, params);
        List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key="+apiKey+"&photoreference=";
        for (int i = 1; i < 6; i++) {
            String imageUrl = photoUrl+places.get(i).getPhotos().get(0).getReference();
            System.out.println(imageUrl);
            if(places.get(i).getRating() < 4 && carusels.size() != 0) {
                i--;
            }
            CarouselColumn temp = new CarouselColumn(imageUrl, Double.toString(places.get(i).getRating()),
                    places.get(i).getVicinity(),
                    Arrays.asList(new MessageAction(places.get(i).getName(), places.get(i).getStatus().toString())));
            carusels.add(temp);
        }
        TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", new CarouselTemplate(carusels));
        return templateMessage;
    }

}
