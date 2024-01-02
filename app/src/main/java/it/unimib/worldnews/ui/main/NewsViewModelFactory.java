package it.unimib.worldnews.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.worldnews.repository.news.INewsRepositoryWithLiveData;

/**
 * Custom ViewModelProvider to be able to have a custom constructor
 * for the NewsViewModel class.
 */
public class NewsViewModelFactory implements ViewModelProvider.Factory {

    private final INewsRepositoryWithLiveData iNewsRepositoryWithLiveData;

    public NewsViewModelFactory(INewsRepositoryWithLiveData iNewsRepositoryWithLiveData) {
        this.iNewsRepositoryWithLiveData = iNewsRepositoryWithLiveData;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NewsViewModel(iNewsRepositoryWithLiveData);
    }
}
