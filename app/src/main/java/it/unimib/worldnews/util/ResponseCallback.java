package it.unimib.worldnews.util;

import java.util.List;

import it.unimib.worldnews.model.News;

/**
 * Interface to send data from Repositories to Activity/Fragment.
 */
public interface ResponseCallback {
    void onSuccess(List<News> newsList, long lastUpdate);
    void onFailure(String errorMessage);
    void onNewsFavoriteStatusChanged(News news);
}
