package com.example.bot.spring.echo.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.bot.spring.echo.pojo.Article;
import com.example.bot.spring.echo.pojo.Default;
import com.example.bot.spring.echo.pojo.GoogleNews;
import com.example.bot.spring.echo.pojo.Item;
import com.example.bot.spring.echo.pojo.Translate;
import com.example.bot.spring.echo.pojo.Youtube;
import com.example.bot.spring.echo.pojo.serch.GoogleSearch;
import com.github.abola.crawler.CrawlerPack;
import com.google.gson.Gson;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import lombok.extern.slf4j.Slf4j;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

@Service
@Slf4j
public class ReplyService {

    public String getGasMessage() {
        String ratePage = "http://www.taiwanoil.org/z.php?z=oiltw";
        List<String> result = new ArrayList<>();
        String parseHtml = CrawlerPack.start().getFromHtml(ratePage).toString();
        Elements els = CrawlerPack.start().htmlToJsoupDoc(parseHtml).select("table tr");
        els.stream().forEach(x -> result.add(x.text() + "\r\n"));
        return String.join("", result);
    }

    public String getNewsMessage() throws IOException {
        StringBuffer sb = new StringBuffer();
        URL url = new URL("https://newsapi.org/v2/top-headlines?country=tw&apiKey=3c9ded024fd34fb8b7966f149ac0a27b");
        InputStreamReader reader = new InputStreamReader(url.openStream());
        GoogleNews news = new Gson().fromJson(reader, GoogleNews.class);
        List<Article> articles = news.getArticles();
        AtomicInteger index = new AtomicInteger();
        articles.stream().filter(n -> index.incrementAndGet() <= 5)
                .forEach(x -> sb.append(x.getTitle() + "\r\n" + x.getUrl() + "\r\n"));
        return sb.toString();
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

    public TemplateMessage getCarousel(double lat, double lng) {
        List<CarouselColumn> carusels = new ArrayList<>();
        CarouselColumn temp1 = new CarouselColumn(null, null, "googleMap",
                Arrays.asList(new PostbackAction("找餐廳", lat + "," + lng + ",g_restaurant"),
                        new PostbackAction("找咖啡廳", lat + "," + lng + ",g_cafe"),
                        new PostbackAction("找bar", lat + "," + lng + ",g_bar")));
        carusels.add(temp1);
        CarouselColumn temp2 = new CarouselColumn(null, null, "googleMap",
                Arrays.asList(new PostbackAction("找捷運", lat + "," + lng + ",g_subway_station"),
                        new PostbackAction("找停車場", lat + "," + lng + ",g_parking"),
                        new PostbackAction("找加油站", lat + "," + lng + ",g_gas_station")));
        carusels.add(temp2);
        TemplateMessage templateMessage = new TemplateMessage("findPlace", new CarouselTemplate(carusels));
        return templateMessage;
    }

    public TemplateMessage getGooglePlaces(String data) throws JSONException {
        List<String> datas = Arrays.asList(StringUtils.split(data, ","));
        if (datas.size() != 3) return null;
        double lat = Double.parseDouble(datas.get(0));
        double lng = Double.parseDouble(datas.get(1));
        String place = datas.get(2).replace("g_", "");
        String apiKey = "AIzaSyCxcqIXbtwQxokBl8CxFEFBChXWU35-5QQ";
        GooglePlaces client = new GooglePlaces(apiKey);
        List<Place> places = client.getNearbyPlacesRankedByDistance(lat, lng, Param.name("type").value(place),
                Param.name("language").value("zh-TW"));
        List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key=" + apiKey
                + "&photoreference=";
        if ("restaurant,cafe,bar".indexOf(place) != -1) {
            places.sort((Place p1,
                    Place p2) -> p1.getRating() > p2.getRating() ? -1 : (p1.getRating() < p2.getRating() ? 1 : 0));
        }
        for (Place p : places) {
            JSONObject json = p.getJson();
            if (json.isNull("photos")) continue;
            String imageUrl = photoUrl + json.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            if (carusels.size() == 5) break;
            String title = p.getName();
            String detail = "評分:" + p.getRating() + "\r\n營業狀態:" + p.getStatus() + "\r\n地址:" + p.getVicinity();
            if (detail.length() > 60) detail = detail.substring(0, 60);
            if (title.length() > 40) title = title.substring(0, 40);
            CarouselColumn temp = new CarouselColumn(imageUrl, title, detail,
                    Arrays.asList(new URIAction("map location",
                            "https://www.google.com/maps/search/?api=1&query=%20&query_place_id=" + p.getPlaceId())));
            carusels.add(temp);
        }
        TemplateMessage templateMessage = new TemplateMessage(place, new CarouselTemplate(carusels));
        return templateMessage;
    }

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

    public String getRateMessage(String country) {
        Map<String, String> map = new HashMap<>();
        map.put("日本", "JPY");
        map.put("美國", "USD");
        map.put("韓國", "KRW");
        map.put("香港", "HKD");
        map.put("中國", "CNY");
        map.put("泰國", "THB");
        map.put("新加坡", "SGD");
        String countryRate = country;
        if (map.containsKey(country)) {
            countryRate = map.get(country);
        }

        String ratePage = "http://www.taiwanrate.org/exchange_rate.php?c=" + countryRate;
        List<String> result = new ArrayList<>();
        Elements rateEl = CrawlerPack.start().getFromHtml(ratePage).select("#accounts>tbody>tr");
        Element first = rateEl.stream().findFirst().get();
        result.add("如果你需要買" + countryRate + "，在" + first.select("td:first-child").text() + "購買"
                + first.select("td:last-child").text() + "會比較便宜。\r\n");

        rateEl.sort((Element p1, Element p2) -> Float.compare(
                Float.parseFloat(NumberUtils.isCreatable(p2.select("td:nth-child(3)").text())
                        ? p2.select("td:nth-child(3)").text()
                        : "0"),
                Float.parseFloat(NumberUtils.isCreatable(p1.select("td:nth-child(3)").text())
                        ? p1.select("td:nth-child(3)").text()
                        : "0")) == -1
                                ? 0
                                : (Float.compare(
                                        Float.parseFloat(NumberUtils.isCreatable(p2.select("td:nth-child(3)").text())
                                                ? p2.select("td:nth-child(3)").text()
                                                : "0"),
                                        Float.parseFloat(NumberUtils.isCreatable(p1.select("td:nth-child(3)").text())
                                                ? p1.select("td:nth-child(3)").text()
                                                : "0")) == -1 ? 1 : -1));
        rateEl.stream().filter(x -> result.size() < 6).forEach(
                x -> result.add(x.select("td:first-child").text() + " " + x.select("td:last-child").text() + "\r\n"));

        rateEl.sort((Element p1, Element p2) -> Float.compare(
                Float.parseFloat(NumberUtils.isCreatable(p1.select("td:nth-child(2)").text())
                        ? p1.select("td:nth-child(2)").text()
                        : "0"),
                Float.parseFloat(NumberUtils.isCreatable(p2.select("td:nth-child(2)").text())
                        ? p2.select("td:nth-child(2)").text()
                        : "0")) == -1
                                ? 0
                                : (Float.compare(
                                        Float.parseFloat(NumberUtils.isCreatable(p1.select("td:nth-child(2)").text())
                                                ? p1.select("td:nth-child(2)").text()
                                                : "0"),
                                        Float.parseFloat(NumberUtils.isCreatable(p2.select("td:nth-child(2)").text())
                                                ? p2.select("td:nth-child(2)").text()
                                                : "0")) == -1 ? 1 : -1));
        Element buy = rateEl.stream().findFirst().get();
        List<String> buys = new ArrayList<>();
        buys.add("如果你要換" + countryRate + "，在" + buy.select("td:first-child").text() + "換回"
                + buy.select("td:nth-child(2)").text() + "會比較划算。\r\n");
        rateEl.stream().filter(x -> buys.size() < 6).forEach(
                x -> buys.add(x.select("td:first-child").text() + " " + x.select("td:nth-child(2)").text() + "\r\n"));
        result.addAll(buys);
        return String.join("", result);
    }

    public List<Message> getSpotify(String queryTerm) throws IOException, InterruptedException, ExecutionException {
        String clientId = "a2c0f7c2f8b340d79d8c9febdf1753a3";
        String clientSecret = "7ebddff0b9fc473fa26e89f4e15ee09c";
        SpotifyApi api = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret)
                .setRedirectUri(null).build();
        ClientCredentialsRequest request = api.clientCredentials().build();
        Future<ClientCredentials> responseFuture = request.executeAsync();
        ClientCredentials clientCredentials = responseFuture.get();
        api.setAccessToken(clientCredentials.getAccessToken());
        SearchTracksRequest result = api.searchTracks(queryTerm).limit(1).build();
        List<Message> messages = new ArrayList<Message>();
        try {
            Paging<Track> trackPaging = result.execute();
            for (Track t : trackPaging.getItems()) {
                for (Image img : t.getAlbum().getImages()) {
                    messages.add(new ImageMessage(img.getUrl(), img.getUrl()));
                    break;
                }
                messages.add(new TextMessage(t.getExternalUrls().get("spotify")));
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
        }
        return messages;
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

    public String getSearchMessage(String queryTerm) throws IOException {
        log.info("=======youtube start=====");
        StringBuffer sb = new StringBuffer();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(
                    "https://www.googleapis.com/customsearch/v1?key=AIzaSyA2XHK-Bv2HTFX9HGzXevCSt91yAttmgX8&cx=016196646623470081627:ceqepq_94ri&fields=items(title,link)&lr=lang_zh-TW&q="
                            + URLEncoder.encode(queryTerm, "UTF-8"));
            request.addHeader("content-type", "application/json; charset=utf-8");
            HttpResponse resp = httpClient.execute(request);
            String result = EntityUtils.toString(resp.getEntity(), "UTF-8");
            GoogleSearch search = new Gson().fromJson(result, GoogleSearch.class);
            List<com.example.bot.spring.echo.pojo.serch.Item> items = search.getItems();

            AtomicInteger index = new AtomicInteger();
            items.stream().filter(n -> index.incrementAndGet() <= 5)
                    .forEach(x -> sb.append(x.getTitle() + "\r\n" + x.getLink() + "\r\n"));
        }
        catch (IOException ex) {}
        return sb.toString();
    }

    public List<Message> getYoutube(String queryTerm) throws IOException {
        log.info("=======Search start=====");
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(
                    "https://www.googleapis.com/youtube/v3/search?part=id,snippet&regionCode=TW&type=video&key=AIzaSyDrWDpehcmxXo4gaqSL2AttQ3UZudOtgyk&q="
                            + URLEncoder.encode(queryTerm, "UTF-8"));
            request.addHeader("content-type", "application/json; charset=utf-8");
            HttpResponse resp = httpClient.execute(request);
            String result = EntityUtils.toString(resp.getEntity(), "UTF-8");
            Youtube youtube = new Gson().fromJson(result, Youtube.class);
            List<Item> items = youtube.getItems();
            return prettyPrint(items);

        }
        catch (IOException ex) {}
        return null;
    }

