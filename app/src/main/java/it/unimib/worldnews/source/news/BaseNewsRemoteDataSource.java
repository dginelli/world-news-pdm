package it.unimib.worldnews.source.news;

/**
 * Base class to get news from a remote source.
 */
public abstract class BaseNewsRemoteDataSource {
    protected NewsCallback newsCallback;

    public void setNewsCallback(NewsCallback newsCallback) {
        this.newsCallback = newsCallback;
    }

    public abstract void getNews(String country, int page);
}
