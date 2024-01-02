package it.unimib.worldnews.repository.news;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.Result;

public interface INewsRepositoryWithLiveData {

    MutableLiveData<Result> fetchNews(String country, int page, long lastUpdate);

    void fetchNews(String country, int page);

    MutableLiveData<Result> getFavoriteNews(boolean firstLoading);

    void updateNews(News news);

    void deleteFavoriteNews();
}
