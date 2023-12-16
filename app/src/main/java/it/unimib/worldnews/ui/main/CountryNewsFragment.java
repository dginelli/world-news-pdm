package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.util.Constants.LAST_UPDATE;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_COUNTRY_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;
import static it.unimib.worldnews.util.Constants.TOP_HEADLINES_PAGE_SIZE_VALUE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.adapter.NewsRecyclerViewAdapter;
import it.unimib.worldnews.databinding.FragmentCountryNewsBinding;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.NewsApiResponse;
import it.unimib.worldnews.model.NewsResponse;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.repository.INewsRepositoryWithLiveData;
import it.unimib.worldnews.util.ErrorMessagesUtil;
import it.unimib.worldnews.util.ServiceLocator;
import it.unimib.worldnews.util.SharedPreferencesUtil;

/**
 * Fragment that shows the news associated with a Country.
 */
public class CountryNewsFragment extends Fragment {

    private static final String TAG = CountryNewsFragment.class.getSimpleName();

    private FragmentCountryNewsBinding fragmentCountryNewsBinding;

    private List<News> newsList;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private NewsViewModel newsViewModel;
    private SharedPreferencesUtil sharedPreferencesUtil;

    private int totalItemCount; // Total number of news
    private int lastVisibleItem; // The position of the last visible news item
    private int visibleItemCount; // Number or total visible news items

    // Based on this value, the process of loading more news is anticipated or postponed
    // Look at the if condition at line 237 to see how it is used
    private final int threshold = 1;

