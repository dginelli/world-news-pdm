package it.unimib.worldnews.source;

import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;

/**
 * Interface to send data from DataSource to Repositories
 * that implement INewsRepositoryWithLiveData interface.
 */
public interface NewsCallback {
    void onSuccessFromRemote(NewsApiResponse newsApiResponse, long lastUpdate);
    void onFailureFromRemote(Exception exception);
    void onSuccessFromLocal(NewsApiResponse newsApiResponse);
    void onFailureFromLocal(Exception exception);
    void onNewsFavoriteStatusChanged(News news, List<News> favoriteNews);
    void onNewsFavoriteStatusChanged(List<News> news);
    void onDeleteFavoriteNewsSuccess(List<News> favoriteNews);
}
