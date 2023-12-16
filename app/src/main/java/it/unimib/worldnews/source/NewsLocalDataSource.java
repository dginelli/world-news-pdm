package it.unimib.worldnews.source;

import static it.unimib.worldnews.util.Constants.LAST_UPDATE;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.UNEXPECTED_ERROR;

import java.util.List;

import it.unimib.worldnews.database.NewsDao;
import it.unimib.worldnews.database.NewsRoomDatabase;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.util.SharedPreferencesUtil;

/**
 * Class to get news from local database using Room.
 */
public class NewsLocalDataSource extends BaseNewsLocalDataSource {

    private final NewsDao newsDao;
    private final SharedPreferencesUtil sharedPreferencesUtil;

    public NewsLocalDataSource(NewsRoomDatabase newsRoomDatabase,
                               SharedPreferencesUtil sharedPreferencesUtil) {
        this.newsDao = newsRoomDatabase.newsDao();
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    /**
     * Gets the news from the local database.
     * The method is executed with an ExecutorService defined in NewsRoomDatabase class
     * because the database access cannot been executed in the main thread.
     */
    @Override
    public void getNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            //TODO Fix this instruction
            NewsApiResponse newsApiResponse = new NewsApiResponse();
            newsApiResponse.setNewsList(newsDao.getAll());
            newsCallback.onSuccessFromLocal(newsApiResponse);
        });
    }

    @Override
    public void getFavoriteNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<News> favoriteNews = newsDao.getFavoriteNews();
            newsCallback.onNewsFavoriteStatusChanged(favoriteNews);
        });
    }

    @Override
    public void updateNews(News news) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            int rowUpdatedCounter = newsDao.updateSingleFavoriteNews(news);

            // It means that the update succeeded because only one row had to be updated
            if (rowUpdatedCounter == 1) {
                News updatedNews = newsDao.getNews(news.getId());
                newsCallback.onNewsFavoriteStatusChanged(updatedNews, newsDao.getFavoriteNews());
            } else {
                newsCallback.onFailureFromLocal(new Exception(UNEXPECTED_ERROR));
            }
        });
    }

    @Override
    public void deleteFavoriteNews() {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<News> favoriteNews = newsDao.getFavoriteNews();
            for (News news : favoriteNews) {
                news.setFavorite(false);
            }
            int updatedRowsNumber = newsDao.updateListFavoriteNews(favoriteNews);

            // It means that the update succeeded because the number of updated rows is
            // equal to the number of the original favorite news
            if (updatedRowsNumber == favoriteNews.size()) {
                newsCallback.onDeleteFavoriteNewsSuccess(favoriteNews);
            } else {
                newsCallback.onFailureFromLocal(new Exception(UNEXPECTED_ERROR));
            }
        });
    }

    /**
     * Saves the news in the local database.
     * The method is executed with an ExecutorService defined in NewsRoomDatabase class
     * because the database access cannot been executed in the main thread.
     * @param newsApiResponse the list of news to be written in the local database.
     */
    @Override
    public void insertNews(NewsApiResponse newsApiResponse) {
        NewsRoomDatabase.databaseWriteExecutor.execute(() -> {
            // Reads the news from the database
            List<News> allNews = newsDao.getAll();
            List<News> newsList = newsApiResponse.getNewsList();

            if (newsList != null) {

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

                sharedPreferencesUtil.writeStringData(SHARED_PREFERENCES_FILE_NAME,
                        LAST_UPDATE, String.valueOf(System.currentTimeMillis()));

                newsCallback.onSuccessFromLocal(newsApiResponse);
            }
        });
    }
}
