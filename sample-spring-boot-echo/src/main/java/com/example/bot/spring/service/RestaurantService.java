package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
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

    public List<Message> getRestaurant(double lat, double lng) {
        List<Message> messages = new ArrayList<Message>();
        String apiKey = "AIzaSyB6t-XO4BEyDh1jBzHmeZn5hVB0WQkZLe8";
        Param a = new Param("types");
        a.value("food");
        GooglePlaces client = new GooglePlaces(apiKey);
        List<Place> places = client.getNearbyPlaces(lat, lng, 500, a);
        List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
        for (Place p : places) {
            String imageUrl = createUri(p.getIconUrl());
            CarouselColumn temp = new CarouselColumn(imageUrl, Double.toString(p.getRating()), p.getAddress(),
                    Arrays.asList(new URIAction(p.getName(), p.getGoogleUrl())));
            carusels.add(temp);
        }
        TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", new CarouselTemplate(carusels));
        messages.add(templateMessage);
        return messages;
    }

    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
    }

}
