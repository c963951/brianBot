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
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {

	Integer loadLastPosts = 10;

	public static void main(String[] args) {
		SpringApplication.run(EchoApplication.class, args);
	}

	@EventMapping
	public TextMessage handleImageMessageEvent(MessageEvent<TextMessageContent> event) {
		System.out.println("event: " + event);
		String board = "Gossiping";
		if (event.getMessage().getText().startsWith("@")) {
			String[] arg = event.getMessage().getText().split("@");
			board = arg[1];
		}
		String gossipMainPage = "https://www.ptt.cc/bbs/" + board + "/index.html";
		String gossipIndexPage = "https://www.ptt.cc/bbs/" + board + "/index%s.html";

		String prevPage = CrawlerPack.start().addCookie("over18", "1").getFromHtml(gossipMainPage)
				.select(".action-bar a:matchesOwn(上頁)").get(0).attr("href")
				.replaceAll("/bbs/" + board + "/index([0-9]+).html", "$1");
		Integer lastPage = Integer.valueOf(prevPage) + 1;

		List<String> lastPostsLink = new ArrayList<String>();

		while (loadLastPosts > lastPostsLink.size()) {
			String currPage = String.format(gossipIndexPage, lastPage--);
			Elements links = CrawlerPack.start().addCookie("over18", "1")
					.getFromHtml(currPage).select(".title > a");
			for (Element link : links) {
				String result = analyzeFeed(link.attr("href"));
				if ("".equals(result)) {
					continue;
				}
				lastPostsLink.add(result);
				try {
					Thread.sleep(150);
				} catch (Exception e) {
				}
			}
		}
		//String.join("%0D%0A", lastPostsLink)
		return new TextMessage("");
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}

	public String analyzeFeed(String url) {

		Document feed = CrawlerPack.start().addCookie("over18", "1")
				.getFromHtml("https://www.ptt.cc" + url);

		Integer feedLikeCount = countReply(feed.select(".push-tag:matchesOwn(推) + .push-userid"));
		if (feedLikeCount < 80) {
			return "";
		}
		return url;
	}

	public Integer countReply(Elements reply) {
		return reply.text().split(" ").length;
	}

	public Integer countReplyNoRepeat(Elements reply) {
		return new HashSet<String>(Arrays.asList(reply.text().split(" "))).size();
	}
}