    private static List<Message> prettyPrint(List<Item> listSearchResults) {
        List<Message> messages = new ArrayList<Message>();
        for (Item singleVideo : listSearchResults) {
            String rId = singleVideo.getId().getVideoId();
            Default thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
            messages.add(new ImageMessage(thumbnail.getUrl(), thumbnail.getUrl()));
            messages.add(new TextMessage(
                    singleVideo.getSnippet().getTitle() + "\r\n" + "https://www.youtube.com/watch?v=" + rId));
            break;
        }
        return messages;
    }

    public TemplateMessage getRestaurant(double lat, double lng) throws JSONException {
        String apiKey = "AIzaSyCxcqIXbtwQxokBl8CxFEFBChXWU35-5QQ";
        GooglePlaces client = new GooglePlaces(apiKey);
        List<Place> places = client.getNearbyPlaces(lat, lng, 1000, Param.name("type").value("restaurant"),
                Param.name("language").value("zh-TW"));
        List<CarouselColumn> carusels = new ArrayList<CarouselColumn>();
        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&key=" + apiKey
                + "&photoreference=";
        places.sort((Place p1,
                Place p2) -> p1.getRating() > p2.getRating() ? -1 : (p1.getRating() < p2.getRating() ? 1 : 0));
        for (Place p : places) {
            JSONObject json = p.getJson();
            if (json.isNull("photos")) continue;
            String imageUrl = photoUrl + json.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            if (carusels.size() == 5) break;
            CarouselColumn temp = new CarouselColumn(imageUrl, p.getName(),
                    "評分:" + p.getRating() + "\r\n營業狀態:" + p.getStatus() + "\r\n地址:" + p.getVicinity(),
                    Arrays.asList(new URIAction("map location",
                            "https://www.google.com/maps/search/?api=1&query=%20&query_place_id=" + p.getPlaceId())));
            carusels.add(temp);
        }
        TemplateMessage templateMessage = new TemplateMessage("food", new CarouselTemplate(carusels));
        return templateMessage;
    }

