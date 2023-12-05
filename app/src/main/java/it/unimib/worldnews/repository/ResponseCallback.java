package it.unimib.worldnews.repository;

import java.util.List;

import it.unimib.worldnews.model.News;

/**
 * Interface to send data from Repositories that implement
 * INewsRepository interface to Activity/Fragment.
 */
public interface ResponseCallback {
    void onSuccess(List<News> newsList, long lastUpdate);
    void onFailure(String errorMessage);
    void onNewsFavoriteStatusChanged(News news);
}
