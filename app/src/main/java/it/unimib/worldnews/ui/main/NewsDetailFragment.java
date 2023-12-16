package it.unimib.worldnews.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.unimib.worldnews.R;
import it.unimib.worldnews.databinding.FragmentNewsDetailBinding;
import it.unimib.worldnews.model.News;

/**
 * Fragment that shows the news content.
 */
public class NewsDetailFragment extends Fragment {

    private static final String TAG = NewsDetailFragment.class.getSimpleName();

    private FragmentNewsDetailBinding fragmentNewsDetailBinding;

    public NewsDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment NewsDetailFragment.
     */
    public static NewsDetailFragment newInstance() {
        return new NewsDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentNewsDetailBinding = FragmentNewsDetailBinding.inflate(inflater, container, false);
        return fragmentNewsDetailBinding.getRoot();
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
                if (menuItem.getItemId() == android.R.id.home) {
                    Navigation.findNavController(requireView()).navigateUp();
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        News news = NewsDetailFragmentArgs.fromBundle(getArguments()).getNews();

        Glide.with(fragmentNewsDetailBinding.imageviewNews.getContext()).
                load(news.getUrlToImage()).
                placeholder(R.drawable.ic_baseline_cloud_download_24).
                into(fragmentNewsDetailBinding.imageviewNews);

        fragmentNewsDetailBinding.textviewNewsTitle.setText(news.getTitle());
        fragmentNewsDetailBinding.textviewNewsContent.setText(news.getContent());

        NavBackStackEntry navBackStackEntry = Navigation.
                findNavController(view).getPreviousBackStackEntry();

        // Logic to highlight the correct tab in BottomNavigationView when the user
        // goes to NewsDetailFragment from a tab which is neither CountryNewsFragment
        // nor FavoriteNewsFragment
        if (navBackStackEntry != null &&
                navBackStackEntry.getDestination().getId() == R.id.fragment_country_news) {
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottom_navigation)).
                    getMenu().findItem(R.id.fragment_country_news).setChecked(true);
        } else if (navBackStackEntry != null &&
                navBackStackEntry.getDestination().getId() == R.id.fragment_favorite_news) {
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottom_navigation)).
                    getMenu().findItem(R.id.fragment_favorite_news).setChecked(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentNewsDetailBinding = null;
    }
}
