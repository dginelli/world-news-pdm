package it.unimib.worldnews.repository.news;

import it.unimib.worldnews.model.News;

/**
 * Interface for Repositories that manage News objects.
 */
public interface INewsRepository {

    void fetchNews(String country, int page, long lastUpdate);

    void updateNews(News news);

    void getFavoriteNews();

    void deleteFavoriteNews();
}
