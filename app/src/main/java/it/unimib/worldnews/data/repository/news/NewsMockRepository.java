package it.unimib.worldnews.data.repository.news;

import static it.unimib.worldnews.util.Constants.NEWS_API_TEST_JSON_FILE;

import android.app.Application;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import it.unimib.worldnews.R;

import it.unimib.worldnews.data.database.NewsDao;
import it.unimib.worldnews.data.database.NewsRoomDatabase;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.util.JSONParserUtil;
import it.unimib.worldnews.util.ServiceLocator;

/**
 * Mock Repository that gets the news from the local JSON file newsapi-test.json,
 * that is saved in "assets" folder.
 */
public class NewsMockRepository implements INewsRepository {

    private final Application application;
    private final NewsResponseCallback newsResponseCallback;
    private final NewsDao newsDao;
    private final JSONParserUtil.JsonParserType jsonParserType;

    public NewsMockRepository(Application application, NewsResponseCallback newsResponseCallback,
                              JSONParserUtil.JsonParserType jsonParserType) {
        this.application = application;
        this.newsResponseCallback = newsResponseCallback;
        NewsRoomDatabase newsRoomDatabase = ServiceLocator.getInstance().getNewsDao(application);
        this.newsDao = newsRoomDatabase.newsDao();
        this.jsonParserType = jsonParserType;
    }

    @Override
    public void fetchNews(String country, int page, long lastUpdate) {

        NewsApiResponse newsApiResponse = null;
        JSONParserUtil jsonParserUtil = new JSONParserUtil(application);

        switch (jsonParserType) {
            case JSON_READER:
                try {
                    newsApiResponse = jsonParserUtil.parseJSONFileWithJsonReader(NEWS_API_TEST_JSON_FILE);
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
                newsResponseCallback.onFailure(application.getString(R.string.error_retrieving_news));
                break;
        }

        if (newsApiResponse != null) {
            saveDataInDatabase(newsApiResponse.getNewsList());
        } else {
            newsResponseCallback.onFailure(application.getString(R.string.error_retrieving_news));
        }
    }

    /**
     * Update the news changing the status of "favorite"
     * in the local database.
     * @param news The news to be updated.
     */
    @Override
    public void updateNews(News news) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            newsDao.updateSingleFavoriteNews(news);
            newsResponseCallback.onNewsFavoriteStatusChanged(news);
        });
    }

    /**
     * Gets the list of favorite news from the local database.
     */
    @Override
    public void getFavoriteNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            newsResponseCallback.onSuccess(newsDao.getFavoriteNews(), System.currentTimeMillis());
        });
    }

    /**
     * Marks the favorite news as not favorite.
     */
    @Override
    public void deleteFavoriteNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<News> favoriteNews = newsDao.getFavoriteNews();
            for (News news : favoriteNews) {
                news.setFavorite(false);
            }
            newsDao.updateListFavoriteNews(favoriteNews);
            newsResponseCallback.onSuccess(newsDao.getFavoriteNews(), System.currentTimeMillis());
        });
    }

    /**
     * Saves the news in the local database.
     * The method is executed with an ExecutorService defined in NewsRoomDatabase class
     * because the database access cannot been executed in the main thread.
     * @param newsList the list of news to be written in the local database.
     */
    private void saveDataInDatabase(List<News> newsList) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            // Reads the news from the database
            List<News> allNews = newsDao.getAll();

            // Checks if the news just downloaded has already been downloaded earlier
            // in order to preserve the news status (marked as favorite or not)
            for (News news : allNews) {
                // This check works because News and NewsSource classes have their own
                // implementation of equals(Object) and hashCode() methods
                if (newsList.contains(news)) {
                    // The primary key and the favorite status is contained only in the News objects
                    // retrieved from the database, and not in the News objects downloaded from the
                    // Web Service. If the same news was already downloaded earlier, the following
                    // line of code replaces the the News object in newsList with the corresponding
                    // News object saved in the database, so that it has the primary key and the
                    // favorite status.
                    newsList.set(newsList.indexOf(news), news);
                }
            }

            // Writes the news in the database and gets the associated primary keys
            List<Long> insertedNewsIds = newsDao.insertNewsList(newsList);
            for (int i = 0; i < newsList.size(); i++) {
                // Adds the primary key to the corresponding object News just downloaded so that
                // if the user marks the news as favorite (and vice-versa), we can use its id
                // to know which news in the database must be marked as favorite/not favorite
                newsList.get(i).setId(insertedNewsIds.get(i));
            }

            newsResponseCallback.onSuccess(newsList, System.currentTimeMillis());
        });
    }

    /**
     * Gets the news from the local database.
     * The method is executed with an ExecutorService defined in NewsRoomDatabase class
     * because the database access cannot been executed in the main thread.
     */
    private void readDataFromDatabase(long lastUpdate) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            newsResponseCallback.onSuccess(newsDao.getAll(), lastUpdate);
        });
    }
}
