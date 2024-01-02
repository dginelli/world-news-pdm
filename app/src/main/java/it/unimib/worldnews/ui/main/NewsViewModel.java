package it.unimib.worldnews.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.data.repository.news.INewsRepositoryWithLiveData;

/**
 * ViewModel to manage the list of News and the list of favorite News.
 */
public class NewsViewModel extends ViewModel {

    private static final String TAG = NewsViewModel.class.getSimpleName();

    private final INewsRepositoryWithLiveData newsRepositoryWithLiveData;
    private int page;
    private int currentResults;
    private int totalResults;
    private boolean isLoading;
    private boolean firstLoading;
    private MutableLiveData<Result> newsListLiveData;
    private MutableLiveData<Result> favoriteNewsListLiveData;

    public NewsViewModel(INewsRepositoryWithLiveData iNewsRepositoryWithLiveData) {
        this.newsRepositoryWithLiveData = iNewsRepositoryWithLiveData;
        this.page = 1;
        this.totalResults = 0;
        this.firstLoading = true;
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
    public MutableLiveData<Result> getFavoriteNewsLiveData(boolean isFirstLoading) {
        if (favoriteNewsListLiveData == null) {
            getFavoriteNews(isFirstLoading);
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

    public void fetchNews(String country) {
        newsRepositoryWithLiveData.fetchNews(country, page);
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
    private void getFavoriteNews(boolean firstLoading) {
        favoriteNewsListLiveData = newsRepositoryWithLiveData.getFavoriteNews(firstLoading);
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getCurrentResults() {
        return currentResults;
    }

    public void setCurrentResults(int currentResults) {
        this.currentResults = currentResults;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isFirstLoading() {
        return firstLoading;
    }

    public void setFirstLoading(boolean firstLoading) {
        this.firstLoading = firstLoading;
    }

    public MutableLiveData<Result> getNewsResponseLiveData() {
        return newsListLiveData;
    }
}
