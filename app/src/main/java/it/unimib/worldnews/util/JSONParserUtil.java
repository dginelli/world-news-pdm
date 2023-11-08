package it.unimib.worldnews.util;

import android.app.Application;
import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.model.NewsSource;

/**
 * Util class to show different ways to parse a JSON file.
 */
public class JSONParserUtil {

    private static final String TAG = JSONParserUtil.class.getSimpleName();

    private final Context context;

    private final String statusParameter = "status";
    private final String totalResultsParameter = "totalResults";
    private final String articlesParameter = "articles";
    private final String sourceParameter = "source";
    private final String authorParameter = "author";
    private final String titleParameter = "title";
    private final String descriptionParameter = "description";
    private final String urlParameter = "url";
    private final String urlToImageParameter = "urlToImage";
    private final String publishedAtParameter = "publishedAt";
    private final String contentParameter = "content";
    private final String nameParameter = "name";

    public JSONParserUtil(Application application) {
        this.context = application.getApplicationContext();
    }

    /**
     * Returns a list of News from a JSON file parsed using JsonReader class.
     * Doc can be read here: <a href="https://developer.android.com/reference/android/util/JsonReader">...</a>
     * @param fileName The JSON file to be parsed.
     * @return The NewsApiResponse object associated with the JSON file content.
     * @throws IOException
     */
    public NewsApiResponse parseJSONFileWithJsonReader(String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        NewsApiResponse newsApiResponse = new NewsApiResponse();
        List<News> newsList = null;

        jsonReader.beginObject(); // Beginning of JSON root

        while (jsonReader.hasNext()) {
            String rootJSONParam = jsonReader.nextName();
            if (rootJSONParam.equals(statusParameter)) {
                newsApiResponse.setStatus(jsonReader.nextString());
            } else if (rootJSONParam.equals(totalResultsParameter)) {
                newsApiResponse.setTotalResults(jsonReader.nextInt());
            } else if (rootJSONParam.equals(articlesParameter)) {
                jsonReader.beginArray(); // Beginning of articles array
                newsList = new ArrayList<>();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject(); // Beginning of article object
                    News news = new News();
                    while (jsonReader.hasNext()) {
                        String articleJSONParam = jsonReader.nextName();
                        if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(authorParameter)) {
                            String author = jsonReader.nextString();
                            news.setAuthor(author);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(titleParameter)) {
                            String title = jsonReader.nextString();
                            news.setTitle(title);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(descriptionParameter)) {
                            String description = jsonReader.nextString();
                            news.setDescription(description);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(urlParameter)) {
                            String url = jsonReader.nextString();
                            news.setUrl(url);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(urlToImageParameter)) {
                            String urlToImage = jsonReader.nextString();
                            news.setUrlToImage(urlToImage);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(publishedAtParameter)) {
                            String date = jsonReader.nextString();
                            news.setDate(date);
                        } else if (jsonReader.peek() != JsonToken.NULL &&
                                articleJSONParam.equals(contentParameter)) {
                            String content = jsonReader.nextString();
                            news.setContent(content);
                        } else if (articleJSONParam.equals(sourceParameter)) {
                            jsonReader.beginObject(); // Beginning of source object
                            while (jsonReader.hasNext()) {
                                String sourceJSONParam = jsonReader.nextName();
                                if (sourceJSONParam.equals(nameParameter)) {
                                    String source = jsonReader.nextString();
                                    news.setSource(new NewsSource(source));
                                } else {
                                    jsonReader.skipValue();
                                }
                            }
                            jsonReader.endObject(); // End of source object
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject(); // End of article object
                    newsList.add(news);
                }
                jsonReader.endArray(); // End of articles array
            }
        }
        jsonReader.endObject(); // End of JSON object

        newsApiResponse.setArticles(newsList);

        return newsApiResponse;
    }

    /**
     * Returns a list of News from a JSON file parsed using JSONObject and JSONReader classes.
     * Doc of JSONObject: <a href="https://developer.android.com/reference/org/json/JSONObject">...</a>
     * Doc of JSONArray: <a href="https://developer.android.com/reference/org/json/JSONArray">...</a>
     * @param fileName The JSON file to be parsed.
     * @return The NewsApiResponse object associated with the JSON file content.
     * @throws IOException
     * @throws JSONException
     */
    public NewsApiResponse parseJSONFileWithJSONObjectArray(String fileName)
            throws IOException, JSONException {

        InputStream inputStream = context.getAssets().open(fileName);
        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        JSONObject rootJSONObject = new JSONObject(content);

        NewsApiResponse newsApiResponse = new NewsApiResponse();
        newsApiResponse.setStatus(rootJSONObject.getString(statusParameter));
        newsApiResponse.setTotalResults(rootJSONObject.getInt(totalResultsParameter));

        JSONArray articlesJSONArray = rootJSONObject.getJSONArray(articlesParameter);

        List<News> newsList = null;
        int articlesCount = articlesJSONArray.length();

        if (articlesCount > 0) {
            newsList = new ArrayList<>();
            News news;
            for (int i = 0; i < articlesCount; i++) {
                JSONObject articleJSONObject = articlesJSONArray.getJSONObject(i);
                JSONObject sourceJSONObject = articleJSONObject.getJSONObject(sourceParameter);
                news = new News();
                news.setAuthor(articleJSONObject.getString(authorParameter));
                news.setTitle(articleJSONObject.getString(titleParameter));
                news.setDescription(articleJSONObject.getString(descriptionParameter));
                news.setUrl(articleJSONObject.getString(urlParameter));
                news.setUrlToImage(articleJSONObject.getString(urlToImageParameter));
                news.setDate(articleJSONObject.getString(publishedAtParameter));
                news.setContent(articleJSONObject.getString(contentParameter));
                news.setSource(new NewsSource(sourceJSONObject.getString(nameParameter)));
                newsList.add(news);
            }
        }
        newsApiResponse.setArticles(newsList);

        return newsApiResponse;
    }

    /**
     * Returns a list of News from a JSON file parsed using Gson.
     * Doc can be read here: <a href="https://github.com/google/gson">...</a>
     * @param fileName The JSON file to be parsed.
     * @return The NewsApiResponse object associated with the JSON file content.
     * @throws IOException
     */
    public NewsApiResponse parseJSONFileWithGSon(String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        return new Gson().fromJson(bufferedReader, NewsApiResponse.class);
    }
}
