package it.unimib.worldnews.data.source.news;

import java.util.List;

import it.unimib.worldnews.model.News;

/**
 * Base class to get the user favorite news from a remote source.
 */
public abstract class BaseFavoriteNewsDataSource {

    protected NewsCallback newsCallback;

    public void setNewsCallback(NewsCallback newsCallback) {
        this.newsCallback = newsCallback;
    }

    public abstract void getFavoriteNews();
    public abstract void addFavoriteNews(News news);
    public abstract void synchronizeFavoriteNews(List<News> notSynchronizedNewsList);
    public abstract void deleteFavoriteNews(News news);
    public abstract void deleteAllFavoriteNews();
}
