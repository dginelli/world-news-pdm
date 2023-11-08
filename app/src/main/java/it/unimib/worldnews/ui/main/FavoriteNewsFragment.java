package it.unimib.worldnews.ui.main;

import static it.unimib.worldnews.util.Constants.NEWS_API_TEST_JSON_FILE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.adapter.NewsArrayAdapter;
import it.unimib.worldnews.adapter.NewsBaseAdapter;
import it.unimib.worldnews.adapter.NewsListAdapter;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.JSONParserUtil;

/**
 * Fragment that shows the favorite news of the user.
 */
public class FavoriteNewsFragment extends Fragment {

    private static final String TAG = FavoriteNewsFragment.class.getSimpleName();

    private ListView listViewFavNews;
    private News[] newsArray;
    private List<News> newsList;

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

        JSONParserUtil jsonParserUtil = new JSONParserUtil(requireActivity().getApplication());
        try {
            newsList =
                    jsonParserUtil.parseJSONFileWithJSONObjectArray(NEWS_API_TEST_JSON_FILE).
                            getArticles().subList(0, 10);
            newsArray = newsList.toArray(new News[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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
                    Log.d(TAG, "Delete menu item pressed");
                }
                return false;
            }
            // Use getViewLifecycleOwner() to avoid that the listener
            // associated with a menu icon is called twice
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        listViewFavNews = view.findViewById(R.id.listview_fav_news);

        // Use one of these four methods to populate the ListView:
        // 1) useDefaultLisAdapter(); 2) useCustomArrayAdapter();
        // 3) useCustomBaseAdapter(); 4) useCustomListAdapter();
        useCustomListAdapter();

        listViewFavNews.setOnItemClickListener((parent, view1, position, id) ->
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        newsList.get(position).getTitle(), Snackbar.LENGTH_SHORT).show());
    }

    /**
     * Creates an ArrayAdapter to be used to populate the ListView
     * R.id.listview_fav_news (listViewFavNews) with an array of News.
     */
    private void useDefaultLisAdapter() {
        ArrayAdapter<News> adapter = new ArrayAdapter<News>(requireContext(),
                android.R.layout.simple_list_item_1, newsArray);
        listViewFavNews.setAdapter(adapter);
    }

    /**
     * Creates a custom ArrayAdapter to be used to populate the ListView
     * R.id.listview_fav_news (listViewFavNews) with an array of News.
     */
    private void useCustomArrayAdapter() {
        NewsArrayAdapter newsListArrayAdapter =
                new NewsArrayAdapter(requireContext(), R.layout.fav_news_list_item, newsArray);
        listViewFavNews.setAdapter(newsListArrayAdapter);
    }

    /**
     * Creates a custom ArrayAdapter to be used to populate the ListView
     * R.id.listview_fav_news (listViewFavNews) with an ArrayList of News.
     */
    private void useCustomListAdapter() {
        NewsListAdapter newsListAdapter =
                new NewsListAdapter(requireContext(), R.layout.fav_news_list_item, newsList,
                        new NewsListAdapter.OnDeleteButtonClickListener() {
                            @Override
                            public void onDeleteButtonClick(News news) {
                                Snackbar.make(listViewFavNews,
                                        news.getTitle(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
        listViewFavNews.setAdapter(newsListAdapter);
    }

    /**
     * Creates a custom BaseAdapter to be used to populate the ListView
     * R.id.listview_fav_news (listViewFavNews).
     */
    private void useCustomBaseAdapter() {
        NewsBaseAdapter newsListBaseAdapter = new NewsBaseAdapter(newsList);
        listViewFavNews.setAdapter(newsListBaseAdapter);
    }
}
