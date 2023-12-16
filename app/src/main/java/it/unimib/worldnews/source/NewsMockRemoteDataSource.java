package it.unimib.worldnews.source;

import static it.unimib.worldnews.util.Constants.API_KEY_ERROR;
import static it.unimib.worldnews.util.Constants.NEWS_API_TEST_JSON_FILE;
import static it.unimib.worldnews.util.Constants.UNEXPECTED_ERROR;

import org.json.JSONException;

import java.io.IOException;

import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.util.JSONParserUtil;

/**
 * Class to get the news from a local JSON file to simulate the Web Service response.
 */
public class NewsMockRemoteDataSource extends BaseNewsRemoteDataSource {

    private final JSONParserUtil jsonParserUtil;
    private final JSONParserUtil.JsonParserType jsonParserType;

    public NewsMockRemoteDataSource(JSONParserUtil jsonParserUtil,
                                    JSONParserUtil.JsonParserType jsonParserType) {

        this.jsonParserUtil = jsonParserUtil;
        this.jsonParserType = jsonParserType;
    }

    @Override
    public void getNews(String country, int page) {
        NewsApiResponse newsApiResponse = null;

        switch (jsonParserType) {
            case JSON_READER:
                try {
                    newsApiResponse =
                            jsonParserUtil.parseJSONFileWithJsonReader(NEWS_API_TEST_JSON_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case JSON_OBJECT_ARRAY:
                try {
                    newsApiResponse = jsonParserUtil.parseJSONFileWithJSONObjectArray(NEWS_API_TEST_JSON_FILE);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            case GSON:
                try {
                    newsApiResponse = jsonParserUtil.parseJSONFileWithGSon(NEWS_API_TEST_JSON_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case JSON_ERROR:
                newsCallback.onFailureFromRemote(new Exception(UNEXPECTED_ERROR));
                break;
        }

        if (newsApiResponse != null) {
            newsCallback.onSuccessFromRemote(newsApiResponse, System.currentTimeMillis());
        } else {
            newsCallback.onFailureFromRemote(new Exception(API_KEY_ERROR));
        }
    }
}
