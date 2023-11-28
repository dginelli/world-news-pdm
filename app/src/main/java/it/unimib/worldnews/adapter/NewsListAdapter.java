package it.unimib.worldnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.DateTimeUtil;

/**
 * Custom adapter that extends ArrayAdapter to show an ArrayList of News.
 */
public class NewsListAdapter extends ArrayAdapter<News> {

    private final List<News> newsList;
    private final int layout;
    private final OnFavoriteButtonClickListener onFavoriteButtonClickListener;

    /**
     * Interface to associate a listener to other elements defined in the layout
     * chosen for the ListView item (e.g., a Button).
     */
    public interface OnFavoriteButtonClickListener {
        void onFavoriteButtonClick(News news);
    }

    public NewsListAdapter(@NonNull Context context, int layout, @NonNull List<News> newsList,
                           OnFavoriteButtonClickListener onDeleteButtonClickListener) {
        super(context, layout, newsList);
        this.layout = layout;
        this.newsList = newsList;
        this.onFavoriteButtonClickListener = onDeleteButtonClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(layout, parent, false);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textview_title);
        TextView textViewDate = convertView.findViewById(R.id.textview_date);
        ImageView imageViewFavoriteNews = convertView.findViewById(R.id.imageview_favorite_news);

        imageViewFavoriteNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavoriteButtonClickListener.onFavoriteButtonClick(newsList.get(position));
            }
        });

        textViewTitle.setText(newsList.get(position).getTitle());
        textViewDate.setText(DateTimeUtil.getDate(newsList.get(position).getDate()));

        return convertView;
    }
}