    public CountryNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment CountryNewsFragment.
     */
    public static CountryNewsFragment newInstance() {
        return new CountryNewsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferencesUtil = new SharedPreferencesUtil(requireActivity().getApplication());

        INewsRepositoryWithLiveData newsRepositoryWithLiveData =
            ServiceLocator.getInstance().getNewsRepository(
                requireActivity().getApplication(),
                requireActivity().getApplication().getResources().getBoolean(R.bool.debug_mode)
            );

        // This is the way to create a ViewModel with custom parameters
        // (see NewsViewModelFactory class for the implementation details)
        newsViewModel = new ViewModelProvider(
                requireActivity(),
                new NewsViewModelFactory(newsRepositoryWithLiveData)).get(NewsViewModel.class);

        newsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        fragmentCountryNewsBinding = FragmentCountryNewsBinding.inflate(inflater, container, false);
        return fragmentCountryNewsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String country = sharedPreferencesUtil.readStringData(
            SHARED_PREFERENCES_FILE_NAME, SHARED_PREFERENCES_COUNTRY_OF_INTEREST
        );

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });

        RecyclerView recyclerViewCountryNews = view.findViewById(R.id.recyclerview_country_news);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false);

        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(newsList,
                requireActivity().getApplication(),
                new NewsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onNewsItemClick(News news) {
                CountryNewsFragmentDirections.ActionCountryNewsFragmentToNewsDetailFragment action =
                        CountryNewsFragmentDirections.actionCountryNewsFragmentToNewsDetailFragment(news);
                Navigation.findNavController(view).navigate(action);
            }

            @Override
            public void onFavoriteButtonPressed(int position) {
                newsList.get(position).setFavorite(!newsList.get(position).isFavorite());
                newsViewModel.updateNews(newsList.get(position));
            }
        });
        recyclerViewCountryNews.setLayoutManager(layoutManager);
        recyclerViewCountryNews.setAdapter(newsRecyclerViewAdapter);

        String lastUpdate = "0";
        if (sharedPreferencesUtil.readStringData(
                        SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE) != null) {
            lastUpdate = sharedPreferencesUtil.readStringData(
                    SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE);
        }

        fragmentCountryNewsBinding.progressBar.setVisibility(View.VISIBLE);

        // Observe the LiveData associated with the MutableLiveData containing all the news
        // returned by the method getNews(String, long) of NewsViewModel class.
        // Pay attention to which LifecycleOwner you give as value to
        // the method observe(LifecycleOwner, Observer).
        // In this case, getViewLifecycleOwner() refers to
        // androidx.fragment.app.FragmentViewLifecycleOwner and not to the Fragment itself.
        // You can read more details here: https://stackoverflow.com/a/58663143/4255576
        newsViewModel.getNews(country, Long.parseLong(lastUpdate)).observe(getViewLifecycleOwner(),
            result -> {
                if (result.isSuccess()) {

                    NewsResponse newsResponse = ((Result.Success) result).getData();
                    List<News> fetchedNews = newsResponse.getNewsList();

                    if (!newsViewModel.isLoading()) {
                        if (newsViewModel.isFirstLoading()) {
                            newsViewModel.setTotalResults(((NewsApiResponse) newsResponse).getTotalResults());
                            newsViewModel.setFirstLoading(false);
                            this.newsList.addAll(fetchedNews);
                            newsRecyclerViewAdapter.notifyItemRangeInserted(0,
                                    this.newsList.size());
                        } else {
                            // Updates related to the favorite status of the news
                            newsList.clear();
                            newsList.addAll(fetchedNews);
                            newsRecyclerViewAdapter.notifyItemChanged(0, fetchedNews.size());
                        }
                        fragmentCountryNewsBinding.progressBar.setVisibility(View.GONE);
                    } else {
                        newsViewModel.setLoading(false);
                        newsViewModel.setCurrentResults(newsList.size());

                        int initialSize = newsList.size();

                        for (int i = 0; i < newsList.size(); i++) {
                            if (newsList.get(i) == null) {
                                newsList.remove(newsList.get(i));
                            }
                        }
                        int startIndex = (newsViewModel.getPage()*TOP_HEADLINES_PAGE_SIZE_VALUE) -
                                                                    TOP_HEADLINES_PAGE_SIZE_VALUE;
                        for (int i = startIndex; i < fetchedNews.size(); i++) {
                            newsList.add(fetchedNews.get(i));
                        }
                        newsRecyclerViewAdapter.notifyItemRangeInserted(initialSize, newsList.size());
                    }
                } else {
                    ErrorMessagesUtil errorMessagesUtil =
                            new ErrorMessagesUtil(requireActivity().getApplication());
                    Snackbar.make(view, errorMessagesUtil.
                                    getErrorMessage(((Result.Error)result).getMessage()),
                        Snackbar.LENGTH_SHORT).show();
                    fragmentCountryNewsBinding.progressBar.setVisibility(View.GONE);
                }
            });

        recyclerViewCountryNews.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean isConnected = isConnected();

                if (isConnected && totalItemCount != newsViewModel.getTotalResults()) {

                    totalItemCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();

                    // Condition to enable the loading of other news while the user is scrolling the list
                    if (totalItemCount == visibleItemCount ||
                            (totalItemCount <= (lastVisibleItem + threshold) &&
                                    dy > 0 &&
                                    !newsViewModel.isLoading()
                            ) &&
                            newsViewModel.getNewsResponseLiveData().getValue() != null &&
                            newsViewModel.getCurrentResults() != newsViewModel.getTotalResults()
                    ) {
                        MutableLiveData<Result> newsListMutableLiveData = newsViewModel.getNewsResponseLiveData();

                        if (newsListMutableLiveData.getValue() != null &&
                                newsListMutableLiveData.getValue().isSuccess()) {

                            newsViewModel.setLoading(true);
                            newsList.add(null);
                            newsRecyclerViewAdapter.notifyItemRangeInserted(newsList.size(),
                                    newsList.size() + 1);

                            int page = newsViewModel.getPage() + 1;
                            newsViewModel.setPage(page);
                            newsViewModel.fetchNews(country);
                        }
                    }
                }
            }
        });
    }

    /**
     * It checks if the device is connected to Internet.
     * See: <a href="https://developer.android.com/training/monitoring-device-state/connectivity-status-type#DetermineConnection">...</a>
     * @return true if the device is connected to Internet; false otherwise.
     */
    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        newsViewModel.setFirstLoading(true);
        newsViewModel.setLoading(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentCountryNewsBinding = null;
    }
}
