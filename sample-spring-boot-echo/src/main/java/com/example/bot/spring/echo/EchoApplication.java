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
		System.out.println("event: " + event);
		if (!event.getMessage().getText().startsWith("@")) {
			return new TextMessage("");
		}
		String gossipMainPage = "https://www.ptt.cc/bbs/Gossiping/index.html";

		String prevPage = CrawlerPack.start().addCookie("over18", "1").getFromHtml(gossipMainPage)
				.select(".action-bar a:matchesOwn(上頁)").get(0).attr("href");

		System.out.println("event: " + prevPage);
		prevPage = prevPage.replaceAll("/bbs/Gossiping/index([0-9]+).html", "$1");
		Integer lastPage = Integer.valueOf(prevPage) + 1;
		String gossipIndexPage = "https://www.ptt.cc/bbs/Gossiping/index%s.html";
		String currPage = String.format(gossipIndexPage, lastPage--);
		String currPage2 = "https://www.ptt.cc/bbs/Gossiping/index22719.html";
		Elements links = CrawlerPack.start().addCookie("over18", "1").getFromHtml(currPage2).select(".title > a");
		System.out.println(links.size());
		String a = "";
		for (Element link : links) {
			a = link.attr("href");
		}
		return new TextMessage(links.size() + "%0D%0A" + a + "%0D%0A" + prevPage + "%0D%0A" + currPage);
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}
}
