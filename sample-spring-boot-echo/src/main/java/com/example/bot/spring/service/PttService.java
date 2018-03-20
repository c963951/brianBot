package com.example.bot.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.abola.crawler.CrawlerPack;

public class PttService {

    private static PttService instance = new PttService();

    public static PttService getInstance() {
        return instance;
    }

    public PttService() {}

    public String getPttMessage(String board) {

        String gossipMainPage = "https://www.ptt.cc/bbs/" + board + "/index.html";
        String gossipIndexPage = "https://www.ptt.cc/bbs/" + board + "/index%s.html";

        String prevPage = CrawlerPack.start().addCookie("over18", "1").getFromHtml(gossipMainPage)
                .select(".action-bar a:matchesOwn(上頁)").get(0).attr("href");

        prevPage = prevPage.replaceAll("/bbs/" + board + "/index([0-9]+).html", "$1");
        Integer lastPage = Integer.valueOf(prevPage);
        List<String> lastPostsLink = new ArrayList<>();
        while (lastPostsLink.size() < 3) {
            String currPage = String.format(gossipIndexPage, lastPage--);
            Elements links = CrawlerPack.start().addCookie("over18", "1").getFromHtml(currPage).select(".r-ent");
            for (Element link : links) {
                if (lastPostsLink.size() > 2) {
                    break;
                }
                boolean MoreThen50 = false;
                Elements pushs = link.select(".nrec span");
                for (Element push : pushs) {
                    String a = push.ownText();
                    if ("爆".equals(a)) {
                        MoreThen50 = true;
                        break;
                    }
                    else if (NumberUtils.isCreatable(a) && Integer.parseInt(a) > 80) {
                        MoreThen50 = true;
                        break;
                    }
                }
                if (!MoreThen50) {
                    continue;
                }

                Elements titles = link.select(".title > a");
                for (Element title : titles) {
                    if (lastPostsLink.size() > 2) {
                        break;
                    }
                    lastPostsLink.add(title.ownText() + "\r\n" + "https://www.ptt.cc" + title.attr("href"));
                }
            }
        }
        return String.join("\r\n", lastPostsLink);
    }
}
