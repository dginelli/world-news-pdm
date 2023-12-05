package it.unimib.worldnews.util;

import android.app.Application;

import it.unimib.worldnews.R;
import it.unimib.worldnews.database.NewsRoomDatabase;
import it.unimib.worldnews.repository.INewsRepositoryWithLiveData;
import it.unimib.worldnews.repository.NewsRepositoryWithLiveData;
import it.unimib.worldnews.service.NewsApiService;
import it.unimib.worldnews.source.BaseNewsLocalDataSource;
import it.unimib.worldnews.source.BaseNewsRemoteDataSource;
import it.unimib.worldnews.source.NewsLocalDataSource;
import it.unimib.worldnews.source.NewsMockRemoteDataSource;
import it.unimib.worldnews.source.NewsRemoteDataSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  Registry to provide the dependencies for the classes
 *  used in the application.
 */
public class ServiceLocator {

    private static volatile ServiceLocator INSTANCE = null;

    private ServiceLocator() {}

    /**
     * Returns an instance of ServiceLocator class.
     * @return An instance of ServiceLocator.
     */
    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized(ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Returns an instance of NewsApiService class using Retrofit.
     * @return an instance of NewsApiService.
     */
    public NewsApiService getNewsApiService() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constants.NEWS_API_BASE_URL).
                addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit.create(NewsApiService.class);
    }

    /**
     * Returns an instance of NewsRoomDatabase class to manage Room database.
     * @param application Param for accessing the global application state.
     * @return An instance of NewsRoomDatabase.
     */
    public NewsRoomDatabase getNewsDao(Application application) {
        return NewsRoomDatabase.getDatabase(application);
    }

    /**
     * Returns an instance of INewsRepositoryWithLiveData.
     * @param application Param for accessing the global application state.
     * @param debugMode Param to establish if the application is run in debug mode.
     * @return An instance of INewsRepositoryWithLiveData.
     */
    public INewsRepositoryWithLiveData getNewsRepository(Application application, boolean debugMode) {
        BaseNewsRemoteDataSource newsRemoteDataSource;
        BaseNewsLocalDataSource newsLocalDataSource;
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(application);

        if (debugMode) {
            JSONParserUtil jsonParserUtil = new JSONParserUtil(application);
            newsRemoteDataSource =
                    new NewsMockRemoteDataSource(jsonParserUtil, JSONParserUtil.JsonParserType.GSON);
        } else {
            newsRemoteDataSource =
                    new NewsRemoteDataSource(application.getString(R.string.news_api_key));
        }

        newsLocalDataSource = new NewsLocalDataSource(getNewsDao(application), sharedPreferencesUtil);

        return new NewsRepositoryWithLiveData(newsRemoteDataSource, newsLocalDataSource);
    }
}