    public AudioMessage getTTs(String word) throws IOException {
        log.info("=======SoundText start=====");
        try {
            String location = "https://c963951.herokuapp.com/tts/" + URLEncoder.encode(word + ".m4a", "UTF-8");
            File file = File.createTempFile("audio", "w4a");
            FileUtils.copyURLToFile(new URL(location), file, 1000, 600000);
            AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
            Map<String, Object> properties = baseFileFormat.properties();
            Long duration = (Long)properties.get("duration");
            log.info("=======SoundText end=====");
            return new AudioMessage(location, (int)(duration / 1000) + 1000);
        }
        catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AudioMessage getCloudTTs(String word, String voice) throws IOException {
        log.info("=======CloudTTs start=====");
        if (voice == null) {
            voice = "en";
        }
        try {
            String location = "https://c963951.herokuapp.com/googleTTs/" + voice + "/"
                    + URLEncoder.encode(word + ".m4a", "UTF-8");
            File file = File.createTempFile("audio", "w4a");
            FileUtils.copyURLToFile(new URL(location), file, 1000, 600000);
            AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
            Map<String, Object> properties = baseFileFormat.properties();
            Long duration = (Long)properties.get("duration");
            log.info("=======CloudTTs end=====");
            return new AudioMessage(location, (int)(duration / 1000) + 1000);
        }
        catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }


    public TemplateMessage getTranslateCard(String data) {
        List<CarouselColumn> carusels = new ArrayList<>();
        CarouselColumn temp1 = new CarouselColumn(null, null, "translate",
                Arrays.asList(new PostbackAction("英文", "en_" + data, "英文"),
                        new PostbackAction("日文", "ja_" + data, "日文"), new PostbackAction("韓文", "ko_" + data, "韓文"),
                        new PostbackAction("西文", "es_" + data, "西文"), new PostbackAction("德文", "de_" + data, "德文")));
        carusels.add(temp1);
        TemplateMessage templateMessage = new TemplateMessage("translate", new CarouselTemplate(carusels));
        return templateMessage;
    }

    public List<Message> getTranslate(String data) {
        List<Message> messages = new ArrayList<Message>();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(
                    "https://translation.googleapis.com/language/translate/v2?source=zh-TW&format=text&key=AIzaSyDrWDpehcmxXo4gaqSL2AttQ3UZudOtgyk&q="
                            + URLEncoder.encode(data.substring(2), "UTF-8") + "&target=" + data.substring(0, 2));
            request.addHeader("content-type", "application/json; charset=utf-8");
            HttpResponse resp = httpClient.execute(request);
            String result = EntityUtils.toString(resp.getEntity(), "UTF-8");
            Translate translate = new Gson().fromJson(result, Translate.class);
            String word = translate.getData().getTranslations().get(0).getTranslatedText();
            messages.add(new TextMessage(word));
            messages.add(getCloudTTs(word,data.substring(0, 2)));

        }
        catch (IOException ex) {}
        return messages;
    }

    public class AudioContent {

        private String audioContent;

        public String getAudioContent() {
            return audioContent;
        }

        public void setAudioContent(String audioContent) {
            this.audioContent = audioContent;
        }
    }
}
