package it.unimib.worldnews.repository;

import static it.unimib.worldnews.util.Constants.FRESH_TIMEOUT;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.model.NewsResponse;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.source.BaseNewsLocalDataSource;
import it.unimib.worldnews.source.BaseNewsRemoteDataSource;
import it.unimib.worldnews.source.NewsCallback;

/**
 * Repository class to get the news from local or from a remote source.
 */
public class NewsRepositoryWithLiveData implements INewsRepositoryWithLiveData, NewsCallback {

    private static final String TAG = NewsRepositoryWithLiveData.class.getSimpleName();

    private final MutableLiveData<Result> allNewsMutableLiveData;
    private final MutableLiveData<Result> favoriteNewsMutableLiveData;
    private final BaseNewsRemoteDataSource newsRemoteDataSource;
    private final BaseNewsLocalDataSource newsLocalDataSource;

    public NewsRepositoryWithLiveData(BaseNewsRemoteDataSource newsRemoteDataSource,
                                      BaseNewsLocalDataSource newsLocalDataSource) {

        allNewsMutableLiveData = new MutableLiveData<>();
        favoriteNewsMutableLiveData = new MutableLiveData<>();
        this.newsRemoteDataSource = newsRemoteDataSource;
        this.newsLocalDataSource = newsLocalDataSource;
        this.newsRemoteDataSource.setNewsCallback(this);
        this.newsLocalDataSource.setNewsCallback(this);
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
    public MutableLiveData<Result> getFavoriteNews() {
        newsLocalDataSource.getFavoriteNews();
        return favoriteNewsMutableLiveData;
    }

    @Override
    public void updateNews(News news) {
        newsLocalDataSource.updateNews(news);
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
            List<News> newsList = ((Result.Success)allNewsMutableLiveData.getValue()).getData().getNewsList();
            newsList.addAll(newsApiResponse.getNewsList());
            newsApiResponse.setNewsList(newsList);
            Result.Success result = new Result.Success(newsApiResponse);
            allNewsMutableLiveData.postValue(result);
        } else {
            Result.Success result = new Result.Success(newsApiResponse);
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
            List<News> oldAllNews = ((Result.Success)allNewsResult).getData().getNewsList();
            if (oldAllNews.contains(news)) {
                oldAllNews.set(oldAllNews.indexOf(news), news);
                allNewsMutableLiveData.postValue(allNewsResult);
            }
        }
        favoriteNewsMutableLiveData.postValue(new Result.Success(new NewsResponse(favoriteNews)));
    }

    @Override
    public void onNewsFavoriteStatusChanged(List<News> news) {
        favoriteNewsMutableLiveData.postValue(new Result.Success(new NewsResponse(news)));
    }

    @Override
    public void onDeleteFavoriteNewsSuccess(List<News> favoriteNews) {
        Result allNewsResult = allNewsMutableLiveData.getValue();

        if (allNewsResult != null && allNewsResult.isSuccess()) {
            List<News> oldAllNews = ((Result.Success)allNewsResult).getData().getNewsList();
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
            Result.Success result = new Result.Success(new NewsResponse(favoriteNews));
            favoriteNewsMutableLiveData.postValue(result);
        }
    }
}
