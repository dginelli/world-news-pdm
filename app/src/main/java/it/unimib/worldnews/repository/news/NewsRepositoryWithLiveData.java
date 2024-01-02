package it.unimib.worldnews.repository.news;

import static it.unimib.worldnews.util.Constants.FRESH_TIMEOUT;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.model.NewsResponse;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.source.news.BaseFavoriteNewsDataSource;
import it.unimib.worldnews.source.news.BaseNewsLocalDataSource;
import it.unimib.worldnews.source.news.BaseNewsRemoteDataSource;
import it.unimib.worldnews.source.news.NewsCallback;

/**
 * Repository class to get the news from local or from a remote source.
 */
public class NewsRepositoryWithLiveData implements INewsRepositoryWithLiveData, NewsCallback {

    private static final String TAG = NewsRepositoryWithLiveData.class.getSimpleName();

    private final MutableLiveData<Result> allNewsMutableLiveData;
    private final MutableLiveData<Result> favoriteNewsMutableLiveData;
    private final BaseNewsRemoteDataSource newsRemoteDataSource;
    private final BaseNewsLocalDataSource newsLocalDataSource;
    private final BaseFavoriteNewsDataSource backupDataSource;

    public NewsRepositoryWithLiveData(BaseNewsRemoteDataSource newsRemoteDataSource,
                                      BaseNewsLocalDataSource newsLocalDataSource,
                                      BaseFavoriteNewsDataSource favoriteNewsDataSource) {

        allNewsMutableLiveData = new MutableLiveData<>();
        favoriteNewsMutableLiveData = new MutableLiveData<>();
        this.newsRemoteDataSource = newsRemoteDataSource;
        this.newsLocalDataSource = newsLocalDataSource;
        this.backupDataSource = favoriteNewsDataSource;
        this.newsRemoteDataSource.setNewsCallback(this);
        this.newsLocalDataSource.setNewsCallback(this);
        this.backupDataSource.setNewsCallback(this);
    }

    @Override
    public MutableLiveData<Result> fetchNews(String country, int page, long lastUpdate) {
        long currentTime = System.currentTimeMillis();

        // It gets the news from the Web Service if the last download
        // of the news has been performed more than FRESH_TIMEOUT value ago
        if (currentTime - lastUpdate > FRESH_TIMEOUT) {
            newsRemoteDataSource.getNews(country, page);
        } else {
            newsLocalDataSource.getNews();
        }
        return allNewsMutableLiveData;
    }

    public void fetchNews(String country, int page) {
        newsRemoteDataSource.getNews(country, page);
    }

    @Override
    public MutableLiveData<Result> getFavoriteNews(boolean isFirstLoading) {
        // The first time the user launches the app, check if she
        // has previously saved favorite news on the cloud
        if (isFirstLoading) {
            backupDataSource.getFavoriteNews();
        } else {
            newsLocalDataSource.getFavoriteNews();
        }
        return favoriteNewsMutableLiveData;
    }

    @Override
    public void updateNews(News news) {
        newsLocalDataSource.updateNews(news);
        if (news.isFavorite()) {
            backupDataSource.addFavoriteNews(news);
        } else {
            backupDataSource.deleteFavoriteNews(news);
        }
    }

    @Override
    public void deleteFavoriteNews() {
        newsLocalDataSource.deleteFavoriteNews();
    }

    @Override
    public void onSuccessFromRemote(NewsApiResponse newsApiResponse, long lastUpdate) {
        newsLocalDataSource.insertNews(newsApiResponse);
    }

    @Override
    public void onFailureFromRemote(Exception exception) {
        Result.Error result = new Result.Error(exception.getMessage());
        allNewsMutableLiveData.postValue(result);
    }

