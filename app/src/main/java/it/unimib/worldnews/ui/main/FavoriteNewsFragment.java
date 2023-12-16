package it.unimib.worldnews.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.adapter.NewsListAdapter;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.model.Result;
import it.unimib.worldnews.util.ErrorMessagesUtil;

/**
 * Fragment that shows the favorite news of the user.
 */
public class FavoriteNewsFragment extends Fragment {

    private static final String TAG = FavoriteNewsFragment.class.getSimpleName();

    private List<News> newsList;
    private NewsListAdapter newsListAdapter;
    private ProgressBar progressBar;
    private NewsViewModel newsViewModel;

    public FavoriteNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FavoriteNewsFragment.
     */
    public static FavoriteNewsFragment newInstance() {
        return new FavoriteNewsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsList = new ArrayList<>();
        newsViewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                // It adds the menu item in the toolbar
                menuInflater.inflate(R.menu.top_app_bar, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.delete) {
                    newsViewModel.deleteAllFavoriteNews();
                }
                return false;
            }
            // Use getViewLifecycleOwner() to avoid that the listener
            // associated with a menu icon is called twice
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        progressBar = view.findViewById(R.id.progress_bar);

        ListView listViewFavNews = view.findViewById(R.id.listview_fav_news);

        newsListAdapter =
            new NewsListAdapter(requireContext(), R.layout.favorite_news_list_item, newsList,
                news -> {
                    news.setFavorite(false);
                    newsViewModel.removeFromFavorite(news);
                });
        listViewFavNews.setAdapter(newsListAdapter);

        progressBar.setVisibility(View.VISIBLE);

        // Observe the LiveData associated with the MutableLiveData containing the favorite news
        // returned by the method getFavoriteNewsLiveData() of NewsViewModel class.
        // Pay attention to which LifecycleOwner you give as value to
        // the method observe(LifecycleOwner, Observer).
        // In this case, getViewLifecycleOwner() refers to
        // androidx.fragment.app.FragmentViewLifecycleOwner and not to the Fragment itself.
        // You can read more details here: https://stackoverflow.com/a/58663143/4255576
        newsViewModel.getFavoriteNewsLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.isSuccess()) {
                    newsList.clear();
                    newsList.addAll(((Result.Success)result).getData().getNewsList());
                    newsListAdapter.notifyDataSetChanged();
                } else {
                    ErrorMessagesUtil errorMessagesUtil =
                            new ErrorMessagesUtil(requireActivity().getApplication());
                    Snackbar.make(view, errorMessagesUtil.
                                    getErrorMessage(((Result.Error)result).getMessage()),
                            Snackbar.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });

        listViewFavNews.setOnItemClickListener((parent, view1, position, id) -> {
            FavoriteNewsFragmentDirections.ActionFavoriteNewsFragmentToNewsDetailFragment action =
                    FavoriteNewsFragmentDirections.
                            actionFavoriteNewsFragmentToNewsDetailFragment(newsList.get(position));
            Navigation.findNavController(view).navigate(action);
        });
    }
}
