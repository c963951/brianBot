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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.impl.SimpleLog;
import org.json.JSONObject;
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

	public static void main(String[] args) throws Exception {
		SpringApplication.run(EchoApplication.class, args);
	}

	@EventMapping
	public TextMessage handleImageMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		CrawlerPack.setLoggerLevel(SimpleLog.LOG_LEVEL_OFF);
		System.out.println("event: " + event);
		String board = "Gossiping";
		if (event.getMessage().getText().startsWith("%")) {
			String[] message = event.getMessage().getText().split("%");
			board = message[1];
		} else if (event.getMessage().getText().startsWith("#")){
			return new TextMessage(getHoroscope(event.getMessage().getText()));
		} else if (event.getMessage().getText().startsWith("&")){
			return new TextMessage(cngetHoroscope(event.getMessage().getText()));
		} else {
			return null;
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
				if ( lastPostsLink.size() > loadLastPosts) {
					break;
				}
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
	
	public static String getHoroscope(String message) {
		
		String[] result = message.split("#");
		String sign = result[1];
		
		sign = sign.toLowerCase();
		
		String url = "http://theastrologer-api.herokuapp.com/api/horoscope/" + sign + "/today";
		String charset = "UTF-8";
		
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();

			String jsonBody = "";
			
			try (Scanner scanner = new Scanner(response)) {
				jsonBody += scanner.useDelimiter("\\A").next();
			}
			
			JSONObject obj = new JSONObject(jsonBody);
			String horoscope = obj.getString("horoscope");
				
			return horoscope;
		}
		catch (MalformedURLException e) {
			return "Problem retrieving horoscope";
		}
		catch(IOException e) {
			return "Problem retrieving horoscope";
		}
		
	}
	public static String cngetHoroscope (String message) throws Exception {
		String[] result = message.split("&");
		String sign = result[1];
		
		sign = sign.toLowerCase();
		
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("牡羊", "baiyang");
		map.put("金牛", "jinniu");
		map.put("雙子", "shuangzi");
		map.put("巨蟹", "juxie");
		map.put("獅子", "shizi");
		map.put("處女", "chunv");
		map.put("天秤", "tiancheng");
		map.put("天蠍", "tianxie");
		map.put("射手", "sheshou");
		map.put("魔羯", "mojie");
		map.put("水瓶", "shuiping");
		map.put("雙魚", "shuangyu");
		
		URL u=new URL("http://route.showapi.com/872-1?showapi_appid=35583&star="+map.get(sign)+"&needTomorrow=&needWeek=&needMonth=&needYear=&showapi_sign=5532b24c2d4646cf9799a36930288cb8");
        InputStream in=u.openStream();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try {
            byte buf[]=new byte[1024];
            int read = 0;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
        }  finally {
            if (in != null) {
                in.close();
            }
        }
        byte b[]=out.toByteArray( );
        JSONObject obj = new JSONObject(new String(b,"utf-8"));
        JSONObject showapi_res_body = (JSONObject) obj.get("showapi_res_body");
        JSONObject day =  (JSONObject) showapi_res_body.get("day");
        String horoscope = day.getString("work_txt");
        System.out.println(horoscope);
        return horoscope;
	}
}
