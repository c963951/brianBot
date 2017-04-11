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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.abola.crawler.CrawlerPack;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {
	
    // 取得最後幾篇的文章數量
    static Integer loadLastPosts = 10;
    
	public static void main(String[] args) {
		SpringApplication.run(EchoApplication.class, args);
	}

	@EventMapping
	public TextMessage handleImageMessageEvent(MessageEvent<TextMessageContent> event) {
		System.out.println("event: " + event);
		String board = "Gossiping";
		if(event.getMessage().getText().startsWith("@")) {
			String[] arg = event.getMessage().getText().split("@");
			board = arg[1];
		}
		String gossipMainPage = "https://www.ptt.cc/bbs/"+board+"/index.html";
	    String gossipIndexPage = "https://www.ptt.cc/bbs/"+board+"/index%s.html";
		
		String prevPage = CrawlerPack.start()
                .addCookie("over18","1")                // 八卦版進入需要設定cookie
                .getFromHtml(gossipMainPage)            // 遠端資料格式為 HTML
                .select(".action-bar a:matchesOwn(上頁)")  // 取得右上角『前一頁』的內容
                .get(0).attr("href")
                .replaceAll("/bbs/"+board+"/index([0-9]+).html", "$1");
        // 目前最末頁 index 編號
        Integer lastPage = Integer.valueOf(prevPage)+1;

        List<String> lastPostsLink = new ArrayList<String>();
        
        while ( loadLastPosts > lastPostsLink.size() ){
            String currPage = String.format(gossipIndexPage, lastPage--);

            Elements links =
                CrawlerPack.start()
                    .addCookie("over18", "1")
                    .getFromHtml(currPage)
                    .select(".title > a");

            for( Element link: links){
            	String result = analyzeFeed(link.attr("href"));
            	if("".equals(result)) continue;
            	lastPostsLink.add(result);
            	// 重要：為什麼要有這一行？
            	try{Thread.sleep(150);}catch(Exception e){}
            }
        }
		return new TextMessage(lastPostsLink.stream().map(Object::toString)
                .collect(Collectors.joining("%0D%0A")));
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}

	/**
	 * 分析輸入的文章，簡易統計
	 * 
	 * @param url
	 * @return
	 */
	public String analyzeFeed(String url) {

		// 取得 Jsoup 物件，稍後要做多次 select
		Document feed = CrawlerPack.start().addCookie("over18", "1") // 八卦版進入需要設定cookie
				.getFromHtml("https://www.ptt.cc" + url); // 遠端資料格式為 HTML

		// 1. 文章作者
		String feedAuthor = feed.select("span:contains(作者) + span").text();

		// 2. 文章標題
		String feedTitle = feed.select("span:contains(標題) + span").text();

		// 3. 按推總數
		Integer feedLikeCount = countReply(feed.select(".push-tag:matchesOwn(推) + .push-userid"));
		if (feedLikeCount < 80)
			return "";

		// 4. 不重複推文數
		Integer feedLikeCountNoRep = countReplyNoRepeat(feed.select(".push-tag:matchesOwn(推) + .push-userid"));

		// 5. 按噓總數
		Integer feedUnLikeCount = countReply(feed.select(".push-tag:matchesOwn(噓) + .push-userid"));

		// 6. 不重複噓文數
		Integer feedUnLikeCountNoRep = countReplyNoRepeat(feed.select(".push-tag:matchesOwn(噓) + .push-userid"));

		// 7. 不重複噓文數
		Integer feedReplyCountNoRep = countReplyNoRepeat(feed.select(".push-tag + .push-userid"));

		String output = "\"" + feedAuthor + "\"," + "\"" + feedTitle + "\"," + feedLikeCount + "," + feedLikeCountNoRep
				+ "," + feedUnLikeCount + "," + feedUnLikeCountNoRep + "," + feedReplyCountNoRep;
		return url;
	}

	/**
	 * 推文人數總計
	 * 
	 * @param reply
	 * @return
	 */
	public Integer countReply(Elements reply) {
		return reply.text().split(" ").length;
	}

	/**
	 * 推文人數總計
	 * 
	 * @param reply
	 * @return
	 */
	public Integer countReplyNoRepeat(Elements reply) {
		return new HashSet<String>(Arrays.asList(reply.text().split(" "))).size();
	}
}
