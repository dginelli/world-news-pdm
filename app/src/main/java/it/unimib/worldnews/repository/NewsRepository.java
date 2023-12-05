package it.unimib.worldnews.repository;

import static it.unimib.worldnews.util.Constants.FRESH_TIMEOUT;
import static it.unimib.worldnews.util.Constants.TOP_HEADLINES_PAGE_SIZE_VALUE;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.database.NewsDao;
import it.unimib.worldnews.database.NewsRoomDatabase;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.service.NewsApiService;
import it.unimib.worldnews.util.ServiceLocator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository to get the news using the API
 * provided by NewsApi.org (https://newsapi.org).
 */
public class NewsRepository implements INewsRepository {

    private static final String TAG = NewsRepository.class.getSimpleName();

    private final Application application;
    private final NewsApiService newsApiService;
    private final NewsDao newsDao;
    private final ResponseCallback responseCallback;

    public NewsRepository(Application application, ResponseCallback responseCallback) {
        this.application = application;
        this.newsApiService = ServiceLocator.getInstance().getNewsApiService();
        NewsRoomDatabase newsRoomDatabase = ServiceLocator.getInstance().getNewsDao(application);
        this.newsDao = newsRoomDatabase.newsDao();
        this.responseCallback = responseCallback;
    }

    @Override
    public void fetchNews(String country, int page, long lastUpdate) {

        long currentTime = System.currentTimeMillis();

        // It gets the news from the Web Service if the last download
        // of the news has been performed more than FRESH_TIMEOUT value ago
        if (currentTime - lastUpdate > FRESH_TIMEOUT) {
            Call<NewsApiResponse> newsResponseCall = newsApiService.getNews(country,
                    TOP_HEADLINES_PAGE_SIZE_VALUE, application.getString(R.string.news_api_key));

            newsResponseCall.enqueue(new Callback<NewsApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<NewsApiResponse> call,
                                       @NonNull Response<NewsApiResponse> response) {

                    if (response.body() != null && response.isSuccessful() &&
                            !response.body().getStatus().equals("error")) {
                        List<News> newsList = response.body().getNewsList();
                        saveDataInDatabase(newsList);
                    } else {
                        responseCallback.onFailure(application.getString(R.string.error_retrieving_news));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NewsApiResponse> call, @NonNull Throwable t) {
                    responseCallback.onFailure(t.getMessage());
                }
            });
        } else {
            Log.d(TAG, application.getString(R.string.data_read_from_local_database));
            readDataFromDatabase(lastUpdate);
        }
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
            responseCallback.onSuccess(newsDao.getFavoriteNews(), System.currentTimeMillis());
        });
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
            responseCallback.onNewsFavoriteStatusChanged(news);
        });
    }

    /**
     * Gets the list of favorite news from the local database.
     */
    @Override
    public void getFavoriteNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            responseCallback.onSuccess(newsDao.getFavoriteNews(), System.currentTimeMillis());
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

            responseCallback.onSuccess(newsList, System.currentTimeMillis());
        });
    }

    /**
     * Gets the news from the local database.
     * The method is executed with an ExecutorService defined in NewsRoomDatabase class
     * because the database access cannot been executed in the main thread.
     */
    private void readDataFromDatabase(long lastUpdate) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            responseCallback.onSuccess(newsDao.getAll(), lastUpdate);
        });
    }
}
