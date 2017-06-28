package com.example.bot.spring.service;

import java.util.HashMap;

import com.github.abola.crawler.CrawlerPack;

public class HoroscopeService {

    private static HoroscopeService instance = new HoroscopeService();

    public static HoroscopeService getInstance() {
        return instance;
    }
    
    public HoroscopeService() {
    }

    public String getHoroscope(String sign) {
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("牡羊", "Aries");
        map.put("金牛", "Taurus");
        map.put("雙子", "Gemini");
        map.put("巨蟹", "Cancer");
        map.put("獅子", "Leo");
        map.put("處女", "Virgo");
        map.put("天秤", "Libra");
        map.put("天蠍", "Scorpio");
        map.put("射手", "Sagittarius");
        map.put("魔羯", "Capricorn");
        map.put("水瓶", "Aquarius");
        map.put("雙魚", "Pisces");

        String uri = "http://www.daily-zodiac.com/zodiac/" + map.get(sign);
        String day = CrawlerPack.start().getFromHtml(uri).select(".user-zodiac > h3").text();
        String article = CrawlerPack.start().getFromHtml(uri).select(".user-zodiac .article").text();
        return day + "\r\n" + article;
    }

}
