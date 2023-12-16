package it.unimib.worldnews.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.Result;

public interface INewsRepositoryWithLiveData {

    MutableLiveData<Result> fetchNews(String country, int page, long lastUpdate);

    void fetchNews(String country, int page);

    MutableLiveData<Result> getFavoriteNews();

    void updateNews(News news);

    void deleteFavoriteNews();
}