    @Override
    public void onSuccessFromLocal(NewsApiResponse newsApiResponse) {
        if (allNewsMutableLiveData.getValue() != null && allNewsMutableLiveData.getValue().isSuccess()) {
            List<News> newsList = ((Result.NewsResponseSuccess)allNewsMutableLiveData.getValue()).getData().getNewsList();
            newsList.addAll(newsApiResponse.getNewsList());
            newsApiResponse.setNewsList(newsList);
            Result.NewsResponseSuccess result = new Result.NewsResponseSuccess(newsApiResponse);
            allNewsMutableLiveData.postValue(result);
        } else {
            Result.NewsResponseSuccess result = new Result.NewsResponseSuccess(newsApiResponse);
            allNewsMutableLiveData.postValue(result);
        }
    }

    @Override
    public void onFailureFromLocal(Exception exception) {
        Result.Error resultError = new Result.Error(exception.getMessage());
        allNewsMutableLiveData.postValue(resultError);
        favoriteNewsMutableLiveData.postValue(resultError);
    }

    @Override
    public void onNewsFavoriteStatusChanged(News news, List<News> favoriteNews) {
        Result allNewsResult = allNewsMutableLiveData.getValue();

        if (allNewsResult != null && allNewsResult.isSuccess()) {
            List<News> oldAllNews = ((Result.NewsResponseSuccess)allNewsResult).getData().getNewsList();
            if (oldAllNews.contains(news)) {
                oldAllNews.set(oldAllNews.indexOf(news), news);
                allNewsMutableLiveData.postValue(allNewsResult);
            }
        }
        favoriteNewsMutableLiveData.postValue(new Result.NewsResponseSuccess(new NewsResponse(favoriteNews)));
    }

    @Override
    public void onNewsFavoriteStatusChanged(List<News> newsList) {

        List<News> notSynchronizedNewsList = new ArrayList<>();

        for (News news : newsList) {
            if (!news.isSynchronized()) {
                notSynchronizedNewsList.add(news);
            }
        }

        if (!notSynchronizedNewsList.isEmpty()) {
            backupDataSource.synchronizeFavoriteNews(notSynchronizedNewsList);
        }

        favoriteNewsMutableLiveData.postValue(new Result.NewsResponseSuccess(new NewsResponse(newsList)));
    }

    @Override
    public void onDeleteFavoriteNewsSuccess(List<News> favoriteNews) {
        Result allNewsResult = allNewsMutableLiveData.getValue();

        if (allNewsResult != null && allNewsResult.isSuccess()) {
            List<News> oldAllNews = ((Result.NewsResponseSuccess)allNewsResult).getData().getNewsList();
            for (News news : favoriteNews) {
                if (oldAllNews.contains(news)) {
                    oldAllNews.set(oldAllNews.indexOf(news), news);
                }
            }
            allNewsMutableLiveData.postValue(allNewsResult);
        }

        if (favoriteNewsMutableLiveData.getValue() != null &&
                favoriteNewsMutableLiveData.getValue().isSuccess()) {
            favoriteNews.clear();
            Result.NewsResponseSuccess result = new Result.NewsResponseSuccess(new NewsResponse(favoriteNews));
            favoriteNewsMutableLiveData.postValue(result);
        }

        backupDataSource.deleteAllFavoriteNews();
    }

    @Override
    public void onSuccessFromCloudReading(List<News> newsList) {
        // Favorite news got from Realtime Database the first time
        if (newsList != null) {
            for (News news : newsList) {
                news.setSynchronized(true);
            }
            newsLocalDataSource.insertNews(newsList);
            favoriteNewsMutableLiveData.postValue(new Result.NewsResponseSuccess(new NewsResponse(newsList)));
        }
    }

    @Override
    public void onSuccessFromCloudWriting(News news) {
        if (news != null && !news.isFavorite()) {
            news.setSynchronized(false);
        }
        newsLocalDataSource.updateNews(news);
        backupDataSource.getFavoriteNews();
    }

    @Override
    public void onSuccessSynchronization() {
        Log.d(TAG, "News synchronized from remote");
    }

    @Override
    public void onFailureFromCloud(Exception exception) {
    }

    @Override
    public void onSuccessDeletion() {

    }
}
