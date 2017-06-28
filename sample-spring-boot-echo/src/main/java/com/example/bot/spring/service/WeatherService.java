package com.example.bot.spring.service;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.abola.crawler.CrawlerPack;

public class WeatherService {

    private static WeatherService instance = new WeatherService();

    public static WeatherService getInstance() {
        return instance;
    }

    public WeatherService() {
    }

    public String getWeather(String region) {
        String regionUrl = "https://works.ioa.tw/weather/api/all.json";
        HashMap<String, String> allMap = new HashMap<String, String>();
        Elements a = CrawlerPack.start().getFromJson(regionUrl).getElementsByTag("towns");
        for (Element b : a) {
            allMap.put(b.getElementsByTag("name").text(), b.getElementsByTag("id").text());
        }
        StringBuffer result = new StringBuffer();
        String detailUrl = "https://works.ioa.tw/weather/api/weathers/" + allMap.get(region) + ".json";
        Document doc = CrawlerPack.start().getFromJson(detailUrl);
        result.append("地區：" + region + "\r\n");
        result.append("敘述：" + doc.select("desc").get(0).text() + "\r\n");
        result.append("溫度：" + doc.select("temperature").get(0).text() + "°C　\r\n");
        result.append("體感溫度：" + doc.select("felt_air_temp").get(0).text() + "°C　\r\n");
        result.append("濕度：" + doc.select("humidity").get(0).text() + "%");
        return result.toString();
    }

}
