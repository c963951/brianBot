/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring.echo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.impl.SimpleLog;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.abola.crawler.CrawlerPack;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EchoApplication.class, args);
	}

	@EventMapping
	public TextMessage handleImageMessageEvent(MessageEvent<TextMessageContent> event) {
		CrawlerPack.setLoggerLevel(SimpleLog.LOG_LEVEL_OFF);
		System.out.println("event: " + event);
		String board = "Gossiping";
		if (event.getMessage().getText().startsWith("%")) {
			String[] message = event.getMessage().getText().split("%");
			board = message[1];
		} else {
			return new TextMessage(event.getMessage().getText());
		}
		String gossipMainPage = "https://www.ptt.cc/bbs/" + board + "/index.html";
		String gossipIndexPage = "https://www.ptt.cc/bbs/" + board + "/index%s.html";

		String prevPage = CrawlerPack.start().addCookie("over18", "1").getFromHtml(gossipMainPage)
				.select(".action-bar a:matchesOwn(上頁)").get(0).attr("href");

		System.out.println("event: " + prevPage);
		
		prevPage = prevPage.replaceAll("/bbs/" + board + "/index([0-9]+).html", "$1");
		Integer lastPage = Integer.valueOf(prevPage);
		Integer loadLastPosts = 2;
		List<String> lastPostsLink = new ArrayList<String>();
        while ( loadLastPosts > lastPostsLink.size() ){
        	String currPage = String.format(gossipIndexPage, lastPage--);
			Elements links = CrawlerPack.start().addCookie("over18", "1").getFromHtml(currPage)
					.select(".title > a");
			System.out.println(links.size());
			for (Element link : links) {
				String[] result = analyzeFeed(link.attr("href"));
				if (result != null) {
					lastPostsLink.add(result[0] + "\r\n" + result[1]);
				}
			}
        }
		return new TextMessage(String.join("\r\n", lastPostsLink));
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}
	
	public String[] analyzeFeed(String url) {
    	
    	// 取得 Jsoup 物件，稍後要做多次 select
        Document feed = 
        	CrawlerPack.start()
		        .addCookie("over18","1")                // 八卦版進入需要設定cookie
		        .getFromHtml("https://www.ptt.cc"+url);           // 遠端資料格式為 HTML
        // 2. 文章標題
        String feedTitle = feed.select("span:contains(標題) + span").text();
        
        // 3. 按推總數
        Integer feedLikeCount = 
        		countReply(feed.select(".push-tag:matchesOwn(推) + .push-userid"));
        if (feedLikeCount < 30)	{
        	return null;
        }
        
    	return new String[] {feedTitle,"https://www.ptt.cc"+url};
    }
	
	public Integer countReply(Elements reply){
    	return reply.text().split(" ").length;
    }
}
