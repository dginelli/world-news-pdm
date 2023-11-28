package it.unimib.worldnews.repository;

import it.unimib.worldnews.model.News;

/**
 * Interface for Repositories that manage News objects.
 */
public interface INewsRepository {

    enum JsonParserType {
        JSON_READER,
        JSON_OBJECT_ARRAY,
        GSON,
        JSON_ERROR
    };

    void fetchNews(String country, int page, long lastUpdate);

    void updateNews(News news);

    void getFavoriteNews();

    void deleteFavoriteNews();
}
