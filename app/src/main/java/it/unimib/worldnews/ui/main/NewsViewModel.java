package it.unimib.worldnews.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.repository.INewsRepositoryWithLiveData;

/**
 * ViewModel to manage the list of News and the list of favorite News.
 */
public class NewsViewModel extends ViewModel {

    private static final String TAG = NewsViewModel.class.getSimpleName();

    private final INewsRepositoryWithLiveData newsRepositoryWithLiveData;
    private final int page;
    private MutableLiveData<Result> newsListLiveData;
    private MutableLiveData<Result> favoriteNewsListLiveData;

    public NewsViewModel(INewsRepositoryWithLiveData iNewsRepositoryWithLiveData) {
        this.newsRepositoryWithLiveData = iNewsRepositoryWithLiveData;
        this.page = 1;
    }

    /**
     * Returns the LiveData object associated with the
     * news list to the Fragment/Activity.
     * @return The LiveData object associated with the news list.
     */
    public MutableLiveData<Result> getNews(String country, long lastUpdate) {
        if (newsListLiveData == null) {
            fetchNews(country, lastUpdate);
        }
        return newsListLiveData;
    }

    /**
     * Returns the LiveData object associated with the
     * list of favorite news to the Fragment/Activity.
     * @return The LiveData object associated with the list of favorite news.
     */
    public MutableLiveData<Result> getFavoriteNewsLiveData() {
        if (favoriteNewsListLiveData == null) {
            getFavoriteNews();
        }
        return favoriteNewsListLiveData;
    }

    /**
     * Updates the news status.
     * @param news The news to be updated.
     */
    public void updateNews(News news) {
        newsRepositoryWithLiveData.updateNews(news);
    }

    /**
     * It uses the Repository to download the news list
     * and to associate it with the LiveData object.
     */
    private void fetchNews(String country, long lastUpdate) {
        newsListLiveData = newsRepositoryWithLiveData.fetchNews(country, page, lastUpdate);
    }

    /**
     * It uses the Repository to get the list of favorite news
     * and to associate it with the LiveData object.
     */
    private void getFavoriteNews() {
        favoriteNewsListLiveData = newsRepositoryWithLiveData.getFavoriteNews();
    }

    /**
     * Removes the news from the list of favorite news.
     * @param news The news to be removed from the list of favorite news.
     */
    public void removeFromFavorite(News news) {
        newsRepositoryWithLiveData.updateNews(news);
    }

    /**
     * Clears the list of favorite news.
     */
    public void deleteAllFavoriteNews() {
        newsRepositoryWithLiveData.deleteFavoriteNews();
    }
}
