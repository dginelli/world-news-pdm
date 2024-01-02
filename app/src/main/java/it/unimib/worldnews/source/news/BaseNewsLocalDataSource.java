package it.unimib.worldnews.source.news;

import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;

/**
 * Base class to get news from a local source.
 */
public abstract class BaseNewsLocalDataSource {

    protected NewsCallback newsCallback;

    public void setNewsCallback(NewsCallback newsCallback) {
        this.newsCallback = newsCallback;
    }

    public abstract void getNews();
    public abstract void getFavoriteNews();
    public abstract void updateNews(News news);
    public abstract void deleteFavoriteNews();
    public abstract void insertNews(NewsApiResponse newsApiResponse);
    public abstract void insertNews(List<News> newsList);
    public abstract void deleteAll();
}
