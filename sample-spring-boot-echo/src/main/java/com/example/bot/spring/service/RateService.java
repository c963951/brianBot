package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.abola.crawler.CrawlerPack;

public class RateService {

  private static RateService instance = new RateService();

  public static RateService getInstance() {
    return instance;
  }

  public RateService() {}

  public String getRateMessage(String country) {
    Map<String, String> map = new HashMap<>();
    map.put("日本", "JPY");
    map.put("美國", "USD");
    map.put("韓國", "KRW");
    map.put("香港", "HKD");
    map.put("中國", "CNY");
    map.put("泰國", "THB");
    map.put("新加坡", "SGD");
    String countryRate = map.containsKey(country) ? (String) map.get(country) : country;

    String ratePage = "http://www.taiwanrate.org/exchange_rate.php?c=" + countryRate;
    List<String> result = new ArrayList<>();
    Elements rateEl = CrawlerPack.start().getFromHtml(ratePage).select("#accounts>tbody>tr");
    Element first = rateEl.stream().findFirst().get();
    result.add(
        "如果你需要買"
            + countryRate
            + "，在"
            + first.select("td:first-child").text()
            + "購買"
            + first.select("td:last-child").text()
            + "會比較便宜。\r\n");
    rateEl
        .stream()
        .filter(x -> result.size() < 6)
        .forEach(
            x ->
                result.add(
                    x.select("td:first-child").text()
                        + " "
                        + x.select("td:last-child").text()
                        + "\r\n"));

    rateEl.sort(
        (Element p1, Element p2) ->
            Float.compare(
                        Float.parseFloat(p1.select("td:nth-child(2)").text()),
                        Float.parseFloat(p2.select("td:nth-child(2)").text()))
                    == -1
                ? 0
                : (Float.compare(
                            Float.parseFloat(p1.select("td:nth-child(2)").text()),
                            Float.parseFloat(p2.select("td:nth-child(2)").text()))
                        == -1
                    ? 1
                    : -1));
    Element buy = rateEl.stream().findFirst().get();
    List<String> buys = new ArrayList<>();
    buys.add(
        "如果你要換"
            + countryRate
            + "，在"
            + buy.select("td:first-child").text()
            + "換回"
            + buy.select("td:nth-child(2)").text()
            + "會比較划算。\r\n");
    rateEl
        .stream()
        .filter(x -> buys.size() < 6)
        .forEach(
            x ->
                buys.add(
                    x.select("td:first-child").text()
                        + " "
                        + x.select("td:nth-child(2)").text()
                        + "\r\n"));
    result.addAll(buys);
    return String.join("", result);
  }
}
