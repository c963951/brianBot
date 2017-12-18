package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.select.Elements;

import com.github.abola.crawler.CrawlerPack;

public class GasService {

  private static GasService instance = new GasService();

  public static GasService getInstance() {
    return instance;
  }

  public GasService() {}

  public String getGasMessage() {
    String ratePage = "http://www.taiwanoil.org/z.php?z=oiltw";
    List<String> result = new ArrayList<>();
    String parseHtml = CrawlerPack.start().getFromHtml(ratePage).toString();
    Elements els = CrawlerPack.start().htmlToJsoupDoc(parseHtml).select("table tr");
    els.stream().forEach(x -> result.add(x.text() + "\r\n"));
    return String.join("", result);
  }
}
