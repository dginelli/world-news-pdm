package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.util.Constants.NEWS_API_TEST_JSON_FILE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.adapter.NewsRecyclerViewAdapter;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.JSONParserUtil;

/**
 * Fragment that shows the news associated with a Country.
 */
public class CountryNewsFragment extends Fragment {

    private static final String TAG = CountryNewsFragment.class.getSimpleName();

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

        RecyclerView recyclerViewCountryNews = view.findViewById(R.id.recyclerview_country_news);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false);

        // Use one of these three methods to get the list of News
        // 1) getNewsListWithJsonReader(), 2) getNewsListJSONObjectArray(),
        // 3) getNewsListWithWithGSon()
        List<News> newsList = getNewsListWithWithGSon();

        NewsRecyclerViewAdapter newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(newsList,
                new NewsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onNewsItemClick(News news) {
                        Snackbar.make(view, news.getTitle(), Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDeleteButtonPressed(int position) {
                        Snackbar.make(view, getString(R.string.list_size_message) + newsList.size(),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
        recyclerViewCountryNews.setLayoutManager(layoutManager);
        recyclerViewCountryNews.setAdapter(newsRecyclerViewAdapter);
    }

    /**
     * Returns the list of News using JsonReader class.
     * @return The list of News.
     */
    private List<News> getNewsListWithJsonReader() {
        JSONParserUtil jsonParserUtil = new JSONParserUtil(requireActivity().getApplication());
        try {
            return jsonParserUtil.parseJSONFileWithJsonReader(NEWS_API_TEST_JSON_FILE).getArticles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the list of News using JSONObject and JSONArray classes.
     * @return The list of News.
     */
    private List<News> getNewsListJSONObjectArray() {
        JSONParserUtil jsonParserUtil = new JSONParserUtil(requireActivity().getApplication());
        try {
            return jsonParserUtil.parseJSONFileWithJSONObjectArray(NEWS_API_TEST_JSON_FILE).getArticles();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the list of News using Gson.
     * @return The list of News.
     */
    private List<News> getNewsListWithWithGSon() {
        JSONParserUtil jsonParserUtil = new JSONParserUtil(requireActivity().getApplication());
        try {
            return jsonParserUtil.parseJSONFileWithGSon(NEWS_API_TEST_JSON_FILE).getArticles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
