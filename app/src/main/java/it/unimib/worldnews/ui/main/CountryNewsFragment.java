package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.util.Constants.LAST_UPDATE;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_COUNTRY_OF_INTEREST;
import static it.unimib.worldnews.util.Constants.SHARED_PREFERENCES_FILE_NAME;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.adapter.NewsRecyclerViewAdapter;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.repository.INewsRepository;
import it.unimib.worldnews.repository.NewsMockRepository;
import it.unimib.worldnews.repository.NewsRepository;
import it.unimib.worldnews.util.ResponseCallback;
import it.unimib.worldnews.util.SharedPreferencesUtil;

/**
 * Fragment that shows the news associated with a Country.
 */
public class CountryNewsFragment extends Fragment implements ResponseCallback {

    private static final String TAG = CountryNewsFragment.class.getSimpleName();

    private List<News> newsList;
    private INewsRepository iNewsRepository;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private ProgressBar progressBar;

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

        Log.d(TAG, "debug mode: " + requireActivity().getResources().getBoolean(R.bool.debug_mode));

        if (requireActivity().getResources().getBoolean(R.bool.debug_mode)) {
            // Use NewsMockRepository to read the news from
            // newsapi-test.json file contained in assets folder
            iNewsRepository =
                    new NewsMockRepository(requireActivity().getApplication(), this,
                            INewsRepository.JsonParserType.GSON);
        } else {
            iNewsRepository =
                    new NewsRepository(requireActivity().getApplication(), this);
        }

        sharedPreferencesUtil = new SharedPreferencesUtil(requireActivity().getApplication());
        newsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_country_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        progressBar = view.findViewById(R.id.progress_bar);

        RecyclerView recyclerViewCountryNews = view.findViewById(R.id.recyclerview_country_news);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false);

        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(newsList,
                requireActivity().getApplication(),
                new NewsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onNewsItemClick(News news) {
                        Snackbar.make(view, news.getTitle(), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFavoriteButtonPressed(int position) {
                        newsList.get(position).setFavorite(!newsList.get(position).isFavorite());
                        iNewsRepository.updateNews(newsList.get(position));
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

        progressBar.setVisibility(View.VISIBLE);
        iNewsRepository.fetchNews(sharedPreferencesUtil.readStringData(
                        SHARED_PREFERENCES_FILE_NAME, SHARED_PREFERENCES_COUNTRY_OF_INTEREST), 0,
                Long.parseLong(lastUpdate));
    }

    @Override
    public void onSuccess(List<News> newsList, long lastUpdate) {
        if (newsList != null) {
            this.newsList.clear();
            this.newsList.addAll(newsList);
            sharedPreferencesUtil.writeStringData(SHARED_PREFERENCES_FILE_NAME, LAST_UPDATE,
                    String.valueOf(lastUpdate));
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newsRecyclerViewAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onFailure(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Snackbar.make(requireActivity().findViewById(android.R.id.content),
                errorMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onNewsFavoriteStatusChanged(News news) {
        if (news.isFavorite()) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.news_added_to_favorite_list_message),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    getString(R.string.news_removed_from_favorite_list_message),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
